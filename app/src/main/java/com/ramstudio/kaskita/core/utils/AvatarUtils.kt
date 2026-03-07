package com.ramstudio.kaskita.core.utils

import androidx.compose.ui.graphics.Color

object AvatarUtils {

    private val fallbackAvatarColors = listOf(
        Color(0xFF6D4C41),
        Color(0xFF0F8B8D),
        Color(0xFF2E7D32),
        Color(0xFF1976D2),
        Color(0xFF7B1FA2),
        Color(0xFFEF6C00),
        Color(0xFF455A64),
        Color(0xFFC2185B)
    )

    fun getInitials(fullName: String?): String {
        if (fullName.isNullOrBlank()) return ""

        val words = fullName
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return ""

        return when (words.size) {
            1 -> words[0].first().uppercaseChar().toString()
            else -> {
                val firstInitial = words.first().first().uppercaseChar()
                val lastInitial = words.last().first().uppercaseChar()
                "$firstInitial$lastInitial"
            }
        }
    }

    fun getFallbackAvatarColor(seed: String?): Color {
        if (seed.isNullOrBlank()) return fallbackAvatarColors.first()
        val index = (seed.hashCode() and Int.MAX_VALUE) % fallbackAvatarColors.size
        return fallbackAvatarColors[index]
    }
}
