package com.ramstudio.kaskita.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.core.domain.model.Community
import com.ramstudio.kaskita.core.domain.model.Transaction
import com.ramstudio.kaskita.core.domain.model.TransactionCategory
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.model.TransactionUiModel
import com.ramstudio.kaskita.core.domain.model.User
import com.ramstudio.kaskita.core.domain.model.toUiModel
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import com.ramstudio.kaskita.core.domain.repository.CommunityRepository
import com.ramstudio.kaskita.core.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val repository: CommunityRepository,
    private val trxRepository: TransactionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _selectedCommunityId = MutableStateFlow<String?>(null)
    private val currentUserFlow: Flow<User?> = flow {
        emit(authRepository.getUser())

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactionFlow: Flow<List<Transaction>> = _selectedCommunityId
        .flatMapLatest { communityId ->
            if (communityId == null) {
                flowOf(emptyList())
            } else {
                trxRepository.getTransactionsByCommunity(communityId)
            }
        }

    val uiState: StateFlow<DashboardUiState> =
        combine(
            currentUserFlow,
            repository.getAllCommunity(),
            _selectedCommunityId,
            transactionFlow
        ) { user, community, communityId, transactions ->

            val activeCommunity = community.find { it.id == communityId }
                ?: community.firstOrNull()

            val isAdmin = activeCommunity?.createdBy != null &&
                    activeCommunity.createdBy == user?.id

            val uiModel = transactions.map { it.toUiModel() }
            val pendingCount = uiModel.count { it.status == TransactionStatus.PENDING }
            val totalIncome = transactions.filter {
                it.type == TransactionCategory.INCOME && it.status == TransactionStatus.SUCCESS
            }.sumOf { it.amount }

            val totalExpense = transactions.filter {
                it.type == TransactionCategory.EXPENSE && it.status == TransactionStatus.SUCCESS

            }.sumOf { it.amount }

            DashboardUiState(
                communities = community,
                selectedCommunity = activeCommunity,
                transactions = uiModel,
                isLoading = false,
                error = null,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                pendingCount = pendingCount,
                isAdmin = isAdmin,
                currentUserId = user?.id
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )


    fun selectCommunity(community: Community) {
        _selectedCommunityId.update { community.id }
    }
}
