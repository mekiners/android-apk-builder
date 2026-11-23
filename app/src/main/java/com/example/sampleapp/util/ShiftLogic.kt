package com.example.sampleapp.util

import com.example.sampleapp.data.ShiftEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Six 8-hour shift types, start times 4 hours apart. ShiftEntity.slots is a bitmask over these slot ids.
 * A shift is stored on its "roster date" (the date of the calendar cell): Mid belongs to the date it ENDS on
 * (it starts the evening before); every other type belongs to the date it starts on.
 */
object ShiftSlots {
    const val count = 6
    // slot ids: 0 Day, 1 Eve, 2 Mid, 3 Day-Eve, 4 Eve-Mid, 5 Mid-Day
    val names = listOf("Day", "Eve", "Mid", "D-E", "E-M", "M-D")
    val longNames = listOf("Day Shift", "Eve Shift", "Mid Shift", "Day-Eve Shift", "Eve-Mid Shift", "Mid-Day Shift")
    val windows = listOf("07:00 - 15:00", "15:00 - 23:00", "23:00 - 07:00", "11:00 - 19:00", "19:00 - 03:00", "03:00 - 11:00")
    val startHours = listOf(7, 15, 23, 11, 19, 3)
    val startDayOffset = listOf(0, 0, -1, 0, 0, 0)
    val notes = listOf("", "", "starts the evening before", "", "ends the next morning", "")

    /** Order of the three boxes in a calendar cell (a day starts with Mid, then Day, then Eve). */
    val boxOrder = listOf(2, 0, 1)

    /** All six in timeline order, used for lists. */
    val clockOrder = listOf(2, 5, 0, 3, 1, 4)

    fun startOf(rosterDay: Long, slot: Int): LocalDateTime =
        LocalDate.ofEpochDay(rosterDay + startDayOffset[slot]).atTime(startHours[slot], 0)
}

data class ShiftWindow(val slot: Int, val start: LocalDateTime, val end: LocalDateTime, val rosterDay: Long)

object ShiftLogic {
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

    fun windows(shifts: List<ShiftEntity>): List<ShiftWindow> =
        shifts.flatMap { s ->
            (0 until ShiftSlots.count).filter { (s.slots and (1 shl it)) != 0 }.map { slot ->
                val start = ShiftSlots.startOf(s.epochDay, slot)
                ShiftWindow(slot, start, start.plusHours(8), s.epochDay)
            }
        }

    /** The shift happening now, or the next one that starts, or null if nothing is scheduled. */
    fun nextOrCurrent(shifts: List<ShiftEntity>, now: LocalDateTime): ShiftWindow? =
        windows(shifts).filter { it.end.isAfter(now) }.minByOrNull { it.start }

    private fun duration(from: LocalDateTime, to: LocalDateTime): String {
        val d = Duration.between(from, to)
        val days = d.toDays()
        val hours = d.toHours() % 24
        val mins = d.toMinutes() % 60
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${mins}m"
            else -> "${mins}m"
        }
    }

    private fun relativeDay(start: LocalDateTime, now: LocalDateTime): String = when (start.toLocalDate()) {
        now.toLocalDate() -> "today"
        now.toLocalDate().plusDays(1) -> "tomorrow"
        else -> start.format(dayFmt)
    }

    fun headline(w: ShiftWindow?, now: LocalDateTime): String {
        if (w == null) return "No upcoming shifts scheduled"
        val name = ShiftSlots.longNames[w.slot]
        return if (!w.start.isAfter(now)) "On duty: $name" else "Next: $name"
    }

    fun detail(w: ShiftWindow?, now: LocalDateTime): String {
        if (w == null) return "Paint your month in the calendar below."
        return if (!w.start.isAfter(now)) {
            "Ends ${w.end.format(timeFmt)} (in ${duration(now, w.end)})"
        } else {
            "${relativeDay(w.start, now)} ${w.start.format(timeFmt)} - ${w.end.format(timeFmt)} (in ${duration(now, w.start)})"
        }
    }

    fun summary(shifts: List<ShiftEntity>, now: LocalDateTime): String {
        val w = nextOrCurrent(shifts, now)
        return if (w == null) "No upcoming shifts" else headline(w, now) + " \u00B7 " + detail(w, now)
    }
}
