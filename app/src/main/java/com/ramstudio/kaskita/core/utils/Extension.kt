package com.ramstudio.kaskita.core.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(nominal: Double): String {
    return try {
        val indonesianLocale = Locale("id", "ID")
        val currencyFormatter = NumberFormat.getCurrencyInstance(indonesianLocale)
        currencyFormatter.format(nominal)
    } catch (e: Exception) {
        e.printStackTrace()
        "Rp 0"
    }
}