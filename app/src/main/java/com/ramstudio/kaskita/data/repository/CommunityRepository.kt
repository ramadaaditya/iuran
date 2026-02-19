package com.ramstudio.kaskita.data.repository

import com.ramstudio.kaskita.domain.model.JoinResponse
import com.ramstudio.kaskita.domain.model.Result
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : ICommunityRepository {
    override suspend fun createCommunity(
        name: String,
        desc: String
    ): Result<String> {
        return try {
            val user = auth.currentUserOrNull() ?: return Result.Error("User belum login")

            // Generate kode unik 6 karakter
            val uniqueCode = (1..6).map { ('A'..'Z').random() }.joinToString("")

            // FIX 1: Gunakan 'buildJsonObject' untuk parameter
            val rpcParams = buildJsonObject {
                put("name_input", name)
                put("desc_input", desc)
                put("code_input", uniqueCode)
            }

            // FIX 2: Ganti .decodeAs() dengan .decodeSingle()
            // Karena fungsi RPC mengembalikan satu objek JSON
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
            e.printStackTrace()
            Result.Error(e.message ?: "Gagal membuat komunitas")
        }
    }

    override suspend fun joinCommunity(codeInput: String): Result<String> {
        return try {
            val rpcParams = buildJsonObject {
                put("input_code", codeInput)
            }

            // FIX 3: Ganti .decodeAs() dengan .decodeSingle()
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
            e.printStackTrace()
            Result.Error("Gagal bergabung: ${e.message}")
        }
    }
}