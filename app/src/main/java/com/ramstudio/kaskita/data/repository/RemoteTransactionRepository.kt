package com.ramstudio.kaskita.data.repository

import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.TransactionDto
import com.ramstudio.kaskita.domain.model.toDomain
import com.ramstudio.kaskita.domain.repository.ITransactionRepository
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

}