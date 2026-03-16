package com.ramstudio.kaskita.core.data.repository

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
class TransactionRepositoryImpl @Inject constructor(
    private val localDataSource: TransactionLocalDataSource,
    private val remoteDataSource: TransactionRemoteDataSource,
    private val syncPolicyStore: SyncPolicyStore,
) : TransactionRepository {

    private val refreshMutex = Mutex()

    // ─── Read (Offline-First / Cached) ────────────────────────────────────────

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return localDataSource.observeAllTransactions()
            .onStart { refreshAllTransactions(force = false) }
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
            .onStart { refreshTransactionsByCommunity(communityId, force = false) }
    }

    // ─── Write (Remote + update cache) ────────────────────────────────────────

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

        if (result is Result.Success) {
            localDataSource.upsertTransaction(result.data)
            syncPolicyStore.markSynced(syncKeyCommunityTransactions(result.data.communityId))
        }

        return result
    }

    override suspend fun updateTransaction(
        transactionId: String,
        newStatus: TransactionStatus,
        approvedBy: String,
        rejectionReason: String?
    ): Result<Transaction> {
        val result = remoteDataSource.updateTransaction(
            transactionId = transactionId,
            newStatus = newStatus,
            approvedBy = approvedBy,
            rejectionReason = rejectionReason,
        )

        if (result is Result.Success) {
            localDataSource.upsertTransaction(result.data)
            syncPolicyStore.markSynced(syncKeyCommunityTransactions(result.data.communityId))
        }

        return result
    }

    override suspend fun resubmitTransaction(
        transactionId: String,
        type: String,
        amount: Long,
        description: String,
        proofUrl: String?
    ): Result<Transaction> {
        val result = remoteDataSource.resubmitTransaction(
            transactionId = transactionId,
            type = type,
            amount = amount,
            description = description,
            proofUrl = proofUrl,
        )

        if (result is Result.Success) {
            localDataSource.upsertTransaction(result.data)
            syncPolicyStore.markSynced(syncKeyCommunityTransactions(result.data.communityId))
        }

        return result
    }

    // ─── Remote-only (tidak perlu cache) ──────────────────────────────────────

    override suspend fun uploadTransactionProof(
        localUri: String,
        userId: String,
        communityId: String
    ): Result<String> {
        return remoteDataSource.uploadTransactionProof(
            localUri = localUri,
            userId = userId,
            communityId = communityId,
        )
    }

    // ─── Sync helpers ─────────────────────────────────────────────────────────

    private suspend fun refreshAllTransactions(force: Boolean) {
        refreshMutex.withLock {
            val shouldSync = force || syncPolicyStore.shouldSync(
                SYNC_KEY_ALL_TRANSACTIONS,
                ALL_TX_SYNC_MAX_AGE
            )
            if (!shouldSync) return

            when (val result = remoteDataSource.fetchAllTransactions()) {
                is Result.Success -> {
                    localDataSource.upsertTransactions(result.data)
                    syncPolicyStore.markSynced(SYNC_KEY_ALL_TRANSACTIONS)
                }
                is Result.Error -> {
                    Log.e("TransactionRepository", "Refresh all transactions failed", result.throwable)
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
