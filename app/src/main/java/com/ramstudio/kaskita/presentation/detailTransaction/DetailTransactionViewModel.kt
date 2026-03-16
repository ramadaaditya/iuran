package com.ramstudio.kaskita.presentation.detailTransaction

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.model.TransactionUiModel
import com.ramstudio.kaskita.core.domain.model.User
import com.ramstudio.kaskita.core.domain.model.toUiModel
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.domain.repository.CommunityRepository
import com.ramstudio.kaskita.core.domain.repository.TransactionRepository
import com.ramstudio.kaskita.core.utils.AppErrorMapper
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
    val canManageTransaction: Boolean = false,
    val canEditTransaction: Boolean = false,
    val selectedTransaction: TransactionUiModel? = null,
    val isActionLoading: Boolean = false,
    val actionSuccess: String? = null
)

@HiltViewModel
class DetailTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val communityRepository: CommunityRepository,
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
                val canManageTransaction = if (transaction != null && currentUser != null) {
                    val community = communityRepository.getCommunityById(transaction.communityId)
                    community?.createdBy == currentUser.id
                } else {
                    false
                }
                val canEditTransaction = transaction != null
                    && transaction.status == TransactionStatus.REJECTED
                    && currentUser?.id == transaction.userId
                val transactionUiModel = transaction?.let { detail ->
                    val memberNameById = runCatching {
                        communityRepository.getMembersByCommunity(detail.communityId)
                    }
                        .getOrDefault(emptyList())
                        .associateBy({ it.id }, { it.name })
                    detail.toUiModel().copy(
                        initiatorName = when {
                            memberNameById[detail.userId].isNullOrBlank()
                                .not() -> memberNameById[detail.userId].orEmpty()

                            currentUser != null && detail.userId == currentUser.id -> currentUser.name
                            else -> "Community Member"
                        }
                    )
                }
                _uiState.update {
                    it.copy(
                        selectedTransaction = transactionUiModel,
                        user = currentUser,
                        canManageTransaction = canManageTransaction,
                        canEditTransaction = canEditTransaction,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    Log.e(TAG, "loadTransactionDetail: ${e.message}")
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

    fun rejectTransaction(transactionId: String, rejectionReason: String) {
        performAdminAction(
            transactionId,
            TransactionStatus.REJECTED,
            "Transaksi ditolak",
            rejectionReason
        )
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
        successMessage: String,
        rejectionReason: String? = null
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.canManageTransaction) {
                _uiState.update {
                    it.copy(error = "Hanya admin komunitas yang dapat memperbarui status transaksi.")
                }
                return@launch
            }
            if (state.selectedTransaction?.status != TransactionStatus.PENDING) {
                _uiState.update {
                    it.copy(error = "Status transaksi ini tidak dapat diubah lagi.")
                }
                return@launch
            }
            _uiState.update { it.copy(isActionLoading = true, error = null) }
            val currentUser = authRepository.getUser()

            val result = repository.updateTransaction(
                transactionId = transactionId,
                newStatus = newStatus,
                approvedBy = currentUser?.id.orEmpty(),
                rejectionReason = rejectionReason
            )

            when (result) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = AppErrorMapper.fromThrowable(
                                throwable = result.throwable,
                                fallback = "Gagal memperbarui transaksi. Silakan coba lagi."
                            )
                        )
                    }
                }

                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isActionLoading = false,
                            actionSuccess = successMessage,
                            selectedTransaction = result.data.toUiModel().copy(
                                initiatorName = state.selectedTransaction?.initiatorName
                                    ?: result.data.userId
                            ),
                            canEditTransaction = result.data.status == TransactionStatus.REJECTED
                                && state.user?.id == result.data.userId
                        )
                    }
                }
            }
        }
    }
}
