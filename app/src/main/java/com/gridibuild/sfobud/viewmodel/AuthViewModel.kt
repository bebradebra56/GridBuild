package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.UserEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = true,
    val isOnboardingDone: Boolean = false,
    val userId: Long = -1L,
    val userName: String = "User"
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    init {
        viewModelScope.launch {
            var userId = prefs.currentUserId.first()
            val onboardingDone = prefs.isOnboardingDone.first()

            if (userId <= 0) {
                userId = repository.getOrCreateDefaultUser()
                prefs.setCurrentUserId(userId)
            }

            val user = repository.getUserById(userId)
            _state.value = AuthUiState(
                isLoading = false,
                isOnboardingDone = onboardingDone,
                userId = userId,
                userName = user?.name ?: "User"
            )
        }
    }

    fun setOnboardingDone() {
        viewModelScope.launch {
            prefs.setOnboardingDone(true)
            _state.value = _state.value.copy(isOnboardingDone = true)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val userId = _state.value.userId
            if (userId > 0) {
                val user = repository.getUserById(userId)
                if (user != null) {
                    repository.updateUser(user.copy(name = name.trim()))
                    _state.value = _state.value.copy(userName = name.trim())
                }
            }
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            val userId = _state.value.userId
            if (userId > 0) repository.deleteAllUserData(userId)
            prefs.clearAll()
            val newUserId = repository.getOrCreateDefaultUser()
            prefs.setCurrentUserId(newUserId)
            _state.value = AuthUiState(isLoading = false, userId = newUserId, userName = "User")
            onComplete()
        }
    }
}
