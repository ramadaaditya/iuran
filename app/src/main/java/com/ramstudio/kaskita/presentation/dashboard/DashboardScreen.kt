package com.ramstudio.kaskita.presentation.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramstudio.kaskita.presentation.CommunityViewModel

@Composable
fun DashboardScreen(
    viewModel: CommunityViewModel = hiltViewModel()
) {
    // State UI Lokal (Hanya untuk input form)
    var tabIndex by remember { mutableIntStateOf(0) }
    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }

    // State dari ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Efek Samping untuk menampilkan Toast (One-time event)
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            // Reset input jika sukses buat/gabung
            if (tabIndex == 0) {
                nameInput = ""; descInput = ""
            } else {
                codeInput = ""
            }
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // === TABS ===
        TabRow(selectedTabIndex = tabIndex) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Buat Baru") })
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Gabung") })
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === CONTENT ===
        if (tabIndex == 0) {
            CreateCommunityForm(
                name = nameInput,
                onNameChange = { nameInput = it },
                desc = descInput,
                onDescChange = { descInput = it },
                isLoading = uiState.isLoading,
                onSubmit = { viewModel.createCommunity(nameInput, descInput) }
            )
        } else {
            JoinCommunityForm(
                code = codeInput,
                onCodeChange = { codeInput = it.uppercase() },
                isLoading = uiState.isLoading,
                onSubmit = { viewModel.joinCommunity(codeInput) }
            )
        }
    }
}

// Extract Composable agar DashboardScreen tidak terlalu panjang (Best Practice)
@Composable
fun CreateCommunityForm(
    name: String, onNameChange: (String) -> Unit,
    desc: String, onDescChange: (String) -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit
) {
    Column {
        Text("Buat Komunitas", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Nama Komunitas") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = desc, onValueChange = onDescChange,
            label = { Text("Deskripsi Singkat") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && name.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Memproses...")
            } else {
                Text("Buat Sekarang")
            }
        }
    }
}

@Composable
fun JoinCommunityForm(
    code: String, onCodeChange: (String) -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit
) {
    Column {
        Text("Gabung Komunitas", style = MaterialTheme.typography.titleLarge)
        Text("Masukkan kode unik dari admin.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = code, onValueChange = onCodeChange,
            label = { Text("Kode Unik") },
            placeholder = { Text("Contoh: AXY78") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && code.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Gabung")
            }
        }
    }
}