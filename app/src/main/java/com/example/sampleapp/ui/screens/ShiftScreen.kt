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
import com.example.sampleapp.data.LeaveDayEntity
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

// slot ids: 0 Day, 1 Eve, 2 Mid, 3 Day-Eve, 4 Eve-Mid, 5 Mid-Day
private val slotColors = listOf(
    StardewGreen, StardewGold, StardewTerracotta,
    Color(0xFF3E9C8F), Color(0xFF8A5FA8), Color(0xFF4F7FB8)
)
private val slotSprites = listOf(Sprites.sun, Sprites.sunset, Sprites.moon, Sprites.sun, Sprites.sunset, Sprites.moon)
private val LEAVE_COLORS = mapOf("Vacation" to Color(0xFF5B8FC9), "Sick" to Color(0xFFE8A0A0))

private fun has(mask: Int, slot: Int): Boolean = (mask and (1 shl slot)) != 0

/**
 * Colours for the left and right halves of one of the three boxes in a calendar cell.
 * position 0 = Mid box, 1 = Day box, 2 = Eve box. A full shift fills both halves; an in-between shift
 * fills one half of two neighbouring boxes (Eve-Mid reaches into the next day's Mid box).
 */
private fun boxHalves(position: Int, slots: Int, prevSlots: Int): Pair<Color?, Color?> = when (position) {
    0 -> if (has(slots, 2)) Pair(slotColors[2], slotColors[2])
    else Pair(if (has(prevSlots, 4)) slotColors[4] else null, if (has(slots, 5)) slotColors[5] else null)
    1 -> if (has(slots, 0)) Pair(slotColors[0], slotColors[0])
    else Pair(if (has(slots, 5)) slotColors[5] else null, if (has(slots, 3)) slotColors[3] else null)
    else -> if (has(slots, 1)) Pair(slotColors[1], slotColors[1])
    else Pair(if (has(slots, 3)) slotColors[3] else null, if (has(slots, 4)) slotColors[4] else null)
}
private val PaperEdge = Color(0xFFC9AA6C)
private val dayLabelFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

