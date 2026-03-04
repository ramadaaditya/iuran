package com.ramstudio.kaskita.presentation.detailCommunity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.User
import com.ramstudio.kaskita.domain.repository.AuthRepository
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailCommunityUiState(
    val community: Community? = null,
    val transactions: List<Transaction> = emptyList(),
    val members: List<User> = emptyList(),
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val isAdmin: Boolean
        get() = community?.createdBy != null && community.createdBy == currentUserId
}

@HiltViewModel
class DetailCommunityViewModel @Inject constructor(
    private val communityRepository: ICommunityRepository,
    private val transactionRepository: ITransactionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailCommunityUiState())
    val uiState: StateFlow<DetailCommunityUiState> = _uiState.asStateFlow()

    fun load(communityId: String) {
        if (communityId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Load current user
                val user = try { authRepository.getUser() } catch (_: Exception) { null }
                _uiState.update { it.copy(currentUserId = user?.id) }

                // Load community details
                val community = communityRepository.getCommunityById(communityId)
                _uiState.update { it.copy(community = community) }

                // Load members
                val members = communityRepository.getMembersByCommunity(communityId)
                _uiState.update { it.copy(members = members) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        // Observe transactions as a flow
        viewModelScope.launch {
            try {
                transactionRepository.getTransactionsByCommunity(communityId).collect { txList ->
                    _uiState.update { it.copy(transactions = txList) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}