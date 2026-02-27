package com.smartbudge.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = staticCompositionLocalOf { true }

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    background = BackgroundLight,
    surface = CardLight,
    onPrimary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight,
)

private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = PrimaryBlue,
    background = BackgroundDark,
    surface = CardDark,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark,
)

@Composable
fun SmartBudgeTheme(
    darkTheme: Boolean = true, // Force Dark Mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
