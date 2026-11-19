package com.example.sampleapp.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val CardShape = RoundedCornerShape(6.dp)

// ------------------------------------------------------------------ panels

/** Textured card with a fine material rim; decorative detail stays around the content, not over it. */
@Composable
fun PanelBox(
    modifier: Modifier = Modifier,
    look: PanelLook = LocalPanelLook.current,
    contentPadding: Dp = 7.dp,
    overlay: (DrawScope.(Float, Float, Int) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tex = LocalTextures.current
    Box(
        modifier = modifier
            .shadow(3.dp, CardShape, clip = false)
            .clip(CardShape)
            .panelChrome(look, tex, overlay)
            .padding(look.ring + contentPadding),
        content = content
    )
}

@Composable
fun StardewPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = StardewParchment,
    contentPadding: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val tex = LocalTextures.current
    val look = if (backgroundColor == StardewParchmentDark) {
        LocalPanelLook.current.withInterior(tex.parchmentDark)
    } else {
        LocalPanelLook.current
    }
    PanelBox(modifier = modifier, look = look, contentPadding = contentPadding) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
    }
}

/** Parchment title plaque on a shaded forest-wood rail, matching the original game-like header. */
@Composable
fun StardewBanner(
    title: String,
    coinBalance: Int,
    onCoinClick: () -> Unit,
    titleIcon: Sprite? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF563A27), Color(0xFF3B2A20))))
            .drawBehind {
                drawRect(Color(0x557F6040), Offset(0f, size.height - 2.dp.toPx()), Size(size.width, 1.dp.toPx()))
                drawRect(Color(0x40FFF0D1), Offset(0f, 0f), Size(size.width, 1.dp.toPx()))
            }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .shadow(2.dp, RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFF0CF), Color(0xFFE9D4A9))))
                .border(1.dp, Color(0xFF35271E), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = StardewTypography.titleLarge, maxLines = 1)
            if (titleIcon != null) PixelIcon(titleIcon, 22.dp)
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF765034), Color(0xFF513823))))
                .border(1.dp, Color(0xFF2F241C), RoundedCornerShape(6.dp))
                .clickable(onClick = onCoinClick)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            PixelIcon(Sprites.coin, 16.dp)
            Text(text = "${coinBalance}g", style = StardewTypography.labelLarge, color = Color(0xFFFFE19A))
        }
    }
}

// ------------------------------------------------------------------ controls

@Composable
fun StardewCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color = StardewGreen
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (checked) checkedColor else Color(0xFFF4E9D2))
            .border(1.dp, Color(0xFF72583D), RoundedCornerShape(4.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) PixelIcon(Sprites.check, 17.dp)
    }
}

@Composable
fun StardewButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = StardewWoodPrimary,
    textColor: Color = StardewParchment
) {
    val shape = RoundedCornerShape(7.dp)
    val brush = when (color) {
        StardewWoodPrimary -> Brush.verticalGradient(listOf(Color(0xFF906443), StardewWoodPrimary, Color(0xFF5B3B29)))
        StardewGreen -> Brush.verticalGradient(listOf(Color(0xFF8EAA69), StardewGreen, Color(0xFF557548)))
        StardewGold -> Brush.verticalGradient(listOf(Color(0xFFF0C969), StardewGold, Color(0xFFC58B2D)))
        else -> Brush.verticalGradient(listOf(color.copy(alpha = 0.94f), color, color.copy(red = (color.red * 0.82f).coerceIn(0f, 1f), green = (color.green * 0.82f).coerceIn(0f, 1f), blue = (color.blue * 0.82f).coerceIn(0f, 1f))))
    }
    Box(
        modifier = modifier
            .shadow(if (enabled) 2.dp else 0.dp, shape, clip = false)
            .clip(shape)
            .background(if (enabled) brush else Brush.verticalGradient(listOf(Color(0xFFB8AE9E), Color(0xFF968C7E))) )
            .border(1.dp, if (enabled) Color(0xFF493729) else Color(0xFF82786C), shape)
            .drawBehind { drawRect(Color(0x25FFF4D7), Offset(1.dp.toPx(), 1.dp.toPx()), Size(size.width - 2.dp.toPx(), 1.dp.toPx())) }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = StardewTypography.labelLarge, color = if (enabled) textColor else Color(0xFF5E584F), maxLines = 1)
    }
}

