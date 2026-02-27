//package com.ramstudio.kaskita.data.repository
//
//import com.ramstudio.kaskita.data.DummyData
//import com.ramstudio.kaskita.domain.model.Community
//import com.ramstudio.kaskita.domain.model.Result
//import com.ramstudio.kaskita.domain.model.Transaction
//import com.ramstudio.kaskita.domain.repository.ICommunityRepository
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import javax.inject.Inject
//import javax.inject.Singleton
//import kotlin.random.Random
//
//@Singleton
//class MockCommunityRepository @Inject constructor() : ICommunityRepository {
//
//    private val _transactions = MutableStateFlow<List<Transaction>>(
//        DummyData.transactions
//    )
//
//    private val _communities = MutableStateFlow<List<Community>>(
//        DummyData.communities
//    )
//
//
//    override suspend fun createCommunity(
//        name: String,
//        desc: String
//    ): Result<String> {
//
//        delay(800)
//
//        val uniqueCode = (1..6)
//            .map { ('A'..'Z').random() }
//            .joinToString("")
//
//        val newCommunity = Community(
//            id = Random.nextInt(100, 999).toString(),
//            name = name,
//            description = desc,
//            code = uniqueCode,
//            balance = 20000.0
//        )
//
//        _communities.value += newCommunity
//
//        return Result.Success("Komunitas $name berhasil dibuat! Kode: $uniqueCode")
//    }
//
//    override suspend fun joinCommunity(codeInput: String): Result<String> {
//        delay(600)
//        val community = _communities.value.find {
//            it.code.equals(codeInput, ignoreCase = true)
//        }
//
//        return if (community != null) {
//            Result.Success("Berhasil bergabung ke ${community.name}")
//        } else {
//            Result.Error("Kode tidak valid")
//        }
//    }
//
//    override fun getAllCommunity(): Flow<List<Community>> = _communities
//    override fun getAllTransactions(): Flow<List<Transaction>> = _transactions
//}