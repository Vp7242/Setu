package com.setu.sanskrit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = InkIndigo, onPrimary = Parchment,
    primaryContainer = Color(0xFFDDE3F5), onPrimaryContainer = InkIndigo,
    secondary = PalmGreen, onSecondary = Color.White,
    tertiary = TurmericDeep, tertiaryContainer = Color(0xFFFFE9B0), onTertiaryContainer = Color(0xFF3D2900),
    background = BirchBark, onBackground = Color(0xFF1A1C17),
    surface = BirchBark, onSurface = Color(0xFF1A1C17),
    surfaceVariant = Color(0xFFDCE0D0), onSurfaceVariant = Color(0xFF44483D),
    surfaceContainerLow = BirchBarkRaised, surfaceContainer = Color(0xFFE6E9DC),
    outline = Color(0xFF7A7F70), outlineVariant = Color(0xFFC4C8B8),
    error = Color(0xFFB3261E), errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFB9C6F2), onPrimary = Color(0xFF1B2748),
    primaryContainer = Color(0xFF2A3A63), onPrimaryContainer = Color(0xFFDDE3F5),
    secondary = Color(0xFF8FD0C0), onSecondary = Color(0xFF00382F),
    tertiary = Turmeric, tertiaryContainer = Color(0xFF4A3600), onTertiaryContainer = Color(0xFFFFE9B0),
    background = NightPage, onBackground = Color(0xFFE4E6DC),
    surface = NightPage, onSurface = Color(0xFFE4E6DC),
    surfaceVariant = Color(0xFF2C3242), onSurfaceVariant = Color(0xFFC3C7B8),
    surfaceContainerLow = NightRaised, surfaceContainer = Color(0xFF1E2433),
    outline = Color(0xFF8A8F9C), outlineVariant = Color(0xFF3A4152),
    error = Color(0xFFF2B8B5), errorContainer = Color(0xFF601410), onErrorContainer = Color(0xFFF9DEDC),
)

@Composable
fun SetuTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = SetuTypography,
        content = content,
    )
}
