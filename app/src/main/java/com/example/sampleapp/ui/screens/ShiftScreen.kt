package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
            .background(StardewCanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Next shift
        StardewPanel(modifier = Modifier.fillMaxWidth()) {
            Text(ShiftLogic.headline(next, now), style = StardewTypography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(ShiftLogic.detail(next, now), style = StardewTypography.bodyMedium)
        }

        // Calendar
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StardewChip("\u25C0", false, { month = month.minusMonths(1) })
                Text(
                    month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                    style = StardewTypography.titleMedium
                )
                StardewChip("\u25B6", false, { month = month.plusMonths(1) })
            }
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(it, style = StardewTypography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(4.dp))

            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
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
            Spacer(Modifier.height(6.dp))
            Text(
                "Each day: [Day] [Eve] [Mid]   \u00B7   Day ${monthCounts[0]}  Eve ${monthCounts[1]}  Mid ${monthCounts[2]}",
                style = StardewTypography.bodySmall
            )
        }

        // Brush
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
            Text("Quick fill: pick a brush, then tap days", style = StardewTypography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StardewChip("Select", brush == -1, { brush = -1 })
                ShiftSlots.names.forEachIndexed { i, n ->
                    StardewChip(n, brush == i, { brush = i }, selectedColor = slotColors[i], selectedTextColor = StardewTextPrimary)
                }
                StardewChip("Off", brush == 3, { brush = 3 }, selectedColor = StardewWoodHighlight)
            }
        }

        // Inspector
        val daySlots = slotMap[selected.toEpochDay()] ?: 0
        StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark) {
            Text(
                selected.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.getDefault())),
                style = StardewTypography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            (0..2).forEach { slot ->
                val on = (daySlots and (1 shl slot)) != 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetSlots(selected.toEpochDay(), daySlots xor (1 shl slot)) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StardewCheckbox(on, { onSetSlots(selected.toEpochDay(), daySlots xor (1 shl slot)) })
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.size(14.dp).background(slotColors[slot]).border(1.dp, StardewWoodDark))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(ShiftSlots.longNames[slot], style = StardewTypography.titleSmall)
                        Text(ShiftSlots.windows[slot], style = StardewTypography.bodySmall)
                    }
                }
            }
        }
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
    val borderColor: Color = when {
        isSelected -> StardewTextPrimary
        isToday -> StardewGold
        else -> StardewWoodHighlight
    }
    val borderWidth = if (isSelected) 3.dp else if (isToday) 2.dp else 1.dp
    Column(
        modifier = modifier
            .background(StardewParchment)
            .border(borderWidth, borderColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = StardewTypography.labelSmall,
            color = if (isToday) StardewTerracotta else StardewTextPrimary
        )
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
            (0..2).forEach { slot ->
                val on = (slots and (1 shl slot)) != 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(if (on) slotColors[slot] else StardewParchmentDark)
                        .border(1.dp, StardewWoodDark)
                )
            }
        }
    }
}
