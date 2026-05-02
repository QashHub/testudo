package com.example.testudo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.testudo.AppSettings

class SettingsViewModel : ViewModel() {

    var settings = mutableStateOf(AppSettings())
        private set

    fun setNotificationEnabled(value: Boolean) {
        settings.value = settings.value.copy(notificationEnabled = value)
    }

    fun setChargingOptEnabled(value: Boolean) {
        settings.value = settings.value.copy(chargingOptEnabled = value)
    }

    fun setAutoUpdateEnabled(value: Boolean) {
        settings.value = settings.value.copy(autoUpdateEnabled = value)
    }

    fun setRealtimeProtEnabled(value: Boolean) {
        settings.value = settings.value.copy(realtimeProtEnabled = value)
    }

    fun setPrivacyPolicyEnabled(value: Boolean) {
        settings.value = settings.value.copy(privacyPolicyEnabled = value)
    }
}