package com.ramstudio.kaskita.presentation.detailTransaction

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.core.domain.model.IconBgGreen
import com.ramstudio.kaskita.core.domain.model.TransactionCategory
import com.ramstudio.kaskita.core.domain.model.TransactionStatus
import com.ramstudio.kaskita.core.domain.model.TransactionUiModel
import com.ramstudio.kaskita.core.navigation.ScreenRoute
import com.ramstudio.kaskita.core.ui.theme.ErrorRed
import com.ramstudio.kaskita.core.ui.theme.SuccessGreen
import com.ramstudio.kaskita.core.ui.theme.White
import com.ramstudio.kaskita.core.utils.AvatarUtils
import com.ramstudio.kaskita.core.utils.LocalAppSnackbarHostState
import com.ramstudio.kaskita.presentation.transaction.TransactionStatusChip
import timber.log.Timber

fun NavController.navigateToDetailTransaction(id: String) {
    navigate(ScreenRoute.DetailTransaction(id))
}


@Composable
fun TransactionDetailsScreen(
    transactionId: String,
    onBackClick: () -> Unit,
    onEditClick: (communityId: String, isAdmin: Boolean, transactionId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalAppSnackbarHostState.current

    LaunchedEffect(transactionId) {
        viewModel.loadTransactionDetail(transactionId)
    }

    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearActionSuccess()
            onBackClick()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

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
                        Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.detail_transaction_not_found),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        else -> {
            TransactionDetailsContent(
                transaction = uiState.selectedTransaction!!,
                isAdmin = uiState.canManageTransaction,
                canEditTransaction = uiState.canEditTransaction,
                onBackClick = onBackClick,
                onApprove = { viewModel.approveTransaction(transactionId) },
                onReject = { reason -> viewModel.rejectTransaction(transactionId, reason) },
                onEditClick = onEditClick,
                modifier = modifier,
                isActionLoading = uiState.isActionLoading,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsContent(
    transaction: TransactionUiModel,
    isAdmin: Boolean,
    canEditTransaction: Boolean,
    onBackClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onEditClick: (communityId: String, isAdmin: Boolean, transactionId: String) -> Unit,
    modifier: Modifier = Modifier,
    isActionLoading: Boolean,
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var showEvidencePreview by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    var rejectionReasonError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detail_transaction_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        // Admin action buttons at the bottom for PENDING transactions
        bottomBar = {
            if (isAdmin && transaction.status == TransactionStatus.PENDING) {
                AdminActionBar(
                    isLoading = isActionLoading,
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
            AmountHeroSection(transaction = transaction)
            Spacer(modifier = Modifier.height(28.dp))
            TransactionDetailsCard(transaction = transaction)
            val rejectionReason = transaction.rejectionReason
            if (transaction.status == TransactionStatus.REJECTED && !rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                RejectionReasonCard(reason = rejectionReason)
            }
            if (canEditTransaction) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        onEditClick(transaction.communityId, isAdmin, transaction.id)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.detail_transaction_edit_title))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            EvidenceSection(
                proofUrl = transaction.proofUrl,
                onImageClick = { showEvidencePreview = true }
            )
            Timber.d("Image proof url: ${transaction.proofUrl}")
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showRejectDialog) {
        RejectConfirmDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = {
                if (rejectionReason.isBlank()) {
                    rejectionReasonError = true
                } else {
                    rejectionReasonError = false
                    showRejectDialog = false
                    onReject(rejectionReason.trim())
                    rejectionReason = ""
                }
            },
            reason = rejectionReason,
            onReasonChange = {
                rejectionReason = it
                if (rejectionReasonError && it.isNotBlank()) {
                    rejectionReasonError = false
                }
            },
            isReasonError = rejectionReasonError
        )
    }

    if (showEvidencePreview && !transaction.proofUrl.isNullOrBlank()) {
        FullScreenEvidencePreview(
            imageUrl = transaction.proofUrl,
            onClose = { showEvidencePreview = false }
        )
    }
}

// ── Amount hero ───────────────────────────────────────────────────────────────

@Composable
private fun AmountHeroSection(transaction: TransactionUiModel) {
    val initial = AvatarUtils.getInitials(transaction.initiatorName)
        .ifBlank { stringResource(R.string.common_unknown_initials) }

    val avatarBg = if (transaction.isPositive) IconBgGreen else Color(0xFFFFCDD2)
    val avatarTint = if (transaction.isPositive) SuccessGreen else ErrorRed

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = avatarTint,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = transaction.amountText,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (transaction.isPositive) SuccessGreen else ErrorRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (transaction.status == TransactionStatus.PENDING) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.detail_transaction_waiting_approval),
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
                label = stringResource(R.string.detail_transaction_label_description),
                value = transaction.title
            )
            DetailDivider()
            DetailRow(
                label = stringResource(R.string.detail_transaction_label_type),
                value = if (transaction.isPositive) {
                    stringResource(R.string.detail_transaction_type_income)
                } else {
                    stringResource(R.string.detail_transaction_type_expense)
                }
            )
            DetailDivider()
            DetailRow(
                label = stringResource(R.string.detail_transaction_label_date),
                value = transaction.dateTimeText
            )
            DetailDivider()
            DetailRow(
                label = stringResource(R.string.detail_transaction_label_submitted_by),
                value = transaction.initiatorName.ifBlank { stringResource(R.string.common_community_member) }
            )
            DetailDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detail_transaction_label_status),
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
private fun RejectionReasonCard(reason: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.detail_transaction_rejection_reason_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            textAlign = TextAlign.End
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
private fun EvidenceSection(
    proofUrl: String?,
    onImageClick: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.detail_transaction_evidence),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (proofUrl.isNullOrBlank()) {
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
                        text = stringResource(R.string.detail_transaction_no_evidence),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val proofRequest = remember(proofUrl, context) {
                ImageRequest.Builder(context)
                    .data(proofUrl)
                    .crossfade(true)
                    .listener(
                        onError = { _, result ->
                            Log.e("EvidenceImage", "Load failed", result.throwable)
                        }
                    )
                    .build()
            }
            SubcomposeAsyncImage(
                model = proofRequest,
                contentDescription = stringResource(R.string.detail_transaction_evidence_cd),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onImageClick() },
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                                text = stringResource(R.string.detail_transaction_failed_load_evidence),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FullScreenEvidencePreview(
    imageUrl: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        val fullPreviewRequest = remember(imageUrl, context) {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .listener(
                    onError = { _, result ->
                        Log.e("EvidenceImage", "Full preview load failed", result.throwable)
                    }
                )
                .build()
        }
        SubcomposeAsyncImage(
            model = fullPreviewRequest,
            contentDescription = stringResource(R.string.detail_transaction_full_preview_cd),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentScale = ContentScale.Fit,
            loading = { CircularProgressIndicator(color = White) },
            error = {
                Text(
                    text = stringResource(R.string.detail_transaction_failed_load_image),
                    color = White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.detail_transaction_close_preview_cd),
                tint = White
            )
        }
    }
}

// ── Admin action bar ──────────────────────────────────────────────────────────

@Composable
private fun AdminActionBar(
    isLoading: Boolean,
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
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed
                ),
                border = BorderStroke(
                    1.5.dp, ErrorRed.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringResource(R.string.detail_transaction_reject),
                    fontWeight = FontWeight.Bold
                )
            }

            // Approve
            Button(
                onClick = onApprove,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.detail_transaction_approve),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
