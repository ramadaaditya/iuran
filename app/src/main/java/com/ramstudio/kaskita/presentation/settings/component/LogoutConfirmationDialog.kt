package com.ramstudio.kaskita.presentation.settings.component


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.ramstudio.kaskita.presentation.settings.DangerRed
import com.ramstudio.kaskita.presentation.settings.TextMainBlack

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Log Out") },
        text = { Text(text = "Are you sure you want to log out from this account?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Log Out", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMainBlack)
            }
        },
        containerColor = Color.White
    )
}