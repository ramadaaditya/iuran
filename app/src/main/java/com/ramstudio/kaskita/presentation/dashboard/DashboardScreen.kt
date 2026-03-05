package com.ramstudio.kaskita.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.ramstudio.kaskita.domain.model.TransactionCategory
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.ui.theme.AlertOrange
import com.ramstudio.kaskita.ui.theme.Border
import com.ramstudio.kaskita.ui.theme.ErrorRed
import com.ramstudio.kaskita.ui.theme.InfoBlue
import com.ramstudio.kaskita.ui.theme.KasKitaTheme
import com.ramstudio.kaskita.ui.theme.Primary
import com.ramstudio.kaskita.ui.theme.SuccessGreen
import com.ramstudio.kaskita.ui.theme.TextDisabled
import com.ramstudio.kaskita.ui.theme.TextHigh
import com.ramstudio.kaskita.ui.theme.TextMedium
import com.ramstudio.kaskita.ui.theme.WarningYellow

val dummyCommunities = listOf(
    Community(
        id = "comm-1",
        name = "Kas RT 07",
        description = "Kas warga RT 07 Blok B",
        code = "RT0701",
        createdBy = "user-admin-1",
        balance = 4250000.0,
        membersCount = 24,
        themeColor = Primary
    ),
    Community(
        id = "comm-2",
        name = "Arisan Ibu-Ibu",
        description = "Arisan bulanan",
        code = "ARS002",
        createdBy = "user-admin-2",
        balance = 1800000.0,
        membersCount = 12,
        themeColor = SuccessGreen
    ),
    Community(
        id = "comm-3",
        name = "Kas Kantor",
        description = "Patungan makan siang kantor",
        code = "KTR003",
        createdBy = "user-admin-1",
        balance = 320000.0,
        membersCount = 8,
        themeColor = InfoBlue
    )
)

val dummyTransactions = listOf(
    TransactionUiModel(
        id = "tx-1",
        icon = Icons.Default.ArrowUpward,
        iconBgColor = SuccessGreen,
        title = "Iuran Bulanan",
        subtitle = "Budi Santoso · 5 menit lalu",
        amountText = "+ Rp50.000",
        isPositive = true,
        timeText = "5 menit lalu",
        status = TransactionStatus.PENDING,
        category = TransactionCategory.INCOME,
        initiatorName = "Budi Santoso",
        amount = 5000.0
    ),
    TransactionUiModel(
        id = "tx-2",
        icon = Icons.Default.ArrowDownward,
        iconBgColor = ErrorRed,
        title = "Beli Galon Air",
        subtitle = "Admin · 2 jam lalu",
        amountText = "- Rp25.000",
        isPositive = false,
        timeText = "2 jam lalu",
        status = TransactionStatus.SUCCESS,
        category = TransactionCategory.EXPENSE,
        initiatorName = "Admin",
        amount = 25000.0
    ),
    TransactionUiModel(
        id = "tx-3",
        icon = Icons.Default.ArrowUpward,
        iconBgColor = SuccessGreen,
        title = "Iuran Bulanan",
        subtitle = "Siti Rahayu · 1 hari lalu",
        amountText = "+ Rp50.000",
        isPositive = true,
        timeText = "1 hari lalu",
        status = TransactionStatus.SUCCESS,
        category = TransactionCategory.INCOME,
        initiatorName = "Siti Rahayu",
        amount = 25000.0
    ),
    TransactionUiModel(
        id = "tx-4",
        icon = Icons.Default.ArrowDownward,
        iconBgColor = AlertOrange,
        title = "Bayar Listrik",
        subtitle = "Admin · 2 hari lalu",
        amountText = "- Rp150.000",
        isPositive = false,
        timeText = "2 hari lalu",
        status = TransactionStatus.SUCCESS,
        category = TransactionCategory.EXPENSE,
        initiatorName = "Admin",
        amount = 25000.0
    ),
    TransactionUiModel(
        id = "tx-5",
        icon = Icons.Default.ArrowUpward,
        iconBgColor = SuccessGreen,
        title = "Iuran Bulanan",
        subtitle = "Ahmad Fauzi · 3 hari lalu",
        amountText = "+ Rp50.000",
        isPositive = true,
        timeText = "3 hari lalu",
        status = TransactionStatus.REJECTED,
        category = TransactionCategory.INCOME,
        initiatorName = "Ahmad Fauzi",
        amount = 25000.0
    )
)

