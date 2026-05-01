package com.example.testudo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testudo.AppTelemetry
import com.example.testudo.MLEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeViewModel(application: Application)
    : AndroidViewModel(application) {

    var uiState = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    fun startScan() {

        val context = getApplication<Application>()

        viewModelScope.launch {

            uiState.value = uiState.value.copy(
                isScanning = true,
                scanStatus = "Scanning..."
            )

            val results = withContext(Dispatchers.IO) {

                val ml = MLEngine(context)
                val apps = AppTelemetry.getUserApps(context)

                apps.mapIndexed { index, appInfo ->

                    uiState.value = uiState.value.copy(
                        scanStatus = "Scanning ${index + 1}/${apps.size}..."
                    )

                    val telemetry =
                        AppTelemetry.collectFeatures(context, appInfo)

                    val result = ml.predict(telemetry.features)

                    Triple(
                        telemetry.appName,
                        result.label,
                        result.riskScore.toInt()
                    )
                }.also {
                    ml.close()
                }
            }

            uiState.value = uiState.value.copy(
                isScanning = false,
                scanResults = results,
                isSafe = results.none {
                    it.second == "Malicious" ||
                            it.second == "Suspicious"
                },
                scanStatus = "Scanning..."
            )
        }
    }
}