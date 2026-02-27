package com.ramstudio.kaskita.presentation.settings.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramstudio.kaskita.domain.model.User
import com.ramstudio.kaskita.presentation.settings.BackgroundLightGrey
import com.ramstudio.kaskita.presentation.settings.PrimaryBlue
import com.ramstudio.kaskita.presentation.settings.TextMainBlack
import com.ramstudio.kaskita.presentation.settings.TextSubGrey

@Composable
fun ProfileCard(
    user: User?,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundLightGrey)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.initial ?: "NA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMainBlack
                )
                Text(
                    text = user?.email ?: "email not found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubGrey
                )
            }

            // Edit Button
            TextButton(
                onClick = onEditClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("EDIT", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}