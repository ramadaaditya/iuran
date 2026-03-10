package com.ramstudio.kaskita.core.data.repository

import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.data.datasource.remote.TransactionRemoteDataSource
import com.ramstudio.kaskita.core.domain.model.Transaction
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteTransactionRepository @Inject constructor(
    private val dataSource: TransactionRemoteDataSource
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dataSource.getAllTransactions()
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return dataSource.getTransactionById(id)
    }

    override fun getTransactionsByCommunity(communityId: String): Flow<List<Transaction>> {
        return dataSource.getTransactionsByCommunity(communityId)
    }

    override suspend fun submitTransaction(
        communityId: String,
        userId: String,
        type: String,
        amount: Long,
        description: String,
        proofUrl: String?,
    ): Result<Transaction> {
        return dataSource.submitTransaction(
            communityId = communityId,
            userId = userId,
            type = type,
            amount = amount,
            description = description,
            proofUrl = proofUrl,
        )
    }


    override suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String
    ): Result<Transaction> {
        return dataSource.updateTransaction(
            transactionId = transactionId,
            newStatus = newStatus,
            approvedBy = approvedBy,
        )
    }

    override suspend fun uploadTransactionProof(
        localUri: String,
        userId: String,
        communityId: String
    ): Result<String> {
        return dataSource.uploadTransactionProof(
            localUri = localUri,
            userId = userId,
            communityId = communityId
        )
    }
}