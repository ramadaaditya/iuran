package com.ramstudio.kaskita.data.repository

import android.net.Uri
import android.util.Log
import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.TransactionDto
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.toDomain
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.time.Duration.Companion.days
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteTransactionRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val supabaseClient: SupabaseClient,
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
                if (!proofUrl.isNullOrBlank()) put("proof_url", proofUrl)
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

    override suspend fun uploadTransactionProof(
        localUri: String,
        userId: String,
        communityId: String
    ): Result<String> {
        return try {
            val uri = Uri.parse(localUri)
            val extension = uri.lastPathSegment
                ?.substringAfterLast('.', "jpg")
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: "jpg"
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
            val objectPath = "$communityId/$userId/$fileName"

            val bucket = supabaseClient.storage.from(PROOF_BUCKET)
            bucket.upload(path = objectPath, uri = uri)

            val signedOrPublicUrl = runCatching {
                bucket.createSignedUrl(path = objectPath, expiresIn = 365.days)
            }.getOrElse {
                bucket.publicUrl(objectPath)
            }

            Result.success(signedOrPublicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String
    ): Result<Transaction> {
        return try {
            when (newStatus) {
                TransactionStatus.PENDING -> throw Exception("Tidak bisa set status ke PENDING")
                TransactionStatus.REJECTED ->
                    postgrest.rpc("reject_transaction", buildJsonObject {
                        put("p_transaction_id", transactionId)
                        put("p_approved_by", approvedBy)
                    })

                TransactionStatus.SUCCESS -> postgrest.rpc("approve_transaction", buildJsonObject {
                    put("p_transaction_id", transactionId)
                    put("p_approved_by", approvedBy)
                }
                )
            }


            val updated = postgrest
                .from("transactions")
                .select {
                    filter { eq("id", transactionId) }
                    limit(1)
                }
                .decodeSingle<TransactionDto>()

            Result.success(updated.toDomain())
        } catch (e: Exception) {
            Log.e("TransactionRepo", "submitTransaction failed: ${e.message}", e) // ← tambah ini
            Result.failure(e)
        }
    }

    private companion object {
        private const val PROOF_BUCKET = "transaction-proofs"
    }

}
