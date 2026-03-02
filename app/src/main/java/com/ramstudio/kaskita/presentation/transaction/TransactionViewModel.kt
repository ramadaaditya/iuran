package com.ramstudio.kaskita.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.TransactionUiModel
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun loadTransactionsByCommunity(communityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getTransactionsByCommunity(communityId).collect { list ->
                    _uiState.update {
                        it.copy(
                            transactions = list.map { t -> t.toUiModel() },
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approveTransaction(transactionId: String) {
        performAdminAction(transactionId, TransactionStatus.SUCCESS, "Transaksi berhasil disetujui")
    }

    fun rejectTransaction(transactionId: String) {
        performAdminAction(transactionId, TransactionStatus.REJECTED, "Transaksi ditolak")
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
                                selectedTransaction = updatedTransaction.toUiModel(),
                                transactions = state.transactions.map { item ->
                                    if (item.id == transactionId) updatedTransaction.toUiModel()
                                    else item
                                }
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                error = e.message ?: "Gagal memperbarui transaksi"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isActionLoading = false, error = e.message ?: "Terjadi kesalahan")
                }
            }
        }
    }


    fun loadTransactionDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val transaction = repository.getTransactionById(id)

                _uiState.update {
                    it.copy(
                        selectedTransaction = transaction?.toUiModel(),
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load detail"
                    )
                }
            }
        }
    }

    fun clearActionSuccess() {
        _uiState.update { it.copy(actionSuccess = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}