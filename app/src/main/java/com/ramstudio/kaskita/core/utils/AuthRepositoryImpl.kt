package com.ramstudio.kaskita.core.utils

import android.app.KeyguardManager
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ramstudio.kaskita.core.common.Result
import com.ramstudio.kaskita.core.domain.model.ProfileDto
import com.ramstudio.kaskita.core.domain.model.User
import com.ramstudio.kaskita.core.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthResponse {
    data object Success : AuthResponse
    data class Error(val message: String?) : AuthResponse
    data object Loading : AuthResponse
}


@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
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
            // 5. Tangkap semua error eksternal (Internet mati, Supabase down, Timeout)
            // TODO: Sangat disarankan untuk mencetak log di sini (misal: Timber.e(e))
            // Agar aplikasi tidak crash, kita anggap saja gagal mengambil user (kembalikan null)
            null
        }
    }


    override suspend fun signUp(
        email: String,
        password: String,
        fullName: String
    ): Result<Unit> {
        return try {
            if (!email.matches(EMAIL_PATTERN.toRegex())) {
                return Result.Error(IllegalArgumentException("Format email tidak valid"))
            }

            if (password.length < 6) {
                return Result.Error(IllegalArgumentException("Password minimal 6 karakter"))
            }

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

    fun signInCredentialManager(context: Context): Flow<AuthResponse> = flow {
        emit(AuthResponse.Loading)

        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isDeviceSecure = keyguardManager.isDeviceSecure

        Log.d(TAG, "Status Device Secure (Ada PIN/Pola): $isDeviceSecure")

        val rawNonce = generateRawNonce()
        val hashedNonce = createNonce(rawNonce)

        val requestBuilder = GetCredentialRequest.Builder()

        if (isDeviceSecure) {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId("572020927724-66sr21ddm9qihihjs6vb1blb2ueddonv.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false)
                .build()
            requestBuilder.addCredentialOption(googleIdOption)
        } else {
            val signInWithGoogleOption: GetSignInWithGoogleOption =
                GetSignInWithGoogleOption.Builder("572020927724-66sr21ddm9qihihjs6vb1blb2ueddonv.apps.googleusercontent.com")
                    .setNonce(hashedNonce)
                    .build()

            requestBuilder.addCredentialOption(signInWithGoogleOption)
        }

        val request = requestBuilder.build()
        val credentialManager = CredentialManager.create(context)
        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                Log.d(TAG, "Token berhasil didapat : ${googleIdToken.take(10)}...")

                supabase.auth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = rawNonce
                }

                Log.d(TAG, "Login Supabase berhasil")
                emit(AuthResponse.Success)
            } else {
                Log.d(TAG, "Tipe kredensial tidak dikenali : ${credential.type}")
                emit(AuthResponse.Error("Tipe login tidak valid"))
            }

        } catch (e: GetCredentialException) {
            val msg = e.message.orEmpty()
            Log.e(TAG, "Gagal mendapatkan credential: $msg")

            val errorMessage = when {
                msg.contains("16") -> "Terlalu banyak percobaan/dibatalkan. Coba clear cache Play Services."
                msg.contains("cancelled", ignoreCase = true) -> "Login dibatalkan"
                msg.contains("network", ignoreCase = true) -> "Periksa koneksi internet Anda"
                else -> AppErrorMapper.fromThrowable(
                    throwable = e,
                    fallback = "Gagal login dengan Google. Silakan coba lagi."
                )
            }
            emit(AuthResponse.Error(errorMessage))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get credentials ${e.localizedMessage}")
            emit(
                AuthResponse.Error(
                    AppErrorMapper.fromThrowable(
                        throwable = e,
                        fallback = "Gagal login dengan Google. Silakan coba lagi."
                    )
                )
            )
        }
    }
}
