package com.ramstudio.kaskita.domain.model


sealed class Result<out T> {
    data object Loading : Result<Nothing>()
    data class Success<out T>(
        val data: T
    ) : Result<T>()

    data class Error<T>(
        val message: String?,
    ) : Result<T>()
}
