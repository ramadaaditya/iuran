package com.ramstudio.kaskita.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.core.utils.formatCurrency
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.presentation.dashboard.component.CreateCommunityDialog
import com.ramstudio.kaskita.presentation.dashboard.component.JoinCommunityDialog

// ── Navigation helper ────────────────────────────────────────────────────────

fun NavController.navigateToCommunity(navOptions: NavOptions? = null) =
    if (navOptions != null) navigate(route = ScreenRoute.Community, navOptions)
    else navigate(ScreenRoute.Community)

// ── Screen entry-point ───────────────────────────────────────────────────────

@Composable
fun CommunityScreen(
    innerPadding: PaddingValues,
    viewModel: CommunityViewModel = hiltViewModel(),
    onDetailClick: (communityId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Derive managed vs joined based on currentUserId from ViewModel
    // For now using createdBy heuristic — replace with real currentUserId when auth is wired
    val currentUserId = uiState.communities.firstOrNull()?.createdBy
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

// ── Main content ─────────────────────────────────────────────────────────────

@Composable
private fun CommunityContent(
    managedCommunities: List<Community>,
    joinedCommunities: List<Community>,
    innerPadding: PaddingValues,
    onJoinClick: () -> Unit,
    onCreateClick: () -> Unit,
    onDetailClick: (communityId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            CommunityScreenHeader(
                totalCommunities = managedCommunities.size + joinedCommunities.size,
                onJoinClick = onJoinClick
            )
        }

        // ── Managed communities ─────────────────────────────────────────────
        item {
            SectionLabel(
                label = "COMMUNITIES YOU MANAGE",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        if (managedCommunities.isEmpty()) {
            item { EmptyState(message = "You haven't created any community yet.") }
        } else {
            items(managedCommunities) { community ->
                CommunityListItem(
                    community = community,
                    isAdmin = true,
                    onClick = { community.id?.let { onDetailClick(it) } }
                )
            }
        }

        // Create new group CTA
        item {
            CreateCommunityButton(
                onClick = onCreateClick,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        // ── Joined communities ──────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionLabel(
                label = "COMMUNITIES YOU JOINED",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        if (joinedCommunities.isEmpty()) {
            item { EmptyState(message = "Join a community using an invite code.") }
        } else {
            items(joinedCommunities) { community ->
                CommunityListItem(
                    community = community,
                    isAdmin = false,
                    onClick = { community.id?.let { onDetailClick(it) } }
                )
            }
        }
    }
}

// ── Header ───────────────────────────────────────────────────────────────────

@Composable
private fun CommunityScreenHeader(
    totalCommunities: Int,
    onJoinClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "My Communities",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$totalCommunities active ${if (totalCommunities == 1) "group" else "groups"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Join via code button
        FilledTonalButton(
            onClick = onJoinClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
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

// ── Community list item ───────────────────────────────────────────────────────

@Composable
private fun CommunityListItem(
    community: Community,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(community.themeColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconForCommunity(community.name),
                contentDescription = null,
                tint = community.themeColor,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Name + member count
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AdminBadge()
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${community.membersCount} members",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Balance
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(community.balance),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = community.themeColor
            )
            Text(
                text = "balance",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )
}

// ── Create CTA ────────────────────────────────────────────────────────────────

@Composable
private fun CreateCommunityButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.5.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "Create a new community",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Start a group and invite members",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
fun AdminBadge(
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "ADMIN",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Community icon mapper (unchanged) ────────────────────────────────────────

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
fun CommunityScreenPreview() {
    MaterialTheme {
        CommunityScreen(innerPadding = PaddingValues(), onDetailClick = {})
    }
}