package com.ramstudio.kaskita.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.domain.model.toUiModel
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


    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}