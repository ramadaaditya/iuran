//package com.ramstudio.kaskita.presentation.dashboard.component
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.rounded.MoreHoriz
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.ramstudio.kaskita.core.utils.formatCurrency
//import com.ramstudio.kaskita.domain.model.Community
//import com.ramstudio.kaskita.presentation.community.TextDark
//import com.ramstudio.kaskita.presentation.community.TextGrey
//import com.ramstudio.kaskita.presentation.community.getIconForCommunity
//
//@Composable
//fun CreateGroupCard(onClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(140.dp)
//            .clip(RoundedCornerShape(24.dp))
//            .clickable(onClick = onClick)
//            .border(2.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
//            .background(Color.Transparent),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Box(
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(CircleShape)
//                    .border(1.dp, Color.LightGray, CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(Icons.Default.Add, contentDescription = null, tint = TextGrey)
//            }
//            Spacer(modifier = Modifier.height(12.dp))
//            Text(
//                text = "Create Group",
//                style = MaterialTheme.typography.bodyMedium,
//                fontWeight = FontWeight.Medium,
//                color = TextGrey
//            )
//        }
//    }
//}
//
//@Composable
//fun CommunityCard(community: Community, isAdmin: Boolean, onDetailClick: () -> Unit) {
//    Card(
//        shape = RoundedCornerShape(24.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
//        modifier = Modifier.fillMaxWidth(),
//        onClick = onDetailClick
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(36.dp)
//                        .clip(CircleShape)
//                        .background(community.themeColor.copy(alpha = 0.15f)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = getIconForCommunity(community.name),
//                        contentDescription = null,
//                        tint = community.themeColor
//                    )
//                }
//                Icon(
//                    imageVector = Icons.Rounded.MoreHoriz,
//                    contentDescription = "Options",
//                    tint = Color.LightGray
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = community.name,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold,
//                color = TextDark
//            )
//            Text(
//                text = "${community.membersCount} Members ${if (isAdmin) "(Admin)" else ""}",
//                style = MaterialTheme.typography.bodySmall,
//                color = TextGrey
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "BALANCE",
//                style = MaterialTheme.typography.labelSmall,
//                color = TextGrey,
//                fontSize = 10.sp
//            )
//            Text(
//                text = formatCurrency(community.balance),
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.ExtraBold,
//                color = community.themeColor
//            )
//        }
//    }
//}
//
//
