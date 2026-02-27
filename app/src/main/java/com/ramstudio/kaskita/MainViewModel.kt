package com.ramstudio.kaskita

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedIn : AuthState
    data object LoggedOut : AuthState
}


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _sessionStatus = MutableStateFlow<AuthState>(AuthState.Loading)
    val sessionStatus = _sessionStatus.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        _sessionStatus.value = AuthState.LoggedIn

                        Log.d(
                            "AUTH_VM",
                            "Authenticated ✅ userId=${user?.id}, email=${user?.email}"
                        )
                    }

                    is SessionStatus.NotAuthenticated -> {
                        _sessionStatus.value = AuthState.LoggedOut
                        Log.d("AUTH_VM", "Not authenticated ❌")
                    }

                    SessionStatus.Initializing -> {
                        _sessionStatus.value = AuthState.Loading
                        Log.d("AUTH_VM", "Auth loading ⏳")
                    }

                    is SessionStatus.RefreshFailure -> {
                        _sessionStatus.value = AuthState.LoggedOut
                        Log.d("AUTH_VM", "Session expired ❗")
                    }
                }
            }
        }
    }
}