package com.ramstudio.kaskita.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramstudio.kaskita.presentation.auth.register.PrimaryGreen
import com.ramstudio.kaskita.presentation.auth.register.TextDark
import com.ramstudio.kaskita.presentation.auth.register.TextGrey

@Composable
fun KasKitaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            color = TextDark,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            supportingText = {
                errorMessage?.let {
                    Text(text = it, color = Color.Red)
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextGrey.copy(alpha = 0.7f)
                )
            },
            shape = RoundedCornerShape(16.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                cursorColor = PrimaryGreen
            ),

            modifier = modifier.fillMaxWidth()
        )
    }
}