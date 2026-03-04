package com.ramstudio.kaskita.presentation.auth.register

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import com.ramstudio.kaskita.core.utils.AuthRepositoryImpl
import com.ramstudio.kaskita.core.utils.AuthResponse
import com.ramstudio.kaskita.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SignUpUiState(
    val isLoading: Boolean = false,
    val message: String = "",
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
    private val authRepositoryImpl: AuthRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<SignUpUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onFullNameChange(newValue: String) {
        _uiState.update {
            it.copy(
                fullName = newValue,
                fullNameError = null
            )
        }
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

//    fun loginGoogle(activityContext: Context) {
//        viewModelScope.launch {
//            Log.d("LOGIN_FLOW", "Mulai login dari ViewModel")
//
//            authRepositoryImpl.signInCredentialManager(activityContext)
//                .collect { result ->
//                    when (result) {
//                        is AuthResponse.Success -> {
//                            Log.d(TAG, "loginGoogle: Login Sukses")
//                        }
//
//                        is AuthResponse.Error -> {
//                            Log.d(TAG, "loginGoogle: Login Gagal")
//                        }
//                    }
//
//                }
//
//        }
//    }

    fun signUpWithEmail() {
        viewModelScope.launch {
            authRepositoryImpl.signUp(
                _uiState.value.email,
                _uiState.value.password,
                _uiState.value.fullName
            )
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false
                                )
                            }
                            _uiEvent.send(SignUpUiEvent.ShowSnackbar(result.data))
                            _uiEvent.send(SignUpUiEvent.NavigateSignIn)
                        }

                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false
                                )
                            }
                            _uiEvent.send(
                                SignUpUiEvent.ShowSnackbar(
                                    AppErrorMapper.fromRawMessage(
                                        rawMessage = result.message,
                                        fallback = "Gagal membuat akun. Silakan coba lagi."
                                    )
                                )
                            )
                        }

                        is Result.Loading -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }
}
