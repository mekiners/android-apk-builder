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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.random.Random

// ------------------------------------------------------------------ textures

/** Horizontal wooden planks with seams and a bit of grain. */
fun Modifier.woodPlanks(base: Color = Color(0xFF8A5A32), plankHeight: Dp = 44.dp): Modifier =
    this.background(base).drawBehind {
        val ph = plankHeight.toPx()
        var y = 0f
        var i = 0
        while (y < size.height) {
            if (i % 2 == 1) drawRect(Color(0x16000000), Offset(0f, y), Size(size.width, ph))
            drawRect(Color(0x66000000), Offset(0f, y), Size(size.width, 2.dp.toPx()))
            drawRect(Color(0x26FFFFFF), Offset(0f, y + 2.dp.toPx()), Size(size.width, 1.dp.toPx()))
            val rnd = Random(i * 31 + 5)
            repeat(7) {
                val gx = rnd.nextFloat() * size.width
                val gy = y + 6.dp.toPx() + rnd.nextFloat() * (ph - 12.dp.toPx()).coerceAtLeast(1f)
                drawRect(Color(0x22000000), Offset(gx, gy), Size(rnd.nextInt(14, 44).dp.toPx(), 1.dp.toPx()))
            }
            y += ph
            i++
        }
    }

/** Cork bulletin board with speckles. */
fun Modifier.corkBoard(): Modifier =
    this.background(Color(0xFFC9A063)).drawBehind {
        val rnd = Random(11)
        repeat(260) {
            val x = rnd.nextFloat() * size.width
            val y = rnd.nextFloat() * size.height
            val s = (2 + rnd.nextInt(3)).dp.toPx()
            drawRect(if (rnd.nextBoolean()) Color(0x30000000) else Color(0x28FFFFFF), Offset(x, y), Size(s, s))
        }
    }

// ------------------------------------------------------------------ panels

/** 16-bit beveled RPG dialogue box. */
@Composable
fun StardewPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = StardewParchment,
    contentPadding: Dp = 12.dp,
    innerBorder: Color = StardewWoodHighlight,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(3.dp)
            .border(width = 2.dp, color = innerBorder)
            .padding(contentPadding)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

/** Wooden signboard at the top: parchment plaque with the title plus the gold pouch. */
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
            .woodPlanks(base = StardewWoodPrimary, plankHeight = 28.dp)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(StardewParchment)
                .border(2.dp, StardewWoodDark)
                .padding(2.dp)
                .border(1.dp, StardewWoodHighlight)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = StardewTypography.titleLarge, maxLines = 1)
            if (titleIcon != null) PixelIcon(titleIcon, 24.dp)
        }
        Row(
            modifier = Modifier
                .background(StardewWoodDark)
                .border(width = 2.dp, color = StardewGold)
                .clickable(onClick = onCoinClick)
                .padding(horizontal = 8.dp, vertical = 5.dp),
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
    Box(
        modifier = modifier
            .size(26.dp)
            .background(if (checked) checkedColor else StardewParchmentDark)
            .border(width = 2.dp, color = StardewWoodDark)
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
    val bg = if (enabled) color else StardewTextSecondary
    Box(
        modifier = modifier
            .background(bg)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(2.dp)
            .border(width = 1.dp, color = StardewWoodHighlight)
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
    Box(
        modifier = modifier
            .size(size)
            .background(color)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(2.dp)
            .border(width = 1.dp, color = StardewWoodHighlight)
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
    Row(
        modifier = modifier
            .background(if (selected) selectedColor else StardewParchmentDark)
            .border(2.dp, StardewWoodDark)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
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
    Box(
        modifier = modifier
            .background(StardewParchmentDark)
            .border(1.dp, StardewWoodHighlight)
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(text = text, style = StardewTypography.labelSmall, maxLines = 1)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, sprite: Sprite? = null) {
    Row(
        modifier = modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (sprite != null) PixelIcon(sprite, 18.dp)
        Text(text = text, style = StardewTypography.labelMedium, color = StardewWoodPrimary)
    }
}

/** The sleeping cat from the mockups. */
@Composable
fun RestingCat(message: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PixelIcon(Sprites.cat, 72.dp)
            PixelIcon(Sprites.sprout, 22.dp)
        }
        if (message != null) {
            Text(text = message, style = StardewTypography.bodySmall)
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
            .background(StardewParchmentDark)
            .border(2.dp, StardewWoodDark),
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
            StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .woodPlanks(base = StardewWoodPrimary, plankHeight = 28.dp)
            .border(3.dp, StardewWoodDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items.forEachIndexed { index, (sprite, label) ->
            val isSel = index == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSel) Color(0xFF8A5A32) else Color(0x00000000))
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
                    fontSize = 11.sp,
                    color = if (isSel) StardewGold else StardewParchment,
                    maxLines = 1
                )
            }
        }
    }
}
