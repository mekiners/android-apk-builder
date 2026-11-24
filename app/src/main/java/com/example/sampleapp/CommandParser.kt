package com.example.sampleapp

data class ParsedCommand(
    val title: String,
    val timeMinutes: Int? = null,
    val dayOffset: Int? = null,      // 0 = today, 1 = tomorrow
    val recurrence: String? = null,  // None / Daily / Weekly / Monthly / Days (null = not mentioned)
    val gold: Int? = null,           // 1 / 5 / 10
    val category: String? = null,    // one of the user's own category names (null = not mentioned)
    val repeatDays: Int = 0,         // weekday bitmask when recurrence == "Days": bit0 = Mon ... bit6 = Sun
    val groceryItems: List<GroceryItem> = emptyList()  // not empty = "buy ..." goes to the grocery list
)

data class GroceryItem(val name: String, val quantity: String = "")

/**
 * Understands things like:
 *   "water the plants daily at 7am"
 *   "buy milk tomorrow at 5:30 pm"
 *   "file report hard quest"            (easy = 1 gold, medium = 5 gold, hard = 10 gold)
 *   "pay rent 10 gold monthly"          (also: one gold, five gold, ten gold, 5g)
 *   "gym every tuesday and thursday"    (also: every wednesday and friday, weekdays, weekends)
 *   "buy eggs #to buy" / "Work: send report" / "send report in work"   (your own categories)
 *   "walk the dog once"                 (once = does not repeat)
 *
 * Careful design: plain words like "hard" or "work" are only treated as settings in clear
 * positions, so titles like "buy hard drive" or "go to work" are left alone.
 */
object CommandParser {
    private val opts = setOf(RegexOption.IGNORE_CASE)

    // ---- time
    private val rxTime12 = Regex("""\b(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*([ap])\.?m\.?\b""", opts)
    private val rxTime24 = Regex("""\bat\s+(\d{1,2}):(\d{2})\b""", opts)

    // ---- repeat
    private const val DAY =
        """(?:mon(?:day)?|tue(?:s(?:day)?)?|wed(?:nesday)?|thu(?:r(?:s(?:day)?)?)?|fri(?:day)?|sat(?:urday)?|sun(?:day)?)s?"""
    private const val PLURAL_DAY = """(?:mon|tues|wednes|thurs|fri|satur|sun)days"""
    private const val SEP = """(?:\s*,\s*(?:and\s+)?|\s*[&+/]\s*|\s+and\s+)(?:(?:every|each)\s+)?"""

    private val rxOncePer = Regex("""\bonce\s+(?:a|per|each|every)\s+(day|week|month)\b""", opts)
    private val rxEveryDays = Regex("""\b(?:on\s+)?(?:every|each)\s+($DAY(?:$SEP$DAY)*)\b""", opts)
    private val rxPluralDays = Regex("""\b(?:on\s+)?($PLURAL_DAY(?:$SEP$PLURAL_DAY)*)\b""", opts)
    private val rxWeekdays = Regex("""\b(?:(?:on\s+)?(?:every|each)\s+weekday|(?:on\s+)?weekdays)\b""", opts)
    private val rxWeekends = Regex("""\b(?:(?:on\s+)?(?:every|each)\s+weekend|(?:on\s+)?weekends)\b""", opts)
    private val rxDaily = Regex("""\b(?:every\s*day(?:\s+of\s+the\s+week)?|each\s+day|daily)\b""", opts)
    private val rxWeekly = Regex("""\b(?:every\s*week|each\s+week|weekly)\b""", opts)
    private val rxMonthly = Regex("""\b(?:every\s*month|each\s+month|monthly)\b""", opts)
    private val rxOnce = Regex("""\b(?:once|one[\s-]?time|one[\s-]?off)\b""", opts)
    private val rxDayPrefix = Regex("""\b(mon|tue|wed|thu|fri|sat|sun)""", opts)
    private val dayPrefixes = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")

    // ---- day
    private val rxTomorrow = Regex("""\btomorrow\b""", opts)
    private val rxToday = Regex("""\btoday\b""", opts)

    // ---- gold
    private val rxGoldWord = Regex(
        """\b(?:(?:worth|reward|rewards|for)\s+)?(1|5|10|one|five|ten)\s*(?:gold(?:\s+coins?)?|coins?|gp)\b""", opts
    )
    private val rxGoldShort = Regex("""\b(?:(?:worth|reward|rewards|for)\s+)?(1|5|10)g\b""", opts)

