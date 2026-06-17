package com.trailmate.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TrailMateMoss = Color(0xFF27694B)
val TrailMateClay = Color(0xFFE77346)
val TrailMateMist = Color(0xFFF5F5F2)
val TrailMateInk = Color(0xFF18211C)

private val LightColors = lightColorScheme(
    primary = TrailMateMoss,
    secondary = TrailMateClay,
    background = TrailMateMist,
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EFE6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TrailMateInk,
    onSurface = TrailMateInk,
    outline = Color(0xFFCCD8CF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF97D8AF),
    secondary = Color(0xFFF0B28D),
    background = Color(0xFF101615),
    surface = Color(0xFF1B2721),
    surfaceVariant = Color(0xFF26352D),
    onPrimary = Color(0xFF102017),
    onSecondary = Color(0xFF351508),
    onBackground = Color(0xFFF4F8F2),
    onSurface = Color(0xFFF4F8F2),
    outline = Color(0xFF52645A)
)

@Composable
fun TrailMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
