package com.ramstudio.kaskita.core.domain.repository

import com.ramstudio.kaskita.core.domain.model.User
import com.ramstudio.kaskita.core.common.Result
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>
    suspend fun logout()
    suspend fun deleteAccount()
    suspend fun getUser(): User?
    suspend fun signUp(email: String, password: String, fullName: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

}