@Composable
fun ShiftScreen(
    shifts: List<ShiftEntity>,
    timeLogs: List<TimeLogEntity>,
    leaveDays: List<LeaveDayEntity>,
    overtimeAfter: Double,
    onSetSlots: (Long, Int) -> Unit,
    onSetLeave: (Long, String?) -> Unit,
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
    var brush by remember { mutableStateOf(-1) } // -1 select, 0..5 shift slot, 6 Off, 7 Leave
    var showAddPunch by remember { mutableStateOf(false) }

    val slotMap = remember(shifts) { shifts.associate { it.epochDay to it.slots } }
    val leaveMap = remember(leaveDays) { leaveDays.associate { it.epochDay to it.kind } }
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

    val monthCounts = (0 until ShiftSlots.count).map { slot ->
        (1..length).count { d -> has(slotMap[month.atDay(d).toEpochDay()] ?: 0, slot) }
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
                                    prevSlots = slotMap[date.toEpochDay() - 1] ?: 0,
                                    leaveKind = leaveMap[date.toEpochDay()],
                                    logState = logState[date.toEpochDay()] ?: 0,
                                    isSelected = date == selected,
                                    isToday = date == LocalDate.now(),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selected = date
                                        when (brush) {
                                            in 0..5 -> onSetSlots(date.toEpochDay(), 1 shl brush)
                                            6 -> onSetSlots(date.toEpochDay(), 0)
                                            7 -> onSetLeave(date.toEpochDay(), if (leaveMap[date.toEpochDay()] == null) "Vacation" else null)
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
                    ShiftSlots.clockOrder.forEach { slot ->
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
                ShiftSlots.clockOrder.forEach { i ->
                    StardewChip(
                        text = ShiftSlots.names[i],
                        sprite = slotSprites[i],
                        selected = brush == i,
                        onClick = { brush = i },
                        selectedColor = slotColors[i],
                        selectedTextColor = StardewTextPrimary
                    )
                }
                StardewChip("Off", brush == 6, { brush = 6 }, selectedColor = StardewWoodHighlight)
                StardewChip("Leave", brush == 7, { brush = 7 }, selectedColor = LEAVE_COLORS.getValue("Vacation"))
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
            ShiftSlots.clockOrder.forEach { slot ->
                val on = (daySlots and (1 shl slot)) != 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetSlots(selected.toEpochDay(), daySlots xor (1 shl slot)) }
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StardewCheckbox(on, { onSetSlots(selected.toEpochDay(), daySlots xor (1 shl slot)) }, checkedColor = slotColors[slot])
                    Spacer(Modifier.width(10.dp))
                    PixelIcon(slotSprites[slot], 22.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(ShiftSlots.longNames[slot], style = StardewTypography.bodyLarge)
                        val note = ShiftSlots.notes[slot]
                        Text(
                            ShiftSlots.windows[slot] + if (note.isNotEmpty()) " ($note)" else "",
                            style = StardewTypography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            val dayLeave = leaveMap[selected.toEpochDay()]
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Day off:", style = StardewTypography.bodyMedium)
                listOf("Vacation", "Sick").forEach { kind ->
                    StardewChip(
                        text = kind,
                        selected = dayLeave == kind,
                        onClick = { onSetLeave(selected.toEpochDay(), if (dayLeave == kind) null else kind) },
                        selectedColor = LEAVE_COLORS.getValue(kind),
                        selectedTextColor = StardewTextPrimary
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            SectionLabel("Punches", sprite = Sprites.clock)
            Spacer(Modifier.height(4.dp))
            if (dayLogs.isEmpty()) {
                Text("No time in / out recorded for this day.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
            }
            dayLogs.forEach { log -> LogRow(log, showDate = false, overtimeAfter = overtimeAfter, onDelete = { onDeleteLog(log) }) }
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
            monthLogs.forEach { log -> LogRow(log, showDate = true, overtimeAfter = overtimeAfter, onDelete = { onDeleteLog(log) }) }
        }

        RestingCat("Rest well between shifts!")
    }

    if (showAddPunch) {
        AddPunchDialog(
            day = selected,
            defaultSlot = ShiftSlots.clockOrder.firstOrNull { has(slotMap[selected.toEpochDay()] ?: 0, it) } ?: 0,
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
private fun LogRow(log: TimeLogEntity, showDate: Boolean, overtimeAfter: Double, onDelete: () -> Unit) {
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
            val overtime = out != null && (out - log.inMillis) / 3600000.0 > overtimeAfter
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "In ${TimeClock.hhmm(log.inMillis)}   Out ${if (out != null) TimeClock.hhmm(out) else "--:--"}" +
                        (if (out != null) "   ${TimeClock.durationLabel(log.inMillis, out)}" else "   (still in)"),
                    style = StardewTypography.bodySmall,
                    color = StardewTextPrimary
                )
                if (overtime) Text("OVERTIME", style = StardewTypography.labelSmall, color = StardewTerracotta)
            }
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
            val scheduled = ShiftSlots.startOf(day.toEpochDay(), slot)
            var inDt = scheduled.toLocalDate().atTime(inMin / 60, inMin % 60)
            // A time long before the scheduled start (e.g. 00:10 on a Mid) belongs to the next calendar day.
            if (inDt.isBefore(scheduled.minusHours(6))) inDt = inDt.plusDays(1)
            var outMillis: Long? = null
            val o = outMin
            if (o != null) {
                var end = inDt.toLocalDate().atTime(o / 60, o % 60)
                if (!end.isAfter(inDt)) end = end.plusDays(1)
                outMillis = TimeClock.millisOf(end)
            }
            onSave(slot, TimeClock.millisOf(inDt), outMillis)
            onDismiss()
        }
    ) {
        Text(day.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())), style = StardewTypography.bodyLarge)
        SectionLabel("Shift")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShiftSlots.clockOrder.forEach { i ->
                StardewChip(
                    text = ShiftSlots.names[i],
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
        Text(ShiftSlots.windows[slot], style = StardewTypography.bodySmall, color = StardewTextPrimary)
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
    prevSlots: Int,
    leaveKind: String?,
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
            // three boxes in time order: Mid, Day, Eve
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                (0..2).forEach { position ->
                    val halves = boxHalves(position, slots, prevSlots)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(StardewParchmentDark)
                            .border(1.dp, StardewWoodDark)
                    ) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(halves.first ?: Color.Transparent))
                        Box(Modifier.weight(1f).fillMaxHeight().background(halves.second ?: Color.Transparent))
                    }
                }
            }
        }
        if (logState != 0) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp)) { PunchMarker(logState) }
        }
        if (leaveKind != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(2.dp)
                    .size(10.dp)
                    .background(LEAVE_COLORS[leaveKind] ?: Color.Gray)
                    .border(1.dp, StardewWoodDark)
            )
        }
    }
}
