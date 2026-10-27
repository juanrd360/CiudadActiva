package com.example.ciudadactiva.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Exact Typography scale from Figma (`Sistema/01 Fundamentos`)
// Titulo/28, Titulo/24, Titulo/20, Titulo/16 (Manrope ExtraBold 800)
// Texto/16, Texto/14, Texto/12 Regular (400) & Semibold (600)
// Marca/Eslogan (Inter Semibold 10, tracking 2sp)

val Titulo28 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 28.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
    color = CaNeutralDark
)

val Titulo24 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp,
    color = CaNeutralDark
)

val Titulo20 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
    color = CaNeutralDark
)

val Titulo16 = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    color = CaNeutralDark
)

val Texto16Regular = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    color = CaNeutralDark
)

val Texto16Semibold = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    color = CaNeutralDark
)

val Texto14Regular = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = CaNeutralDark
)

val Texto14Semibold = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = CaNeutralDark
)

val Texto12Regular = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    color = CaMuted
)

val Texto12Semibold = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    color = CaNeutralDark
)

val MarcaEslogan = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 10.sp,
    lineHeight = 16.sp,
    letterSpacing = 2.sp,
    color = CaMuted
)

val Typography = Typography(
    headlineLarge = Titulo28,
    headlineMedium = Titulo24,
    headlineSmall = Titulo20,
    titleMedium = Titulo16,
    bodyLarge = Texto16Regular,
    bodyMedium = Texto14Regular,
    bodySmall = Texto12Regular,
    labelLarge = Texto14Semibold,
    labelMedium = Texto12Semibold,
    labelSmall = MarcaEslogan
)
