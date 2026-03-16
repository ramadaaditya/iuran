package com.ramstudio.kaskita

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.data.datasource.local.DataStoreManager
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedIn : AuthState
    data object LoggedOut : AuthState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val datastore: DataStoreManager
) : ViewModel() {

    private val _selectedCommunityId = MutableStateFlow<String?>(null)
    val selectedCommunityId = _selectedCommunityId.asStateFlow()

    private val _sessionStatus = MutableStateFlow<AuthState>(AuthState.Loading)
    val sessionStatus = _sessionStatus.asStateFlow()

    private var activeUserId: String? = null
    private var selectedCommunityObserverJob: Job? = null

    init {
        checkSession()
    }

    fun setSelectedCommunityId(communityId: String?) {
        val normalized = communityId?.takeIf { it.isNotBlank() }
        _selectedCommunityId.value = normalized

        val userId = activeUserId ?: return
        viewModelScope.launch {
            datastore.saveSelectedCommunityId(userId = userId, communityId = normalized)
        }
    }

    private fun observeLastCommunity(userId: String?) {
        selectedCommunityObserverJob?.cancel()

        if (userId.isNullOrBlank()) {
            _selectedCommunityId.value = null
            return
        }

        selectedCommunityObserverJob = viewModelScope.launch {
            datastore.observeSelectedCommunityId(userId).collectLatest { communityId ->
                _selectedCommunityId.value = communityId
            }
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            repository.sessionStatus.collectLatest { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        activeUserId = user?.id
                        _sessionStatus.value = AuthState.LoggedIn
                        observeLastCommunity(user?.id)

                        Log.d(
                            "AUTH_VM",
                            "Authenticated ✅ userId=${user?.id}, email=${user?.email}"
                        )
                    }

                    is SessionStatus.NotAuthenticated -> {
                        activeUserId = null
                        _sessionStatus.value = AuthState.LoggedOut
                        observeLastCommunity(null)
                        Log.d("AUTH_VM", "Not authenticated ❌")
                    }

                    SessionStatus.Initializing -> {
                        _sessionStatus.value = AuthState.Loading
                        Log.d("AUTH_VM", "Auth loading ⏳")
                    }

                    is SessionStatus.RefreshFailure -> {
                        activeUserId = null
                        _sessionStatus.value = AuthState.LoggedOut
                        observeLastCommunity(null)
                        Log.d("AUTH_VM", "Session expired ❗")
                    }
                }
            }
        }
    }
}