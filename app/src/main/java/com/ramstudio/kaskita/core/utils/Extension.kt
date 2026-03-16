package com.ramstudio.kaskita.core.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(nominal: Double): String {
    val localeID = Locale("id", "ID")
    val formatter = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    return formatter.format(nominal)
}

fun formatRupiahTransaction(amount: Double, isExpense: Boolean = false): String {
    val localeID = Locale("id", "ID")
    val formatter = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    val formatted = formatter.format(kotlin.math.abs(amount))
    return if (isExpense) "-$formatted" else "+$formatted"
}

fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(timestamp))
}

fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(timestamp))
}