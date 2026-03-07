package com.ramstudio.kaskita.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.domain.model.toUiModel
import com.ramstudio.kaskita.domain.repository.AuthRepository
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val actionSuccess: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: ITransactionRepository,
    private val communityRepository: ICommunityRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun loadTransactionsByCommunity(communityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = runCatching { authRepository.getUser() }.getOrNull()
                val memberNameById = runCatching {
                    communityRepository.getMembersByCommunity(communityId)
                }.getOrDefault(emptyList()).associateBy({ it.id }, { it.name })

                repository.getTransactionsByCommunity(communityId).collect { list ->
                    _uiState.update {
                        it.copy(
                            transactions = list.map { transaction ->
                                val initiatorName = when {
                                    memberNameById[transaction.userId].isNullOrBlank().not() ->
                                        memberNameById[transaction.userId].orEmpty()

                                    currentUser != null && transaction.userId == currentUser.id ->
                                        currentUser.name

                                    else -> "Community Member"
                                }

                                transaction.toUiModel().copy(
                                    initiatorName = initiatorName,
                                    subtitle = initiatorName
                                )
                            },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppErrorMapper.fromThrowable(
                            throwable = e,
                            fallback = "Gagal memuat transaksi. Silakan coba lagi."
                        )
                    )
                }
            }
        }
    }


    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
