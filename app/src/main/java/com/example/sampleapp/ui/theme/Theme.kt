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

val StardewCanvasBg = Color(0xFFEED9B7)
val StardewParchment = Color(0xFFF7E6C4)
val StardewParchmentDark = Color(0xFFE4CFA9)
val StardewWoodPrimary = Color(0xFF6B4423)
val StardewWoodDark = Color(0xFF3B240E)
val StardewWoodHighlight = Color(0xFF9C6B3E)

val StardewGold = Color(0xFFE5A93B)
val StardewGreen = Color(0xFF5B8C3E)
val StardewTerracotta = Color(0xFFC86D51)

val StardewTextPrimary = Color(0xFF2C1E11)
val StardewTextSecondary = Color(0xFF6E553F)

/** Jersey 15: clear pixel font for body text and numbers (a 5 never looks like an S). */
val JerseyFont = FontFamily(Font(R.font.jersey15_regular, FontWeight.Normal))

/** Pixelify Sans: used for big titles only. */
val PixelFont = FontFamily(
    Font(R.font.pixelify_regular, FontWeight.Normal),
    Font(R.font.pixelify_bold, FontWeight.Bold)
)

private val StardewColorScheme = lightColorScheme(
    primary = StardewWoodPrimary,
    onPrimary = StardewParchment,
    secondary = StardewGreen,
    onSecondary = StardewParchment,
    tertiary = StardewGold,
    background = StardewCanvasBg,
    onBackground = StardewTextPrimary,
    surface = StardewParchment,
    onSurface = StardewTextPrimary,
    surfaceVariant = StardewParchmentDark,
    onSurfaceVariant = StardewTextSecondary,
    outline = StardewWoodDark
)

val StardewTypography = Typography(
    titleLarge = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = StardewTextPrimary),
    titleMedium = TextStyle(fontFamily = PixelFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StardewTextPrimary),
    titleSmall = TextStyle(fontFamily = JerseyFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StardewTextPrimary),
    bodyLarge = TextStyle(fontFamily = JerseyFont, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = StardewTextPrimary),
    bodyMedium = TextStyle(fontFamily = JerseyFont, fontWeight = FontWeight.Normal, fontSize = 18.sp, color = StardewTextPrimary),
    bodySmall = TextStyle(fontFamily = JerseyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, color = StardewTextSecondary),
    labelMedium = TextStyle(fontFamily = JerseyFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = StardewTextPrimary),
    labelSmall = TextStyle(fontFamily = JerseyFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StardewTextSecondary)
)

@Composable
fun StardewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StardewColorScheme,
        typography = StardewTypography
    ) {
        // Anything without an explicit style still gets the pixel font.
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
