package com.ramstudio.kaskita.data.datasource.remote

import com.ramstudio.kaskita.data.repository.RemoteTransactionRepository
import com.ramstudio.kaskita.domain.model.Transaction
import com.ramstudio.kaskita.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRemoteDataSourceImpl @Inject constructor(
    private val remoteRepository: RemoteTransactionRepository
) : TransactionRemoteDataSource {
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return remoteRepository.getAllTransactions()
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return remoteRepository.getTransactionById(id)
    }

    override fun getTransactionsByCommunity(communityId: String): Flow<List<Transaction>> {
        return remoteRepository.getTransactionsByCommunity(communityId)
    }

    override suspend fun submitTransaction(
        communityId: String,
        userId: String,
        type: String,
        amount: Long,
        description: String,
        proofUrl: String?
    ): Result<Transaction> {
        return remoteRepository.submitTransaction(
            communityId = communityId,
            userId = userId,
            type = type,
            amount = amount,
            description = description,
            proofUrl = proofUrl,
        )
    }

    override suspend fun uploadTransactionProof(
        localUri: String,
        userId: String,
        communityId: String
    ): Result<String> {
        return remoteRepository.uploadTransactionProof(
            localUri = localUri,
            userId = userId,
            communityId = communityId
        )
    }

    override suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String
    ): Result<Transaction> {
        return remoteRepository.updateTransaction(
            transactionId = transactionId,
            newStatus = newStatus,
            approvedBy = approvedBy,
        )
    }
}
