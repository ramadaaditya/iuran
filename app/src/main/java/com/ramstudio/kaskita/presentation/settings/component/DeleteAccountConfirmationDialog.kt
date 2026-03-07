package com.ramstudio.kaskita.presentation.settings.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramstudio.kaskita.R
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
        title = { Text(text = stringResource(R.string.dialog_delete_account_title)) },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_account_body)
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
                    Text(
                        stringResource(R.string.dialog_delete_account_confirm),
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(stringResource(R.string.common_cancel), color = TextMainBlack)
            }
        },
        containerColor = Color.White
    )
}
