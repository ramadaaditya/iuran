package com.ramstudio.kaskita.presentation.settings.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.presentation.settings.DangerRed
import com.ramstudio.kaskita.presentation.settings.TextMainBlack

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.dialog_logout_title)) },
        text = { Text(text = stringResource(R.string.dialog_logout_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.settings_logout_title),
                    color = DangerRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel), color = TextMainBlack)
            }
        },
        containerColor = Color.White
    )
}
