package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.ShiftEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.ShiftLogic
import com.example.sampleapp.util.ShiftSlots
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val slotColors = listOf(StardewGreen, StardewGold, StardewTerracotta)
private val slotSprites = listOf(Sprites.sun, Sprites.sunset, Sprites.moon)

@Composable
fun ShiftScreen(
    shifts: List<ShiftEntity>,
    onSetSlots: (Long, Int) -> Unit
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }

    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var brush by remember { mutableStateOf(-1) } // -1 select, 0 Day, 1 Eve, 2 Mid, 3 Off

    val slotMap = remember(shifts) { shifts.associate { it.epochDay to it.slots } }
    val next = remember(shifts, now) { ShiftLogic.nextOrCurrent(shifts, now) }

    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1 // Monday = 0
    val length = month.lengthOfMonth()
    val rows = (offset + length + 6) / 7

    val monthCounts = (0..2).map { slot ->
        (1..length).count { d -> ((slotMap[month.atDay(d).toEpochDay()] ?: 0) and (1 shl slot)) != 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Next shift
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 10.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelIcon(if (next != null) slotSprites[next.slot] else Sprites.calendar, 32.dp)
                Column {
                    Text(ShiftLogic.headline(next, now), style = StardewTypography.titleMedium)
                    Text(ShiftLogic.detail(next, now), style = StardewTypography.bodySmall, color = StardewTextPrimary)
                }
            }
        }

        // Calendar surface: readable month header, spacious day cells and light borders.
        PanelBox(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
            backgroundColor = StardewParchment,
            borderColor = StardewCardBorder
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StardewWoodPrimary)
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StardewIconButton(Sprites.left, { month = month.minusMonths(1) }, size = 34.dp, iconSize = 18.dp, color = Color(0xFF63836C))
                    Text(
                        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = StardewTypography.titleMedium,
                        color = Color.White
                    )
                    StardewIconButton(Sprites.right, { month = month.plusMonths(1) }, size = 34.dp, iconSize = 18.dp, color = Color(0xFF63836C))
                }

                Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                            Text(
                                it,
                                style = StardewTypography.labelSmall,
                                color = StardewTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(5.dp))

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0..6) {
                                val dayNum = r * 7 + c - offset + 1
                                if (dayNum in 1..length) {
                                    val date = month.atDay(dayNum)
                                    DayCell(
                                        date = date,
                                        slots = slotMap[date.toEpochDay()] ?: 0,
                                        isSelected = date == selected,
                                        isToday = date == LocalDate.now(),
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selected = date
                                            when (brush) {
                                                in 0..2 -> onSetSlots(date.toEpochDay(), 1 shl brush)
                                                3 -> onSetSlots(date.toEpochDay(), 0)
                                                else -> Unit
                                            }
                                        }
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        (0..2).forEach { slot ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(10.dp).background(slotColors[slot], RoundedCornerShape(3.dp)))
                                Text("${ShiftSlots.names[slot]} ${monthCounts[slot]}", style = StardewTypography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Brush
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
            Text("Quick fill: pick a brush, then tap days", style = StardewTypography.bodySmall, color = StardewTextPrimary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                StardewChip("Select", brush == -1, { brush = -1 })
                ShiftSlots.names.forEachIndexed { i, n ->
                    StardewChip(
                        text = n,
                        sprite = slotSprites[i],
                        selected = brush == i,
                        onClick = { brush = i },
                        selectedColor = slotColors[i],
                        selectedTextColor = StardewTextPrimary
                    )
                }
                StardewChip("Off", brush == 3, { brush = 3 }, selectedColor = StardewWoodHighlight)
            }
        }

        // Inspector note
        val daySlots = slotMap[selected.toEpochDay()] ?: 0
        StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark) {
            Text(
                selected.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault())),
                style = StardewTypography.titleSmall
            )
            Spacer(Modifier.height(6.dp))
            (0..2).forEach { slot ->
                val on = (daySlots and (1 shl slot)) != 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetSlots(selected.toEpochDay(), daySlots xor (1 shl slot)) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StardewCheckbox(on, { onSetSlots(selected.toEpochDay(), daySlots xor (1 shl slot)) }, checkedColor = slotColors[slot])
                    Spacer(Modifier.width(10.dp))
                    PixelIcon(slotSprites[slot], 24.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(ShiftSlots.longNames[slot], style = StardewTypography.bodyLarge)
                        Text(ShiftSlots.windows[slot], style = StardewTypography.bodySmall)
                    }
                }
            }
        }

        RestingCat("Rest well between shifts!")
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    slots: Int,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(9.dp)
    val borderColor = when {
        isSelected -> StardewWoodPrimary
        isToday -> StardewGold
        else -> Color(0xFFE5E9E1)
    }
    val fillColor = when {
        isSelected -> StardewSoftGreen
        else -> Color(0xFFFCFCF8)
    }
    Column(
        modifier = modifier
            .clip(shape)
            .background(fillColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = StardewTypography.labelSmall,
            color = if (isToday) StardewTerracotta else StardewTextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
            (0..2).forEach { slot ->
                val on = (slots and (1 shl slot)) != 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(if (on) slotColors[slot] else Color(0xFFE8ECE4), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