private fun RejectConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    reason: String,
    onReasonChange: (String) -> Unit,
    isReasonError: Boolean
) {
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
                stringResource(R.string.detail_transaction_reject_dialog_title),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.detail_transaction_reject_dialog_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(R.string.detail_transaction_reject_reason_label)) },
                    placeholder = { Text(stringResource(R.string.detail_transaction_reject_reason_placeholder)) },
                    isError = isReasonError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ErrorRed,
                        focusedLabelColor = ErrorRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isReasonError) {
                    Text(
                        text = stringResource(R.string.detail_transaction_reject_reason_required),
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.detail_transaction_reject_confirm),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.common_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        TransactionDetailsContent(
            onBackClick = {},
            transaction = TransactionUiModel(
                id = "TRX-001",
                communityId = "comm-1",
                userId = "user-1",
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                iconBgColor = Color(0xFF6650A4),
                title = "Pembayaran Iuran Bulanan",
                subtitle = "Aditya Ramada",
                amount = 40000.00,
                amountText = "- Rp 40.000",
                isPositive = false,
                timeText = "10:30",
                dateTimeText = "10 Mar 2026, 10:30",
                status = TransactionStatus.PENDING,
                category = TransactionCategory.EXPENSE,
                initiatorName = "Aditya Ramada",
                rejectionReason = null
            ),
            isAdmin = true,
            canEditTransaction = false,
            onApprove = {},
            onReject = {},
            onEditClick = { _, _, _ -> },
            isActionLoading = false
        )
    }
}
