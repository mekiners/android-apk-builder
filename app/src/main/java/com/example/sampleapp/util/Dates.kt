package com.example.sampleapp.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Dates {
    private val shortDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private fun weekdayIndex(day: Long): Int = LocalDate.ofEpochDay(day).dayOfWeek.value - 1 // Mon = 0

    fun nextOccurrence(from: Long, recurrence: String, repeatDays: Int = 0): Long = when (recurrence) {
        "Daily" -> from + 1
        "Weekly" -> from + 7
        "Monthly" -> LocalDate.ofEpochDay(from).plusMonths(1).toEpochDay()
        "Days" -> {
            var result = from + 1
            if (repeatDays != 0) {
                for (i in 1..7) {
                    val d = from + i
                    if ((repeatDays and (1 shl weekdayIndex(d))) != 0) { result = d; break }
                }
            }
            result
        }
        else -> from + 1
    }

    /** For custom weekday repeats: if the chosen date isn't one of the days, move to the next one that is. */
    fun alignToRepeatDays(day: Long, repeatDays: Int): Long {
        if (repeatDays == 0 || (repeatDays and (1 shl weekdayIndex(day))) != 0) return day
        return nextOccurrence(day, "Days", repeatDays)
    }

    fun repeatLabel(recurrence: String, repeatDays: Int): String = when (recurrence) {
        "Daily" -> "Daily"
        "Weekly" -> "Weekly"
        "Monthly" -> "Monthly"
        "Days" -> dayNames.filterIndexed { i, _ -> (repeatDays and (1 shl i)) != 0 }.joinToString(" ")
        else -> "Once"
    }

    fun timeLabel(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        val suffix = if (h >= 12) "PM" else "AM"
        val h12 = if (h % 12 == 0) 12 else h % 12
        return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, suffix)
    }

    fun dayLabel(day: Long, today: Long): String = when {
        day == today -> "Today"
        day == today + 1 -> "Tomorrow"
        day < today -> "Overdue \u00B7 " + LocalDate.ofEpochDay(day).format(shortDate)
        else -> LocalDate.ofEpochDay(day).format(shortDate)
    }

    fun dueLabel(dueDay: Long?, timeMinutes: Int?, today: Long): String {
        val parts = listOfNotNull(
            dueDay?.let { dayLabel(it, today) },
            timeMinutes?.let { timeLabel(it) }
        )
        return parts.joinToString(" \u00B7 ")
    }

    fun toMillis(day: Long, minutes: Int): Long =
        LocalDate.ofEpochDay(day)
            .atTime(minutes / 60, minutes % 60)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
