package com.example.sampleapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sampleapp.R

// This loads your new NT Brick Sans font
val PixelFont = FontFamily(
    Font(R.font.nt_brick_sans_regular, FontWeight.Normal),
    Font(R.font.nt_brick_sans_regular, FontWeight.Bold) // Fallback if you don't have a bold version
)

val StardewTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp, // Slightly larger for readability
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp // Slight spacing helps pixel fonts
    ),
    bodyMedium = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PixelFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    )
)
