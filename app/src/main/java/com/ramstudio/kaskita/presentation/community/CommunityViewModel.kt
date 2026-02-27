package com.ramstudio.kaskita.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.Result
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val communities: List<Community> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: ICommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeCommunities()
    }

    private fun observeCommunities() {
        viewModelScope.launch {
            repository.getAllCommunity().collect { communities ->
                _uiState.update {
                    it.copy(communities = communities)
                }
            }
        }
    }

    fun createCommunity(name: String, desc: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama komunitas tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            when (val result = repository.createCommunity(name, desc)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = result.data)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Terjadi kesalahan misterius"
                        )
                    }
                }

                is Result.Loading -> {}
            }
        }
    }

    fun joinCommunity(code: String) {
        if (code.length < 5) { // Validasi sederhana
            _uiState.update { it.copy(errorMessage = "Kode komunitas tidak valid") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            when (val result = repository.joinCommunity(code)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = result.data)
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Gagal bergabung"
                        )
                    }
                }

                is Result.Loading -> {}
            }
        }
    }

    // Fungsi untuk mereset pesan error/sukses setelah ditampilkan (misal oleh Snackbar)
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}