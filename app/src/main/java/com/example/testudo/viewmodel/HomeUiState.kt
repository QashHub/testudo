package com.example.testudo.viewmodel

data class HomeUiState(
    val isScanning: Boolean = false,
    val isSafe: Boolean = true,
    val scanStatus: String = "Scanning...",
    val scanResults: List<Triple<String, String, Int>> = emptyList()
)