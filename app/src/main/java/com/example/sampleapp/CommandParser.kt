package com.example.sampleapp

import java.util.Calendar

data class ParsedCommand(
    val type: CommandType,
    val text: String,
    val timestamp: Long? = null
)

enum class CommandType { REMINDER, NOTE, TODO }

object CommandParser {

    fun parse(raw: String): ParsedCommand? {
        val lower = raw.lowercase().trim()

        if (lower.startsWith("remind")) {
            val body = raw.substringAfter("to", "").trim()
            if (body.isBlank()) return null

            val timePart = body.substringAfterLast(" at ", "")
            val textPart = body.substringBeforeLast(" at ", body).trim()

            val timestamp = parseTime(timePart)
            return ParsedCommand(CommandType.REMINDER, textPart, timestamp)
        }

        if (lower.startsWith("note:")) {
            val text = raw.substringAfter(":").trim()
            return if (text.isBlank()) null else ParsedCommand(CommandType.NOTE, text)
        }

        if (lower.startsWith("todo:")) {
            val text = raw.substringAfter(":").trim()
            return if (text.isBlank()) null else ParsedCommand(CommandType.TODO, text)
        }

        return null
    }

    private fun parseTime(timeStr: String): Long? {
        if (timeStr.isBlank()) return null
        val cleaned = timeStr.lowercase().replace(" ", "")
        val hour = when {
            cleaned.endsWith("pm") -> {
                val h = cleaned.removeSuffix("pm").toIntOrNull() ?: return null
                if (h < 12) h + 12 else h
            }
            cleaned.endsWith("am") -> {
                val h = cleaned.removeSuffix("am").toIntOrNull() ?: return null
                if (h == 12) 0 else h
            }
            else -> {
                cleaned.substringBefore(":").toIntOrNull() ?: return null
            }
        }
        val minute = cleaned.substringAfter(":", "0").toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1) 
            }
        }
        return cal.timeInMillis
    }
}
