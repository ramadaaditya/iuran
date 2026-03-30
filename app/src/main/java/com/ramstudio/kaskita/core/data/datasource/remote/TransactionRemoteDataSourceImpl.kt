package com.ramstudio.kaskita.core.data.datasource.remote

import androidx.core.net.toUri
import android.util.Log
import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.model.Transaction
import com.ramstudio.kaskita.core.domain.model.TransactionDto
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.model.toDomain
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Singleton
class TransactionRemoteDataSourceImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val supabaseClient: SupabaseClient
) : TransactionRemoteDataSource {
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
        } catch (_: Exception) {
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

    override suspend fun fetchAllTransactions(): Result<List<Transaction>> {
        return try {
            val data = postgrest
                .from("transactions")
                .select()
                .decodeList<TransactionDto>()
            Result.Success(data.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e)
        }
    }


    override suspend fun submitTransaction(
        communityId: String,
        userId: String,
        type: String,
        amount: Long,
        description: String,
        proofUrl: String?
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

            Result.Success(result.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String,
        rejectionReason: String?
    ): Result<Transaction> {
        return try {
            when (newStatus) {
                TransactionStatus.PENDING -> throw Exception("Tidak bisa set status ke PENDING")
                TransactionStatus.REJECTED -> {
                    postgrest.rpc("reject_transaction", buildJsonObject {
                        put("p_transaction_id", transactionId)
                        put("p_approved_by", approvedBy)
                    })
                    if (!rejectionReason.isNullOrBlank()) {
                        postgrest
                            .from("transactions")
                            .update(
                                buildJsonObject { put("rejection_reason", rejectionReason) }
                            ) {
                                filter { eq("id", transactionId) }
                            }
                    }
                }

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

            Result.Success(updated.toDomain())
        } catch (e: Exception) {
            Log.e("TransactionRepo", "submitTransaction failed: ${e.message}", e) // ← tambah ini
            Result.Error(e)
        }
    }

    override suspend fun resubmitTransaction(
        transactionId: String,
        type: String,
        amount: Long,
        description: String,
        proofUrl: String?
    ): Result<Transaction> {
        return try {
            val body = buildJsonObject {
                put("type", type)
                put("amount", amount)
                put("description", description)
                put("status", "PENDING")
                put("rejection_reason", JsonNull)
                put("approved_by", JsonNull)
                put("approved_at", JsonNull)
                if (!proofUrl.isNullOrBlank()) put("proof_url", proofUrl)
            }

            // Perform the update
            postgrest
                .from("transactions")
                .update(body) {
                    filter { eq("id", transactionId) }
                }

            // Fetch the updated row separately — avoids RLS issues with
            // "return=representation" where UPDATE is allowed but the updated
            // row is not visible to the SELECT embedded inside the update response.
            val updated = postgrest
                .from("transactions")
                .select {
                    filter { eq("id", transactionId) }
                    limit(1)
                }
                .decodeSingle<TransactionDto>()

            Result.Success(updated.toDomain())
        } catch (e: Exception) {
            Log.e("TransactionRepo", "resubmitTransaction failed: ${e.message}", e)
            Result.Error(e)
        }
    }

    override suspend fun uploadTransactionProof(
        localUri: String,
        userId: String,
        communityId: String
    ): Result<String> {
        return try {
            val uri = localUri.toUri()
            val extension = uri.lastPathSegment
                ?.substringAfterLast('.', "jpg")
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: "jpg"
            val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
            val objectPath = "$communityId/$userId/$fileName"

            val bucket = supabaseClient.storage.from(PROOF_BUCKET)
            bucket.upload(path = objectPath, uri = uri)

            // ✅ Ganti publicUrl → createSignedUrl
            // Signed URL menyertakan token auth baked-in, tidak butuh session aktif saat diakses
            val signedUrl = bucket.createSignedUrl(
                path = objectPath,
                expiresIn = 365.days
            )

            Result.Success(signedUrl)
        } catch (e: Exception) {
            Timber.tag("TransactionRepo").e(e, "uploadTransactionProof failed: ${e.message}")
            Result.Error(e)
        }
    }

    private companion object {
        private const val PROOF_BUCKET = "transaction-proofs"
    }

}
