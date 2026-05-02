package com.example.testudo.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.testudo.AppTelemetry
import com.example.testudo.OnDeviceLearning
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

        if (packageName.isNotEmpty()) {
            UserListManager.addToWhitelist(context, packageName)
            OnDeviceLearning.recordFeedback(
                context,
                packageName,
                FloatArray(15) { 0f },
                0
            )
        }
    }

    fun markMalicious(appName: String) {
        val context = getApplication<Application>()
        val packageName = findPackageName(appName)

        if (packageName.isNotEmpty()) {
            UserListManager.addToBlacklist(context, packageName)
            OnDeviceLearning.recordFeedback(
                context,
                packageName,
                FloatArray(15) { 100f },
                2
            )
        }
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