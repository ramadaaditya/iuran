package com.ramstudio.kaskita.presentation.detailCommunity

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.core.utils.formatCurrency
import com.ramstudio.kaskita.domain.model.Community
import com.ramstudio.kaskita.domain.model.CommunityTab
import com.ramstudio.kaskita.domain.model.User
import com.ramstudio.kaskita.presentation.community.AdminBadge
import com.ramstudio.kaskita.presentation.community.getIconForCommunity
import com.ramstudio.kaskita.ui.theme.ErrorRed
import com.ramstudio.kaskita.ui.theme.SuccessGreen
import com.ramstudio.kaskita.ui.theme.WarningYellow
import kotlin.math.abs

// ── Navigation helper ─────────────────────────────────────────────────────────

fun NavController.navigateToDetailCommunity(communityId: String, navOptions: NavOptions? = null) =
    if (navOptions != null) navigate(route = ScreenRoute.DetailCommunity, navOptions)
    else navigate(ScreenRoute.DetailCommunity)

// ── Screen entry-point ────────────────────────────────────────────────────────

@Composable
fun CommunityDetailScreen(
    communityId: String = "",
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit = {},
    viewModel: DetailCommunityViewModel = hiltViewModel()
) {
    val currentUserId = "user_123"

    val community = Community(
        id = "komunitas_001",
        name = "Garden Club",
        description = "Dedicated to purchasing organic fertilizers, new seeds, and covering monthly maintenance costs for our community garden.",
        code = "GARDEN-A",
        createdBy = "user_123",
        balance = 3420.00,
        membersCount = 12,
        themeColor = Color(0xFF00BFA5)
    )

    val dummyTransactions = listOf(
        DummyTransaction("t1", "Monthly Dues", "Ramada Aditya", 150.00, "Oct 24", "SUCCESS"),
        DummyTransaction("t2", "Fertilizer Purchase", "Sarah Jenkins", -45.00, "Oct 22", "SUCCESS"),
        DummyTransaction("t3", "Garden Tools", "Michael Chen", -120.00, "Oct 21", "PENDING"),
        DummyTransaction("t4", "Donation", "Budi Santoso", 50.00, "Oct 20", "SUCCESS"),
        DummyTransaction("t5", "Seed Purchase", "Ramada Aditya", -30.00, "Oct 18", "REJECTED")
    )

    val members = listOf(
        User("user_123", "Ramada Aditya", "Admin", "RA"),
        User("user_456", "Sarah Jenkins", "Member", "SJ"),
        User("user_789", "Michael Chen", "Member", "MC"),
        User("user_101", "Budi Santoso", "Member", "BS")
    )

    CommunityDetailContent(
        community = community,
        currentUserId = currentUserId,
        transactions = dummyTransactions,
        members = members,
        onBackClick = onBackClick,
        onAddTransactionClick = onAddTransactionClick,
    )
}

