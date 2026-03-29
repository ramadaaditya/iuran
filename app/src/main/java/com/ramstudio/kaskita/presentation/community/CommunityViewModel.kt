package com.ramstudio.kaskita.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.common.UiState
import com.ramstudio.kaskita.core.domain.model.Community
import com.ramstudio.kaskita.core.domain.model.Result
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.domain.repository.CommunityRepository
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val screenState: UiState<List<Community>> = UiState.Loading,
    val isActionLoading: Boolean = false,
)

sealed interface CommunityEvent {
    data class ShowError(val message: String) : CommunityEvent
    data class ShowSuccess(val message: String) : CommunityEvent
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<CommunityEvent>()

    val uiEvent = _uiEvent.receiveAsFlow()


    companion object {
        private const val MIN_COMMUNITY_CODE_LENGTH = 5
    }

    init {
        observeCommunities()
    }

//    private fun loadCurrentUser() {
//        viewModelScope.launch {
//            try {
//                val user = authRepository.getUser()
//                _uiState.update { it.copy(currentUserId = user?.id) }
//            } catch (_: Exception) {
//                _uiState.update { it.copy(currentUserId = null) }
//            }
//        }
//    }


    private fun observeCommunities() {
        viewModelScope.launch {
            repository.getAllCommunity().collect { communities ->
                _uiState.update {
                    it.copy(screenState = UiState.Success(communities))
                }
            }
        }
    }

    fun createCommunity(name: String, desc: String) {
        if (name.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(CommunityEvent.ShowError("Nama komunitas tidak boleh kosong"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isActionLoading = true)
            }

            when (val result = repository.createCommunity(name, desc)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    _uiEvent.send(CommunityEvent.ShowSuccess(result.data))
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(isActionLoading = false)
                    }
                    _uiEvent.send(
                        CommunityEvent.ShowError(
                            AppErrorMapper.fromRawMessage(
                                rawMessage = result.message,
                                fallback = "Gagal membuat komunitas. Silakan coba lagi."
                            )
                        )
                    )
                }

                is Result.Loading -> {}
            }
        }
    }

    fun joinCommunity(code: String) {
        if (code.length < MIN_COMMUNITY_CODE_LENGTH) {
            viewModelScope.launch {
                _uiEvent.send(CommunityEvent.ShowError("Kode komunitas tidak valid"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isActionLoading = true)
            }

            when (val result = repository.joinCommunity(code)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isActionLoading = false) }
                    _uiEvent.send(CommunityEvent.ShowSuccess(result.data))
                }

                is Result.Error -> {
                    _uiState.update { it.copy(isActionLoading = false)
                    }

                    _uiEvent.send(
                        CommunityEvent.ShowError(
                            AppErrorMapper.fromRawMessage(
                                rawMessage = result.message,
                                fallback = "Gagal bergabung ke komunitas. Silakan coba lagi."
                            )
                        )
                    )
                }

                is Result.Loading -> {}
            }
        }
    }
}
