package com.example.testudo.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testudo.AppCacheInfo
import com.example.testudo.getAppsSortedByCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CacheUiState(
    val isLoading: Boolean = true,
    val cacheList: List<AppCacheInfo> = emptyList()
)

class CacheViewModel(application: Application) : AndroidViewModel(application) {

    var uiState = mutableStateOf(CacheUiState())
        private set

    init {
        loadCacheList()
    }

    fun loadCacheList() {
        val context = getApplication<Application>()

        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true)

            val result = withContext(Dispatchers.IO) {
                getAppsSortedByCache(context).take(20)
            }

            uiState.value = CacheUiState(
                isLoading = false,
                cacheList = result
            )
        }
    }
}