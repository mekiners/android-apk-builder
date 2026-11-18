package com.example.sampleapp.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** 16-bit beveled RPG dialogue box. */
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
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun StardewBanner(title: String, coinBalance: Int, onCoinClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StardewWoodPrimary)
            .border(width = 3.dp, color = StardewWoodDark)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = StardewTypography.titleLarge, color = StardewParchment)
        Row(
            modifier = Modifier
                .background(StardewWoodDark)
                .border(width = 2.dp, color = StardewGold)
                .clickable(onClick = onCoinClick)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(StardewGold)
                    .border(1.dp, StardewWoodHighlight)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "${coinBalance}g", style = StardewTypography.titleMedium, color = StardewGold)
        }
    }
}

@Composable
fun StardewCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .background(if (checked) StardewGreen else StardewParchmentDark)
            .border(width = 2.dp, color = StardewWoodDark)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = StardewParchment,
                modifier = Modifier.size(18.dp)
            )
        }
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
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = StardewTypography.titleSmall, color = textColor)
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
    onLongClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .background(if (selected) selectedColor else StardewParchmentDark)
            .border(2.dp, StardewWoodDark)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = StardewTypography.labelSmall,
            color = if (selected) selectedTextColor else StardewTextPrimary,
            maxLines = 1
        )
    }
}

@Composable
fun GoldBadge(gold: Int) {
    Box(
        modifier = Modifier
            .background(StardewGold)
            .border(1.dp, StardewWoodDark)
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(text = "+${gold}g", style = StardewTypography.labelSmall, color = StardewTextPrimary)
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
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text.uppercase(), style = StardewTypography.labelMedium, color = StardewWoodPrimary, modifier = modifier.padding(top = 4.dp))
}

@Composable
fun StardewTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    onDone: (() -> Unit)? = null
) {
    Box(modifier = modifier.background(StardewParchmentDark).border(2.dp, StardewWoodDark)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = StardewTypography.bodyMedium, color = StardewTextSecondary) },
            textStyle = StardewTypography.bodyMedium,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default
            ),
            keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = StardewTextPrimary,
                unfocusedTextColor = StardewTextPrimary,
                cursorColor = StardewWoodPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
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

/** Bottom wooden navigation dock. items = (emoji, label). */
@Composable
fun StardewDock(items: List<Pair<String, String>>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StardewWoodPrimary)
            .border(3.dp, StardewWoodDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEachIndexed { index, (emoji, label) ->
            val isSel = index == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(if (isSel) StardewWoodHighlight else StardewWoodPrimary)
                    .border(2.dp, if (isSel) StardewGold else StardewWoodPrimary)
                    .clickable { onSelect(index) }
                    .padding(vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = emoji, fontSize = 20.sp)
                Text(
                    text = label,
                    style = StardewTypography.labelSmall,
                    fontSize = 9.sp,
                    color = if (isSel) StardewGold else StardewParchment,
                    maxLines = 1
                )
            }
        }
    }
}
