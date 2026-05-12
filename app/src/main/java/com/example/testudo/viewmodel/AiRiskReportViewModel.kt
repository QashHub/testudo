package com.example.testudo.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.testudo.AppTelemetry
import com.example.testudo.OnDeviceLearning
import com.example.testudo.QuarantineManager
import com.example.testudo.RealScanner
import com.example.testudo.UserListManager

data class AiRiskReportUiState(
    val selectedFilter: String = "All",
    val expandedItem: String? = null,
    val scoreVisible: Boolean = false,
    val filters: List<String> = listOf("All", "Safe", "Suspicious", "Malicious")
)

class AiRiskReportViewModel(application: Application) : AndroidViewModel(application) {

    var uiState = mutableStateOf(AiRiskReportUiState())
        private set

    fun showScore() {
        uiState.value = uiState.value.copy(scoreVisible = true)
    }

    fun selectFilter(filter: String) {
        uiState.value = uiState.value.copy(selectedFilter = filter)
    }

    fun toggleExpandedItem(appName: String) {
        uiState.value = uiState.value.copy(
            expandedItem = if (uiState.value.expandedItem == appName) null else appName
        )
    }

    fun markSafe(appName: String) {
        val context = getApplication<Application>()
        val packageName = findPackageName(appName)
        if (packageName.isEmpty()) return

        UserListManager.addToWhitelist(context, packageName)

        UserListManager.removeFromBlacklist(context, packageName)

        QuarantineManager.restore(context, packageName)

        OnDeviceLearning.recordFeedback(
            context,
            packageName,
            FloatArray(50) { 0f },
            0
        )
    }

    fun markMalicious(appName: String, riskScore: Int = 100) {
        val context = getApplication<Application>()
        val packageName = findPackageName(appName)
        if (packageName.isEmpty()) return

        UserListManager.addToBlacklist(context, packageName)

        OnDeviceLearning.recordFeedback(
            context,
            packageName,
            FloatArray(50) { 1f },
            2
        )

        val scanResult = RealScanner.ScanResult(
            appName       = appName,
            packageName   = packageName,
            riskScore     = riskScore,
            label         = "Malicious",
            reasons       = listOf("Manually marked as malicious by user"),
            isSideloaded  = false,
            isBlacklisted = true,
            isWhitelisted = false,
            mlScore       = riskScore.toFloat()
        )
        QuarantineManager.quarantine(context, scanResult)
    }

    private fun findPackageName(appName: String): String {
        val context = getApplication<Application>()
        return AppTelemetry.getUserApps(context)
            .find { app ->
                context.packageManager
                    .getApplicationLabel(app)
                    .toString() == appName
            }
            ?.packageName ?: ""
    }
}