package com.ramstudio.kaskita.domain.model

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.Locale

val PrimaryGreen = Color(0xFF2E7D32)
val IconBgGreen = Color(0xFFC8E6C9)
val IconBgYellow = Color(0xFFFFF9C4)

data class Transaction(
    val id: String,
    val communityId: String,
    val userId: String,
    val amount: Double,
    val description: String?,
    val createdAt: Long,
    val type: TransactionCategory,
    val status: TransactionStatus
)

enum class TransactionStatus {
    PENDING, SUCCESS, REJECTED
}

enum class TransactionCategory {
    INCOME, EXPENSE
}

data class TransactionUiModel(
    val id: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val title: String,
    val subtitle: String,
    val amountText: String,
    val isPositive: Boolean,
    val timeText: String,
    val status: TransactionStatus,
    val category: TransactionCategory,
    val initiatorName: String = "",
)


fun Transaction.toUiModel(): TransactionUiModel {
    val isPositive = type == TransactionCategory.INCOME

    val icon = if (isPositive)
        Icons.Default.Add
    else
        Icons.Default.Remove

    return TransactionUiModel(
        id = id,
        icon = icon,
        iconBgColor = if (isPositive) IconBgGreen else IconBgYellow,
        title = description ?: if (isPositive) "Pemasukan" else "Pengeluaran",
        subtitle = "Status: $status",
        amountText = formatCurrency(amount, isPositive),
        isPositive = isPositive,
        timeText = formatTime(createdAt),
        category = type,
        initiatorName = userId,
        status = status
    )
}

fun formatCurrency(amount: Double, isPositive: Boolean): String {
    val prefix = if (isPositive) "+" else "-"
    val formatted = "%,.0f".format(kotlin.math.abs(amount))
    return "${prefix}Rp $formatted"
}

fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("dd MMM", Locale.getDefault())
        .format(Date(timestamp))
}

fun TransactionDto.toDomain(): Transaction {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Transaction(
            id = id,
            communityId = communityId,
            userId = userId,
            amount = amount.toDouble(),
            description = description,
            createdAt = OffsetDateTime.parse(createdAt)
                .toInstant()
                .toEpochMilli(),
            type = if (type == "IN") {
                TransactionCategory.INCOME
            } else {
                TransactionCategory.EXPENSE
            },
            status = when (status.uppercase()) {
                "APPROVED" -> TransactionStatus.SUCCESS
                "PENDING" -> TransactionStatus.PENDING
                "REJECTED" -> TransactionStatus.REJECTED
                else -> TransactionStatus.PENDING
            }
        )
    } else {
        TODO("VERSION.SDK_INT < O")
    }
}

@Serializable
data class TransactionDto(
    val id: String,
    @SerialName("community_id")
    val communityId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("type")
    val type: String,
    @SerialName("amount")
    val amount: Long,
    @SerialName("description")
    val description: String? = null,
    @SerialName("status")
    val status: String,
    @SerialName("proof_url")
    val proofUrl: String?,
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("approved_by")
    val approvedBy: String? = null,
    @SerialName("approved_at")
    val approvedAt: String? = null
)
