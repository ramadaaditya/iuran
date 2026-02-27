package com.ramstudio.kaskita.presentation.detailTransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.domain.model.TransactionStatus
import com.ramstudio.kaskita.domain.model.TransactionUiModel
import com.ramstudio.kaskita.presentation.transaction.TransactionStatusChip
import com.ramstudio.kaskita.presentation.transaction.TransactionViewModel
import com.ramstudio.kaskita.ui.theme.ErrorRed
import com.ramstudio.kaskita.ui.theme.SuccessGreen
import com.ramstudio.kaskita.ui.theme.WarningYellow
import com.ramstudio.kaskita.ui.theme.White

// ── Navigation helper ─────────────────────────────────────────────────────────

fun NavController.navigateToDetailTransaction(id: String) {
    navigate(ScreenRoute.DetailTransaction(id))
}

// ── Screen entry-point ────────────────────────────────────────────────────────

@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) {
        viewModel.loadTransactionDetail(transactionId)
    }

    // TODO: Replace with real isAdmin from community context
    val isAdmin = true

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        uiState.selectedTransaction == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Transaction not found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        else -> {
            TransactionDetailsContent(
                transaction = uiState.selectedTransaction!!,
                isAdmin = isAdmin,
                onBackClick = onBackClick,
                onApprove = { /* TODO: viewModel.approveTransaction(transactionId) */ },
                onReject = { /* TODO: viewModel.rejectTransaction(transactionId) */ },
                modifier = modifier
            )
        }
    }
}

// ── Main content ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsContent(
    transaction: TransactionUiModel,
    isAdmin: Boolean,
    onBackClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Transaction Detail",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        // Admin action buttons at the bottom for PENDING transactions
        bottomBar = {
            if (isAdmin && transaction.status == TransactionStatus.PENDING) {
                AdminActionBar(
                    onApprove = onApprove,
                    onReject = { showRejectDialog = true }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Hero amount section ───────────────────────────────────────────
            AmountHeroSection(transaction = transaction)

            Spacer(modifier = Modifier.height(28.dp))

            // ── Details card ──────────────────────────────────────────────────
            TransactionDetailsCard(transaction = transaction)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Evidence section ──────────────────────────────────────────────
            EvidenceSection()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── Reject confirmation dialog ────────────────────────────────────────────
    if (showRejectDialog) {
        RejectConfirmDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = {
                showRejectDialog = false
                onReject()
            }
        )
    }
}

// ── Amount hero ───────────────────────────────────────────────────────────────

@Composable
private fun AmountHeroSection(transaction: TransactionUiModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Avatar with larger size as hero element
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(transaction.iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    tint = transaction.iconBgColor,
                    modifier = Modifier.size(36.dp)
                )
            }
            val dotColor = when (transaction.status) {
                TransactionStatus.SUCCESS -> SuccessGreen
                TransactionStatus.PENDING -> WarningYellow
                TransactionStatus.REJECTED -> ErrorRed
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(White)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = transaction.amountText,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (transaction.isPositive) SuccessGreen
            else MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        TransactionStatusChip(status = transaction.status)

        if (transaction.status == TransactionStatus.PENDING) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Waiting for admin approval",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Details card ──────────────────────────────────────────────────────────────

@Composable
private fun TransactionDetailsCard(transaction: TransactionUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            DetailRow(
                label = "Description",
                value = transaction.title
            )
            DetailDivider()
            DetailRow(
                label = "Type",
                value = if (transaction.isPositive) "Income (Deposit)" else "Expense (Withdrawal)"
            )
            DetailDivider()
            DetailRow(
                label = "Date",
                value = transaction.timeText
            )
            DetailDivider()
            DetailRow(
                label = "Submitted by",
                value = transaction.initiatorName.ifBlank { "Community Member" }
            )
            DetailDivider()
            // Status row with chip instead of plain text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                TransactionStatusChip(status = transaction.status)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.widthIn(max = 200.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

// ── Evidence section ──────────────────────────────────────────────────────────

@Composable
private fun EvidenceSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Evidence",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Placeholder — replace with AsyncImage (Coil) when evidence URL is available
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No evidence uploaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Admin action bar ──────────────────────────────────────────────────────────

@Composable
private fun AdminActionBar(
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reject
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp, ErrorRed.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reject", fontWeight = FontWeight.Bold)
            }

            // Approve
            Button(
                onClick = onApprove,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Approve", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Reject confirmation dialog ────────────────────────────────────────────────

@Composable
private fun RejectConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ErrorRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                "Reject Transaction?",
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                "This action cannot be undone. The transaction will be marked as rejected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Yes, Reject", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(20.dp)
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun TransactionDetailsScreenPreview() {
    MaterialTheme {
        TransactionDetailsScreen(
            transactionId = "",
            onBackClick = {}
        )
    }
}