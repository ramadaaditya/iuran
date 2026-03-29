package com.ramstudio.kaskita.presentation.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import com.ramstudio.kaskita.core.utils.Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


data class SignInUiState(
    val isLoading: Boolean = false,
    val message: String = "",
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

sealed interface SignInUiEvent {
    data object NavigateHome : SignInUiEvent
    data object NavigateSignUp : SignInUiEvent
    data class ShowSnackbar(val message: String) : SignInUiEvent
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<SignInUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun validate(): Boolean {
        val currentState = _uiState.value

        val emailError = Validator.validateEmail(currentState.email)
        val passwordError = Validator.validatePassword(currentState.password)

        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        }

        return emailError == null && passwordError == null
    }


    fun onEmailChange(newValue: String) {
        _uiState.update {
            it.copy(
                email = newValue, emailError = null
            )
        }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update {
            it.copy(
                password = newValue, passwordError = null
            )
        }
    }

    fun signInWithEmail() {
        viewModelScope.launch {
            if (!validate()) return@launch
            _uiState.update { it.copy(isLoading = true) }
            val result =
                authRepository.signInWithEmail(_uiState.value.email, _uiState.value.password)
            _uiState.update {
                it.copy(isLoading = false)
            }
            when (result) {
                is Result.Error -> {
                    val errorMessage = result.throwable.message ?: "Unknown error"

                    Timber.e(
                        result.throwable,
                        "SignInViewModel: signInWithEmail failed -> $errorMessage"
                    )
                    _uiEvent.send(
                        SignInUiEvent.ShowSnackbar(
                            AppErrorMapper.fromThrowable(
                                throwable = result.throwable,
                                fallback = "Gagal masuk ke akun. Silakan coba lagi."
                            )
                        )
                    )
                }

                is Result.Success -> {
                    _uiEvent.send(SignInUiEvent.ShowSnackbar("Berhasil login"))
                    _uiEvent.send(SignInUiEvent.NavigateHome)
                }
            }
        }
    }
}
