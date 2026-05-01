package com.example.testudo.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testudo.utils.getMostUsedApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StatusUiState(
    val apps: List<String> = emptyList(),
    val isSafe: Boolean = true,
    val suspiciousCount: Int = 0,
    val virusCount: Int = 0,
    val blockedCount: Int = 0
)

class StatusViewModel(application: Application) : AndroidViewModel(application) {

    var uiState = mutableStateOf(StatusUiState())
        private set

    init {
        loadStatus()
    }

    fun loadStatus() {
        val context = getApplication<Application>()

        viewModelScope.launch {
            val mostUsedApps = withContext(Dispatchers.IO) {
                getMostUsedApps(context)
            }

            uiState.value = uiState.value.copy(
                apps = mostUsedApps,
                suspiciousCount = 0,
                virusCount = 0,
                blockedCount = 0,
                isSafe = true
            )
        }
    }
}