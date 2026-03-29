package com.ramstudio.kaskita.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
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

data class ExtendedColors(
    val success: Color,
    val info: Color,
    val warning: Color,
    val alert: Color,
    val contentBackground: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = SuccessGreen,
        info = InfoBlue,
        warning = WarningYellow,
        alert = AlertOrange,
        contentBackground = ContentWhite
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
            alert = AlertOrange,
            contentBackground = ContentWhite
        )
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

object KasKitaTheme {
    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val extendedColors: ExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColors.current


}