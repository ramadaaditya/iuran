package com.ramstudio.kaskita.presentation.dashboard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramstudio.kaskita.R

@Composable
fun CreateCommunityDialog(
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    onSuccessHandled: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    if (successMessage != null) {
        LaunchedEffect(successMessage) {
            onSuccessHandled()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onCreate(name.trim(), desc.trim()) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Text(stringResource(R.string.common_creating))
                } else {
                    Text(stringResource(R.string.common_create))
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        title = {
            Text(stringResource(R.string.community_create_dialog_title))
        },
        text = {
            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.community_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.community_description_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}
