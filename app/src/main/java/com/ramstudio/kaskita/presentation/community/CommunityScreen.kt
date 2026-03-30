package com.ramstudio.kaskita.presentation.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.core.common.UiState
import com.ramstudio.kaskita.core.domain.model.Community
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.core.ui.component.EmptyStateAction
import com.ramstudio.kaskita.core.ui.component.EmptyStateView
import com.ramstudio.kaskita.core.utils.LocalAppSnackbarHostState
import com.ramstudio.kaskita.presentation.dashboard.component.CreateCommunityDialog
import com.ramstudio.kaskita.presentation.dashboard.component.JoinCommunityDialog
import timber.log.Timber

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
    var showActionFab by remember { mutableStateOf(false) }
    var createCommunityError by remember { mutableStateOf<String?>(null) }
    var joinCommunityError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = LocalAppSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CommunityEvent.ShowError -> {
                    Timber.d("Error: ${event.message}")
                    when {
                        showCreateDialog -> createCommunityError = event.message
                        showJoinDialog -> joinCommunityError = event.message
                        else -> snackbarHostState.showSnackbar(event.message)
                    }
                }

                is CommunityEvent.ShowSuccess -> {
                    Timber.d("Success: ${event.message}")
                    showCreateDialog = false
                    showJoinDialog = false
                    createCommunityError = null
                    joinCommunityError = null
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CommunityContent(
            uiState = uiState,
            innerPadding = innerPadding,
            onDetailClick = onDetailClick
        )

        CommunityActionsFab(
            expanded = showActionFab,
            onToggleExpanded = { showActionFab = !showActionFab },
            onJoinClick = {
                showActionFab = false
                joinCommunityError = null
                showJoinDialog = true
            },
            onCreateClick = {
                showActionFab = false
                createCommunityError = null
                showCreateDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = innerPadding.calculateBottomPadding() + 20.dp)
        )

        if (showCreateDialog) {
            CreateCommunityDialog(
                onDismiss = {
                    showCreateDialog = false
                    createCommunityError = null
                },
                isLoading = uiState.isActionLoading,
                onCreate = { name, desc -> viewModel.createCommunity(name, desc) },
                errorMessage = createCommunityError,
            )
        }

        if (showJoinDialog) {
            JoinCommunityDialog(
                isLoading = uiState.isActionLoading,
                onDismiss = {
                    showJoinDialog = false
                    joinCommunityError = null
                },
                onJoin = { code -> viewModel.joinCommunity(code) },
                errorMessage = joinCommunityError
            )
        }
    }
}

@Composable
fun CommunityContent(
    modifier: Modifier = Modifier,
    uiState: CommunityUiState,
    innerPadding: PaddingValues,
    onDetailClick: (communityId: String) -> Unit
) {
    when (val screenState = uiState.screenState) {
        is UiState.Loading -> {}// BUat loading screen nya
        is UiState.Error -> EmptyStateView(
            icon = Icons.Default.CloudOff,
            iconTint = MaterialTheme.colorScheme.error,
            title = "Gagal Memuat Komunitas",
            subtitle = screenState.message,
            modifier = modifier,
            action = EmptyStateAction(
                label = "Coba Lagi",
                onClick = {} // TODO: expose retry dari ViewModel
            )
        )

        is UiState.Success -> CommunityMainContent(
            communities = screenState.data,
            innerPadding = innerPadding,
            onDetailClick = onDetailClick,
            modifier = modifier
        )
    }
}

@Composable
private fun CommunityMainContent(
    communities: List<Community>,
    innerPadding: PaddingValues,
    onDetailClick: (communityId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (communities.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.GroupAdd,
                    title = stringResource(R.string.community_empty),
                    subtitle = "Buat atau bergabung ke komunitas untuk memulai",
                    modifier = Modifier.fillParentMaxSize()
                )
            }
        } else {
            items(communities, key = { it.id ?: it.code }) { community ->
                CommunityCard(
                    community = community,
                    onClick = { community.id?.let(onDetailClick) }
                )
            }
        }
    }
}


@Composable
private fun CommunityActionsFab(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onJoinClick: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FabActionItem(
                    label = stringResource(R.string.community_create_title),
                    icon = Icons.Default.Add,
                    onClick = onCreateClick
                )
                FabActionItem(
                    label = stringResource(R.string.common_join),
                    icon = Icons.Rounded.QrCodeScanner,
                    onClick = onJoinClick
                )
            }
        }

        FloatingActionButton(
            onClick = onToggleExpanded,
            containerColor = FinanceBlue,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.Close else Icons.Default.Add,
                contentDescription = if (expanded) {
                    stringResource(R.string.common_close)
                } else {
                    stringResource(R.string.community_hero_title)
                }
            )
        }
    }
}

@Composable
private fun FabActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = FinanceBlue,
            contentColor = Color.White
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}

@Composable
private fun CommunityCard(
    community: Community,
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
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.common_members_count, community.membersCount),
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
private fun EmptySectionCard(message: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = FinanceBlueSurface
        ),
        modifier = Modifier.fillMaxWidth()
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
