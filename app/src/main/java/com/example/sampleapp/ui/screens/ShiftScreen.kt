package com.example.sampleapp.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.ShiftEntity
import com.example.sampleapp.data.TimeLogEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.ShiftLogic
import com.example.sampleapp.util.ShiftSlots
import com.example.sampleapp.util.TimeClock
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val slotColors = listOf(StardewGreen, StardewGold, StardewTerracotta)
private val slotSprites = listOf(Sprites.sun, Sprites.sunset, Sprites.moon)
private val PaperEdge = Color(0xFFC9AA6C)
private val dayLabelFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

@Composable
fun ShiftScreen(
    shifts: List<ShiftEntity>,
    timeLogs: List<TimeLogEntity>,
    onSetSlots: (Long, Int) -> Unit,
    onPunch: () -> Unit,
    onAddLog: (Long, Int, Long, Long?) -> Unit,
    onDeleteLog: (TimeLogEntity) -> Unit
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
    var showAddPunch by remember { mutableStateOf(false) }

    val slotMap = remember(shifts) { shifts.associate { it.epochDay to it.slots } }
    val next = remember(shifts, now) { ShiftLogic.nextOrCurrent(shifts, now) }
    val logState = remember(timeLogs) {
        timeLogs.groupBy { it.shiftDay }.mapValues { (_, l) -> if (l.any { it.outMillis == null }) 1 else 2 }
    }
    val open = timeLogs.firstOrNull { it.outMillis == null }
    val nowMillis = TimeClock.millisOf(now)

    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1 // Monday = 0
    val length = month.lengthOfMonth()
    val rows = (offset + length + 6) / 7

    val monthCounts = (0..2).map { slot ->
        (1..length).count { d -> ((slotMap[month.atDay(d).toEpochDay()] ?: 0) and (1 shl slot)) != 0 }
    }
    val monthLogs = remember(timeLogs, month) {
        timeLogs.filter { YearMonth.from(LocalDate.ofEpochDay(it.shiftDay)) == month }.sortedByDescending { it.inMillis }
    }
    val monthMinutes = monthLogs.sumOf { l -> l.outMillis?.let { (it - l.inMillis) / 60000L } ?: 0L }

    val tex = LocalTextures.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Next shift
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelIcon(if (next != null) slotSprites[next.slot] else Sprites.calendar, 32.dp)
                Column {
                    Text(ShiftLogic.headline(next, now), style = StardewTypography.titleMedium)
                    Text(ShiftLogic.detail(next, now), style = StardewTypography.bodySmall, color = StardewTextPrimary)
                }
            }
        }

        // Time clock
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelIcon(Sprites.clock, 30.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (open != null) "On the clock" else "Off the clock", style = StardewTypography.titleSmall)
                    val todayLog = timeLogs.firstOrNull { TimeClock.toDateTime(it.inMillis).toLocalDate() == now.toLocalDate() }
                    val todayOut = todayLog?.outMillis
                    val line = if (open != null) {
                        "In at ${TimeClock.hhmm(open.inMillis)} (${TimeClock.durationLabel(open.inMillis, nowMillis)})"
                    } else if (todayLog != null && todayOut != null) {
                        "Today: in ${TimeClock.hhmm(todayLog.inMillis)}, out ${TimeClock.hhmm(todayOut)}"
                    } else {
                        "No punches today"
                    }
                    Text(line, style = StardewTypography.bodySmall, color = StardewTextPrimary)
                }
                StardewButton(
                    text = if (open != null) "TIME OUT" else "TIME IN",
                    onClick = onPunch,
                    color = if (open != null) StardewTerracotta else StardewGreen
                )
            }
        }

        // Wall calendar: wooden header plank + paper body
        Column(modifier = Modifier.fillMaxWidth().border(3.dp, StardewWoodDark)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tiledBackground(tex.wood, 3.dp)
                    .bevel(inset = 0.dp, thickness = 2.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StardewIconButton(Sprites.left, { month = month.minusMonths(1) }, size = 34.dp, iconSize = 18.dp, color = StardewWoodHighlight)
                Box(
                    modifier = Modifier
                        .tiledBackground(tex.parchment, 2.dp)
                        .border(2.dp, StardewWoodDark)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = StardewTypography.titleMedium
                    )
                }
                StardewIconButton(Sprites.right, { month = month.plusMonths(1) }, size = 34.dp, iconSize = 18.dp, color = StardewWoodHighlight)
            }

            Column(modifier = Modifier.fillMaxWidth().tiledBackground(tex.parchment, 3.dp).padding(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                        Text(
                            it,
                            style = StardewTypography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (c in 0..6) {
                            val dayNum = r * 7 + c - offset + 1
                            if (dayNum in 1..length) {
                                val date = month.atDay(dayNum)
                                DayCell(
                                    date = date,
                                    slots = slotMap[date.toEpochDay()] ?: 0,
                                    logState = logState[date.toEpochDay()] ?: 0,
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
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (0..2).forEach { slot ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(12.dp).background(slotColors[slot]).border(1.dp, StardewWoodDark))
                            Text("${ShiftSlots.names[slot]} ${monthCounts[slot]}", style = StardewTypography.labelSmall)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PunchMarker(2)
                        Text("timed in/out", style = StardewTypography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PunchMarker(1)
                        Text("still in", style = StardewTypography.labelSmall)
                    }
                }
            }
        }

        // Brush
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            Text("Quick fill: pick a brush, then tap days", style = StardewTypography.bodySmall, color = StardewTextPrimary)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
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

        // Inspector note: shifts of the selected day plus its punches
        val daySlots = slotMap[selected.toEpochDay()] ?: 0
        val dayLogs = timeLogs.filter { it.shiftDay == selected.toEpochDay() }.sortedBy { it.inMillis }
        StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark, contentPadding = 4.dp) {
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

            Spacer(Modifier.height(6.dp))
            SectionLabel("Punches", sprite = Sprites.clock)
            Spacer(Modifier.height(4.dp))
            if (dayLogs.isEmpty()) {
                Text("No time in / out recorded for this day.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
            }
            dayLogs.forEach { log -> LogRow(log, showDate = false, onDelete = { onDeleteLog(log) }) }
            Spacer(Modifier.height(6.dp))
            StardewChip("Add punch", false, { showAddPunch = true }, sprite = Sprites.plusDark)
        }

        // Time log compilation for the month on the calendar
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelIcon(Sprites.scroll, 24.dp)
                Column {
                    Text("Time Log", style = StardewTypography.titleMedium)
                    Text(
                        "${month.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))}: ${monthLogs.size} punches, " +
                            "${monthMinutes / 60}h ${monthMinutes % 60}m total",
                        style = StardewTypography.bodySmall,
                        color = StardewTextPrimary
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            if (monthLogs.isEmpty()) {
                Text("Nothing logged this month yet.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
            }
            monthLogs.forEach { log -> LogRow(log, showDate = true, onDelete = { onDeleteLog(log) }) }
        }

        RestingCat("Rest well between shifts!")
    }

    if (showAddPunch) {
        AddPunchDialog(
            day = selected,
            defaultSlot = (0..2).firstOrNull { ((slotMap[selected.toEpochDay()] ?: 0) and (1 shl it)) != 0 } ?: 0,
            onSave = { slot, inMillis, outMillis -> onAddLog(selected.toEpochDay(), slot, inMillis, outMillis) },
            onDismiss = { showAddPunch = false }
        )
    }
}

@Composable
private fun PunchMarker(state: Int) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(if (state == 2) StardewGreen else StardewGold)
            .border(1.dp, StardewWoodDark),
        contentAlignment = Alignment.Center
    ) {
        if (state == 2) PixelIcon(Sprites.check, 9.dp)
    }
}

@Composable
private fun LogRow(log: TimeLogEntity, showDate: Boolean, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PixelIcon(if (log.slot >= 0) slotSprites[log.slot] else Sprites.clock, 20.dp)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            val title = if (showDate) LocalDate.ofEpochDay(log.shiftDay).format(dayLabelFmt) else
                (if (log.slot >= 0) ShiftSlots.longNames[log.slot] else "Unscheduled")
            Text(title, style = StardewTypography.bodyLarge)
            val out = log.outMillis
            Text(
                "In ${TimeClock.hhmm(log.inMillis)}   Out ${if (out != null) TimeClock.hhmm(out) else "--:--"}" +
                    (if (out != null) "   ${TimeClock.durationLabel(log.inMillis, out)}" else "   (still in)"),
                style = StardewTypography.bodySmall,
                color = StardewTextPrimary
            )
        }
        PixelIconButton(Sprites.trash, onDelete)
    }
}

