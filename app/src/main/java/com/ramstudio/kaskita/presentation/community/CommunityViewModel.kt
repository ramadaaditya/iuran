package com.ramstudio.kaskita.presentation.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.Result
import com.ramstudio.kaskita.domain.repository.AuthRepository
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val communities: List<Community> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUserId: String? = null,
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: ICommunityRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    // Backing property untuk state
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    companion object {
        private const val MIN_COMMUNITY_CODE_LENGTH = 5
    }

    init {
        loadCurrentUser()
        observeCommunities()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = authRepository.getUser()
                _uiState.update { it.copy(currentUserId = user.id) }
            } catch (_: Exception) {
                // User not logged in or error fetching user
                _uiState.update { it.copy(currentUserId = null) }
            }
        }
    }


    private fun observeCommunities() {
        viewModelScope.launch {
            repository.getAllCommunity().collect { communities ->
                _uiState.update {
                    it.copy(communities = communities)
                }
                // 🔥 DEBUG DI SINI
                Log.d("CommunityVM", "Communities size: ${communities.size}")
                communities.forEach {
                    Log.d("CommunityVM", "Community: $it")
                }
            }
        }
    }

    fun createCommunity(name: String, desc: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama komunitas tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            when (val result = repository.createCommunity(name, desc)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = result.data)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppErrorMapper.fromRawMessage(
                                rawMessage = result.message,
                                fallback = "Gagal membuat komunitas. Silakan coba lagi."
                            )
                        )
                    }
                }

                is Result.Loading -> { /* Handled by isLoading flag */
                }
            }
        }
    }

    fun joinCommunity(code: String) {
        if (code.length < MIN_COMMUNITY_CODE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "Kode komunitas tidak valid") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            when (val result = repository.joinCommunity(code)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = result.data)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppErrorMapper.fromRawMessage(
                                rawMessage = result.message,
                                fallback = "Gagal bergabung ke komunitas. Silakan coba lagi."
                            )
                        )
                    }
                }

                is Result.Loading -> {}
            }
        }
    }

    // Fungsi untuk mereset pesan error/sukses setelah ditampilkan (misal oleh Snackbar)
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
