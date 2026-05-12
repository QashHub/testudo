package com.example.testudo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testudo.AppTelemetry
import com.example.testudo.MLEngine
import com.example.testudo.QuarantineManager
import com.example.testudo.RealScanner
import com.example.testudo.data.local.db.DatabaseProvider
import com.example.testudo.data.local.db.entity.QuarantineRecordEntity
import com.example.testudo.data.local.db.entity.ScanHistoryEntity
import com.example.testudo.data.local.db.entity.ThreatLogEntity
import com.example.testudo.ui.screens.ScanResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    var uiState = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    val scanResults: List<ScanResultItem>
        get() = uiState.value.scanResults

    fun startScan() {
        val context = getApplication<Application>()
        val db = DatabaseProvider.getDatabase(context)
        val scanHistoryDao = db.scanHistoryDao()
        val threatLogDao = db.threatLogDao()
        val quarantineDao = db.quarantineDao()

        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isScanning = true,
                scanStatus = "Scanning..."
            )

            val results = withContext(Dispatchers.IO) {
                val ml = MLEngine(context)
                val apps = AppTelemetry.getUserApps(context)

                apps.mapIndexed { index, appInfo ->
                    withContext(Dispatchers.Main) {
                        uiState.value = uiState.value.copy(
                            scanStatus = "Scanning ${index + 1}/${apps.size}..."
                        )
                    }

                    val telemetry = AppTelemetry.collectFeatures(context, appInfo)
                    val result = ml.predict(telemetry.features)

                    ScanResultItem(
                        appName     = telemetry.appName,
                        packageName = telemetry.packageName,
                        status      = result.label,
                        riskScore   = result.riskScore.toInt()
                    )
                }.also {
                    ml.close()
                }
            }

            // ── Update UI state ───────────────────────────────────────
            uiState.value = uiState.value.copy(
                isScanning  = false,
                scanResults = results,
                isSafe      = results.none {
                    it.status == "Malicious" || it.status == "Suspicious"
                },
                scanStatus  = "Scanning..."
            )

            // ── Persist results to DB + quarantine malicious apps ─────
            // Now runs AFTER results are populated
            withContext(Dispatchers.IO) {
                results
                    .filter { it.status == "Malicious" || it.status == "Suspicious" }
                    .forEach { item ->
                        // 1. Write scan history
                        val scanId = scanHistoryDao.insertScanHistory(
                            ScanHistoryEntity(
                                packageName     = item.packageName,
                                appName         = item.appName,
                                scannedAt       = System.currentTimeMillis(),
                                riskScore       = item.riskScore,
                                riskLevel       = item.status,
                                behaviorSummary = "Flagged by ML engine",
                                actionTaken     = if (item.status == "Malicious")
                                    "Auto-quarantined"
                                else "Review Required",
                                isManualScan    = true,
                                modelVersion    = "drebin-v1"
                            )
                        )

                        // 2. Write threat log
                        val threatId = threatLogDao.insertThreatLog(
                            ThreatLogEntity(
                                scanHistoryId     = scanId,
                                packageName       = item.packageName,
                                appName           = item.appName,
                                threatType        = if (item.status == "Malicious")
                                    "Malware"
                                else "Suspicious Behaviour",
                                severity          = item.status,
                                detectionReason   = "ML risk score: ${item.riskScore}",
                                detectedAt        = System.currentTimeMillis(),
                                confidenceScore   = item.riskScore / 100f,
                                recommendedAction = if (item.status == "Malicious")
                                    "Quarantine"
                                else "Review app activity"
                            )
                        )

                        // 3. Quarantine + DB record for malicious only
                        if (item.status == "Malicious") {
                            val scanResult = RealScanner.ScanResult(
                                appName       = item.appName,
                                packageName   = item.packageName,
                                riskScore     = item.riskScore,
                                label         = item.status,
                                reasons       = listOf("Auto-quarantined: flagged as Malicious by scan"),
                                isSideloaded  = false,
                                isBlacklisted = false,
                                isWhitelisted = false,
                                mlScore       = item.riskScore.toFloat()
                            )
                            QuarantineManager.quarantine(context, scanResult)

                            quarantineDao.insertQuarantineRecord(
                                QuarantineRecordEntity(
                                    threatLogId                = threatId,
                                    packageName                = item.packageName,
                                    appName                    = item.appName,
                                    quarantinedAt              = System.currentTimeMillis(),
                                    quarantineReason           = "Auto-quarantined: ML score ${item.riskScore}/100",
                                    actionStatus               = "Quarantined",
                                    evidenceSnapshotPath       = null,
                                    permissionsRevoked         = true,
                                    backgroundExecutionBlocked = true,
                                    isRestored                 = false
                                )
                            )
                        }
                    }

                // Debug logging
                Log.d("DB_SCAN_TEST",       "Total scans: ${scanHistoryDao.getAllScanHistory().size}")
                Log.d("DB_THREAT_TEST",     "Total threats: ${threatLogDao.getAllThreatLogs().size}")
                Log.d("DB_QUARANTINE_TEST", "Total quarantine: ${quarantineDao.getAllQuarantineRecords().size}")
            }
        }
    }
}