fun NavController.navigateToDashboard(navOptions: NavOptions? = null) =
    if (navOptions != null) {
        navigate(route = ScreenRoute.DashboardRoute, navOptions)
    } else {
        navigate(ScreenRoute.DashboardRoute)
    }


val previewDashboardUiState = DashboardUiState(
    communities = dummyCommunities,
    selectedCommunity = dummyCommunities.first(),
    transactions = dummyTransactions,
    isLoading = false,
    totalIncome = 150000.0,
    totalExpense = 175000.0,
    pendingCount = 1,
    isAdmin = true,
    currentUserId = "user-admin-1"
)

val previewEmptyUiState = DashboardUiState(
    communities = emptyList(),
    selectedCommunity = null,
    isLoading = false
)

@Composable
fun DashboardScreen(
    innerPadding: PaddingValues,
    onTransactionClick: (String) -> Unit,
    onPendingApprovalsClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        modifier = Modifier.padding(innerPadding),
        onCommunitySelected = viewModel::selectCommunity,
        onTransactionClick = onTransactionClick,
        onPendingApprovalsClick = onPendingApprovalsClick
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
    onCommunitySelected: (Community) -> Unit,
    onTransactionClick: (String) -> Unit,
    onPendingApprovalsClick: () -> Unit
) {
    when {
        uiState.isLoading -> DashboardLoadingState(modifier)
        uiState.selectedCommunity == null -> DashboardEmptyState(modifier)
        else -> DashboardMainContent(
            uiState = uiState,
            modifier = modifier,
            onCommunitySelected = onCommunitySelected,
            onTransactionClick = onTransactionClick,
            onPendingApprovalsClick = onPendingApprovalsClick
        )
    }
}

// ---------------------------------------------------------------------------
// Main Content
// ---------------------------------------------------------------------------

