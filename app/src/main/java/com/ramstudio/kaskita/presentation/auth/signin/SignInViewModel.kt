package com.ramstudio.kaskita.presentation.auth.signin

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.utils.AuthRepository
import com.ramstudio.kaskita.core.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@Immutable
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
//            authRepository.signInCredentialManager(activityContext)
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

    fun signInWithEmail() {
        viewModelScope.launch {
            authRepository.signInWithEmail(_uiState.value.email, uiState.value.password)
                .collect { result ->
                    when (result) {
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false
                                )
                            }
                            _uiEvent.send(SignInUiEvent.ShowSnackbar(result.message))
                        }

                        Result.Loading -> {
                            _uiState.update {
                                it.copy(isLoading = true)
                            }
                        }

                        is Result.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false
                                )
                            }
                            _uiEvent.send(SignInUiEvent.ShowSnackbar(result.data))
                            _uiEvent.send(SignInUiEvent.NavigateHome)
                        }
                    }

                }
        }
    }
}