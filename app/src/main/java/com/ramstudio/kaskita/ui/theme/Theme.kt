package com.ramstudio.kaskita.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // BRAND
    primary = Primary,
    onPrimary = White,

    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Primary,

    // BACKGROUND
    background = White,
    onBackground = TextHigh,

    surface = White,
    onSurface = TextHigh,

    surfaceVariant = PrimaryLightBackground,
    onSurfaceVariant = TextMedium,

    // SEMANTIC
    error = ErrorRed,
    onError = White,

    outline = Border
)

// Data class untuk warna semantic di luar standar Material 3
data class ExtendedColors(
    val success: Color,
    val info: Color,
    val warning: Color,
    val alert: Color
)

// Inisialisasi CompositionLocal untuk custom colors
val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = SuccessGreen,
        info = InfoBlue,
        warning = WarningYellow,
        alert = AlertOrange
    )
}

@Composable
fun KasKitaTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedColors provides ExtendedColors(
            success = SuccessGreen,
            info = InfoBlue,
            warning = WarningYellow,
            alert = AlertOrange
        )
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            // Pastikan Anda memiliki referensi objek Typography yang valid
            // typography = Typography,
            content = content
        )
    }
}