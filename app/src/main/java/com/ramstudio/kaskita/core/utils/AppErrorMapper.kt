package com.ramstudio.kaskita.core.utils

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object AppErrorMapper {
    private const val DEFAULT_FALLBACK = "Terjadi kendala. Silakan coba lagi."

    fun fromThrowable(
        throwable: Throwable?,
        fallback: String = DEFAULT_FALLBACK
    ): String {
        if (throwable == null) return fallback

        return when (throwable) {
            is UnknownHostException,
            is SocketTimeoutException,
            is SSLException -> "Koneksi internet bermasalah. Periksa jaringan Anda."

            else -> fromRawMessage(throwable.message, fallback)
        }
    }

    fun fromRawMessage(
        rawMessage: String?,
        fallback: String = DEFAULT_FALLBACK
    ): String {
        val message = rawMessage?.trim().orEmpty()
        if (message.isBlank()) return fallback

        val lower = message.lowercase()
        return when {
            lower.contains("invalid login credentials") ||
                lower.contains("invalid email or password") ->
                "Email atau kata sandi tidak valid."

            lower.contains("email not confirmed") ->
                "Email belum diverifikasi. Periksa inbox Anda."

            lower.contains("already registered") ||
                lower.contains("already exists") ->
                "Akun sudah terdaftar. Silakan gunakan email lain."

            lower.contains("network") ||
                lower.contains("timeout") ||
                lower.contains("connection") ||
                lower.contains("host") ->
                "Koneksi internet bermasalah. Periksa jaringan Anda."

            lower.contains("unauthorized") ||
                lower.contains("forbidden") ||
                lower.contains("jwt") ||
                lower.contains("token") ||
                lower.contains("permission") ->
                "Sesi Anda tidak valid. Silakan login ulang."

            lower.contains("sql") ||
                lower.contains("rpc") ||
                lower.contains("http ") ||
                lower.contains("stacktrace") ||
                lower.contains("exception") ||
                lower.contains("postgrest") ||
                lower.contains("supabase") ->
                fallback

            else -> message
        }
    }
}