    // ---- difficulty (only in clear positions, so "hard drive" stays a title)
    private val rxDiff1 = Regex("""\b(easy|medium|hard)\s+(?:quest|task|difficulty|level)\b""", opts)
    private val rxDiff2 = Regex("""\b(?:difficulty|level)\s*:?\s*(easy|medium|hard)\b""", opts)
    private val rxDiffStart = Regex("""^\s*(easy|medium|hard)\s*(?::|,|[-\u2013\u2014]\s)\s*""", opts)
    private val rxDiffEnd = Regex("""(?:^|[\s,])(easy|medium|hard)\s*[.!]?\s*$""", opts)

    private val rxRemind = Regex("""^\s*remind me to\s+""", opts)
    private val rxLeadAdd = Regex("""^\s*add\s+""", opts)
    private val rxDangling = Regex("""(?:\s+(?:on|at|in|for|and|to|by|every|each|worth|reward|rewards))+\s*$""", opts)
    private val rxDanglingLead = Regex("""^(?:and|on|at)\s+""", opts)

    private fun goldForWord(word: String): Int = when (word.lowercase()) {
        "easy" -> 1
        "medium" -> 5
        "hard" -> 10
        "1", "one" -> 1
        "5", "five" -> 5
        else -> 10
    }

    private fun dayMask(text: String): Int {
        var mask = 0
        rxDayPrefix.findAll(text).forEach { m ->
            val i = dayPrefixes.indexOf(m.groupValues[1].lowercase())
            if (i >= 0) mask = mask or (1 shl i)
        }
        return mask
    }

    /** Looks for one of the user's categories in clear positions. Returns name + where it was found. */
    private fun findCategory(s: String, names: List<String>): Triple<String, MatchResult, Boolean>? {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() && !it.equals("All", true) }
            .distinct().sortedByDescending { it.length }
        if (clean.isEmpty()) return null
        fun body(name: String) =
            name.split(Regex("""\s+""")).joinToString("""\s+""") { Regex.escape(it) }

