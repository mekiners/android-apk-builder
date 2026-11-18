package com.example.sampleapp.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Dates {
    private val shortDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

    fun nextOccurrence(from: Long, recurrence: String): Long = when (recurrence) {
        "Daily" -> from + 1
        "Weekly" -> from + 7
        "Monthly" -> LocalDate.ofEpochDay(from).plusMonths(1).toEpochDay()
        else -> from + 1
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
