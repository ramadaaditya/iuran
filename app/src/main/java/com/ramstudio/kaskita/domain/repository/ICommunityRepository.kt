package com.ramstudio.kaskita.domain.repository

import com.ramstudio.kaskita.domain.model.Result

interface ICommunityRepository {
    suspend fun createCommunity(name: String, desc: String): Result<String>
    suspend fun joinCommunity(codeInput: String): Result<String>
}