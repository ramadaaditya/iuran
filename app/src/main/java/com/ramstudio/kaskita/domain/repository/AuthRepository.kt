package com.ramstudio.kaskita.domain.repository

import com.ramstudio.kaskita.core.utils.Result
import com.ramstudio.kaskita.domain.model.User
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>
    suspend fun logout()
    suspend fun deleteAccount()
    suspend fun getUser(): User
    fun signUp(emailValue: String, passwordValue: String, fullName: String): Flow<Result<String>>
    fun signInWithEmail(emailValue: String, passwordValue: String): Flow<Result<String>>

}
