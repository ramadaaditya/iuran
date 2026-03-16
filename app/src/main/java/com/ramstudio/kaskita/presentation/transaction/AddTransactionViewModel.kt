package com.ramstudio.kaskita.presentation.transaction

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.model.TransactionCategory
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.domain.repository.TransactionRepository
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val amount: String = "",
    val description: String = "",
    val transactionType: TransactionCategory = TransactionCategory.INCOME,
    val hasReceipt: Boolean = false,
    val receiptUri: String? = null,
    val uploadedProofUrl: String? = null,
    val isEditMode: Boolean = false,
    val editTransactionId: String? = null,
    val editCommunityId: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amount = value) }
    }

    fun onReceiptSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                hasReceipt = uri != null,
                receiptUri = uri?.toString(),
                uploadedProofUrl = null
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onTypeChange(value: TransactionCategory) {
        _uiState.update { it.copy(transactionType = value) }
    }


    fun clearForm() {
        _uiState.update {
            it.copy(
                amount = "",
                description = "",
                transactionType = TransactionCategory.INCOME,
                hasReceipt = false,
                receiptUri = null,
                uploadedProofUrl = null,
                isLoading = false,
                isUploading = false,
                isSuccess = false,
                errorMessage = null,
                isEditMode = false,
                editTransactionId = null,
                editCommunityId = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submitTransaction(communityId: String, isAdmin: Boolean) {
        val state = _uiState.value
        if (state.isLoading || state.isUploading) return

        val amountDouble = state.amount.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0.0) {
            Log.d("AddTxVM", "BLOCKED: amount invalid")
            _uiState.update { it.copy(errorMessage = "Masukkan nominal yang valid") }
            return
        }
        if (state.description.isBlank()) {
            Log.d("AddTxVM", "BLOCKED: description blank")
            _uiState.update { it.copy(errorMessage = "Deskripsi tidak boleh kosong") }
            return
        }
        if (!state.hasReceipt || state.receiptUri.isNullOrBlank()) {
            Log.d("AddTxVM", "BLOCKED: no receipt")
            _uiState.update { it.copy(errorMessage = "Bukti transfer wajib dilampirkan") }
            return
        }
        if (!isAdmin && state.transactionType == TransactionCategory.EXPENSE) {
            Log.d("AddTxVM", "BLOCKED: non-admin attempted EXPENSE")
            _uiState.update { it.copy(errorMessage = "Hanya admin yang bisa membuat transaksi pengeluaran") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentUser = authRepository.getUser()
            val userId = currentUser?.id.orEmpty()
            if (communityId.isBlank() || userId.isBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Data pengguna atau komunitas tidak valid."
                    )
                }
                return@launch
            }

            val uploadedProofUrl = when (
                val proofResult = ensureProofUploaded(
                    state = _uiState.value,
                    communityId = communityId,
                    userId = userId
                )
            ) {
                is Result.Success -> proofResult.data
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppErrorMapper.fromThrowable(
                                throwable = proofResult.throwable,
                                fallback = "Gagal upload bukti transfer. Coba lagi."
                            )
                        )
                    }
                    return@launch
                }
            }

            val latestState = _uiState.value
            val result = transactionRepository.submitTransaction(
                communityId = communityId,
                type = if (latestState.transactionType == TransactionCategory.INCOME) "IN" else "OUT",
                amount = amountDouble.toLong(),
                description = latestState.description.trim(),
                proofUrl = uploadedProofUrl,
                userId = userId
            )

            when (result) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppErrorMapper.fromThrowable(
                                throwable = result.throwable,
                                fallback = "Gagal mengirim transaksi. Silakan coba lagi."
                            )
                        )
                    }
                }

                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                }
            }
        }
    }

    fun loadTransactionForEdit(transactionId: String) {
        val currentState = _uiState.value
        if (currentState.isEditMode && currentState.editTransactionId == transactionId) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val transaction = transactionRepository.getTransactionById(transactionId)
            val currentUser = authRepository.getUser()

            if (transaction == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Transaksi tidak ditemukan."
                    )
                }
                return@launch
            }

            val canEdit =
                transaction.status == TransactionStatus.REJECTED &&
                        currentUser?.id == transaction.userId

            if (!canEdit) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Transaksi ini tidak dapat diedit."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isEditMode = true,
                    editTransactionId = transactionId,
                    editCommunityId = transaction.communityId,
                    amount = transaction.amount.toLong().toString(),
                    description = transaction.description.orEmpty(),
                    transactionType = transaction.type,
                    hasReceipt = !transaction.proofUrl.isNullOrBlank(),
                    receiptUri = transaction.proofUrl,
                    uploadedProofUrl = transaction.proofUrl
                )
            }
        }
    }

    fun submitEditedTransaction(fallbackCommunityId: String, isAdmin: Boolean) {
        val state = _uiState.value
        if (state.isLoading || state.isUploading) return

        val transactionId = state.editTransactionId ?: run {
            _uiState.update { it.copy(errorMessage = "Transaksi tidak valid untuk diedit.") }
            return
        }

        val amountDouble = state.amount.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Masukkan nominal yang valid") }
            return
        }
        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Deskripsi tidak boleh kosong") }
            return
        }
        if (!state.hasReceipt || state.receiptUri.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Bukti transfer wajib dilampirkan") }
            return
        }
        if (!isAdmin && state.transactionType == TransactionCategory.EXPENSE) {
            _uiState.update { it.copy(errorMessage = "Hanya admin yang bisa membuat transaksi pengeluaran") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentUser = authRepository.getUser()
            val userId = currentUser?.id.orEmpty()
            val communityId = state.editCommunityId ?: fallbackCommunityId
            if (communityId.isBlank() || userId.isBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Komunitas atau pengguna tidak valid."
                    )
                }
                return@launch
            }

            val uploadedProofUrl = when (
                val uploadResult = ensureProofUploaded(
                    state = _uiState.value,
                    communityId = communityId,
                    userId = userId
                )
            ) {
                is Result.Success -> uploadResult.data
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppErrorMapper.fromThrowable(
                                throwable = uploadResult.throwable,
                                fallback = "Gagal upload bukti transfer. Coba lagi."
                            )
                        )
                    }
                    return@launch
                }
            }

            val latestState = _uiState.value
            val result = transactionRepository.resubmitTransaction(
                transactionId = transactionId,
                type = if (latestState.transactionType == TransactionCategory.INCOME) "IN" else "OUT",
                amount = amountDouble.toLong(),
                description = latestState.description.trim(),
                proofUrl = uploadedProofUrl
            )

            when (result) {
                is Result.Error -> {
                    Log.e(TAG, "submitEditedTransaction: ${result.throwable.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppErrorMapper.fromThrowable(
                                throwable = result.throwable,
                                fallback = "Gagal mengirim ulang transaksi. Silakan coba lagi."
                            )
                        )
                    }
                }

                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                }
            }
        }
    }

    private suspend fun ensureProofUploaded(
        state: AddTransactionUiState,
        communityId: String,
        userId: String
    ): Result<String> {
        state.uploadedProofUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { return Result.Success(it) }

        val localUri = state.receiptUri?.takeIf { it.isNotBlank() }
            ?: return Result.Error(IllegalStateException("Bukti transfer belum dipilih."))

        if (localUri.startsWith("http://") || localUri.startsWith("https://")) {
            _uiState.update { it.copy(uploadedProofUrl = localUri, hasReceipt = true) }
            return Result.Success(localUri)
        }

        _uiState.update { it.copy(isUploading = true) }
        val result = transactionRepository.uploadTransactionProof(
            localUri = localUri,
            userId = userId,
            communityId = communityId
        )

        return when (result) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadedProofUrl = result.data,
                        receiptUri = result.data,
                        hasReceipt = true
                    )
                }
                Result.Success(result.data)
            }

            is Result.Error -> {
                _uiState.update { it.copy(isUploading = false) }
                Result.Error(result.throwable)
            }
        }
    }
}
