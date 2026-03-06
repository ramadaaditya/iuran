package com.ramstudio.kaskita.data.datasource.remote

import com.ramstudio.kaskita.data.repository.RemoteCommunityRepository
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.Result
import com.ramstudio.kaskita.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRemoteDataSourceImpl @Inject constructor(
    private val remoteRepository: RemoteCommunityRepository
) : CommunityRemoteDataSource {
    override suspend fun createCommunity(name: String, desc: String): Result<String> {
        return remoteRepository.createCommunity(name, desc)
    }

    override suspend fun joinCommunity(codeInput: String): Result<String> {
        return remoteRepository.joinCommunity(codeInput)
    }

    override fun getAllCommunity(): Flow<List<Community>> {
        return remoteRepository.getAllCommunity()
    }

    override suspend fun getCommunityById(communityId: String): Community? {
        return remoteRepository.getCommunityById(communityId)
    }

    override suspend fun getMembersByCommunity(communityId: String): List<User> {
        return remoteRepository.getMembersByCommunity(communityId)
    }
}
