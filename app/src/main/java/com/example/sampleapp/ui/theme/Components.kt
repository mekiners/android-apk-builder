package com.example.sampleapp.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StardewPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = StardewParchment,
    contentPadding: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val tex = LocalTextures.current
    var look = LocalPanelLook.current
    if (backgroundColor == StardewParchmentDark) look = look.withInterior(tex.parchmentDark)
    
    // Added drop shadow for modern 3D depth
    PanelBox(
        modifier = modifier.shadow(
            elevation = 6.dp, 
            shape = RoundedCornerShape(12.dp), 
            clip = false
        ), 
        look = look, 
        contentPadding = contentPadding
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}
