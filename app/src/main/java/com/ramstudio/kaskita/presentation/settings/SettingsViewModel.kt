package com.ramstudio.kaskita.presentation.settings

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.domain.model.User
import com.ramstudio.kaskita.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SettingsUiState(
    val isLoading: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = authRepository.getUser()
                _uiState.update { it.copy(isLoading = false, user = user) }
                Log.d(TAG, "loadUser: $user")
            } catch (e: Exception) {
                Log.e(TAG, "loadUser: ${e.message}")
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to logout")
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, error = null) }
            try {
                authRepository.deleteAccount()
                _uiState.update { it.copy(isDeletingAccount = false) }
            } catch (e: Exception) {
                Log.e(TAG, "deleteAccount: ${e.message}")
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        error = e.message ?: "Failed to delete account"
                    )
                }
            }
        }
    }
}
