package com.example.sampleapp

import java.util.Calendar

data class ParsedCommand(
    val type: CommandType,
    val text: String,
    val timestamp: Long? = null,
    val recurrence: String = "None"
)

enum class CommandType { TASK }

object CommandParser {
    fun parse(raw: String): ParsedCommand? {
        val lower = raw.lowercase().trim()
        if (lower.isEmpty()) return null

        var text = raw
        var timestamp: Long? = null
        var recurrence = "None"

        // Look for recurrence words
        if (lower.contains("daily")) { recurrence = "Daily"; text = text.replace("daily", "", ignoreCase = true) }
        if (lower.contains("weekly")) { recurrence = "Weekly"; text = text.replace("weekly", "", ignoreCase = true) }
        if (lower.contains("monthly")) { recurrence = "Monthly"; text = text.replace("monthly", "", ignoreCase = true) }

        // Very basic time parsing (looks for "at 5pm")
        if (lower.contains(" at ")) {
            val timePart = lower.substringAfterLast(" at ").trim()
            timestamp = parseTime(timePart)
            if (timestamp != null) {
                text = text.substringBeforeLast(" at ", text).trim()
            }
        }

        // Clean up text
        text = text.replace("remind me to", "", ignoreCase = true).trim()
        if (text.startsWith(":")) text = text.substring(1).trim()

        return ParsedCommand(CommandType.TASK, text, timestamp, recurrence)
    }

    private fun parseTime(timeStr: String): Long? {
        val cleaned = timeStr.replace(" ", "")
        val hour = when {
            cleaned.endsWith("pm") -> (cleaned.removeSuffix("pm").toIntOrNull() ?: return null).let { if (it < 12) it + 12 else it }
            cleaned.endsWith("am") -> (cleaned.removeSuffix("am").toIntOrNull() ?: return null).let { if (it == 12) 0 else it }
            else -> return null
        }
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
