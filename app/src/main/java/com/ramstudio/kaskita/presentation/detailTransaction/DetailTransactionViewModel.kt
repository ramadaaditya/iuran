package com.ramstudio.kaskita.presentation.detailTransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.domain.model.User
import com.ramstudio.kaskita.domain.model.toUiModel
import com.ramstudio.kaskita.domain.repository.AuthRepository
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailTransactionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val selectedTransaction: TransactionUiModel? = null,
    val isActionLoading: Boolean = false,
    val actionSuccess: String? = null
)

@HiltViewModel
class DetailTransactionViewModel @Inject constructor(
    private val repository: ITransactionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailTransactionUiState())
    val uiState: StateFlow<DetailTransactionUiState> = _uiState.asStateFlow()

    fun loadTransactionDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, selectedTransaction = null) }

            try {
                val transaction = repository.getTransactionById(id)
                val currentUser = runCatching { authRepository.getUser() }.getOrNull()
                val transactionUiModel = transaction?.let { detail ->
                    detail.toUiModel().copy(
                        initiatorName = when {
                            currentUser == null -> detail.userId
                            detail.userId == currentUser.id -> currentUser.name
                            else -> detail.userId
                        }
                    )
                }
                _uiState.update {
                    it.copy(
                        selectedTransaction = transactionUiModel,
                        user = currentUser,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppErrorMapper.fromThrowable(
                            throwable = e,
                            fallback = "Gagal memuat detail transaksi. Silakan coba lagi."
                        )
                    )
                }
            }
        }
    }

    fun approveTransaction(transactionId: String) {
        performAdminAction(transactionId, TransactionStatus.SUCCESS, "Transaksi berhasil disetujui")
    }

    fun rejectTransaction(transactionId: String) {
        performAdminAction(transactionId, TransactionStatus.REJECTED, "Transaksi ditolak")
    }

    fun clearActionSuccess() {
        _uiState.update { it.copy(actionSuccess = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }


    private fun performAdminAction(
        transactionId: String,
        newStatus: TransactionStatus,
        successMessage: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true, error = null) }
            try {
                val currentUser = authRepository.getUser()

                val result = repository.updateTransaction(
                    transactionId = transactionId,
                    newStatus = newStatus,
                    approvedBy = currentUser.id
                )

                result.fold(
                    onSuccess = { updatedTransaction ->
                        _uiState.update { state ->
                            state.copy(
                                isActionLoading = false,
                                actionSuccess = successMessage,
                                selectedTransaction = updatedTransaction.toUiModel().copy(
                                    initiatorName = state.selectedTransaction?.initiatorName
                                        ?: updatedTransaction.userId
                                ),
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                error = AppErrorMapper.fromThrowable(
                                    throwable = e,
                                    fallback = "Gagal memperbarui transaksi. Silakan coba lagi."
                                )
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        error = AppErrorMapper.fromThrowable(
                            throwable = e,
                            fallback = "Gagal memperbarui transaksi. Silakan coba lagi."
                        )
                    )
                }
            }
        }
    }
}
