package com.example.ciudadactiva.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CiudadActivaColorScheme = lightColorScheme(
    primary = CaPrimary,
    onPrimary = CaNeutralDark,
    secondary = CaSecondary,
    onSecondary = CaWhite,
    tertiary = CaAccent,
    onTertiary = CaNeutralDark,
    background = CaNeutralLight,
    onBackground = CaNeutralDark,
    surface = CaWhite,
    onSurface = CaNeutralDark,
    surfaceVariant = CaNeutralLight,
    onSurfaceVariant = CaMuted,
    outline = CaBorder,
    error = CaSecondary,
    onError = CaWhite
)

@Composable
fun CiudadActivaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CiudadActivaColorScheme,
        typography = Typography,
        content = content
    )
}
