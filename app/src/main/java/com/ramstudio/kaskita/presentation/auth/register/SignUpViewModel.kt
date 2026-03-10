package com.ramstudio.kaskita.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val fullNameError: String? = null
)

sealed interface SignUpUiEvent {
    data object NavigateSignIn : SignUpUiEvent
    data class ShowSnackbar(val message: String) : SignUpUiEvent
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<SignUpUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onFullNameChange(fullName: String) = _uiState.update {
        it.copy(fullName = fullName, fullNameError = null)
    }


    fun onEmailChange(email: String) = _uiState.update {
        it.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) = _uiState.update {
        it.copy(password = password, passwordError = null)
    }


    fun signUpWithEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.signUp(
                email = uiState.value.email,
                password = uiState.value.password,
                fullName = uiState.value.fullName
            )
            _uiState.update { it.copy(isLoading = false) }
            when (result) {
                is Result.Success -> {
                    _uiEvent.send(SignUpUiEvent.ShowSnackbar("Akun Berhasil dibuat !"))
                    _uiEvent.send(SignUpUiEvent.NavigateSignIn)
                }

                is Result.Error -> {
                    _uiEvent.send(
                        SignUpUiEvent.ShowSnackbar(
                            AppErrorMapper.fromRawMessage(
                                rawMessage = "Terjadi kesalahan",
                                fallback = "Gagal membuat akun. Silakan coba lagi."
                            )
                        )
                    )
                }
            }
        }
    }
}
