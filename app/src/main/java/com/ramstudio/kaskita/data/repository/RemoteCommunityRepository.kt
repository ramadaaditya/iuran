package com.ramstudio.kaskita.data.repository

import android.os.Build
import com.ramstudio.kaskita.core.utils.AvatarUtils
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.JoinResponse
import com.ramstudio.kaskita.domain.model.Result
import com.ramstudio.kaskita.domain.model.User
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteCommunityRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : ICommunityRepository {

    override suspend fun createCommunity(
        name: String,
        desc: String
    ): Result<String> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Result.Error("User belum login")

            val uniqueCode = (1..6).map { ('A'..'Z').random() }.joinToString("")

            val rpcParams = buildJsonObject {
                put("name_input", name)
                put("desc_input", desc)
                put("code_input", uniqueCode)
            }

            val response = postgrest.rpc(
                function = "create_community_with_admin",
                parameters = rpcParams
            ).decodeSingle<JoinResponse>()

            if (response.success) {
                Result.Success("Komunitas $name berhasil dibuat! Kode: $uniqueCode")
            } else {
                Result.Error(response.message)
            }

        } catch (e: Exception) {
            Result.Error(e.message ?: "Gagal membuat komunitas")
        }
    }

    // ----------------------------
    // JOIN COMMUNITY (RPC)
    // ----------------------------
    override suspend fun joinCommunity(codeInput: String): Result<String> {
        return try {
            val rpcParams = buildJsonObject {
                put("input_code", codeInput)
            }

            val response = postgrest.rpc(
                function = "join_community_by_code",
                parameters = rpcParams
            ).decodeSingle<JoinResponse>()

            if (response.success) {
                Result.Success(response.message)
            } else {
                Result.Error(response.message)
            }
        } catch (e: Exception) {
            Result.Error("Gagal bergabung: ${e.message}")
        }
    }

    override fun getAllCommunity(): Flow<List<Community>> = flow {

        val user = auth.currentUserOrNull()
            ?: return@flow emit(emptyList())

        val memberRows = postgrest
            .from("community_members")
            .select {
                filter {
                    eq("user_id", user.id)
                }
            }
            .decodeList<MemberRow>()

        val communityIds = memberRows.map { it.community_id }

        if (communityIds.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        // 2️⃣ Ambil data community berdasarkan list ID
        val communities = postgrest
            .from("communities")
            .select {
                filter {
                    isIn("id", communityIds)
                }
            }
            .decodeList<CommunityRow>()

        val communityMemberRows = postgrest
            .from("community_members")
            .select {
                filter {
                    isIn("community_id", communityIds)
                }
            }
            .decodeList<MemberRow>()

        val memberCountByCommunityId = communityMemberRows
            .groupingBy { it.community_id }
            .eachCount()

        emit(
            communities.map {
                Community(
                    id = it.id,
                    name = it.name,
                    description = it.description ?: "",
                    code = it.code,
                    createdBy = it.created_by,
                    balance = it.balance?.toDouble() ?: 0.0,
                    membersCount = memberCountByCommunityId[it.id] ?: 0
                )
            }
        )
    }

    override suspend fun getCommunityById(communityId: String): Community? {
        return try {
            val row = postgrest
                .from("communities")
                .select {
                    filter { eq("id", communityId) }
                    limit(1)
                }
                .decodeSingle<CommunityRow>()
            Community(
                id = row.id,
                name = row.name,
                description = row.description ?: "",
                code = row.code,
                createdBy = row.created_by,
                balance = row.balance?.toDouble() ?: 0.0
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getMembersByCommunity(communityId: String): List<User> {
        return try {
            val memberRows = postgrest
                .from("community_members")
                .select {
                    filter { eq("community_id", communityId) }
                }
                .decodeList<MemberRow>()

            val userIds = memberRows.map { it.user_id }
            if (userIds.isEmpty()) return emptyList()

            val adminId = getCommunityById(communityId)?.createdBy

            val profiles = postgrest
                .from("profiles")
                .select {
                    filter { isIn("id", userIds) }
                }
                .decodeList<ProfileRow>()

            profiles.map { profile ->
                val isAdmin = profile.id == adminId
                User(
                    id = profile.id,
                    name = profile.full_name ?: "Unknown",
                    role = if (isAdmin) "Admin" else "Member",
                    initial = AvatarUtils.getInitials(profile.full_name),
                    email = profile.email
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

private fun parseIsoToMillis(iso: String): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        java.time.Instant.parse(iso).toEpochMilli()
    } else {
        TODO("VERSION.SDK_INT < O")
    }
}

@Serializable
private data class MemberRow(
    val community_id: String,
    val user_id: String
)

@Serializable
private data class CommunityRow(
    val id: String,
    val name: String,
    val description: String? = null,
    val code: String,
    val balance: Long? = 0,
    val created_by: String? = null
)

@Serializable
private data class ProfileRow(
    val id: String,
    val full_name: String? = null,
    val email: String? = null
)
