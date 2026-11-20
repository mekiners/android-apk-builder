package com.example.sampleapp.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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

/** Framed, textured box. The look (wood frame, stone frame, quest accents...) comes from LocalPanelLook unless given. */
@Composable
fun PanelBox(
    modifier: Modifier = Modifier,
    look: PanelLook = LocalPanelLook.current,
    contentPadding: Dp = 8.dp,
    overlay: (DrawScope.(Float, Float, Int) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tex = LocalTextures.current
    Box(
        modifier = modifier
            .panelChrome(look, tex, overlay)
            .padding(3.dp + look.ring + contentPadding),
        content = content
    )
}

/** 16-bit RPG dialogue panel: textured frame, parchment interior, rivets. */
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
    PanelBox(modifier = modifier, look = look, contentPadding = contentPadding) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

/** Wooden signboard at the top: framed plaque with the title plus the gold pouch. */
@Composable
fun StardewBanner(
    title: String,
    coinBalance: Int,
    onCoinClick: () -> Unit,
    titleIcon: Sprite? = null,
    modifier: Modifier = Modifier
) {
    val tex = LocalTextures.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .tiledBackground(tex.woodDark, 3.dp)
            .bevel(inset = 0.dp, thickness = 2.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PanelBox(modifier = Modifier.weight(1f), contentPadding = 2.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = StardewTypography.titleLarge, maxLines = 1)
                if (titleIcon != null) PixelIcon(titleIcon, 24.dp)
            }
        }
        Row(
            modifier = Modifier
                .tiledBackground(tex.woodDark, 2.dp)
                .border(3.dp, StardewWoodDark)
                .padding(2.dp)
                .border(2.dp, StardewGold)
                .clickable(onClick = onCoinClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            PixelIcon(Sprites.coin, 16.dp)
            Text(text = "${coinBalance}g", style = StardewTypography.titleSmall, color = StardewGold)
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
    val tex = LocalTextures.current
    Box(
        modifier = modifier
            .size(26.dp)
            .then(if (checked) Modifier.background(checkedColor) else Modifier.tiledBackground(tex.parchmentDark, 2.dp))
            .border(width = 2.dp, color = StardewWoodDark)
            .bevel(inset = 2.dp, thickness = 1.dp)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) PixelIcon(Sprites.check, 18.dp)
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
    val tex = LocalTextures.current
    val fill = if (color == StardewWoodPrimary) Modifier.tiledBackground(tex.woodDark, 2.dp) else Modifier.background(color)
    Box(
        modifier = modifier
            .then(fill)
            .border(width = 3.dp, color = StardewWoodDark)
            .bevel(inset = 3.dp, thickness = 2.dp)
            .then(if (enabled) Modifier else Modifier.background(Color(0x99777777)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = StardewTypography.titleSmall, color = textColor)
    }
}

/** Square beveled button with a pixel icon (mic, plus, arrows). */
@Composable
fun StardewIconButton(
    sprite: Sprite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    color: Color = StardewWoodPrimary
) {
    val tex = LocalTextures.current
    val fill = if (color == StardewWoodPrimary) Modifier.tiledBackground(tex.woodDark, 2.dp) else Modifier.tiledBackground(tex.wood, 2.dp)
    Box(
        modifier = modifier
            .size(size)
            .then(fill)
            .border(width = 3.dp, color = StardewWoodDark)
            .bevel(inset = 3.dp, thickness = 2.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PixelIcon(sprite, iconSize)
    }
}

/** Small flat icon button (edit / delete on cards). */
@Composable
fun PixelIconButton(sprite: Sprite, onClick: () -> Unit, modifier: Modifier = Modifier, iconSize: Dp = 18.dp) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PixelIcon(sprite, iconSize)
    }
}

/** Tiny nail heads in the four corners, like a tag nailed to the board. */
private fun Modifier.nails(): Modifier = drawBehind {
    val n = 2.dp.toPx()
    val m = 3.dp.toPx()
    val c = Color(0x66000000)
    drawRect(c, Offset(m, m), Size(n, n))
    drawRect(c, Offset(size.width - m - n, m), Size(n, n))
    drawRect(c, Offset(m, size.height - m - n), Size(n, n))
    drawRect(c, Offset(size.width - m - n, size.height - m - n), Size(n, n))
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
    val tex = LocalTextures.current
    val fill = when {
        selected && selectedColor == StardewWoodPrimary -> Modifier.tiledBackground(tex.woodDark, 2.dp)
        selected -> Modifier.background(selectedColor)
        else -> Modifier.tiledBackground(tex.parchmentDark, 2.dp)
    }
    Row(
        modifier = modifier
            .then(fill)
            .border(2.dp, StardewWoodDark)
            .nails()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        if (sprite != null) PixelIcon(sprite, 15.dp)
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

/** Coin icon with the gold amount on a dark wood tag. */
@Composable
fun GoldBadge(gold: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(StardewWoodDark)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        PixelIcon(Sprites.coin, 11.dp)
        Text(text = "$gold", style = StardewTypography.labelSmall, color = StardewGold)
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    val tex = LocalTextures.current
    Box(
        modifier = modifier
            .tiledBackground(tex.parchmentDark, 2.dp)
            .border(1.dp, StardewWoodHighlight)
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(text = text, style = StardewTypography.labelSmall, maxLines = 1)
    }
}

/** A little wooden sign: used for section headings so they read on any background. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, sprite: Sprite? = null) {
    val tex = LocalTextures.current
    Row(
        modifier = modifier
            .tiledBackground(tex.woodDark, 2.dp)
            .border(2.dp, StardewWoodDark)
            .nails()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (sprite != null) PixelIcon(sprite, 16.dp)
        Text(text = text, style = StardewTypography.labelMedium, color = StardewParchment)
    }
}

/** A short note on a parchment tag, for messages that sit directly on a textured background. */
@Composable
fun NoteTag(text: String, modifier: Modifier = Modifier) {
    val tex = LocalTextures.current
    Box(
        modifier = modifier
            .tiledBackground(tex.parchment, 2.dp)
            .border(2.dp, StardewWoodDark)
            .nails()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, style = StardewTypography.bodyMedium)
    }
}

/** The sleeping cat from the mockups. */
@Composable
fun RestingCat(message: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PixelIcon(Sprites.cat, 72.dp)
            PixelIcon(Sprites.sprout, 22.dp)
        }
        if (message != null) {
            NoteTag(message)
        }
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
    val tex = LocalTextures.current
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
            .tiledBackground(tex.parchmentDark, 2.dp)
            .border(2.dp, StardewWoodDark)
            .drawBehind {
                drawRect(Color(0x30000000), Offset(2.dp.toPx(), 2.dp.toPx()), Size(size.width - 4.dp.toPx(), 2.dp.toPx()))
            },
        decorationBox = { inner ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = StardewTypography.bodyMedium, color = StardewTextSecondary, maxLines = 1)
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
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
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

/** Bottom wooden dock. items = (sprite, label). */
@Composable
fun StardewDock(items: List<Pair<Sprite, String>>, selected: Int, onSelect: (Int) -> Unit) {
    val tex = LocalTextures.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tiledBackground(tex.woodDark, 3.dp)
            .bevel(inset = 0.dp, thickness = 2.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items.forEachIndexed { index, (sprite, label) ->
            val isSel = index == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(if (isSel) Modifier.tiledBackground(tex.wood, 3.dp) else Modifier)
                    .border(2.dp, if (isSel) StardewGold else Color(0x00000000))
                    .clickable { onSelect(index) }
                    .padding(vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PixelIcon(sprite, 22.dp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = StardewTypography.labelSmall,
                    fontSize = 14.sp,
                    color = if (isSel) StardewGold else StardewParchment,
                    maxLines = 1
                )
            }
        }
    }
}
