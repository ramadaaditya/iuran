package com.ramstudio.kaskita.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.domain.model.TransactionUiModel
import com.ramstudio.kaskita.core.domain.model.toUiModel
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.domain.repository.CommunityRepository
import com.ramstudio.kaskita.core.domain.repository.TransactionRepository
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<TransactionUiModel> = emptyList(),
    val selectedTransaction: TransactionUiModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isActionLoading: Boolean = false,
    val actionSuccess: String? = null,
    val isAdmin: Boolean = false
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadTransactionsByCommunity(communityId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val memberNameById = runCatching {
                    communityRepository.getMembersByCommunity(communityId)
                }.getOrDefault(emptyList()).associateBy({ it.id }, { it.name })

                val currentUser = runCatching { authRepository.getUser() }.getOrNull()
                val community = runCatching {
                    communityRepository.getCommunityById(communityId)
                }.getOrNull()
                val isAdmin = currentUser?.id != null && community?.createdBy == currentUser.id

                repository.getTransactionsByCommunity(communityId).collect { list ->
                    val mapped = list.map { t ->
                        val fullName = when {
                            memberNameById[t.userId]?.isNotBlank() == true -> memberNameById[t.userId]!!
                            currentUser != null && t.userId == currentUser.id -> currentUser.name
                            else -> "Community Member"
                        }
                        t.toUiModel().copy(
                            initiatorName = fullName,
                            subtitle = fullName,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            transactions = mapped,
                            isLoading = false,
                            isAdmin = isAdmin
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAdmin = false,
                        error = AppErrorMapper.fromThrowable(
                            throwable = e,
                            fallback = "Gagal memuat transaksi. Silakan coba lagi."
                        )
                    )
                }
            }
        }
    }
}
