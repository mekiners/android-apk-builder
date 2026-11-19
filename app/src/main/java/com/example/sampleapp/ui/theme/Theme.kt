package com.example.sampleapp.ui.theme

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

// Warm, botanical palette: the pixel-art identity stays in the illustrations,
// while surfaces and text use a cleaner, more modern treatment.
val StardewCanvasBg = Color(0xFFF5F3EA)
val StardewParchment = Color(0xFFFFFDF8)
val StardewParchmentDark = Color(0xFFEEF0E7)
val StardewWoodPrimary = Color(0xFF42664F)       // deep sage / forest
val StardewWoodDark = Color(0xFF2F4938)
val StardewWoodHighlight = Color(0xFF789276)

val StardewGold = Color(0xFFE4B45D)
val StardewGreen = Color(0xFF6F916B)
val StardewTerracotta = Color(0xFFC77A62)

val StardewTextPrimary = Color(0xFF29362E)
val StardewTextSecondary = Color(0xFF68756B)
val StardewCardBorder = Color(0xFFDDE4D8)
val StardewSoftGreen = Color(0xFFEAF1E7)
val StardewSoftGold = Color(0xFFFFF4DE)
val StardewSoftTerracotta = Color(0xFFF8ECE7)
val StardewInputBg = Color(0xFFFAFAF5)

/** Kept for small decorative labels where a pixel accent is useful. */
val PixelFont = FontFamily(
    Font(R.font.pixelify_regular, FontWeight.Normal),
    Font(R.font.pixelify_bold, FontWeight.Bold)
)

private val StardewColorScheme = lightColorScheme(
    primary = StardewWoodPrimary,
    onPrimary = Color.White,
    secondary = StardewGreen,
    onSecondary = Color.White,
    tertiary = StardewGold,
    onTertiary = StardewTextPrimary,
    background = StardewCanvasBg,
    onBackground = StardewTextPrimary,
    surface = StardewParchment,
    onSurface = StardewTextPrimary,
    surfaceVariant = StardewParchmentDark,
    onSurfaceVariant = StardewTextSecondary,
    outline = StardewCardBorder
)

// System sans-serif (Roboto on most Android devices) is intentionally used
// for all working text and numbers. Tabular numerals keep counts aligned.
val StardewTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = StardewTextPrimary),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = StardewTextPrimary),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = StardewTextPrimary),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = StardewTextPrimary),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, color = StardewTextPrimary),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = StardewTextPrimary),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = StardewTextPrimary),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = StardewTextPrimary),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = StardewTextPrimary),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, color = StardewTextSecondary, fontFeatureSettings = "tnum"),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = StardewTextSecondary, fontFeatureSettings = "tnum")
)

@Composable
fun StardewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StardewColorScheme,
        typography = StardewTypography
    ) {
        val tex = rememberTextures()
        CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides StardewTypography.bodyMedium,
            LocalTextures provides tex,
            LocalPanelLook provides tex.woodLook()
        ) {
            content()
        }
    }
}