@Composable
private fun AddPunchDialog(
    day: LocalDate,
    defaultSlot: Int,
    onSave: (Int, Long, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var slot by remember { mutableStateOf(defaultSlot) }
    var inMin by remember { mutableStateOf(ShiftSlots.startHours[defaultSlot] * 60) }
    var outMin by remember { mutableStateOf<Int?>((ShiftSlots.startHours[defaultSlot] * 60 + 480) % 1440) }

    fun fmt(m: Int) = String.format(Locale.getDefault(), "%02d:%02d", m / 60, m % 60)
    fun pick(initial: Int, done: (Int) -> Unit) {
        TimePickerDialog(context, { _, h, min -> done(h * 60 + min) }, initial / 60, initial % 60, true).show()
    }

    StardewDialog(
        title = "Add punch",
        confirmText = "Save",
        onDismiss = onDismiss,
        onConfirm = {
            val start = day.atTime(inMin / 60, inMin % 60)
            var outMillis: Long? = null
            val o = outMin
            if (o != null) {
                var end = day.atTime(o / 60, o % 60)
                if (!end.isAfter(start)) end = end.plusDays(1) // e.g. a Mid shift ending the next morning
                outMillis = TimeClock.millisOf(end)
            }
            onSave(slot, TimeClock.millisOf(start), outMillis)
            onDismiss()
        }
    ) {
        Text(day.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())), style = StardewTypography.bodyLarge)
        SectionLabel("Shift")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ShiftSlots.names.forEachIndexed { i, n ->
                StardewChip(
                    text = n,
                    sprite = slotSprites[i],
                    selected = slot == i,
                    onClick = {
                        slot = i
                        inMin = ShiftSlots.startHours[i] * 60
                        outMin = (ShiftSlots.startHours[i] * 60 + 480) % 1440
                    },
                    selectedColor = slotColors[i],
                    selectedTextColor = StardewTextPrimary
                )
            }
        }
        SectionLabel("Time")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            StardewChip("In ${fmt(inMin)}", true, { pick(inMin) { inMin = it } }, sprite = Sprites.clock)
            val o = outMin
            StardewChip(
                text = if (o != null) "Out ${fmt(o)}" else "Out --:--",
                selected = o != null,
                onClick = { pick(o ?: ((inMin + 480) % 1440)) { outMin = it } },
                sprite = Sprites.clock
            )
        }
        if (outMin != null) StardewChip("Still timed in (no out)", false, { outMin = null })
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    slots: Int,
    logState: Int,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val borderColor: Color = when {
        isSelected -> StardewTextPrimary
        isToday -> StardewGold
        else -> PaperEdge
    }
    val borderWidth = if (isSelected) 3.dp else if (isToday) 2.dp else 1.dp
    Box(
        modifier = modifier
            .tiledBackground(LocalTextures.current.parchment, 2.dp)
            .border(borderWidth, borderColor)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 3.dp),
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
        if (logState != 0) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp)) { PunchMarker(logState) }
        }
    }
}
