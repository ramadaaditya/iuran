package com.ramstudio.kaskita.domain.model

data class DashboardUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)