@Composable
private fun DashboardMainContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
    onCommunitySelected: (Community) -> Unit,
    onTransactionClick: (String) -> Unit,
    onPendingApprovalsClick: () -> Unit
) {
    val community = uiState.selectedCommunity ?: return

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ---- Header ----
        item {
            DashboardHeader(
                community = community,
                communities = uiState.communities,
                isAdmin = uiState.isAdmin,
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                onCommunitySelected = onCommunitySelected
            )
        }

        // ---- Pending Banner (admin only) ----
        if (uiState.isAdmin && uiState.pendingCount > 0) {
            item {
                PendingApprovalsBanner(
                    pendingCount = uiState.pendingCount,
                    onClick = onPendingApprovalsClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        // ---- Section title ----
        item {
            RecentActivityHeader(
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 24.dp,
                    bottom = 8.dp
                )
            )
        }

        // ---- Transaction list ----
        if (uiState.transactions.isEmpty()) {
            item {
                EmptyTransactionState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp)
                )
            }
        } else {
            items(
                items = uiState.transactions,
                key = { it.id }
            ) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

@Composable
private fun DashboardHeader(
    community: Community,
    communities: List<Community>,
    isAdmin: Boolean,
    totalIncome: Double,
    totalExpense: Double,
    onCommunitySelected: (Community) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Balance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(community.balance),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextHigh,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                CommunitySelector(
                    selectedCommunity = community,
                    communities = communities,
                    isAdmin = isAdmin,
                    onCommunitySelected = onCommunitySelected
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Income & Expense Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                label = "Income",
                amount = totalIncome,
                isPositive = true,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Expense",
                amount = totalExpense,
                isPositive = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunitySelector(
    selectedCommunity: Community,
    communities: List<Community>,
    isAdmin: Boolean,
    onCommunitySelected: (Community) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(18.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { if (communities.size > 1) expanded = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedCommunity.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextHigh,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                if (communities.size > 1) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Ganti komunitas",
                        tint = TextMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Dropdown
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .widthIn(min = 200.dp)
            ) {
                communities.forEach { community ->
                    DropdownMenuItem(
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(community.themeColor)
                            )
                        },
                        text = {
                            Text(
                                text = community.name,
                                color = TextHigh,
                                fontWeight = if (community.id == selectedCommunity.id)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onCommunitySelected(community)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPositive) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.12f)
    val iconTint = if (isPositive) SuccessGreen else ErrorRed

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMedium,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatCurrency(amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextHigh
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Pending Approvals Banner
// ---------------------------------------------------------------------------

@Composable
private fun PendingApprovalsBanner(
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            WarningYellow.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(WarningYellow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = AlertOrange,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$pendingCount waiting approvals",
                    fontWeight = FontWeight.Bold,
                    color = TextHigh,
                    fontSize = 14.sp
                )
                Text(
                    text = "Tap to review",
                    fontSize = 12.sp,
                    color = AlertOrange
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMedium,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RecentActivityHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextHigh
        )
        Text(
            text = "See all",
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
private fun TransactionCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}


@Composable
fun TransactionItem(
    transaction: TransactionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TransactionCard(
        modifier = modifier,
        onClick = onClick
    ) {
        // Avatar
        TransactionAvatar(transaction = transaction)

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = transaction.sub,
                style = MaterialTheme.typography.bodySmall,
                color = TextMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Amount + Status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.amountText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isPositive) SuccessGreen else ErrorRed
            )
            Spacer(modifier = Modifier.height(4.dp))
            TransactionStatusChip(status = transaction.status)
        }
    }
}

@Composable
private fun TransactionAvatar(transaction: TransactionUiModel) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(transaction.iconBgColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.icon,
                contentDescription = null,
                tint = transaction.iconBgColor,
                modifier = Modifier.size(22.dp)
            )
        }
        // Status indicator dot
        val dotColor = when (transaction.status) {
            TransactionStatus.SUCCESS -> SuccessGreen
            TransactionStatus.PENDING -> WarningYellow
            TransactionStatus.REJECTED -> ErrorRed
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(2.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

@Composable
private fun TransactionStatusChip(status: TransactionStatus) {
    val (label, bgColor, textColor) = when (status) {
        TransactionStatus.PENDING -> Triple(
            "PENDING",
            WarningYellow.copy(alpha = 0.15f),
            AlertOrange
        )

        TransactionStatus.SUCCESS -> Triple(
            "SUKSES",
            SuccessGreen.copy(alpha = 0.15f),
            SuccessGreen
        )

        TransactionStatus.REJECTED -> Triple(
            "DITOLAK",
            ErrorRed.copy(alpha = 0.12f),
            ErrorRed
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

// ---------------------------------------------------------------------------
// States
// ---------------------------------------------------------------------------

@Composable
private fun DashboardLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
private fun DashboardEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            tint = TextMedium,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada komunitas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextHigh
        )
        Text(
            text = "Buat atau bergabung ke komunitas\ndi tab Komunitas",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMedium,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyTransactionState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            tint = Border,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Belum ada transaksi",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextMedium
        )
        Text(
            text = "Transaksi komunitas akan muncul di sini",
            style = MaterialTheme.typography.bodySmall,
            color = TextDisabled,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Normal (Admin)")
@Composable
private fun DashboardPreviewAdmin() {
    KasKitaTheme {
        DashboardContent(
            uiState = previewDashboardUiState,
            onCommunitySelected = {},
            onTransactionClick = {},
            onPendingApprovalsClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Normal (Member)")
@Composable
private fun DashboardPreviewMember() {
    KasKitaTheme {
        DashboardContent(
            uiState = previewDashboardUiState.copy(
                isAdmin = false,
                pendingCount = 0
            ),
            onCommunitySelected = {},
            onTransactionClick = {},
            onPendingApprovalsClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Empty (No Community)")
@Composable
private fun DashboardPreviewEmpty() {
    KasKitaTheme {
        DashboardContent(
            uiState = previewEmptyUiState,
            onCommunitySelected = {},
            onTransactionClick = {},
            onPendingApprovalsClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Loading")
@Composable
private fun DashboardPreviewLoading() {
    KasKitaTheme {
        DashboardContent(
            uiState = previewDashboardUiState.copy(isLoading = true),
            onCommunitySelected = {},
            onTransactionClick = {},
            onPendingApprovalsClick = {}
        )
    }
}
