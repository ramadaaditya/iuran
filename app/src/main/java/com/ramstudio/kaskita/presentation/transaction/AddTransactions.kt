package com.ramstudio.kaskita.presentation.transaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramstudio.kaskita.R
import com.ramstudio.kaskita.core.utils.LocalAppSnackbarHostState
import com.ramstudio.kaskita.domain.model.TransactionCategory
import com.ramstudio.kaskita.ui.theme.ErrorRed
import com.ramstudio.kaskita.ui.theme.SuccessGreen

@Composable
fun AddTransactionScreen(
    communityId: String,
    onCloseClick: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalAppSnackbarHostState.current
    val successMessage = stringResource(R.string.add_transaction_success)
    val receiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onReceiptSelected(uri)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(successMessage)
            viewModel.clearForm()
            onSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    AddTransactionContent(
        modifier = modifier,
        transactionType = uiState.transactionType,
        amount = uiState.amount,
        description = uiState.description,
        hasReceiptAttached = uiState.hasReceipt,
        onTypeChange = viewModel::onTypeChange,
        onAmountChange = viewModel::onAmountChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onAttachReceipt = { receiptPickerLauncher.launch("image/*") },
        onCloseClick = onCloseClick,
        onSubmitClick = { viewModel.submitTransaction(communityId, isAdmin) },
        isLoading = uiState.isLoading,
        isAdmin = isAdmin,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionContent(
    modifier: Modifier = Modifier,
    transactionType: TransactionCategory,
    amount: String,
    description: String,
    hasReceiptAttached: Boolean,
    onTypeChange: (TransactionCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAttachReceipt: () -> Unit,
    onCloseClick: () -> Unit,
    onSubmitClick: () -> Unit,
    isLoading: Boolean,
    isAdmin: Boolean = false
) {
    val accentColor by animateColorAsState(
        targetValue = if (transactionType == TransactionCategory.INCOME) SuccessGreen else ErrorRed,
        animationSpec = tween(300),
        label = "accentColor"
    )

    val isFormValid = amount.isNotBlank() && amount.toDoubleOrNull() != null
            && description.isNotBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_transaction_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.common_close),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            SubmitBar(
                accentColor = accentColor,
                isEnabled = isFormValid && !isLoading,
                onSubmitClick = onSubmitClick,
                isLoading = isLoading
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            TypeToggle(
                selected = transactionType,
                onSelect = onTypeChange,
                accentColor = accentColor,
                isAdmin = isAdmin
            )

            Spacer(modifier = Modifier.height(40.dp))

            AmountInput(
                amount = amount,
                onAmountChange = onAmountChange,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Description ───────────────────────────────────────────────────
            FormField(label = stringResource(R.string.add_transaction_desc_label)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    placeholder = {
                        Text(
                            stringResource(R.string.add_transaction_desc_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = accentColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Receipt upload — REQUIRED for MVP ────────────────────────────
            FormField(
                label = stringResource(R.string.add_transaction_proof_label),
                required = true
            ) {
                ReceiptUploadField(
                    isAttached = hasReceiptAttached,
                    accentColor = accentColor,
                    onClick = onAttachReceipt
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Info note ─────────────────────────────────────────────────────
            StatusInfoNote(
                transactionType = transactionType,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Type toggle ───────────────────────────────────────────────────────────────

@Composable
private fun TypeToggle(
    selected: TransactionCategory,
    onSelect: (TransactionCategory) -> Unit,
    accentColor: Color,
    isAdmin: Boolean = false
) {
    val visibleTypes = if (isAdmin) TransactionCategory.values().toList()
    else listOf(TransactionCategory.INCOME)

    // If the current selection is EXPENSE but user is not admin, force INCOME
    LaunchedEffect(isAdmin) {
        if (!isAdmin && selected == TransactionCategory.EXPENSE) {
            onSelect(TransactionCategory.INCOME)
        }
    }

    // Only show the toggle row when there are multiple options (admin)
    if (visibleTypes.size > 1) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            visibleTypes.forEach { type ->
                val isSelected = selected == type
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) accentColor else Color.Transparent,
                    animationSpec = tween(250),
                    label = "typeBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(250),
                    label = "typeText"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable { onSelect(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (type == TransactionCategory.INCOME) {
                            stringResource(R.string.transaction_type_income)
                        } else {
                            stringResource(R.string.transaction_type_expense)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// ── Amount input ──────────────────────────────────────────────────────────────

@Composable
private fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.currency_rp),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(accentColor),
                decorationBox = { innerTextField ->
                    Box {
                        if (amount.isEmpty()) {
                            Text(
                                text = "0",
                                fontSize = 52.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.widthIn(min = 60.dp, max = 240.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Animated underline
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(2.dp)
                .background(
                    if (amount.isNotBlank()) accentColor
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.add_transaction_amount_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
    }
}

// ── Form field wrapper ────────────────────────────────────────────────────────

@Composable
private fun FormField(
    label: String,
    required: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            if (required) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.add_transaction_required),
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

// ── Receipt upload field ──────────────────────────────────────────────────────

@Composable
private fun ReceiptUploadField(
    isAttached: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.5.dp,
                color = if (isAttached) accentColor
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                if (isAttached) accentColor.copy(alpha = 0.06f)
                else MaterialTheme.colorScheme.background
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isAttached) accentColor.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAttached) Icons.Rounded.CheckCircle else Icons.Rounded.AddPhotoAlternate,
                contentDescription = null,
                tint = if (isAttached) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isAttached) {
                    stringResource(R.string.add_transaction_receipt_attached)
                } else {
                    stringResource(R.string.add_transaction_upload_receipt)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isAttached) accentColor
                else MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isAttached) {
                    stringResource(R.string.add_transaction_tap_to_change)
                } else {
                    stringResource(R.string.add_transaction_receipt_format)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = if (isAttached) Icons.Rounded.Edit else Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── Info note ─────────────────────────────────────────────────────────────────

@Composable
private fun StatusInfoNote(
    transactionType: TransactionCategory,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.07f))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Rounded.Info,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = when (transactionType) {
                TransactionCategory.INCOME ->
                    stringResource(R.string.add_transaction_info_income)

                TransactionCategory.EXPENSE ->
                    stringResource(R.string.add_transaction_info_expense)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

// ── Submit bar ────────────────────────────────────────────────────────────────

@Composable
private fun SubmitBar(
    accentColor: Color,
    isEnabled: Boolean,
    isLoading: Boolean,
    onSubmitClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onSubmitClick,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {


                    Text(
                        text = stringResource(R.string.add_transaction_submit),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (!isEnabled && !isLoading) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.add_transaction_submit_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", name = "Member view")
@Composable
fun AddTransactionScreenMemberPreview() {
    MaterialTheme {
        AddTransactionContent(
            transactionType = TransactionCategory.INCOME,
            amount = "",
            description = "",
            hasReceiptAttached = false,
            onTypeChange = {},
            onAmountChange = {},
            onDescriptionChange = {},
            onAttachReceipt = {},
            onCloseClick = {},
            onSubmitClick = {},
            isLoading = false,
            isAdmin = false
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", name = "Admin view")
@Composable
fun AddTransactionScreenAdminPreview() {
    MaterialTheme {
        AddTransactionContent(
            transactionType = TransactionCategory.EXPENSE,
            amount = "40000",
            description = "Pembelian alat",
            hasReceiptAttached = true,
            onTypeChange = {},
            onAmountChange = {},
            onDescriptionChange = {},
            onAttachReceipt = {},
            onCloseClick = {},
            onSubmitClick = {},
            isLoading = false,
            isAdmin = true
        )
    }
}
