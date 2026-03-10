package com.ramstudio.kaskita.core.data.repository.offline

import android.util.Log
import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.data.datasource.local.TransactionLocalDataSource
import com.ramstudio.kaskita.core.data.datasource.remote.TransactionRemoteDataSource
import com.ramstudio.kaskita.core.data.sync.SyncPolicyStore
import com.ramstudio.kaskita.core.domain.model.Transaction
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstTransactionRepository @Inject constructor(
    private val localDataSource: TransactionLocalDataSource,
    private val remoteDataSource: TransactionRemoteDataSource,
    private val syncPolicyStore: SyncPolicyStore,
) : TransactionRepository {

    private val refreshMutex = Mutex()

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return localDataSource.observeAllTransactions()
            .onStart {
                refreshAllTransactions(force = false)
            }
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        localDataSource.getTransactionById(id)?.let { return it }

        val remote = runCatching { remoteDataSource.getTransactionById(id) }.getOrNull()
        if (remote != null) {
            localDataSource.upsertTransaction(remote)
            syncPolicyStore.markSynced(syncKeyTransactionById(id))
        }

        return localDataSource.getTransactionById(id) ?: remote
    }

    override fun getTransactionsByCommunity(communityId: String): Flow<List<Transaction>> {
        return localDataSource.observeTransactionsByCommunity(communityId)
            .onStart {
                refreshTransactionsByCommunity(communityId, force = false)
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
        val result = remoteDataSource.submitTransaction(
            communityId = communityId,
            userId = userId,
            type = type,
            amount = amount,
            description = description,
            proofUrl = proofUrl,
        )

        when (result) {
            is Result.Error -> {}
            is Result.Success -> {
                localDataSource.upsertTransaction(result.data)
                syncPolicyStore.markSynced(syncKeyCommunityTransactions(result.data.communityId))

            }
        }
        return result
    }

    override suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String
    ): Result<Transaction> {
        val result = remoteDataSource.updateTransaction(
            transactionId = transactionId,
            newStatus = newStatus,
            approvedBy = approvedBy,
        )

        when (result) {
            is Result.Error -> {

            }

            is Result.Success -> {
                localDataSource.upsertTransaction(result.data)
                syncPolicyStore.markSynced(syncKeyCommunityTransactions(result.data.communityId))
            }
        }

        return result
    }

    private suspend fun refreshAllTransactions(force: Boolean) {
        refreshMutex.withLock {
            val shouldSync = force || syncPolicyStore.shouldSync(
                SYNC_KEY_ALL_TRANSACTIONS,
                ALL_TX_SYNC_MAX_AGE
            )
            if (!shouldSync) return

            // ✅ DataSource return custom Result, handle eksplisit
            when (val result = remoteDataSource.fetchAllTransactions()) {
                is Result.Success -> {
                    localDataSource.upsertTransactions(result.data)
                    syncPolicyStore.markSynced(SYNC_KEY_ALL_TRANSACTIONS)
                }

                is Result.Error -> {
                    // Pilihan: log, rethrow, atau biarkan — tapi harus disadari
                    Log.e("TransactionRepo", "Refresh failed", result.throwable)
                    // Jangan markSynced kalau gagal
                }
            }
        }
    }

    private suspend fun refreshTransactionsByCommunity(communityId: String, force: Boolean) {
        refreshMutex.withLock {
            val key = syncKeyCommunityTransactions(communityId)
            val shouldSync = force || syncPolicyStore.shouldSync(key, COMMUNITY_TX_SYNC_MAX_AGE)
            if (!shouldSync) return

            runCatching {
                val remote = remoteDataSource.getTransactionsByCommunity(communityId).first()
                localDataSource.replaceTransactionsByCommunity(communityId, remote)
                syncPolicyStore.markSynced(key)
            }
        }
    }

    private fun syncKeyCommunityTransactions(communityId: String) =
        "sync_transactions_community_$communityId"

    private fun syncKeyTransactionById(transactionId: String) =
        "sync_transaction_by_id_$transactionId"

    companion object {
        private const val SYNC_KEY_ALL_TRANSACTIONS = "sync_all_transactions"
        private const val ALL_TX_SYNC_MAX_AGE = 5 * 60 * 1000L
        private const val COMMUNITY_TX_SYNC_MAX_AGE = 60 * 1000L
    }
}
