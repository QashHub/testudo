package com.example.testudo

data class AppSettings(
    val notificationEnabled: Boolean = true,
    val chargingOptEnabled: Boolean = false,
    val autoUpdateEnabled: Boolean = true,
    val realtimeProtEnabled: Boolean = true,
    val privacyPolicyEnabled: Boolean = false

)