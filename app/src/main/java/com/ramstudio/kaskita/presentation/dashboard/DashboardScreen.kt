package com.ramstudio.kaskita.presentation.dashboard

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.core.domain.model.Community
import com.ramstudio.kaskita.core.domain.model.TransactionCategory
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.model.TransactionUiModel
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.core.ui.theme.AlertOrange
import com.ramstudio.kaskita.core.ui.theme.Border
import com.ramstudio.kaskita.core.ui.theme.DividerColor
import com.ramstudio.kaskita.core.ui.theme.ErrorRed
import com.ramstudio.kaskita.core.ui.theme.InfoBlue
import com.ramstudio.kaskita.core.ui.theme.Primary
import com.ramstudio.kaskita.core.ui.theme.SuccessGreen
import com.ramstudio.kaskita.core.ui.theme.TextDisabled
import com.ramstudio.kaskita.core.ui.theme.TextHigh
import com.ramstudio.kaskita.core.ui.theme.TextMedium
import com.ramstudio.kaskita.core.ui.theme.WarningYellow
import com.ramstudio.kaskita.core.ui.theme.White
import com.ramstudio.kaskita.presentation.transaction.TransactionItem
import java.text.NumberFormat
import java.util.Locale


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
        communityId = "comm-1",
        userId = "user-1",
        icon = Icons.Default.ArrowUpward,
        iconBgColor = SuccessGreen,
        title = "Iuran Bulanan",
        subtitle = "Budi Santoso",
        amountText = "+ Rp50.000",
        isPositive = true,
        timeText = "08:30",
        dateTimeText = "10 Mar 2026, 08:30",
        status = TransactionStatus.PENDING,
        category = TransactionCategory.INCOME,
        initiatorName = "Budi Santoso",
        amount = 5000.0,
        rejectionReason = null
    ),
    TransactionUiModel(
        id = "tx-2",
        communityId = "comm-1",
        userId = "user-admin-1",
        icon = Icons.Default.ArrowDownward,
        iconBgColor = ErrorRed,
        title = "Beli Galon Air",
        subtitle = "Admin",
        amountText = "- Rp25.000",
        isPositive = false,
        timeText = "10:15",
        dateTimeText = "10 Mar 2026, 10:15",
        status = TransactionStatus.SUCCESS,
        category = TransactionCategory.EXPENSE,
        initiatorName = "Admin",
        amount = 25000.0,
        rejectionReason = null
    ),
    TransactionUiModel(
        id = "tx-3",
        communityId = "comm-2",
        userId = "user-2",
        icon = Icons.Default.ArrowUpward,
        iconBgColor = SuccessGreen,
        title = "Iuran Bulanan",
        subtitle = "Siti Rahayu",
        amountText = "+ Rp50.000",
        isPositive = true,
        timeText = "14:22",
        dateTimeText = "09 Mar 2026, 14:22",
        status = TransactionStatus.SUCCESS,
        category = TransactionCategory.INCOME,
        initiatorName = "Siti Rahayu",
        amount = 25000.0,
        rejectionReason = null
    ),
    TransactionUiModel(
        id = "tx-4",
        communityId = "comm-2",
        userId = "user-admin-2",
        icon = Icons.Default.ArrowDownward,
        iconBgColor = AlertOrange,
        title = "Bayar Listrik",
        subtitle = "Admin",
        amountText = "- Rp150.000",
        isPositive = false,
        timeText = "09:00",
        dateTimeText = "08 Mar 2026, 09:00",
        status = TransactionStatus.SUCCESS,
        category = TransactionCategory.EXPENSE,
        initiatorName = "Admin",
        amount = 25000.0,
        rejectionReason = null
    ),
    TransactionUiModel(
        id = "tx-5",
        communityId = "comm-3",
        userId = "user-3",
        icon = Icons.Default.ArrowUpward,
        iconBgColor = SuccessGreen,
        title = "Iuran Bulanan",
        subtitle = "Ahmad Fauzi",
        amountText = "+ Rp50.000",
        isPositive = true,
        timeText = "17:45",
        dateTimeText = "07 Mar 2026, 17:45",
        status = TransactionStatus.REJECTED,
        category = TransactionCategory.INCOME,
        initiatorName = "Ahmad Fauzi",
        amount = 25000.0,
        rejectionReason = "Bukti transfer tidak jelas"
    )
)

