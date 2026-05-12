package com.example.testudo.viewmodel

import com.example.testudo.ui.screens.ScanResultItem

data class HomeUiState(
    val isScanning: Boolean = false,
    val isSafe: Boolean = true,
    val scanStatus: String = "Scanning...",
    val scanResults: List<ScanResultItem> = emptyList()
)