package com.ramstudio.kaskita

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
import timber.log.Timber
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

    private val _activeUserId = MutableStateFlow<String?>(null)

    private var selectedCommunityObserverJob: Job? = null

    init {
        observeSession()
    }

    fun setSelectedCommunityId(communityId: String?) {
        val normalized = communityId?.takeIf { it.isNotBlank() }
        _selectedCommunityId.value = normalized

        val userId = _activeUserId.value ?: return
        viewModelScope.launch {
            datastore.saveSelectedCommunityId(userId = userId, communityId = normalized)
        }
    }

    private fun reObserveSelectedCommunity(userId: String?) {
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

    // ✅ Lebih simpel — tidak perlu Job, tidak perlu observe saat login
//    private fun resetCommunityForUser(userId: String?) {
//        _selectedCommunityId.value = null  // selalu reset, titik
//
//        // Opsional: bersihkan juga yang tersimpan di datastore
//        if (userId != null) {
//            viewModelScope.launch {
//                datastore.saveSelectedCommunityId(userId, communityId = null)
//            }
//        }
//    }

    private fun observeSession() {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        _activeUserId.value = user?.id
                        _sessionStatus.value = mapToAuthState(status)
                        reObserveSelectedCommunity(user?.id)
                        Timber.d(
                            "AUTH_VM : Authenticated ✅ userId=${user?.id}, email=${user?.email}"
                        )
                    }

                    is SessionStatus.Initializing -> {
                        _sessionStatus.value = mapToAuthState(status)
                        Timber.d("AUTH_VM : Auth loading ⏳")
                    }

                    is SessionStatus.NotAuthenticated,
                    is SessionStatus.RefreshFailure -> {
                        _activeUserId.value = null
                        _sessionStatus.value = mapToAuthState(status)
                        reObserveSelectedCommunity(null)
                        Timber.d("AUTH_VM : Session expired ❗")
                    }
                }
            }
        }
    }

    private fun mapToAuthState(session: SessionStatus): AuthState {
        return when (session) {
            is SessionStatus.Authenticated -> AuthState.LoggedIn
            is SessionStatus.NotAuthenticated -> AuthState.LoggedOut
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.RefreshFailure -> AuthState.LoggedOut
        }
    }
}