fun NavController.navigateToDashboard(navOptions: NavOptions? = null) =
    if (navOptions != null) {
        navigate(route = ScreenRoute.DashboardRoute, navOptions)
    } else {
        navigate(ScreenRoute.DashboardRoute)
    }


//val previewDashboardUiState = DashboardUiState(
//    communities = dummyCommunities,
//    selectedCommunity = dummyCommunities.first(),
//    transactions = dummyTransactions,
//    isLoading = false,
//    totalIncome = 150000.0,
//    totalExpense = 175000.0,
//    pendingCount = 1,
//    isAdmin = true,
//    currentUserId = "user-admin-1"
//)

//val previewEmptyUiState = DashboardUiState(
//    communities = emptyList(),
//    selectedCommunity = null,
//    isLoading = false
//)

@Composable
fun DashboardRouteScreen(
    innerPadding: PaddingValues,
    onTransactionClick: (String) -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onPendingApprovalsClick: () -> Unit = {},
    selectedCommunityId: String? = null,
    onSelectedCommunityChanged: (String?) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedCommunityId) {
        viewModel.setSelectedCommunityId(selectedCommunityId)
    }

    val activeId = (uiState.screenState as? DashboardScreenState.Success)?.selectedCommunity?.id

    LaunchedEffect(activeId) {
        if (activeId != selectedCommunityId) {
            onSelectedCommunityChanged(activeId)
        }
    }

    DashboardContent(
        uiState = uiState,
        modifier = Modifier.padding(innerPadding),
        onCommunitySelected = { community ->
            viewModel.selectCommunity(community)
            onSelectedCommunityChanged(community.id)
        },
        onTransactionClick = onTransactionClick,
        onViewAllTransactionsClick = onViewAllTransactionsClick,
        onPendingApprovalsClick = onPendingApprovalsClick
    )
}


@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
    onCommunitySelected: (Community) -> Unit,
    onTransactionClick: (String) -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onPendingApprovalsClick: () -> Unit
) {
    when (val state = uiState.screenState) {
        is DashboardScreenState.Loading -> DashboardLoadingState(modifier)
        is DashboardScreenState.Empty -> DashboardEmptyState(modifier)
        is DashboardScreenState.Error -> DashboardEmptyState()// ini perlu disesuaikan lagi
        is DashboardScreenState.Success -> DashboardMainContent(
            uiState = state,
            modifier = modifier,
            onCommunitySelected = onCommunitySelected,
            onTransactionClick = onTransactionClick,
            onViewAllTransactionsClick = onViewAllTransactionsClick,
            onPendingApprovalsClick = onPendingApprovalsClick,
            isAdmin = uiState.isAdmin
        )
    }
}

