package com.ramstudio.kaskita.data.datasource.local

import com.ramstudio.kaskita.data.local.dao.CommunityDao
import com.ramstudio.kaskita.data.local.toDomain
import com.ramstudio.kaskita.data.local.toEntity
import com.ramstudio.kaskita.data.local.toMemberEntity
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityLocalDataSource @Inject constructor(
    private val communityDao: CommunityDao
) {
    fun observeCommunities(): Flow<List<Community>> {
        return communityDao.observeAllCommunities().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    suspend fun getCommunityById(communityId: String): Community? {
        val community = communityDao.getCommunityById(communityId) ?: return null
        val memberCount = communityDao.countMembersByCommunity(communityId)
        return community.toDomain(memberCountOverride = maxOf(memberCount, community.membersCount))
    }

    suspend fun replaceCommunities(communities: List<Community>) {
        val now = System.currentTimeMillis()
        communityDao.clearCommunities()
        communityDao.upsertCommunities(communities.mapNotNull { it.toEntity(now) })
    }

    suspend fun upsertCommunity(community: Community) {
        community.toEntity(System.currentTimeMillis())?.let { communityDao.upsertCommunity(it) }
    }

    suspend fun getMembersByCommunity(communityId: String): List<User> {
        return communityDao.getMembersByCommunity(communityId).map { it.toDomain() }
    }

    suspend fun replaceMembersByCommunity(communityId: String, members: List<User>) {
        val now = System.currentTimeMillis()
        communityDao.clearMembersByCommunity(communityId)
        communityDao.upsertMembers(members.map { it.toMemberEntity(communityId, now) })
    }
}
