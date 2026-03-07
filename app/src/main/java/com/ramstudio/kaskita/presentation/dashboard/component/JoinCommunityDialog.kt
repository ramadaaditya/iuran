package com.ramstudio.kaskita.presentation.dashboard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.presentation.auth.register.PrimaryGreen
import com.ramstudio.kaskita.presentation.auth.register.TextDark
import com.ramstudio.kaskita.presentation.auth.register.TextGrey

@Composable
fun JoinCommunityDialog(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onJoin: (inviteCode: String) -> Unit
) {
    // Menyimpan state input dari user
    var inviteCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            // Cegah dismiss jika sedang proses loading
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = stringResource(R.string.join_dialog_title),
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.join_dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text(stringResource(R.string.join_dialog_invite_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen,
                        cursorColor = PrimaryGreen,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark
                    )
                )

                // Menampilkan error message jika gagal validasi dari API/ViewModel
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onJoin(inviteCode) },
                // Disable button jika input kosong atau sedang loading
                enabled = inviteCode.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.common_join), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.common_cancel), color = TextGrey, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = White,
        shape = RoundedCornerShape(24.dp) // Radius yang konsisten dengan desain Anda
    )
}
