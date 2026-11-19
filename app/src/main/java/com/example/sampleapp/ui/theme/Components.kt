package com.example.sampleapp.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

// ------------------------------------------------------------------ panels

/** Soft, layered card. Pixel textures are reserved for the small illustrations. */
@Suppress("UNUSED_PARAMETER")
@Composable
fun PanelBox(
    modifier: Modifier = Modifier,
    look: PanelLook = LocalPanelLook.current,
    contentPadding: Dp = 12.dp,
    overlay: (DrawScope.(Float, Float, Int) -> Unit)? = null,
    backgroundColor: Color = StardewParchment,
    borderColor: Color = StardewCardBorder,
    accentColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .shadow(3.dp, shape, clip = false)
            .clip(shape)
            .background(backgroundColor)
            .drawBehind {
                if (accentColor != null) {
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset.Zero,
                        size = Size(5.dp.toPx(), size.height),
                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                    )
                }
                overlay?.invoke(this, size.width, size.height, 1)
            }
            .border(1.dp, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}

/** Reusable surface for forms, settings and content sections. */
@Composable
fun StardewPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = StardewParchment,
    contentPadding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val fill = if (backgroundColor == StardewParchmentDark) StardewParchmentDark else StardewParchment
    PanelBox(
        modifier = modifier,
        contentPadding = contentPadding,
        backgroundColor = fill,
        borderColor = if (fill == StardewParchmentDark) Color(0xFFE0E4D9) else StardewCardBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
    }
}

/** Clean forest-green app header with a warm coin accent. */
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
            .background(StardewWoodPrimary)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (titleIcon != null) {
            Box(
                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) { PixelIcon(titleIcon, 22.dp) }
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = StardewTypography.titleLarge,
            color = Color.White,
            maxLines = 1
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFFF3D9))
                .clickable(onClick = onCoinClick)
                .padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            PixelIcon(Sprites.coin, 16.dp)
            Text(text = "${coinBalance}g", style = StardewTypography.labelLarge, color = StardewTextPrimary)
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
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(25.dp)
            .clip(shape)
            .background(if (checked) checkedColor else Color(0xFFF8F8F2))
            .border(1.5.dp, if (checked) checkedColor else Color(0xFFB8C4B6), shape)
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
    textColor: Color = Color.White
) {
    val shape = RoundedCornerShape(13.dp)
    Box(
        modifier = modifier
            .shadow(if (enabled) 2.dp else 0.dp, shape)
            .clip(shape)
            .background(if (enabled) color else Color(0xFFB8BDB5))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = StardewTypography.labelLarge, color = textColor)
    }
}

@Composable
fun StardewIconButton(
    sprite: Sprite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    color: Color = StardewSoftGreen
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(color)
            .border(1.dp, Color(0xFFD8E3D4), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { PixelIcon(sprite, iconSize) }
}

@Composable
fun PixelIconButton(sprite: Sprite, onClick: () -> Unit, modifier: Modifier = Modifier, iconSize: Dp = 18.dp) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFFF0F2EA))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { PixelIcon(sprite, iconSize) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StardewChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = StardewWoodPrimary,
    selectedTextColor: Color = Color.White,
    sprite: Sprite? = null,
    onLongClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(50)
    val fill = if (selected) selectedColor else Color(0xFFF0F2EB)
    val textColor = if (selected) selectedTextColor else StardewTextSecondary
    Row(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(1.dp, if (selected) selectedColor else Color(0xFFE1E6DC), shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
    ) {
        if (sprite != null) PixelIcon(sprite, 14.dp)
        if (text.isNotEmpty()) {
            Text(text = text, style = StardewTypography.labelMedium, color = textColor, maxLines = 1)
        }
    }
}

/** Soft amber reward tag; the coin remains a small pixel-art accent. */
@Composable
fun GoldBadge(gold: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(StardewSoftGold)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PixelIcon(Sprites.coin, 11.dp)
        Text(text = "$gold", style = StardewTypography.labelMedium, color = Color(0xFF8B641F))
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(StardewSoftGreen)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = StardewTypography.labelSmall, color = StardewWoodDark, maxLines = 1)
    }
}

/** Simple section title: less visual weight than another framed box. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, sprite: Sprite? = null) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (sprite != null) PixelIcon(sprite, 16.dp)
        Text(text = text, style = StardewTypography.titleSmall, color = StardewWoodDark)
    }
}

@Composable
fun NoteTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(StardewSoftGreen)
            .border(1.dp, Color(0xFFDCE7D8), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(text = text, style = StardewTypography.bodyMedium, color = StardewTextSecondary)
    }
}

/** The sleeping cat from the original mockups is kept as a playful pixel-art accent. */
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
    val shape = RoundedCornerShape(12.dp)
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
            .clip(shape)
            .background(StardewInputBg)
            .border(1.dp, Color(0xFFDCE3D8), shape),
        decorationBox = { inner ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = StardewTypography.bodyMedium, color = Color(0xFF89948A), maxLines = 1)
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
            StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                Text(text = title, style = StardewTypography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    StardewButton(text = "Cancel", onClick = onDismiss, color = Color(0xFFE9EDE5), textColor = StardewWoodDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    StardewButton(text = confirmText, onClick = onConfirm)
                }
            }
        }
    }
}

/** Bottom navigation with a calm selected state instead of a heavy wooden dock. */
@Composable
fun StardewDock(items: List<Pair<Sprite, String>>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StardewParchment)
            .border(width = 1.dp, color = Color(0xFFE4E8DE))
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items.forEachIndexed { index, (sprite, label) ->
            val isSel = index == selected
            val shape = RoundedCornerShape(13.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(if (isSel) StardewSoftGreen else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PixelIcon(sprite, 20.dp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = StardewTypography.labelSmall,
                    color = if (isSel) StardewWoodDark else StardewTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
