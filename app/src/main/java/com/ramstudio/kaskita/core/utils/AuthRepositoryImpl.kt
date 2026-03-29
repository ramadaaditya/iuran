package com.ramstudio.kaskita.core.utils

import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.model.ProfileDto
import com.ramstudio.kaskita.core.domain.model.User
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import timber.log.Timber
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthResponse {
    data object Success : AuthResponse
    data class Error(val message: String?) : AuthResponse
}


@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    companion object {
        private const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    }

    override val sessionStatus: Flow<SessionStatus>
        get() = supabase.auth.sessionStatus

    override suspend fun logout() {
        supabase.auth.signOut()
    }

    override suspend fun deleteAccount() {
        supabase.postgrest.rpc(
            function = "delete_my_account",
            parameters = buildJsonObject { }
        )
        supabase.auth.signOut()
    }

    override suspend fun getUser(): User? {
        return try {
            val authUser = supabase.auth.currentUserOrNull() ?: return null

            val profile = supabase.from("profiles")
                .select {
                    filter {
                        eq("id", authUser.id)
                    }
                }.decodeSingleOrNull<ProfileDto>()

            if (profile == null) {
                return null
            }
            User(
                id = profile.id,
                name = profile.fullName ?: "No Name",
                role = "",
                initial = AvatarUtils.getInitials(profile.fullName),
                email = authUser.email,
            )
        } catch (e: Exception) {
            Timber.e(e, "AuthRepositoryImpl: getUser failed")
            null
        }
    }


    override suspend fun signUp(
        email: String,
        password: String,
        fullName: String
    ): Result<Unit> {
        return try {
            require(email.isNotBlank()){"Email Kosong"}
            require(password.isNotBlank()){"Password Kosong"}

            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun createNonce(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

    fun generateRawNonce(): String =
        UUID.randomUUID().toString()

}
