package com.example.testudo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.testudo.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.testudo.ui.components.TitleSection
import com.example.testudo.ui.components.ScanButton
import com.example.testudo.ui.components.SurroundingButtons
import com.example.testudo.viewmodel.HomeViewModel
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.testudo.data.local.db.DatabaseProvider
import com.example.testudo.data.local.db.entity.ScanHistoryEntity
import kotlinx.coroutines.launch
import com.example.testudo.data.local.db.entity.ThreatLogEntity
import com.example.testudo.data.local.db.entity.QuarantineRecordEntity
import com.example.testudo.data.local.db.entity.VirusSignatureEntity
import androidx.compose.runtime.LaunchedEffect

@Composable
fun MainScreen(
    navController: NavHostController,
    vm: HomeViewModel = viewModel()
) {
    val state by vm.uiState
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val scanHistoryDao = remember { db.scanHistoryDao() }
    val threatLogDao = remember { db.threatLogDao() }
    val quarantineDao = remember { db.quarantineDao() }
    val virusSignatureDao = remember { db.virusSignatureDao() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val existingSignatures = virusSignatureDao.getAllVirusSignatures()

        if (existingSignatures.isEmpty()) {
            val sampleSignatures = listOf(
                VirusSignatureEntity(
                    signatureHash = "MALWARE_HASH_001",
                    virusName = "Trojan.Testudo.A",
                    severity = "High",
                    description = "Sample Trojan signature used for local malware detection testing.",
                    recommendedAction = "Quarantine",
                    definitionVersion = "demo-v1",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null
                ),
                VirusSignatureEntity(
                    signatureHash = "MALWARE_HASH_002",
                    virusName = "Spyware.Testudo.B",
                    severity = "Medium",
                    description = "Sample spyware signature used to simulate suspicious behaviour detection.",
                    recommendedAction = "Review",
                    definitionVersion = "demo-v1",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null
                ),
                VirusSignatureEntity(
                    signatureHash = "MALWARE_HASH_003",
                    virusName = "Adware.Testudo.C",
                    severity = "Low",
                    description = "Sample adware signature for demonstration purposes.",
                    recommendedAction = "Monitor",
                    definitionVersion = "demo-v1",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null
                )
            )

            virusSignatureDao.insertVirusSignatures(sampleSignatures)
            Log.d("DB_VIRUS_TEST", "Seeded virus signatures: ${sampleSignatures.size}")
        } else {
            Log.d("DB_VIRUS_TEST", "Virus signatures already exist: ${existingSignatures.size}")
        }
    }
    fun performScan() {
        coroutineScope.launch {
            val scanRecord = ScanHistoryEntity(
                packageName = "com.example.testapp",
                appName = "Test App",
                scannedAt = System.currentTimeMillis(),
                riskScore = 65,
                riskLevel = "Suspicious",
                behaviorSummary = "Suspicious background activity detected.",
                actionTaken = "Review Required",
                isManualScan = true,
                modelVersion = "demo-v1"
            )

            val scanId = scanHistoryDao.insertScanHistory(scanRecord)

            if (scanRecord.riskLevel == "Suspicious" || scanRecord.riskLevel == "Malicious") {
                val threatLog = ThreatLogEntity(
                    scanHistoryId = scanId,
                    packageName = scanRecord.packageName,
                    appName = scanRecord.appName,
                    threatType = "Suspicious Behaviour",
                    severity = scanRecord.riskLevel,
                    detectionReason = scanRecord.behaviorSummary,
                    detectedAt = System.currentTimeMillis(),
                    confidenceScore = 0.75f,
                    recommendedAction = "Review app activity"
                )

                val threatLogId = threatLogDao.insertThreatLog(threatLog)
                val quarantineRecord = QuarantineRecordEntity(
                    threatLogId = threatLogId,

                    packageName = scanRecord.packageName,
                    appName = scanRecord.appName,

                    quarantinedAt = System.currentTimeMillis(),
                    quarantineReason = "Suspicious application quarantined",

                    actionStatus = "Quarantined",

                    evidenceSnapshotPath = null,

                    permissionsRevoked = true,
                    backgroundExecutionBlocked = true,
                    isRestored = false,
                    )

                quarantineDao.insertQuarantineRecord(quarantineRecord)
            }

            val allScans = scanHistoryDao.getAllScanHistory()
            val allThreats = threatLogDao.getAllThreatLogs()
            val allQuarantine = quarantineDao.getAllQuarantineRecords()

            Log.d("DB_SCAN_TEST", "Total scan records: ${allScans.size}")
            Log.d("DB_THREAT_TEST", "Total threat records: ${allThreats.size}")
            Log.d("DB_QUARANTINE_TEST", "Total quarantine records: ${allQuarantine.size}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))
            TitleSection()

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Hello John!",
                color = Color.White
            )

            Spacer(Modifier.height(32.dp))

            if (state.isScanning) {

                CircularProgressIndicator()

                Text(state.scanStatus)

            } else {

                Box(contentAlignment = Alignment.Center) {

                    SurroundingButtons(
                        navController,
                        alertCount = state.scanResults.count {
                            it.second == "Malicious" ||
                                    it.second == "Suspicious"
                        }
                    )

                    ScanButton(
                        isSafe = state.isSafe,
                        onClick = {
                            vm.startScan()
                            performScan()
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E3A5F))
                    .clickable {
                        navController.navigate(Screen.AIRiskReport.route)
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI Risk Report",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}