/** Rounded pixel-art icon button. The original sprite art is preserved at native pixel edges. */
@Composable
fun StardewIconButton(
    sprite: Sprite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    color: Color = StardewWoodPrimary
) {
    val shape = RoundedCornerShape(7.dp)
    val brush = if (color == StardewWoodPrimary) {
        Brush.verticalGradient(listOf(Color(0xFF906443), StardewWoodPrimary, Color(0xFF5B3B29)))
    } else {
        Brush.verticalGradient(listOf(color.copy(alpha = 0.95f), color, color.copy(alpha = 0.9f)))
    }
    Box(
        modifier = modifier
            .size(size)
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(brush)
            .border(1.dp, Color(0xFF493729), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PixelIcon(sprite, iconSize)
    }
}

@Composable
fun PixelIconButton(sprite: Sprite, onClick: () -> Unit, modifier: Modifier = Modifier, iconSize: Dp = 18.dp) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PixelIcon(sprite, iconSize)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StardewChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = StardewWoodPrimary,
    selectedTextColor: Color = StardewParchment,
    sprite: Sprite? = null,
    onLongClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(7.dp)
    val selectedBrush = when (selectedColor) {
        StardewWoodPrimary -> Brush.verticalGradient(listOf(Color(0xFF8D6542), StardewWoodPrimary))
        StardewGreen -> Brush.verticalGradient(listOf(Color(0xFF8EA96B), StardewGreen))
        StardewGold -> Brush.verticalGradient(listOf(Color(0xFFF2D17A), StardewGold))
        else -> Brush.verticalGradient(listOf(selectedColor.copy(alpha = 0.94f), selectedColor))
    }
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (selected) selectedBrush else Brush.verticalGradient(listOf(Color(0xFFFFF3D8), Color(0xFFE9D8B8))))
            .border(1.dp, if (selected) Color(0xFF513B2A) else Color(0xFFB79B73), shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
    ) {
        if (sprite != null) PixelIcon(sprite, 14.dp)
        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = StardewTypography.labelSmall,
                color = if (selected) selectedTextColor else StardewTextPrimary,
                maxLines = 1
            )
        }
    }
}

/** Compact dark-gold reward token; numerals use the clear reading face with tabular digits. */
@Composable
fun GoldBadge(gold: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF634528), Color(0xFF49311F))))
            .border(1.dp, Color(0xFF8F6837), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PixelIcon(Sprites.coin, 11.dp)
        Text(text = "$gold", style = StardewTypography.labelSmall.copy(color = Color(0xFFFFE09A)), maxLines = 1)
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFFE8D8B8))
            .border(1.dp, Color(0xFFB89A70), RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text = text, style = StardewTypography.labelSmall, maxLines = 1)
    }
}

/** Small timber nameplate for section labels; no repeated nail/rivet decoration. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, sprite: Sprite? = null) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF79573A), Color(0xFF523A29))))
            .border(1.dp, Color(0xFF3B2A20), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (sprite != null) PixelIcon(sprite, 15.dp)
        Text(text = text, style = StardewTypography.labelMedium, color = Color(0xFFFFEAC5))
    }
}

@Composable
fun NoteTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF2D7), Color(0xFFE9D8B7))))
            .border(1.dp, Color(0xFFAD9067), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(text = text, style = StardewTypography.bodyMedium)
    }
}

@Composable
fun RestingCat(message: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PixelIcon(Sprites.cat, 64.dp)
            PixelIcon(Sprites.sprout, 20.dp)
        }
        if (message != null) NoteTag(message)
    }
}

@Composable
fun StardewTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 42.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDone: (() -> Unit)? = null
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = StardewTypography.bodyMedium.copy(color = StardewTextPrimary),
        cursorBrush = SolidColor(StardewWoodPrimary),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFFFFF5DE))
            .border(1.dp, Color(0xFF9C7A50), RoundedCornerShape(7.dp)),
        decorationBox = { inner ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = StardewTypography.bodyMedium, color = StardewTextMuted, maxLines = 1)
                }
                inner()
            }
        }
    )
}

@Composable
fun StardewDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String = "Save",
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
                Text(text = title, style = StardewTypography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    StardewButton(text = "Cancel", onClick = onDismiss, color = StardewWoodHighlight)
                    Spacer(modifier = Modifier.width(8.dp))
                    StardewButton(text = confirmText, onClick = onConfirm)
                }
            }
        }
    }
}

/** Fixed-height bottom navigation with original pixel sprites and a calm selected state. */
@Composable
fun StardewDock(items: List<Pair<Sprite, String>>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF553A29), Color(0xFF35261D))))
            .drawBehind {
                drawRect(Color(0x55D6B27B), Offset(0f, 0f), Size(size.width, 1.dp.toPx()))
            }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items.forEachIndexed { index, (sprite, label) ->
            val isSelected = index == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isSelected) Brush.verticalGradient(listOf(Color(0xFF907047), Color(0xFF65482D))) else Brush.verticalGradient(listOf(Color(0x00000000), Color(0x00000000))))
                    .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color(0xFFD9AD58) else Color.Transparent, RoundedCornerShape(7.dp))
                    .clickable { onSelect(index) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                PixelIcon(sprite, 20.dp)
                Text(
                    text = label,
                    style = StardewTypography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isSelected) Color(0xFFFFE1A0) else Color(0xFFE8D8BE),
                    maxLines = 1
                )
            }
        }
    }
}
