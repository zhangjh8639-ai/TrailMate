package com.trailmate.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TrailMateMoss = Color(0xFF0C5D3F)
val TrailMateMossLight = Color(0xFF1F7A54)
val TrailMateClay = Color(0xFFE36F43)
val TrailMateAmber = Color(0xFFD88A00)
val TrailMateMist = Color(0xFFFBFAF6)
val TrailMateField = Color(0xFFF3F6F1)
val TrailMateLine = Color(0xFFD9DFDB)
val TrailMateInk = Color(0xFF16211D)
val TrailMateMuted = Color(0xFF66716B)

private val LightColors = lightColorScheme(
    primary = TrailMateMoss,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBD5),
    onPrimaryContainer = Color(0xFF052113),
    inversePrimary = Color(0xFF97D8AF),
    secondary = TrailMateClay,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE7B3),
    onSecondaryContainer = Color(0xFF4E2C00),
    tertiary = Color(0xFF2D75E8),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8E2FF),
    onTertiaryContainer = Color(0xFF071A3A),
    background = TrailMateMist,
    onBackground = TrailMateInk,
    surface = Color.White,
    onSurface = TrailMateInk,
    surfaceVariant = TrailMateField,
    onSurfaceVariant = TrailMateMuted,
    surfaceTint = TrailMateMoss,
    inverseSurface = Color(0xFF2C342F),
    inverseOnSurface = Color(0xFFEFF5EF),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF8EA096),
    outlineVariant = TrailMateLine,
    scrim = Color.Black,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD8DED7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8F4),
    surfaceContainer = Color(0xFFF0F4EE),
    surfaceContainerHigh = Color(0xFFE9EFE8),
    surfaceContainerHighest = Color(0xFFE2E9E2)
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
        typography = TrailMateTypography,
        content = content
    )
}

private val TrailMateTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 34.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
)
