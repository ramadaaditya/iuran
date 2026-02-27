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
    // Summary
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val pendingCount: Int = 0,
    // Role
    val isAdmin: Boolean = false,
    // Current logged-in user id (for role check)
    val currentUserId: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ICommunityRepository,
    private val trxRepository: ITransactionRepository,

    ) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeCommunities()
        observeTransactions()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            trxRepository.getAllTransactions().collect { transactions ->
                val uiModels = transactions.map { it.toUiModel() }
                _uiState.update { state ->
                    state.copy(
                        transactions = uiModels,
                        pendingCount = uiModels.count {
                            it.status == TransactionStatus.PENDING
                        },
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

    private fun observeCommunities() {
        viewModelScope.launch {
            repository.getAllCommunity().collect { communities ->
                _uiState.update { currentState ->
                    val selected = currentState.selectedCommunity
                        ?: communities.firstOrNull()
                    val isAdmin = selected?.createdBy == currentState.currentUserId

                    currentState.copy(
                        communities = communities,
                        selectedCommunity = selected,
                        isAdmin = isAdmin
                    )
                }
            }
        }
    }

    fun selectCommunity(community: Community) {
        _uiState.update { state ->
            state.copy(
                selectedCommunity = community,
                isAdmin = community.createdBy == state.currentUserId
            )
        }
    }
}