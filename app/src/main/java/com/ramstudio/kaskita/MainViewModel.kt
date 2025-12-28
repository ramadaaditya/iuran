package com.ramstudio.kaskita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {
    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    var isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        _isUserLoggedIn.value = true

                        android.util.Log.d(
                            "AUTH_VM",
                            "Authenticated ✅ userId=${user?.id}, email=${user?.email}"
                        )
                    }

                    is SessionStatus.NotAuthenticated -> {
                        _isUserLoggedIn.value = false
                        android.util.Log.d("AUTH_VM", "Not authenticated ❌")
                    }

                    SessionStatus.Initializing -> {
                        _isUserLoggedIn.value = null
                        android.util.Log.d("AUTH_VM", "Auth loading ⏳")
                    }

                    else -> {}
                }
            }
        }
    }

}