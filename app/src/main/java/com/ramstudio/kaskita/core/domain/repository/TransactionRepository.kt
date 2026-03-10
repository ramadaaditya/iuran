package com.ramstudio.kaskita.core.domain.repository

import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.model.Transaction
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
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
    ): Result<Transaction>

    suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String,
    ): Result<Transaction>


    suspend fun uploadTransactionProof(
        localUri: String,
        userId: String,
        communityId: String
    ): Result<String>


}