@Composable
private fun DashboardMainContent(
    uiState: DashboardScreenState.Success,
    isAdmin: Boolean,
    modifier: Modifier = Modifier,
    onCommunitySelected: (Community) -> Unit,
    onTransactionClick: (String) -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onPendingApprovalsClick: () -> Unit
) {
    val recentTransactions = uiState.transactions.take(5)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            DashboardHeader(
                community = uiState.selectedCommunity,
                communities = uiState.communities,
                isAdmin = isAdmin,
                totalIncome = uiState.totalIncome,
                totalExpense = uiState.totalExpense,
                onCommunitySelected = onCommunitySelected
            )
        }

        if (isAdmin && uiState.pendingCount > 0) {
            item {
                PendingApprovalsBanner(
                    pendingCount = uiState.pendingCount,
                    onClick = onPendingApprovalsClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        item {
            RecentActivityHeader(
                onViewAllClick = onViewAllTransactionsClick,
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 10.dp
                )
            )
        }

        if (uiState.transactions.isEmpty()) {
            item {
                EmptyTransactionState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp)
                )
            }
        } else {
            item {
                TransactionHistoryCard(
                    transactions = recentTransactions,
                    onTransactionClick = onTransactionClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

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
            .background(Primary)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommunitySelector(
            selectedCommunity = community,
            communities = communities,
            isAdmin = isAdmin,
            onCommunitySelected = onCommunitySelected
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.dashboard_total_balance),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = White.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatCurrency(community.balance),
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = White,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                label = stringResource(R.string.dashboard_income),
                amount = totalIncome,
                isPositive = true,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = stringResource(R.string.dashboard_expense),
                amount = totalExpense,
                isPositive = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CommunitySelector(
    selectedCommunity: Community,
    communities: List<Community>,
    isAdmin: Boolean,
    onCommunitySelected: (Community) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(White.copy(alpha = 0.15f))
                    .clickable { if (communities.size > 1) expanded = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(selectedCommunity.themeColor)
                )
                Text(
                    text = selectedCommunity.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                if (communities.size > 1) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = stringResource(R.string.dashboard_change_community_cd),
                        tint = White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(White)
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

        if (isAdmin) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(WarningYellow)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.community_admin_badge),
                    color = TextHigh,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(White.copy(alpha = 0.12f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isPositive) SuccessGreen else ErrorRed,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = White.copy(alpha = 0.65f),
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = White
        )
    }
}

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
        colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningYellow.copy(alpha = 0.4f))
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
                    text = stringResource(R.string.common_exclamation),
                    color = AlertOrange,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_pending_waiting, pendingCount),
                    fontWeight = FontWeight.Bold,
                    color = TextHigh,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.dashboard_pending_tap_review),
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
private fun RecentActivityHeader(
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.dashboard_history_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextHigh
        )
        Text(
            text = stringResource(R.string.common_see_all),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Primary,
            modifier = Modifier.clickable { onViewAllClick() }
        )
    }
}


@Composable
private fun TransactionHistoryCard(
    transactions: List<TransactionUiModel>,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            transactions.forEachIndexed { index, transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
                if (index != transactions.lastIndex) {
                    HorizontalDivider(color = DividerColor)
                }
            }
        }
    }
}


@Composable
private fun DashboardLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
private fun DashboardEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            tint = TextMedium,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.dashboard_empty_community_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextHigh
        )
        Text(
            text = stringResource(R.string.dashboard_empty_community_subtitle),
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
            text = stringResource(R.string.dashboard_empty_transaction_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextMedium
        )
        Text(
            text = stringResource(R.string.dashboard_empty_transaction_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TextDisabled,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}


//@Preview(showBackground = true, name = "Dashboard - Normal (Admin)")
//@Composable
//private fun DashboardPreviewAdmin() {
//    KasKitaTheme {
//        DashboardContent(
//            uiState = previewDashboardUiState,
//            onCommunitySelected = {},
//            onTransactionClick = {},
//            onViewAllTransactionsClick = {},
//            onPendingApprovalsClick = {}
//        )
//    }
//}

//@Preview(showBackground = true, name = "Dashboard - Normal (Member)")
//@Composable
//private fun DashboardPreviewMember() {
//    KasKitaTheme {
//        DashboardContent(
//            uiState = previewDashboardUiState.copy(
//                isAdmin = false,
//                pendingCount = 0
//            ),
//            onCommunitySelected = {},
//            onTransactionClick = {},
//            onViewAllTransactionsClick = {},
//            onPendingApprovalsClick = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Dashboard - Empty (No Community)")
//@Composable
//private fun DashboardPreviewEmpty() {
//    KasKitaTheme {
//        DashboardContent(
//            uiState = previewEmptyUiState,
//            onCommunitySelected = {},
//            onTransactionClick = {},
//            onViewAllTransactionsClick = {},
//            onPendingApprovalsClick = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Dashboard - Loading")
//@Composable
//private fun DashboardPreviewLoading() {
//    KasKitaTheme {
//        DashboardContent(
//            uiState = previewDashboardUiState.copy(isLoading = true),
//            onCommunitySelected = {},
//            onTransactionClick = {},
//            onViewAllTransactionsClick = {},
//            onPendingApprovalsClick = {}
//        )
//    }
//}
