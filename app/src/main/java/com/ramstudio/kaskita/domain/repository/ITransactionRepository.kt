package com.ramstudio.kaskita.domain.repository

import com.ramstudio.kaskita.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface ITransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
}