package com.ramstudio.kaskita.core.utils

import android.app.KeyguardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

sealed interface AuthResponse {
    data object Success : AuthResponse
    data class Error(val message: String?) : AuthResponse
}

class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun authSession() {
        supabase.auth.sessionStatus.collect {
            when (it) {
                is SessionStatus.Authenticated -> {
                    println("Received new authenticated session.")

                    when (it.source) {
                        SessionSource.External -> TODO()
                        is SessionSource.Refresh -> TODO()
                        is SessionSource.SignIn -> TODO()
                        is SessionSource.SignUp -> TODO()
                        SessionSource.Storage -> TODO()
                        SessionSource.Unknown -> TODO()
                        is SessionSource.UserChanged -> TODO()
                        is SessionSource.UserIdentitiesChanged -> TODO()
                        SessionSource.AnonymousSignIn -> TODO()
                    }
                }

                SessionStatus.Initializing -> println("Initializing")
                is SessionStatus.RefreshFailure -> {
                    println("Session expired and could not be refreshed")
                }

                is SessionStatus.NotAuthenticated -> {
                    if (it.isSignOut) {
                        println("User signed out")
                    } else {
                        println("User not signed in")
                    }
                }
            }
        }
    }

    fun signUpWithEmail(emailValue: String, passwordValue: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            supabase.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }
            Log.e(TAG, "Berhasil signup")
            emit(Result.Success("Sign up successfully! Please check your email."))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "An unexpected error occurred"))
        }
    }

    fun signInWithEmail(emailValue: String, passwordValue: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            supabase.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(Result.Success("Sign in Successfully!"))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "An unexpected error occurred"))
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
            // Filter error spesifik
            val msg = e.message ?: "Credential Error"
            Log.e(TAG, "Gagal mendapatkan credential: $msg")

            // Opsional: Cek error code "16" di sini
            if (msg.contains("16")) {
                emit(AuthResponse.Error("Terlalu banyak percobaan/dibatalkan. Coba clear cache Play Services."))
            } else {
                emit(AuthResponse.Error(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get credentials ${e.localizedMessage}")
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }
}