package com.example.testudo.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel

data class AlertData(
    val leftText: String,
    val rightText: String,
    val icon: ImageVector
)

data class AlertsUiState(
    val alerts: List<AlertData> = listOf(
        AlertData(
            leftText = "Poor network connection — AI processing may take longer than usual.",
            rightText = "Check your internet connection.",
            icon = Icons.Default.WifiOff
        ),
        AlertData(
            leftText = "Free up space to save AI results and continue using the app.",
            rightText = "Storage space full",
            icon = Icons.Default.Storage
        )
    )
)

class AlertsViewModel : ViewModel() {

    var uiState = mutableStateOf(AlertsUiState())
        private set

    fun clearAllAlerts() {
        uiState.value = uiState.value.copy(alerts = emptyList())
    }

    fun dismissAlert(alert: AlertData) {
        uiState.value = uiState.value.copy(
            alerts = uiState.value.alerts.filterNot {
                it.leftText == alert.leftText
            }
        )
    }
}