package com.ramstudio.kaskita.core.utils

object Validator {
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email Tidak boleh kosong"
            !isValidEmail(email) -> "Email Tidak Valid"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password tidak boleh kosong"

            password.length < 8 ->
                "Password minimal 8 karakter"

            !password.any { it.isLetter() } ->
                "Password harus mengandung huruf"

            !password.any { it.isDigit() } ->
                "Password harus mengandung angka"

            else -> null
        }
    }

    fun validateFullName(fullName: String): String? {
        return when {
            fullName.isBlank() -> "Nama Tidak boleh kosong"
            else -> null
        }
    }
}