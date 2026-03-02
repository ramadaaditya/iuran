package com.ramstudio.kaskita.data.repository

import android.util.Log
import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.TransactionDto
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.toDomain
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteTransactionRepository @Inject constructor(
    private val postgrest: Postgrest
) : ITransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> = flow {
        val response = postgrest
            .from("transactions")
            .select()
            .decodeList<TransactionDto>()
        emit(response.map { it.toDomain() })

    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return try {
            val result = postgrest
                .from("transactions")
                .select {
                    filter {
                        eq("id", id)
                    }
                    limit(1)
                }
                .decodeSingle<TransactionDto>()
            result.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun getTransactionsByCommunity(communityId: String): Flow<List<Transaction>> = flow {
        val response = postgrest
            .from("transactions")
            .select {
                filter { eq("community_id", communityId) }
                order("created_at", order = Order.DESCENDING)
            }.decodeList<TransactionDto>()
        emit(response.map { it.toDomain() })
    }

    override suspend fun submitTransaction(
        communityId: String,
        userId: String,
        type: String,
        amount: Long,
        description: String,
        proofUrl: String?,
    ): Result<Transaction> {
        return try {
            val body = buildJsonObject {
                put("community_id", communityId)
                put("user_id", userId)
                put("type", type)
                put("amount", amount)
                put("description", description)
                put("status", "PENDING")
                put("proof_url", proofUrl ?: "https://placeholder.com/proof.jpg")
            }

            val result = postgrest
                .from("transactions")
                .insert(body) {
                    select()
                }.decodeSingle<TransactionDto>()

            Result.success(result.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /*
    -- ═══════════════════════════════════════════════════════════════════════════
-- Cara panggil dari RemoteTransactionRepository.kt setelah RPC dibuat:
--
-- Ganti implementasi updateTransactionStatus dengan:
--
-- postgrest.rpc("approve_transaction", buildJsonObject {
--     put("p_transaction_id", transactionId)
--     put("p_approved_by", approvedBy)
-- })
--
-- postgrest.rpc("reject_transaction", buildJsonObject {
--     put("p_transaction_id", transactionId)
--     put("p_approved_by", approvedBy)
-- })
-- ═══════════════════════════════════════════════════════════════════════════
     */
    override suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String
    ): Result<Transaction> {
        return try {
            val statusString = when (newStatus) {
                TransactionStatus.PENDING -> throw Exception("Tidak bisa set status ke PENDING")
                TransactionStatus.REJECTED ->
                    postgrest.rpc("reject_transaction", buildJsonObject {
                        put("p_transaction_id", transactionId)
                        put("p_approved_by", approvedBy)
                    })

                TransactionStatus.SUCCESS -> postgrest.rpc("approve_transaction", buildJsonObject {
                    put("p_transaction_id", transactionId)
                    put("p_approved_by", approvedBy)
                })
            }


            val updated = postgrest
                .from("transactions")
                .select {
                    filter { eq("id", transactionId) }
                }
                .decodeSingle<TransactionDto>()

            Result.success(updated.toDomain())
        } catch (e: Exception) {
            Log.e("TransactionRepo", "submitTransaction failed: ${e.message}", e) // ← tambah ini
            Result.failure(e)
        }
    }

}