package com.ramstudio.kaskita.presentation.transaction

import android.content.ContentValues.TAG
import android.util.Log
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.ramstudio.kaskita.data.DummyData
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.domain.model.toUiModel
import com.ramstudio.kaskita.ui.theme.AlertOrange
import com.ramstudio.kaskita.ui.theme.ErrorRed
import com.ramstudio.kaskita.ui.theme.KasKitaTheme
import com.ramstudio.kaskita.ui.theme.SuccessGreen
import com.ramstudio.kaskita.ui.theme.WarningYellow
import com.ramstudio.kaskita.ui.theme.White

fun NavController.navigateToTransactions(navOptions: NavOptions? = null) =
    if (navOptions != null) navigate(route = ScreenRoute.Transaction, navOptions)
    else navigate(ScreenRoute.Transaction)


private enum class TransactionFilter(val label: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense"),
    PENDING("Pending")
}


@Composable
fun TransactionScreen(
    innerPadding: PaddingValues,
    viewModel: TransactionViewModel = hiltViewModel(),
    onDetailClick: (String) -> Unit,
    communityId: String,
    isAdmin: Boolean = false
) {
    Log.d(TAG, "TransactionScreen: $isAdmin")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(communityId) {
        if (communityId.isNotBlank()) {
            viewModel.loadTransactionsByCommunity(communityId)
        }
    }

    TransactionContent(
        transactions = uiState.transactions,
        isAdmin = isAdmin,
        isLoading = uiState.isLoading,
        innerPadding = innerPadding,
        onDetailClick = onDetailClick
    )
}


@Composable
fun TransactionContent(
    transactions: List<TransactionUiModel>,
    isAdmin: Boolean,
    isLoading: Boolean,
    innerPadding: PaddingValues,
    onDetailClick: (String) -> Unit
) {
    var activeFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    val filtered = remember(transactions, activeFilter) {
        when (activeFilter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.INCOME -> transactions.filter { it.isPositive }
            TransactionFilter.EXPENSE -> transactions.filter { !it.isPositive }
            TransactionFilter.PENDING -> transactions.filter { it.status == TransactionStatus.PENDING }
        }
    }

    val pendingCount = remember(transactions) {
        transactions.count { it.status == TransactionStatus.PENDING }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        item {
            TransactionScreenHeader(
                totalCount = transactions.size,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
            )
        }

        if (isAdmin && pendingCount > 0) {
            item {
                PendingApprovalBanner(
                    count = pendingCount,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (isAdmin) {
            item {
                AdminSummaryRow(
                    transactions = transactions,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // ── Filter chips ──────────────────────────────────────────────────────
        item {
            FilterChipRow(
                active = activeFilter,
                onSelect = { activeFilter = it },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Section label ─────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (activeFilter) {
                        TransactionFilter.ALL -> "All Transactions"
                        TransactionFilter.INCOME -> "Income"
                        TransactionFilter.EXPENSE -> "Expense"
                        TransactionFilter.PENDING -> "Pending Review"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filtered.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Loading state ─────────────────────────────────────────────────────
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // ── Empty state ────────────────────────────────────────────────────────
        else if (filtered.isEmpty()) {
            item { TransactionEmptyState(filter = activeFilter) }
        }

        // ── Transaction list ──────────────────────────────────────────────────
        else {
            items(items = filtered, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onDetailClick(transaction.id) },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun TransactionScreenHeader(totalCount: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "$totalCount records in this community",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Pending approval banner (admin only) ──────────────────────────────────────

@Composable
private fun PendingApprovalBanner(count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(WarningYellow.copy(alpha = 0.2f)),
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
                    text = "$count transaction${if (count > 1) "s" else ""} need your approval",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Filter by Pending to review",
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

// ── Admin summary chips ────────────────────────────────────────────────────────

@Composable
private fun AdminSummaryRow(
    transactions: List<TransactionUiModel>,
    modifier: Modifier = Modifier
) {
    val totalIncome = transactions
        .filter { it.isPositive && it.status == TransactionStatus.SUCCESS }
        .sumOf {
            it.amountText.replace("+", "").replace("Rp", "").replace(",", "").trim()
                .toDoubleOrNull() ?: 0.0
        }
    val totalExpense = transactions
        .filter { !it.isPositive && it.status == TransactionStatus.SUCCESS }
        .sumOf {
            it.amountText.replace("-", "").replace("Rp", "").replace(",", "").trim()
                .toDoubleOrNull() ?: 0.0
        }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryChip(
            label = "INCOME",
            value = transactions.count { it.isPositive && it.status == TransactionStatus.SUCCESS }
                .toString() + " txn",
            dotColor = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "EXPENSE",
            value = transactions.count { !it.isPositive && it.status == TransactionStatus.SUCCESS }
                .toString() + " txn",
            dotColor = ErrorRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// ── Filter chip row ────────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(
    active: TransactionFilter,
    onSelect: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionFilter.values().forEach { filter ->
            val isSelected = active == filter
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun TransactionEmptyState(filter: TransactionFilter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (filter) {
                TransactionFilter.PENDING -> "No pending approvals"
                else -> "No transactions yet"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (filter) {
                TransactionFilter.PENDING -> "All caught up! Nothing to review."
                else -> "Transactions in this community will appear here."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


@Composable
fun TransactionItem(
    transaction: TransactionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TransactionAvatar(transaction = transaction)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
//            Text(
//                text = transaction.subtitle,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.amountText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isPositive) SuccessGreen else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
//            TransactionStatusChip(status = transaction.status)
        }
    }
}

@Composable
fun TransactionAvatar(transaction: TransactionUiModel) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
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
        val dotColor = when (transaction.status) {
            TransactionStatus.SUCCESS -> SuccessGreen
            TransactionStatus.PENDING -> WarningYellow
            TransactionStatus.REJECTED -> ErrorRed
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(White)
                .padding(2.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(dotColor)
        )
    }
}

@Composable
fun TransactionStatusChip(status: TransactionStatus) {
    val (label, bgColor, textColor) = when (status) {
        TransactionStatus.PENDING -> Triple(
            "PENDING",
            WarningYellow.copy(alpha = 0.15f),
            AlertOrange
        )

        TransactionStatus.SUCCESS -> Triple(
            "SUCCESS",
            SuccessGreen.copy(alpha = 0.15f),
            SuccessGreen
        )

        TransactionStatus.REJECTED -> Triple("REJECTED", ErrorRed.copy(alpha = 0.12f), ErrorRed)
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

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TransactionScreenPreview() {
    MaterialTheme {
        TransactionContent(
            transactions = DummyData.transactions.map { it.toUiModel() },
            isAdmin = true,
            isLoading = false,
            innerPadding = PaddingValues(),
            onDetailClick = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun PendingApprovalBannerPreview() {
    KasKitaTheme {
        PendingApprovalBanner(count = 2)
    }
}