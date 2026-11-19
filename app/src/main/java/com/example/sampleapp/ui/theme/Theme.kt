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

// Forest-cottage palette: warm paper, shaded greens, toasted wood and controlled honey accents.
val StardewCanvasBg = Color(0xFF6F8759)
val StardewParchment = Color(0xFFF2E5C9)
val StardewParchmentDark = Color(0xFFE5D3AE)
val StardewWoodPrimary = Color(0xFF765036)
val StardewWoodDark = Color(0xFF3A2C22)
val StardewWoodHighlight = Color(0xFF9A704A)

val StardewGold = Color(0xFFE3AD43)
val StardewGreen = Color(0xFF6F9258)
val StardewTerracotta = Color(0xFFC87555)
val StardewMoss = Color(0xFF496D43)
val StardewCream = Color(0xFFFFF3D8)

val StardewTextPrimary = Color(0xFF382F25)
val StardewTextSecondary = Color(0xFF76634D)
val StardewTextMuted = Color(0xFF8C795F)

/** Pixelify is retained for display headings only; long copy and numbers use the system sans-serif. */
val PixelFont = FontFamily(
    Font(R.font.pixelify_regular, FontWeight.Normal),
    Font(R.font.pixelify_bold, FontWeight.Bold)
)
val ReadingFont = FontFamily.SansSerif

private val StardewColorScheme = lightColorScheme(
    primary = StardewWoodPrimary,
    onPrimary = StardewCream,
    secondary = StardewGreen,
    onSecondary = Color(0xFF1F321F),
    tertiary = StardewGold,
    background = StardewCanvasBg,
    onBackground = StardewTextPrimary,
    surface = StardewParchment,
    onSurface = StardewTextPrimary,
    surfaceVariant = StardewParchmentDark,
    onSurfaceVariant = StardewTextSecondary,
    outline = Color(0xFF816347)
)

val StardewTypography = Typography(
    // The display face is deliberately limited to short headings and page titles.
    titleLarge = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 26.sp, color = StardewTextPrimary),
    titleMedium = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 23.sp, color = StardewTextPrimary),
    titleSmall = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 19.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    bodyLarge = TextStyle(fontFamily = ReadingFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    bodyMedium = TextStyle(fontFamily = ReadingFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 19.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    bodySmall = TextStyle(fontFamily = ReadingFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, color = StardewTextSecondary, fontFeatureSettings = "tnum"),
    labelLarge = TextStyle(fontFamily = ReadingFont, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    labelMedium = TextStyle(fontFamily = ReadingFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 15.sp, color = StardewTextPrimary, fontFeatureSettings = "tnum"),
    labelSmall = TextStyle(fontFamily = ReadingFont, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, color = StardewTextSecondary, fontFeatureSettings = "tnum")
)

@Composable
fun StardewTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = StardewColorScheme, typography = StardewTypography) {
        val tex = rememberTextures()
        CompositionLocalProvider(
            androidx.compose.material3.LocalTextStyle provides StardewTypography.bodyMedium,
            LocalTextures provides tex,
            LocalPanelLook provides tex.forestLook()
        ) {
            content()
        }
    }
}
