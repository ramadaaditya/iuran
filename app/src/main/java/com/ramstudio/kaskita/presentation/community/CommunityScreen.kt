package com.ramstudio.kaskita.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.presentation.dashboard.component.CreateCommunityDialog
import com.ramstudio.kaskita.presentation.dashboard.component.JoinCommunityDialog

fun NavController.navigateToCommunity(navOptions: NavOptions? = null) =
    if (navOptions != null) navigate(route = ScreenRoute.Community, navOptions)
    else navigate(ScreenRoute.Community)

private val FinanceBlue = Color(0xFF1D4ED8)
private val FinanceBlueDeep = Color(0xFF0F2A6B)
private val FinanceBlueBright = Color(0xFF38BDF8)
private val FinanceBlueSurface = Color(0xFFEFF6FF)

@Composable
fun CommunityScreen(
    innerPadding: PaddingValues,
    viewModel: CommunityViewModel = hiltViewModel(),
    onDetailClick: (communityId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val currentUserId = uiState.currentUserId
    val managedCommunities = uiState.communities.filter { it.createdBy == currentUserId }
    val joinedCommunities = uiState.communities.filter { it.createdBy != currentUserId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CommunityContent(
            managedCommunities = managedCommunities,
            joinedCommunities = joinedCommunities,
            innerPadding = innerPadding,
            onJoinClick = { showJoinDialog = true },
            onCreateClick = { showCreateDialog = true },
            onDetailClick = onDetailClick
        )

        if (showCreateDialog) {
            CreateCommunityDialog(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                successMessage = uiState.successMessage,
                onDismiss = {
                    showCreateDialog = false
                    viewModel.clearMessages()
                },
                onCreate = { name, desc -> viewModel.createCommunity(name, desc) },
                onSuccessHandled = {
                    showCreateDialog = false
                    viewModel.clearMessages()
                }
            )
        }

        if (showJoinDialog) {
            JoinCommunityDialog(
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onDismiss = {
                    showJoinDialog = false
                    viewModel.clearMessages()
                },
                onJoin = { code -> viewModel.joinCommunity(code) }
            )
        }
    }
}

@Composable
private fun CommunityContent(
    managedCommunities: List<Community>,
    joinedCommunities: List<Community>,
    innerPadding: PaddingValues,
    onJoinClick: () -> Unit,
    onCreateClick: () -> Unit,
    onDetailClick: (communityId: String) -> Unit
) {
    val totalCommunities = managedCommunities.size + joinedCommunities.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroCard(
                totalCommunities = totalCommunities,
                onJoinClick = onJoinClick
            )
        }

        item {
            SectionTitle("Managed by You")
        }

        if (managedCommunities.isEmpty()) {
            item {
                EmptySectionCard("You have not created a community yet.")
            }
        } else {
            items(managedCommunities, key = { it.id ?: it.code }) { community ->
                CommunityCard(
                    community = community,
                    role = "Admin",
                    roleColor = FinanceBlue,
                    onClick = { community.id?.let(onDetailClick) }
                )
            }
        }

        item {
            CreateCommunityCard(onClick = onCreateClick)
        }

        item {
            SectionTitle("Joined Communities")
        }

        if (joinedCommunities.isEmpty()) {
            item {
                EmptySectionCard("Join a community using invite code.")
            }
        } else {
            items(joinedCommunities, key = { it.id ?: it.code }) { community ->
                CommunityCard(
                    community = community,
                    role = "Member",
                    roleColor = FinanceBlueDeep,
                    onClick = { community.id?.let(onDetailClick) }
                )
            }
        }
    }
}

@Composable
private fun HeroCard(
    totalCommunities: Int,
    onJoinClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(FinanceBlueDeep, FinanceBlue)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Communities",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "$totalCommunities active group${if (totalCommunities == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            FilledTonalButton(
                onClick = onJoinClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = FinanceBlueBright.copy(alpha = 0.2f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Rounded.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Join", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CommunityCard(
    community: Community,
    role: String,
    roleColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FinanceBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FinanceBlueSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForCommunity(community.name),
                    contentDescription = null,
                    tint = FinanceBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RoleBadge(text = role, color = roleColor)
                }
                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${community.membersCount} members",
                    style = MaterialTheme.typography.labelMedium,
                    color = FinanceBlueDeep.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun CreateCommunityCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FinanceBlueSurface
        ),
        border = BorderStroke(1.dp, FinanceBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FinanceBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = FinanceBlue
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Create Community",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Start a group and invite members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RoleBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun AdminBadge(
    containerColor: Color = FinanceBlue.copy(alpha = 0.12f),
    textColor: Color = FinanceBlue
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "ADMIN",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun SectionTitle(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EmptySectionCard(message: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = FinanceBlueSurface
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getIconForCommunity(name: String): ImageVector = when {
    name.contains("Garden", ignoreCase = true) -> Icons.Rounded.LocalFlorist
    name.contains("HOA", ignoreCase = true) -> Icons.Rounded.Home
    name.contains("Reading", ignoreCase = true) -> Icons.AutoMirrored.Rounded.MenuBook
    name.contains("Block", ignoreCase = true) -> Icons.Rounded.Celebration
    name.contains("Pool", ignoreCase = true) -> Icons.Rounded.WaterDrop
    else -> Icons.Rounded.Groups
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CommunityScreenPreview() {
    MaterialTheme {
        CommunityScreen(
            innerPadding = PaddingValues(),
            onDetailClick = {}
        )
    }
}
