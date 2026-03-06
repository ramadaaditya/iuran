package com.ramstudio.kaskita.data.datasource.local

import com.ramstudio.kaskita.data.local.dao.TransactionDao
import com.ramstudio.kaskita.data.local.toDomain
import com.ramstudio.kaskita.data.local.toEntity
import com.ramstudio.kaskita.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLocalDataSource @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun observeAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.observeAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeTransactionsByCommunity(communityId: String): Flow<List<Transaction>> {
        return transactionDao.observeTransactionsByCommunity(communityId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTransactionById(transactionId: String): Transaction? {
        return transactionDao.getTransactionById(transactionId)?.toDomain()
    }

    suspend fun replaceTransactionsByCommunity(communityId: String, transactions: List<Transaction>) {
        val now = System.currentTimeMillis()
        transactionDao.clearTransactionsByCommunity(communityId)
        transactionDao.upsertTransactions(transactions.map { it.toEntity(now) })
    }

    suspend fun upsertTransactions(transactions: List<Transaction>) {
        val now = System.currentTimeMillis()
        transactionDao.upsertTransactions(transactions.map { it.toEntity(now) })
    }

    suspend fun upsertTransaction(transaction: Transaction) {
        transactionDao.upsertTransaction(transaction.toEntity(System.currentTimeMillis()))
    }
}
