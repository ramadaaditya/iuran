package com.ramstudio.kaskita.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.TransactionCategory
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.domain.model.toUiModel
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val communities: List<Community> = emptyList(),
    val selectedCommunity: Community? = null,
    val transactions: List<TransactionUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val pendingCount: Int = 0,
    val isAdmin: Boolean = false,
    val currentUserId: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ICommunityRepository,
    private val trxRepository: ITransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var transactionJob: Job? = null

    init {
        observeCommunities()
    }

    private fun observeCommunities() {
        viewModelScope.launch {
            repository.getAllCommunity().collect { communities ->
                _uiState.update { currentState ->
                    val selected = currentState.selectedCommunity
                        ?.let { prev -> communities.find { it.id == prev.id } }
                        ?: communities.firstOrNull()
                    val isAdmin = selected?.createdBy == currentState.currentUserId
                    currentState.copy(
                        communities = communities,
                        selectedCommunity = selected,
                        isAdmin = isAdmin
                    )
                }
                // Kick off transactions for the (possibly new) selected community
                val selectedId = _uiState.value.selectedCommunity?.id
                if (selectedId != null) {
                    observeTransactionsForCommunity(selectedId)
                }
            }
        }
    }

    private fun observeTransactionsForCommunity(communityId: String) {
        transactionJob?.cancel()
        transactionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            trxRepository.getTransactionsByCommunity(communityId).collect { transactions ->
                val uiModels = transactions.map { it.toUiModel() }
                _uiState.update { state ->
                    state.copy(
                        transactions = uiModels,
                        isLoading = false,
                        pendingCount = uiModels.count { it.status == TransactionStatus.PENDING },
                        totalIncome = transactions
                            .filter { it.type == TransactionCategory.INCOME && it.status == TransactionStatus.SUCCESS }
                            .sumOf { it.amount },
                        totalExpense = transactions
                            .filter { it.type == TransactionCategory.EXPENSE && it.status == TransactionStatus.SUCCESS }
                            .sumOf { it.amount }
                    )
                }
            }
        }
    }

    fun selectCommunity(community: Community) {
        val communityId = community.id ?: return
        _uiState.update { state ->
            state.copy(
                selectedCommunity = community,
                isAdmin = community.createdBy == state.currentUserId,
                // Clear stale data while new fetch is in progress
                transactions = emptyList(),
                totalIncome = 0.0,
                totalExpense = 0.0,
                pendingCount = 0
            )
        }
        observeTransactionsForCommunity(communityId)
    }
}