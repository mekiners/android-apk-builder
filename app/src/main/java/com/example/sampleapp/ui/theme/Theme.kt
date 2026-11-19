package com.example.sampleapp.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sampleapp.R

// Warm woodland palette from the visual reference.
val StardewCanvasBg = Color(0xFF315B3C)
val StardewParchment = Color(0xFFF0DDB5)
val StardewParchmentDark = Color(0xFFD8BD8E)
val StardewWoodPrimary = Color(0xFF654027)
val StardewWoodDark = Color(0xFF382315)
val StardewWoodHighlight = Color(0xFF9A6635)

val StardewGold = Color(0xFFE7AC32)
val StardewGreen = Color(0xFF47794A)
val StardewTerracotta = Color(0xFFC66B4A)

val StardewTextPrimary = Color(0xFF2C2117)
val StardewTextSecondary = Color(0xFF725A3E)

/** Pixel type is kept for short display headings and game-like labels. */
val PixelFont = FontFamily(
    Font(R.font.pixelify_regular, FontWeight.Normal),
    Font(R.font.pixelify_bold, FontWeight.Bold)
)

/** Clear Android sans-serif for longer copy, numeric values and input fields. */
val ReadableFont = FontFamily.SansSerif

private val StardewColorScheme = lightColorScheme(
    primary = StardewWoodPrimary,
    onPrimary = StardewParchment,
    secondary = StardewGreen,
    onSecondary = StardewParchment,
    tertiary = StardewGold,
    background = StardewCanvasBg,
    onBackground = StardewParchment,
    surface = StardewParchment,
    onSurface = StardewTextPrimary,
    surfaceVariant = StardewParchmentDark,
    onSurfaceVariant = StardewTextSecondary,
    outline = StardewWoodDark
)

/** A deliberate type hierarchy: pixel headings, readable UI copy and numerals. */
val StardewTypography = Typography(
    titleLarge = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StardewTextPrimary, letterSpacing = 0.15.sp),
    titleMedium = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StardewTextPrimary, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StardewTextPrimary, letterSpacing = 0.05.sp),
    bodyLarge = TextStyle(fontFamily = ReadableFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = StardewTextPrimary),
    bodyMedium = TextStyle(fontFamily = ReadableFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = StardewTextPrimary),
    bodySmall = TextStyle(fontFamily = ReadableFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = StardewTextSecondary),
    labelMedium = TextStyle(fontFamily = ReadableFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = StardewTextPrimary),
    labelSmall = TextStyle(fontFamily = ReadableFont, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = StardewTextSecondary)
)

@Composable
fun StardewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StardewColorScheme,
        typography = StardewTypography
    ) {
        val tex = rememberTextures()
        CompositionLocalProvider(
            LocalTextStyle provides StardewTypography.bodyMedium,
            LocalTextures provides tex,
            LocalPanelLook provides tex.woodLook()
        ) {
            content()
        }
    }
}
