package com.ramstudio.kaskita.presentation.transaction

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.utils.AppErrorMapper
import com.ramstudio.kaskita.domain.model.TransactionCategory
import com.ramstudio.kaskita.domain.repository.AuthRepository
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
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
    val errorMessage: String? = null,
    val amount: String = "",
    val description: String = "",
    val transactionType: TransactionCategory = TransactionCategory.INCOME,
    val hasReceipt: Boolean = false,
    val receiptUri: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: ITransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onAmountChange(value: String) {
        _uiState.update { it.copy(amount = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onTypeChange(value: TransactionCategory) {
        _uiState.update { it.copy(transactionType = value) }
    }

    fun onReceiptSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                hasReceipt = uri != null,
                receiptUri = uri?.toString()
            )
        }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                amount = "",
                description = "",
                transactionType = TransactionCategory.INCOME,
                hasReceipt = false,
                receiptUri = null,
                isSuccess = false,
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submitTransaction(communityId: String, isAdmin: Boolean) {
        val state = _uiState.value

        Log.d("AddTxVM", "submitTransaction called — communityId: '$communityId'")
        Log.d(
            "AddTxVM",
            "amount: '${state.amount}', desc: '${state.description}', hasReceipt: ${state.hasReceipt}"
        )

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
        if (!state.hasReceipt) {
            Log.d("AddTxVM", "BLOCKED: no receipt")
            _uiState.update { it.copy(errorMessage = "Bukti transfer wajib dilampirkan") }
            return
        }
        if (state.receiptUri.isNullOrBlank()) {
            Log.d("AddTxVM", "BLOCKED: receipt URI missing")
            _uiState.update { it.copy(errorMessage = "Bukti transfer tidak valid. Pilih ulang foto.") }
            return
        }
        if (!isAdmin && state.transactionType == TransactionCategory.EXPENSE) {
            Log.d("AddTxVM", "BLOCKED: non-admin attempted EXPENSE")
            _uiState.update { it.copy(errorMessage = "Hanya admin yang bisa membuat transaksi pengeluaran") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Log.d("AddTxVM", "Calling repository.submitTransaction...")

            try {
                val currentUser = authRepository.getUser()
                val uploadResult = transactionRepository.uploadTransactionProof(
                    localUri = state.receiptUri,
                    userId = currentUser.id,
                    communityId = communityId
                )
                val uploadedProofUrl = uploadResult.getOrElse { uploadError ->
                    Log.e(TAG, "GAGAL UPLOAD BUKTI ${uploadError.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = AppErrorMapper.fromThrowable(
                                throwable = uploadError,
                                fallback = "Gagal upload bukti transfer. Coba lagi."
                            )
                        )
                    }
                    return@launch
                }

                val result = transactionRepository.submitTransaction(
                    communityId = communityId,
                    type = if (state.transactionType == TransactionCategory.INCOME) "IN" else "OUT",
                    amount = amountDouble.toLong(),
                    description = state.description.trim(),
                    proofUrl = uploadedProofUrl,
                    userId = currentUser.id
                )
                Log.d(
                    "AddTxVM",
                    "Result: isSuccess=${result.isSuccess}, error=${result.exceptionOrNull()?.message}"
                )
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true
                            )
                        }
                    },
                    onFailure = { e ->
                        Log.e("AddTxVM", "FAILED: ${e.message}", e)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = AppErrorMapper.fromThrowable(
                                    throwable = e,
                                    fallback = "Gagal mengirim transaksi. Silakan coba lagi."
                                )
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("AddTxVM", "EXCEPTION: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = AppErrorMapper.fromThrowable(
                            throwable = e,
                            fallback = "Gagal mengirim transaksi. Silakan coba lagi."
                        )
                    )
                }
            }
        }
    }
}
