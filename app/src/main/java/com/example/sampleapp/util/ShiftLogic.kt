package com.example.sampleapp.util

import com.example.sampleapp.data.ShiftEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** ATC rotation: Day 07-15, Eve 15-23, Mid 23-07 (ends next morning). */
object ShiftSlots {
    val names = listOf("Day", "Eve", "Mid")
    val longNames = listOf("Day Shift", "Eve Shift", "Mid Shift")
    val windows = listOf("07:00 - 15:00", "15:00 - 23:00", "23:00 - 07:00")
    val startHours = listOf(7, 15, 23)
}

data class ShiftWindow(val slot: Int, val start: LocalDateTime, val end: LocalDateTime)

object ShiftLogic {
    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

    fun windows(shifts: List<ShiftEntity>): List<ShiftWindow> =
        shifts.flatMap { s ->
            (0..2).filter { (s.slots and (1 shl it)) != 0 }.map { slot ->
                val start = LocalDate.ofEpochDay(s.epochDay).atTime(ShiftSlots.startHours[slot], 0)
                ShiftWindow(slot, start, start.plusHours(8))
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
        return if (!w.start.isAfter(now)) "On duty: $name"
        else "Next: $name"
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
