package com.ramstudio.kaskita.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ramstudio.kaskita.data.local.entity.CommunityEntity
import com.ramstudio.kaskita.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {
    @Query("SELECT * FROM communities ORDER BY name ASC")
    fun observeAllCommunities(): Flow<List<CommunityEntity>>

    @Query("SELECT * FROM communities WHERE id = :communityId LIMIT 1")
    suspend fun getCommunityById(communityId: String): CommunityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommunities(communities: List<CommunityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommunity(community: CommunityEntity)

    @Query("DELETE FROM communities")
    suspend fun clearCommunities()

    @Query("SELECT * FROM community_members WHERE communityId = :communityId ORDER BY name ASC")
    suspend fun getMembersByCommunity(communityId: String): List<MemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMembers(members: List<MemberEntity>)

    @Query("DELETE FROM community_members WHERE communityId = :communityId")
    suspend fun clearMembersByCommunity(communityId: String)

    @Query("SELECT COUNT(*) FROM community_members WHERE communityId = :communityId")
    suspend fun countMembersByCommunity(communityId: String): Int
}
