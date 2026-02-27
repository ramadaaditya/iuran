package com.ramstudio.kaskita.data.repository

import android.os.Build
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.JoinResponse
import com.ramstudio.kaskita.domain.model.Result
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

        emit(
            communities.map {
                Community(
                    id = it.id,
                    name = it.name,
                    description = it.description ?: "",
                    code = it.code,
                    balance = it.balance?.toDouble() ?: 0.0
                )
            }
        )
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
    val balance: Long? = 0
)