        val shapes = listOf<(String) -> Regex>(
            // "#work", "#to buy"
            { n -> Regex("""(?<!\w)#\s*$n(?!\w)""", opts) },
            // "in work", "category work", "under to buy", "to my work list", "in the to do list"
            { n ->
                Regex(
                    """(?<!\w)(?:(?:category|cat|in|into|under)\s*:?\s*|to\s+(?:my|the)\s+)(?:(?:my|the)\s+)?$n(?!\w)(?:\s+(?:list|category))?""",
                    opts
                )
            },
            // "work list"
            { n -> Regex("""(?<!\w)$n\s+(?:list|category)(?!\w)""", opts) },
            // "Work: send report" / "to buy - milk" at the very start
            { n -> Regex("""^\s*$n(?!\w)\s*(?::\s*|[-\u2013\u2014]\s+)""", opts) },
            // "send report, work" at the very end
            { n -> Regex(""",\s*$n\s*$""", opts) }
        )
        for (shape in shapes) {
            for (name in clean) {
                val m = shape(body(name)).find(s) ?: continue
                val viaTo = m.value.trimStart().startsWith("to ", ignoreCase = true)
                return Triple(name, m, viaTo)
            }
        }
        return null
    }

    // ---- grocery ("buy milk", "bumili ng itlog and gatas", "bili 2 kg rice")
    private val rxGrocery = Regex(
        """^(?:(?:i\s+)?(?:need\s+to|have\s+to|got\s+to|gotta|should|must|please|pls|kailangan(?:\s+kong|\s+ko)?|dapat)\s+)?(buy|bili|bumili|bilhin|pabili|by)\b[\s:,\-]*(.*)$""",
        opts
    )
    private val rxByGuard = Regex(
        """^(?:mon|tue|wed|thu|fri|sat|sun|tomorrow|tmrw|today|tonight|next|this|the|end|noon|midnight|eod|eow|\d)""", opts
    )
    private val rxTagalogLead = Regex("""^(?:ka\s+|tayo\s+|ako\s+|ko\s+)?(?:ng|nang)\s+""", opts)
    private val rxItemLead = Regex("""^(?:and\s+|some\s+|a\s+|an\s+|the\s+|ng\s+)+""", opts)
    private val rxSplitEnglish = Regex("""\s*,\s*|\s*&\s*|\s+\+\s+|\s+and\s+""", opts)
    private val rxSplitTagalog = Regex("""\s*,\s*|\s*&\s*|\s+\+\s+|\s+and\s+|\s+at\s+""", opts)
    private val rxQty = Regex(
        """^(\d+(?:[.,]\d+)?\s*(?:kg|kgs|g|l|ml|pcs|pc|pack|packs|box|boxes|dozen|bottles?|cans?|x)?)\s+(.+)$""", opts
    )

    private fun groceryItems(text: String): List<GroceryItem> {
        val m = rxGrocery.find(text.trim()) ?: return emptyList()
        val trigger = m.groupValues[1].lowercase()
        var rest = m.groupValues[2].trim()
        if (rest.isBlank()) return emptyList()
        // "by" is a common typo for "buy", but "by friday" / "by the end of" are deadlines, not shopping
        if (trigger == "by" && rxByGuard.containsMatchIn(rest)) return emptyList()
        val tagalog = trigger in setOf("bili", "bumili", "bilhin", "pabili")
        rest = rxTagalogLead.replace(rest, "")
        val parts = rest.split(if (tagalog) rxSplitTagalog else rxSplitEnglish)
        val items = parts.mapNotNull { part ->
            var item = rxItemLead.replace(part.trim(), "").trim().trim('.', '!', ';')
            if (item.isBlank()) return@mapNotNull null
            var qty = ""
            rxQty.find(item)?.let { q -> qty = q.groupValues[1].trim(); item = q.groupValues[2].trim() }
            GroceryItem(item.replaceFirstChar { it.uppercase() }, qty)
        }
        return items
    }

    fun parse(raw: String, categoryNames: List<String> = emptyList()): ParsedCommand {
        var s = raw.trim()
        var time: Int? = null
        var day: Int? = null
        var rec: String? = null
        var days = 0
        var gold: Int? = null
        var category: String? = null
        var cutSomething = false
        var stripAdd = false

        fun cut(m: MatchResult) { s = s.removeRange(m.range); cutSomething = true }

        // time (12-hour "5pm" / "5:30 pm" first, then 24-hour "at 17:30")
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
        if (time == null) {
            rxTime24.find(s)?.let { m ->
                val h = m.groupValues[1].toInt(); val min = m.groupValues[2].toInt()
                if (h in 0..23 && min in 0..59) { time = h * 60 + min; cut(m) }
            }
        }

        // gold amount ("5 gold", "ten gold", "10g")
        (rxGoldWord.find(s) ?: rxGoldShort.find(s))?.let { gold = goldForWord(it.groupValues[1]); cut(it) }

        // repeat: most specific first
        rxOncePer.find(s)?.let { m ->
            rec = when (m.groupValues[1].lowercase()) { "day" -> "Daily"; "week" -> "Weekly"; else -> "Monthly" }
            cut(m)
        }
        if (rec == null) {
            val m = rxEveryDays.find(s) ?: rxPluralDays.find(s)
            if (m != null) {
                val mask = dayMask(m.groupValues[1])
                if (mask != 0) {
                    if (mask == 127) rec = "Daily" else { rec = "Days"; days = mask }
                    cut(m)
                }
            }
        }
        if (rec == null) rxWeekdays.find(s)?.let { rec = "Days"; days = 31; cut(it) }
        if (rec == null) rxWeekends.find(s)?.let { rec = "Days"; days = 96; cut(it) }
        if (rec == null) rxDaily.find(s)?.let { rec = "Daily"; cut(it) }
        if (rec == null) rxWeekly.find(s)?.let { rec = "Weekly"; cut(it) }
        if (rec == null) rxMonthly.find(s)?.let { rec = "Monthly"; cut(it) }
        if (rec == null) rxOnce.find(s)?.let { rec = "None"; cut(it) }

        // day
        rxTomorrow.find(s)?.let { day = 1; cut(it) }
        rxToday.find(s)?.let { day = 0; cut(it) }

        // category
        findCategory(s, categoryNames)?.let { (name, m, viaTo) ->
            category = name
            stripAdd = viaTo
            cut(m)
        }

        // difficulty (an explicit gold amount wins if both are given)
        fun diff(m: MatchResult) { if (gold == null) gold = goldForWord(m.groupValues[1]); cut(m) }
        rxDiff1.find(s)?.let { diff(it) }
        rxDiff2.find(s)?.let { diff(it) }
        rxDiffStart.find(s)?.let { diff(it) }
        rxDiffEnd.find(s)?.let { diff(it) }

        s = rxRemind.replace(s, "")
        if (stripAdd) s = rxLeadAdd.replace(s, "")
        s = s.replace(Regex("""\s+"""), " ").trim().trim(',', ':', '-', ';').trim()
        if (cutSomething) {
            s = rxDangling.replace(s, "").trim()
            s = rxDanglingLead.replace(s, "").trim()
            s = s.trim(',', ':', '-', ';').trim()
        }

        // "buy ..." with no time, day, repeat, gold or category = grocery list item(s), not a quest
        val grocery = if (time == null && day == null && rec == null && gold == null && category == null)
            groceryItems(s) else emptyList()

        val title = if (s.isBlank()) raw.trim() else s.replaceFirstChar { it.uppercase() }
        return ParsedCommand(title, time, day, rec, gold, category, days, grocery)
    }
}
