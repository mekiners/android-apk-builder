package com.example.sampleapp

data class ParsedCommand(
    val title: String,
    val timeMinutes: Int? = null,
    val dayOffset: Int? = null,      // 0 = today, 1 = tomorrow
    val recurrence: String? = null,  // Daily / Weekly / Monthly
    val gold: Int? = null            // 1 / 5 / 10
)

/**
 * Understands things like:
 *   "water the plants daily at 7am"
 *   "buy milk tomorrow at 5:30 pm"
 *   "file report hard quest"        (easy quest = 1g, medium quest = 5g, hard quest = 10g)
 */
object CommandParser {
    private val opts = setOf(RegexOption.IGNORE_CASE)
    private val rxTime12 = Regex("""\b(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*([ap])\.?m\.?\b""", opts)
    private val rxTime24 = Regex("""\bat\s+(\d{1,2}):(\d{2})\b""", opts)
    private val rxDaily = Regex("""\b(every\s*day|daily)\b""", opts)
    private val rxWeekly = Regex("""\b(every\s*week|weekly)\b""", opts)
    private val rxMonthly = Regex("""\b(every\s*month|monthly)\b""", opts)
    private val rxTomorrow = Regex("""\btomorrow\b""", opts)
    private val rxToday = Regex("""\btoday\b""", opts)
    private val rxDiff1 = Regex("""\b(easy|medium|hard)\s+(?:quest|task)\b""", opts)
    private val rxDiff2 = Regex("""\bdifficulty\s+(easy|medium|hard)\b""", opts)
    private val rxRemind = Regex("""^\s*remind me to\s+""", opts)

    private fun goldFor(word: String): Int = when (word.lowercase()) {
        "easy" -> 1
        "medium" -> 5
        else -> 10
    }

    fun parse(raw: String): ParsedCommand {
        var s = raw.trim()
        var time: Int? = null
        var day: Int? = null
        var rec: String? = null
        var gold: Int? = null

        fun cut(m: MatchResult) { s = s.removeRange(m.range) }

        rxTime24.find(s)?.let { m ->
            val h = m.groupValues[1].toInt(); val min = m.groupValues[2].toInt()
            if (h in 0..23 && min in 0..59) { time = h * 60 + min; cut(m) }
        }
        if (time == null) {
            rxTime12.find(s)?.let { m ->
                val h = m.groupValues[1].toInt()
                val min = m.groupValues[2].ifEmpty { "0" }.toInt()
                val pm = m.groupValues[3].lowercase() == "p"
                if (h in 1..12 && min in 0..59) {
                    val h24 = if (pm) (if (h < 12) h + 12 else 12) else (if (h == 12) 0 else h)
                    time = h24 * 60 + min
                    cut(m)
                }
            }
        }
        rxDaily.find(s)?.let { rec = "Daily"; cut(it) }
        rxWeekly.find(s)?.let { rec = "Weekly"; cut(it) }
        rxMonthly.find(s)?.let { rec = "Monthly"; cut(it) }
        rxTomorrow.find(s)?.let { day = 1; cut(it) }
        rxToday.find(s)?.let { day = 0; cut(it) }
        rxDiff1.find(s)?.let { gold = goldFor(it.groupValues[1]); cut(it) }
        rxDiff2.find(s)?.let { gold = goldFor(it.groupValues[1]); cut(it) }

        s = rxRemind.replace(s, "")
        s = s.replace(Regex("""\s+"""), " ").trim().trim(',', ':', '-').trim()
        val title = if (s.isBlank()) raw.trim() else s.replaceFirstChar { it.uppercase() }
        return ParsedCommand(title, time, day, rec, gold)
    }
}
