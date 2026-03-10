package com.ramstudio.kaskita.core.domain.repository

import com.ramstudio.kaskita.core.domain.model.Community
import com.ramstudio.kaskita.core.domain.model.Result
import com.ramstudio.kaskita.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    suspend fun createCommunity(name: String, desc: String): Result<String>
    suspend fun joinCommunity(codeInput: String): Result<String>
    fun getAllCommunity(): Flow<List<Community>>
    suspend fun getCommunityById(communityId: String): Community?
    suspend fun getMembersByCommunity(communityId: String): List<User>
}