// ── Main content ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityDetailContent(
    community: Community,
    currentUserId: String,
    transactions: List<DummyTransaction>,
    members: List<User>,
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = community.createdBy == currentUserId
    var selectedTab by remember { mutableStateOf(CommunityTab.TRANSACTIONS) }
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            snackbarHostState.showSnackbar("Invite code copied!")
            showCopiedSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { /* TODO: Community settings */ }) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            // Only show FAB for transactions tab
            if (selectedTab == CommunityTab.TRANSACTIONS) {
                ExtendedFloatingActionButton(
                    onClick = onAddTransactionClick,
                    containerColor = community.themeColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Transaction", fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp) // space for FAB
        ) {
            // ── Balance card ─────────────────────────────────────────────────
            item {
                CommunityBalanceCard(
                    community = community,
                    isAdmin = isAdmin,
                    onCopyCodeClick = {
                        clipboardManager.setText(AnnotatedString(community.code))
                        showCopiedSnackbar = true
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // ── Summary stats (income / expense) — visible for admin ─────────
            if (isAdmin) {
                item {
                    AdminSummaryRow(
                        transactions = transactions,
                        themeColor = community.themeColor,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Tabs ─────────────────────────────────────────────────────────
            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = community.themeColor,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = community.themeColor,
                                height = 2.5.dp
                            )
                        },
                        divider = {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    ) {
                        CommunityTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == tab) community.themeColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // ── Tab content ───────────────────────────────────────────────────
            when (selectedTab) {
                CommunityTab.TRANSACTIONS -> {
                    if (transactions.isEmpty()) {
                        item {
                            CommunityEmptyState(
                                icon = Icons.Rounded.ReceiptLong,
                                title = "No transactions yet",
                                subtitle = "Tap 'Add Transaction' to record the first one."
                            )
                        }
                    } else {
                        items(transactions) { tx ->
                            TransactionItem(
                                tx = tx,
                                themeColor = community.themeColor,
                                isAdmin = isAdmin
                            )
                        }
                    }
                }

                CommunityTab.MEMBERS -> {
                    items(members) { member ->
                        MemberItem(
                            member = member,
                            themeColor = community.themeColor
                        )
                    }
                }
            }
        }
    }
}

// ── Balance card ──────────────────────────────────────────────────────────────

@Composable
private fun CommunityBalanceCard(
    community: Community,
    isAdmin: Boolean,
    onCopyCodeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = community.themeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Community icon + name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForCommunity(community.name),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AdminBadge(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        textColor = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "TOTAL BALANCE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(community.balance),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Invite code pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable(onClick = onCopyCodeClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Invite code: ${community.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${community.membersCount} members",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

// ── Admin summary (income/expense) ───────────────────────────────────────────

@Composable
private fun AdminSummaryRow(
    transactions: List<DummyTransaction>,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    val totalIncome = transactions.filter { it.amount > 0 && it.status == "SUCCESS" }
        .sumOf { it.amount }
    val totalExpense = transactions.filter { it.amount < 0 && it.status == "SUCCESS" }
        .sumOf { it.amount }
    val pendingCount = transactions.count { it.status == "PENDING" }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryChip(
                label = "INCOME",
                value = "+${formatCurrency(totalIncome)}",
                valueColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            SummaryChip(
                label = "EXPENSE",
                value = formatCurrency(abs(totalExpense)),
                valueColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }

        if (pendingCount > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            PendingApprovalBanner(
                count = pendingCount,
                onClick = { /* TODO: navigate to pending list */ }
            )
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun PendingApprovalBanner(count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(WarningYellow.copy(alpha = 0.4f))
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(WarningYellow.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = WarningYellow,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$count Pending Approval${if (count > 1) "s" else ""}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap to review and approve",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Transaction item ──────────────────────────────────────────────────────────

@Composable
private fun TransactionItem(
    tx: DummyTransaction,
    themeColor: Color,
    isAdmin: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: navigate to transaction detail */ }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar initials
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    when (tx.status) {
                        "SUCCESS" -> if (tx.amount > 0) SuccessGreen.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant

                        "PENDING" -> WarningYellow.copy(alpha = 0.12f)
                        else -> ErrorRed.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tx.submittedBy.take(2).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = when (tx.status) {
                    "SUCCESS" -> if (tx.amount > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    "PENDING" -> WarningYellow
                    else -> ErrorRed
                }
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${tx.submittedBy} · ${tx.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (tx.amount > 0) "+${formatCurrency(tx.amount)}"
                else formatCurrency(tx.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (tx.amount > 0) SuccessGreen else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(3.dp))
            StatusBadge(status = tx.status)
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "SUCCESS" -> Triple(SuccessGreen.copy(alpha = 0.12f), SuccessGreen, "SUCCESS")
        "PENDING" -> Triple(WarningYellow.copy(alpha = 0.12f), WarningYellow, "PENDING")
        "REJECTED" -> Triple(ErrorRed.copy(alpha = 0.1f), ErrorRed, "REJECTED")
        else -> Triple(Color.LightGray.copy(alpha = 0.2f), Color.Gray, status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.3.sp
        )
    }
}

// ── Member item ───────────────────────────────────────────────────────────────

@Composable
private fun MemberItem(
    member: User,
    themeColor: Color
) {
    val isAdmin = member.role == "Admin"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: view member profile */ }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isAdmin) themeColor.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.initial,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isAdmin) themeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = member.role,
                style = MaterialTheme.typography.bodySmall,
                color = if (isAdmin) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isAdmin) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        if (isAdmin) {
            AdminBadge(
                containerColor = themeColor.copy(alpha = 0.1f),
                textColor = themeColor
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun CommunityEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ── Data model (temporary until Transaction domain model is ready) ────────────

data class DummyTransaction(
    val id: String,
    val title: String,
    val submittedBy: String,
    val amount: Double,
    val date: String,
    val status: String // "SUCCESS" | "PENDING" | "REJECTED"
)

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CommunityDetailScreenPreview() {
    MaterialTheme {
        CommunityDetailScreen(
            communityId = "preview",
            onBackClick = {}
        )
    }
}