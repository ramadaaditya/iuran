package com.ramstudio.kaskita.presentation.transaction

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
import androidx.compose.material.icons.rounded.Add
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
import com.ramstudio.kaskita.data.DummyData
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.domain.model.toUiModel
import com.ramstudio.kaskita.ui.theme.AlertOrange
import com.ramstudio.kaskita.ui.theme.ErrorRed
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
    onAddTransactionClick: () -> Unit = {},
    communityId: String,
    isAdmin: Boolean = false
) {
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
        hasSelectedCommunity = communityId.isNotBlank(),
        innerPadding = innerPadding,
        onAddTransactionClick = onAddTransactionClick,
        onDetailClick = onDetailClick
    )
}

@Composable
fun TransactionContent(
    transactions: List<TransactionUiModel>,
    isAdmin: Boolean,
    isLoading: Boolean,
    hasSelectedCommunity: Boolean,
    innerPadding: PaddingValues,
    onAddTransactionClick: () -> Unit,
    onDetailClick: (String) -> Unit
) {
    var activeFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    val pendingCount = remember(transactions) { transactions.count { it.status == TransactionStatus.PENDING } }
    val incomeCount = remember(transactions) { transactions.count { it.isPositive && it.status == TransactionStatus.SUCCESS } }
    val expenseCount = remember(transactions) { transactions.count { !it.isPositive && it.status == TransactionStatus.SUCCESS } }
    val filtered = remember(transactions, activeFilter) {
        when (activeFilter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.INCOME -> transactions.filter { it.isPositive }
            TransactionFilter.EXPENSE -> transactions.filter { !it.isPositive }
            TransactionFilter.PENDING -> transactions.filter { it.status == TransactionStatus.PENDING }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            TransactionHeader(
                totalCount = transactions.size,
                hasSelectedCommunity = hasSelectedCommunity,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
            )
        }

        if (!hasSelectedCommunity) {
            item {
                SelectCommunityState(modifier = Modifier.padding(horizontal = 20.dp))
            }
            return@LazyColumn
        }

        item {
            QuickAddTransactionCard(
                onClick = onAddTransactionClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            SummaryRow(
                incomeCount = incomeCount,
                expenseCount = expenseCount,
                pendingCount = pendingCount,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (isAdmin && pendingCount > 0) {
            item {
                PendingApprovalBanner(
                    count = pendingCount,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        item {
            FilterChipRow(
                active = activeFilter,
                onSelect = { activeFilter = it },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filtered.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
        } else if (filtered.isEmpty()) {
            item {
                TransactionEmptyState(
                    filter = activeFilter,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        } else {
            items(items = filtered, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onDetailClick(transaction.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun TransactionHeader(
    totalCount: Int,
    hasSelectedCommunity: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (hasSelectedCommunity) "$totalCount records in selected community"
            else "Select a community on dashboard to load records",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickAddTransactionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Add New Transaction",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Record income or expense quickly",
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
private fun SummaryRow(
    incomeCount: Int,
    expenseCount: Int,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryChip(
            label = "Income",
            value = "$incomeCount",
            dotColor = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Expense",
            value = "$expenseCount",
            dotColor = ErrorRed,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Pending",
            value = "$pendingCount",
            dotColor = WarningYellow,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PendingApprovalBanner(count: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.08f))
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
                    tint = AlertOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$count need approval",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Use Pending filter to review faster",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    active: TransactionFilter,
    onSelect: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionFilter.entries.forEach { filter ->
            val isSelected = active == filter
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = { Text(filter.label, fontSize = 12.sp) },
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
                )
            )
        }
    }
}

@Composable
private fun SelectCommunityState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No community selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Open Dashboard and choose a community first.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TransactionEmptyState(
    filter: TransactionFilter,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(52.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (filter == TransactionFilter.PENDING) "No pending approvals" else "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (filter == TransactionFilter.PENDING) "All approvals are completed."
            else "Create a transaction to start tracking activity.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransactionAvatar(transaction = transaction)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = transaction.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.amountText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isPositive) SuccessGreen else MaterialTheme.colorScheme.onBackground
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

@Composable fun TransactionStatusChip(status: TransactionStatus) {
    val (label, bgColor, textColor) = when (status) {
        TransactionStatus.PENDING -> Triple("PENDING", WarningYellow.copy(alpha = 0.15f), AlertOrange)
        TransactionStatus.SUCCESS -> Triple("SUCCESS", SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
        TransactionStatus.REJECTED -> Triple("REJECTED", ErrorRed.copy(alpha = 0.12f), ErrorRed)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TransactionScreenPreview() {
    MaterialTheme {
        TransactionContent(
            transactions = DummyData.transactions.map { it.toUiModel() },
            isAdmin = true,
            isLoading = false,
            hasSelectedCommunity = true,
            innerPadding = PaddingValues(),
            onAddTransactionClick = {},
            onDetailClick = {}
        )
    }
}
