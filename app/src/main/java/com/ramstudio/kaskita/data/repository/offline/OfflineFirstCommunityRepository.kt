package com.ramstudio.kaskita.data.repository.offline

import com.ramstudio.kaskita.data.datasource.local.CommunityLocalDataSource
import com.ramstudio.kaskita.data.datasource.remote.CommunityRemoteDataSource
import com.ramstudio.kaskita.data.sync.SyncPolicyStore
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.Result
import com.ramstudio.kaskita.domain.model.User
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstCommunityRepository @Inject constructor(
    private val localDataSource: CommunityLocalDataSource,
    private val remoteDataSource: CommunityRemoteDataSource,
    private val syncPolicyStore: SyncPolicyStore,
) : ICommunityRepository {

    private val refreshMutex = Mutex()

    override suspend fun createCommunity(name: String, desc: String): Result<String> {
        val result = remoteDataSource.createCommunity(name, desc)
        if (result is Result.Success) {
            refreshCommunities(force = true)
        }
        return result
    }

    override suspend fun joinCommunity(codeInput: String): Result<String> {
        val result = remoteDataSource.joinCommunity(codeInput)
        if (result is Result.Success) {
            refreshCommunities(force = true)
        }
        return result
    }

    override fun getAllCommunity(): Flow<List<Community>> {
        return localDataSource.observeCommunities()
            .onStart {
                refreshCommunities(force = false)
            }
    }

    override suspend fun getCommunityById(communityId: String): Community? {
        localDataSource.getCommunityById(communityId)?.let { return it }

        val remote = runCatching { remoteDataSource.getCommunityById(communityId) }.getOrNull()
        if (remote != null) {
            localDataSource.upsertCommunity(remote)
            syncPolicyStore.markSynced(syncKeyCommunityById(communityId))
        }

        return localDataSource.getCommunityById(communityId) ?: remote
    }

    override suspend fun getMembersByCommunity(communityId: String): List<User> {
        val cached = localDataSource.getMembersByCommunity(communityId)
        if (cached.isNotEmpty() && !syncPolicyStore.shouldSync(syncKeyMembers(communityId), MEMBERS_SYNC_MAX_AGE)) {
            return cached
        }

        val remote = runCatching { remoteDataSource.getMembersByCommunity(communityId) }
            .getOrDefault(emptyList())
        if (remote.isNotEmpty()) {
            localDataSource.replaceMembersByCommunity(communityId, remote)
            syncPolicyStore.markSynced(syncKeyMembers(communityId))
            return remote
        }

        return cached
    }

    private suspend fun refreshCommunities(force: Boolean) {
        refreshMutex.withLock {
            val shouldSync = force || syncPolicyStore.shouldSync(SYNC_KEY_ALL_COMMUNITIES, COMMUNITIES_SYNC_MAX_AGE)
            if (!shouldSync) return

            runCatching {
                val remote = remoteDataSource.getAllCommunity().first()
                localDataSource.replaceCommunities(remote)
                syncPolicyStore.markSynced(SYNC_KEY_ALL_COMMUNITIES)
            }
        }
    }

    private fun syncKeyMembers(communityId: String) = "sync_community_members_$communityId"
    private fun syncKeyCommunityById(communityId: String) = "sync_community_by_id_$communityId"

    companion object {
        private const val SYNC_KEY_ALL_COMMUNITIES = "sync_all_communities"
        private const val COMMUNITIES_SYNC_MAX_AGE = 5 * 60 * 1000L
        private const val MEMBERS_SYNC_MAX_AGE = 5 * 60 * 1000L
    }
}
