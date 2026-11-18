package com.example.sampleapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StardewPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = StardewParchment,
    contentPadding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(3.dp)
            .border(width = 2.dp, color = StardewWoodHighlight)
            .padding(contentPadding)
    ) {
        Column(content = content)
    }
}

@Composable
fun StardewWoodenBanner(
    title: String,
    coinBalance: Int,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StardewWoodPrimary)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = StardewParchment)
            }
            Text(text = title, style = StardewTypography.titleLarge, color = StardewParchment)
        }

        Row(
            modifier = Modifier
                .background(StardewWoodDark)
                .border(width = 2.dp, color = StardewGold)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(12.dp).background(StardewGold).border(1.dp, StardewWoodHighlight))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "${coinBalance}g", style = StardewTypography.titleMedium, color = StardewGold)
        }
    }
}

@Composable
fun StardewCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .background(if (checked) StardewGreen else StardewParchmentDark)
            .border(width = 2.dp, color = StardewWoodDark)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = "Completed", tint = StardewParchment, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun StardewButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val bg = if (enabled) StardewWoodPrimary else StardewTextSecondary
    Box(
        modifier = modifier
            .background(bg)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(2.dp)
            .border(width = 1.dp, color = StardewWoodHighlight)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = StardewTypography.titleMedium, color = StardewParchment, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
