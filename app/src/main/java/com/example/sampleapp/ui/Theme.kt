package com.example.sampleapp.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CozyTheme {
    val Background = Color(0xFFFDF6E3) // Warm cream
    val Surface = Color(0xFFFFFDF5)    // Soft white for cards
    val Primary = Color(0xFF8D6E63)    // Soft brown
    val TextPrimary = Color(0xFF4E342E) // Dark brown text
    val Accent = Color(0xFFE6C1B6)     // Pastel pink
    
    val CornerRadius = 16.dp // Slightly tighter corners
    val Padding = 12.dp      // Tighter padding
}

// NEW: Custom Typography for a compact, comfortable feel
val CozyTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp, // Slightly smaller than default (16sp)
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
)

// A list of preset cozy colors for the user to choose from
val PastelColors = listOf(
    "#FFFDF5", // Cream
    "#FFE5E5", // Soft Pink
    "#E5F0FF", // Soft Blue
    "#E5FFE5", // Soft Green
    "#FFF0E5", // Soft Orange
    "#F0E5FF"  // Soft Purple
)
