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
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBD5),
    onPrimaryContainer = Color(0xFF052113),
    inversePrimary = Color(0xFF97D8AF),
    secondary = TrailMateClay,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9C8),
    onSecondaryContainer = Color(0xFF331104),
    tertiary = Color(0xFF4E6392),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8E2FF),
    onTertiaryContainer = Color(0xFF071A3A),
    background = TrailMateMist,
    onBackground = TrailMateInk,
    surface = Color.White,
    onSurface = TrailMateInk,
    surfaceVariant = Color(0xFFE8EFE6),
    onSurfaceVariant = Color(0xFF4B584F),
    surfaceTint = TrailMateMoss,
    inverseSurface = Color(0xFF2C342F),
    inverseOnSurface = Color(0xFFEFF5EF),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF7B897F),
    outlineVariant = Color(0xFFC9D5CC),
    scrim = Color.Black,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD8DED7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F5EF),
    surfaceContainer = Color(0xFFEAF0E9),
    surfaceContainerHigh = Color(0xFFE4EAE3),
    surfaceContainerHighest = Color(0xFFDEE4DD)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF97D8AF),
    onPrimary = Color(0xFF102017),
    primaryContainer = Color(0xFF0F4F34),
    onPrimaryContainer = Color(0xFFCDEBD5),
    inversePrimary = TrailMateMoss,
    secondary = Color(0xFFF0B28D),
    onSecondary = Color(0xFF351508),
    secondaryContainer = Color(0xFF6B321B),
    onSecondaryContainer = Color(0xFFFFD9C8),
    tertiary = Color(0xFFBAC6F0),
    onTertiary = Color(0xFF1E2F56),
    tertiaryContainer = Color(0xFF35476F),
    onTertiaryContainer = Color(0xFFD8E2FF),
    background = Color(0xFF101615),
    onBackground = Color(0xFFF4F8F2),
    surface = Color(0xFF1B2721),
    onSurface = Color(0xFFF4F8F2),
    surfaceVariant = Color(0xFF26352D),
    onSurfaceVariant = Color(0xFFC7D2C9),
    surfaceTint = Color(0xFF97D8AF),
    inverseSurface = Color(0xFFE1E7E0),
    inverseOnSurface = Color(0xFF27302B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF91A096),
    outlineVariant = Color(0xFF425047),
    scrim = Color.Black,
    surfaceBright = Color(0xFF37413B),
    surfaceDim = Color(0xFF101615),
    surfaceContainerLowest = Color(0xFF0B100F),
    surfaceContainerLow = Color(0xFF161D1A),
    surfaceContainer = Color(0xFF1A231F),
    surfaceContainerHigh = Color(0xFF24302A),
    surfaceContainerHighest = Color(0xFF2E3A34)
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
