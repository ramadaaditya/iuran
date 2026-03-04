package com.ramstudio.kaskita.presentation.settings.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramstudio.kaskita.presentation.settings.DangerRed
import com.ramstudio.kaskita.presentation.settings.TextMainBlack

@Composable
fun DeleteAccountConfirmationDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete account") },
        text = {
            Text(
                text = "This action is permanent and cannot be undone. " +
                    "Your account data will be removed from the app."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier,
                        strokeWidth = 2.dp,
                        color = DangerRed
                    )
                } else {
                    Text("Delete", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", color = TextMainBlack)
            }
        },
        containerColor = Color.White
    )
}
