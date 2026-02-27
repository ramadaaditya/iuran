package com.ramstudio.kaskita.core.utils

object AvatarUtils {

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
}