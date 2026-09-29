package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryScanBlue,
    onPrimary = Color.White,
    primaryContainer = BrandHeaderBlue,
    onPrimaryContainer = Color.White,
    secondary = BrandHeaderDark,
    onSecondary = Color.White,
    background = PageBackground,
    onBackground = TextSlate,
    surface = CardBackground,
    onSurface = TextSlate,
    surfaceVariant = PaleSkyBlueTint,
    onSurfaceVariant = TextSteel,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
