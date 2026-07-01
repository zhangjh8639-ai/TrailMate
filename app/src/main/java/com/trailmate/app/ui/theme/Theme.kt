package com.trailmate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TrailMateLightColors = lightColorScheme(
    primary = ForestGreen,
    secondary = PineGreen,
    secondaryContainer = SoftGreen,
    onSecondaryContainer = ForestGreen,
    background = AppBackground,
    surface = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = Color(0xFF59665F),
)

@Composable
fun TrailMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TrailMateLightColors,
        content = content,
    )
}
