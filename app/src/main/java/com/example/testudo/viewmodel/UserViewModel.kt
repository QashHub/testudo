package com.example.testudo.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testudo.data.local.db.DatabaseProvider
import com.example.testudo.data.local.db.entity.UserProfileEntity
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider
        .getDatabase(application)
        .userProfileDao()

    var user = mutableStateOf<UserProfileEntity?>(null)
        private set

    var editMode = mutableStateOf(false)
        private set

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val existingUser = dao.getUserProfile()

            user.value = existingUser ?: UserProfileEntity(
                id = 1,
                name = "John Doe",
                email = "john@example.com",
                phone = "+44 7123456789",
                paymentDetails = "Visa •••• 1234",
                isPremium = false
            )
        }
    }

    fun toggleEditMode() {
        editMode.value = !editMode.value
    }

    fun cancelEditMode() {
        editMode.value = false
    }

    fun updateName(value: String) {
        user.value = user.value?.copy(name = value)
    }

    fun updateEmail(value: String) {
        user.value = user.value?.copy(email = value)
    }

    fun updatePhone(value: String) {
        user.value = user.value?.copy(phone = value)
    }

    fun updatePaymentDetails(value: String) {
        user.value = user.value?.copy(paymentDetails = value)
    }

    fun updatePremium(value: Boolean) {
        user.value = user.value?.copy(isPremium = value)
    }

    fun saveUser() {
        val currentUser = user.value ?: return

        viewModelScope.launch {
            dao.insertOrUpdateUserProfile(currentUser)
            editMode.value = false
        }
    }
}