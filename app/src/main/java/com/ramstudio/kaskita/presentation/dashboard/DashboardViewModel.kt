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
import javax.inject.Inject

sealed interface DashboardScreenState {

    data object Loading : DashboardScreenState
    data object Empty : DashboardScreenState
    data class Error(val message: String) : DashboardScreenState
    data class Success(
        val communities: List<Community> = emptyList(),
        val selectedCommunity: Community,
        val transactions: List<TransactionUiModel> = emptyList(),
        val totalIncome: Double = 0.0,
        val totalExpense: Double = 0.0,
        val pendingCount: Int = 0,
    ) : DashboardScreenState
}


data class DashboardUiState(
    val screenState: DashboardScreenState = DashboardScreenState.Loading,
    val isAdmin: Boolean = false,
    val currentUserId: String? = null,
    val selectedCommunityId: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val trxRepository: TransactionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _selectedCommunityId = MutableStateFlow<String?>(null)

    private val currentUserFlow: StateFlow<User?> = flow {
        emit(runCatching { authRepository.getUser() }.getOrNull())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val communityFlow: Flow<List<Community>> = communityRepository.getAllCommunity()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactionFlow: Flow<List<Transaction>> = _selectedCommunityId
        .flatMapLatest { communityId ->
            if (communityId == null) flowOf(emptyList())
            else trxRepository.getTransactionsByCommunity(communityId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val memberMapFlow: Flow<Map<String, String>> = _selectedCommunityId
        .flatMapLatest { communityId ->
            if (communityId == null) flowOf(emptyMap())
            else flow {
                val members = runCatching {
                    communityRepository.getMembersByCommunity(communityId)
                }.getOrDefault(emptyList())
                emit(members.associateBy({ it.id }, { it.name }))
            }
        }

    val uiState: StateFlow<DashboardUiState> =
        combine(
            currentUserFlow,
            communityFlow,
            _selectedCommunityId,
            transactionFlow,
            memberMapFlow
        ) { user, community, communityId, transactions, memberMap ->

            val activeCommunity = community.find { it.id == communityId }
                ?: community.firstOrNull()

            if (activeCommunity?.id != communityId) {
                _selectedCommunityId.value = activeCommunity?.id
            }

            val isAdmin = activeCommunity?.createdBy != null &&
                    activeCommunity.createdBy == user?.id


            val screenState: DashboardScreenState = if (activeCommunity == null) {
                DashboardScreenState.Empty
            } else {

                val uiModel = transactions.map { t ->
                    val fullName = when {
                        memberMap[t.userId]?.isNotBlank() == true -> memberMap[t.userId]!!
                        user != null && t.userId == user.id -> user.name
                        else -> "Community Member"
                    }
                    t.toUiModel().copy(
                        initiatorName = fullName,
                        subtitle = fullName,
                    )

                }

                val pendingCount = uiModel.count { it.status == TransactionStatus.PENDING }
                val totalIncome = transactions.filter {
                    it.type == TransactionCategory.INCOME && it.status == TransactionStatus.SUCCESS
                }.sumOf { it.amount }

                val totalExpense = transactions.filter {
                    it.type == TransactionCategory.EXPENSE && it.status == TransactionStatus.SUCCESS
                }.sumOf { it.amount }

                DashboardScreenState.Success(
                    communities = community,
                    selectedCommunity = activeCommunity,
                    transactions = uiModel,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    pendingCount = pendingCount,
                )
            }

            DashboardUiState(
                screenState = screenState,
                isAdmin = isAdmin,
                currentUserId = user?.id
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )


    fun setSelectedCommunityId(communityId: String?) {
        _selectedCommunityId.value = communityId?.takeIf { it.isNotBlank() }
    }

    fun selectCommunity(community: Community) {
        setSelectedCommunityId(community.id)
    }
}
