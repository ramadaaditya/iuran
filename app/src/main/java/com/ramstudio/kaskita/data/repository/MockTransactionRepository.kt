//package com.ramstudio.kaskita.data.repository
//
//import com.ramstudio.kaskita.data.DummyData
//import com.ramstudio.kaskita.domain.model.Transaction
//import com.ramstudio.kaskita.domain.repository.ITransactionRepository
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class MockTransactionRepository @Inject constructor() : ITransactionRepository {
//    private val _transactions = MutableStateFlow(
//        DummyData.transactions
//    )
//
//    override fun getAllTransactions(): Flow<List<Transaction>> = _transactions
//
//    override suspend fun getTransactionById(id: String): Transaction? {
//        return _transactions.value.firstOrNull { it.id == id }
//    }
//}