package com.ramstudio.kaskita.domain.repository

import com.ramstudio.kaskita.domain.model.Result
import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

interface ITransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?

    fun getTransactionsByCommunity(communityId: String): Flow<List<Transaction>>

    suspend fun submitTransaction(
        communityId: String,
        userId: String,
        type: String,
        amount: Long,
        description: String,
        proofUrl: String?
    ): kotlin.Result<Transaction>

    suspend fun uploadTransactionProof(
        localUri: String,
        userId: String,
        communityId: String
    ): kotlin.Result<String>

    suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String,
    ): kotlin.Result<Transaction>


}
