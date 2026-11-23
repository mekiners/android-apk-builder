package com.example.sampleapp.util

import com.example.sampleapp.data.GroceryEntity
import com.example.sampleapp.data.LeaveDayEntity
import com.example.sampleapp.data.QuestLogEntity
import com.example.sampleapp.data.ShiftEntity
import com.example.sampleapp.data.TimeLogEntity
import com.example.sampleapp.data.WorkoutLogEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

/** Pure calculation helpers for pay, punctuality, leave, recaps, heatmap, time budget and gym insights. */
object Stats {
    fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value - 1).toLong())

    fun fmtKg(v: Double): String =
        if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.US, "%.1f", v)

    fun money(symbol: String, v: Double): String = symbol + String.format(Locale.US, "%,.2f", v)

    fun hoursLabel(hours: Double): String {
        val totalMinutes = Math.round(hours * 60.0)
        return "${totalMinutes / 60}h ${totalMinutes % 60}m"
    }

    // ------------------------------------------------------------------ pay

    class Pay(val regularHours: Double, val overtimeHours: Double, val nightHours: Double, val amount: Double)

    private fun overlap(a1: Long, a2: Long, b1: Long, b2: Long): Long = maxOf(0L, minOf(a2, b2) - maxOf(a1, b1))

    /** Hours between 22:00 and 06:00 inside the given period. */
    fun nightHours(inMillis: Long, outMillis: Long): Double {
        var total = 0L
        var d = TimeClock.toDateTime(inMillis).toLocalDate()
        val last = TimeClock.toDateTime(outMillis).toLocalDate()
        while (!d.isAfter(last)) {
            val morningStart = TimeClock.millisOf(d.atStartOfDay())
            val morningEnd = TimeClock.millisOf(d.atTime(6, 0))
            val eveningStart = TimeClock.millisOf(d.atTime(22, 0))
            val eveningEnd = TimeClock.millisOf(d.plusDays(1).atStartOfDay())
            total += overlap(inMillis, outMillis, morningStart, morningEnd)
            total += overlap(inMillis, outMillis, eveningStart, eveningEnd)
            d = d.plusDays(1)
        }
        return total / 3600000.0
    }

    /** Pay for finished sessions whose roster date is in [fromDay, toDayExclusive). */
    fun pay(logs: List<TimeLogEntity>, fromDay: Long, toDayExclusive: Long, s: AppSettings): Pay {
        var reg = 0.0
        var ot = 0.0
        var night = 0.0
        var amount = 0.0
        for (l in logs) {
            val out = l.outMillis ?: continue
            if (l.shiftDay < fromDay || l.shiftDay >= toDayExclusive) continue
            val hours = (out - l.inMillis) / 3600000.0
            val o = maxOf(0.0, hours - s.overtimeAfterHours)
            val r = hours - o
            val n = nightHours(l.inMillis, out)
            reg += r
            ot += o
            night += n
            amount += r * s.hourlyRate + o * s.hourlyRate * s.overtimeMultiplier + n * s.hourlyRate * (s.nightDiffPercent / 100.0)
        }
        return Pay(reg, ot, night, amount)
    }

    /** Estimate for scheduled shifts that have not started yet. */
    fun projectedPay(shifts: List<ShiftEntity>, now: LocalDateTime, fromDay: Long, toDayExclusive: Long, s: AppSettings): Pay {
        var reg = 0.0
        var night = 0.0
        var amount = 0.0
        for (w in ShiftLogic.windows(shifts)) {
            if (w.rosterDay < fromDay || w.rosterDay >= toDayExclusive || !w.start.isAfter(now)) continue
            val n = nightHours(TimeClock.millisOf(w.start), TimeClock.millisOf(w.end))
            reg += 8.0
            night += n
            amount += 8.0 * s.hourlyRate + n * s.hourlyRate * (s.nightDiffPercent / 100.0)
        }
        return Pay(reg, 0.0, night, amount)
    }

    // ------------------------------------------------------------------ punctuality

    class Punctuality(
        val samples: Int,
        val onTimePercent: Int,
        val avgOffsetMinutes: Int,   // negative = early, positive = late
        val avgShiftMinutes: Int,
        val onTimeStreak: Int,
        val punchStreak: Int
    )

    fun punctuality(logs: List<TimeLogEntity>, shifts: List<ShiftEntity>, s: AppSettings, now: LocalDateTime): Punctuality {
        val matched = logs.filter { it.slot >= 0 }.sortedByDescending { it.inMillis }
        val offsets = matched.map { l ->
            (l.inMillis - TimeClock.millisOf(ShiftSlots.startOf(l.shiftDay, l.slot))) / 60000L
        }
        val onTime = offsets.count { it <= s.graceMinutes }
        var onTimeStreak = 0
        for (o in offsets) {
            if (o <= s.graceMinutes) onTimeStreak++ else break
        }
        val completed = logs.filter { it.outMillis != null }
        val avgShift = if (completed.isEmpty()) 0 else
            (completed.sumOf { l -> ((l.outMillis ?: l.inMillis) - l.inMillis) / 60000L } / completed.size).toInt()

        var punchStreak = 0
        val first = logs.minOfOrNull { it.inMillis }
        if (first != null) {
            val past = ShiftLogic.windows(shifts)
                .filter { it.end.isBefore(now) && TimeClock.millisOf(it.end) >= first }
                .sortedByDescending { it.start }
            for (w in past) {
                val done = logs.any { it.shiftDay == w.rosterDay && it.slot == w.slot && it.outMillis != null }
                if (done) punchStreak++ else break
            }
        }
        return Punctuality(
            samples = offsets.size,
            onTimePercent = if (offsets.isEmpty()) 0 else onTime * 100 / offsets.size,
            avgOffsetMinutes = if (offsets.isEmpty()) 0 else (offsets.sum() / offsets.size).toInt(),
            avgShiftMinutes = avgShift,
            onTimeStreak = onTimeStreak,
            punchStreak = punchStreak
        )
    }

    // ------------------------------------------------------------------ leave

    class LeaveUsed(val vacation: Int, val sick: Int)

    fun leaveUsed(days: List<LeaveDayEntity>, year: Int): LeaveUsed {
        val inYear = days.filter { LocalDate.ofEpochDay(it.epochDay).year == year }
        return LeaveUsed(inYear.count { it.kind == "Vacation" }, inYear.count { it.kind == "Sick" })
    }

    // ------------------------------------------------------------------ recap, heatmap, time budget

    class Recap(val quests: Int, val gold: Int, val hours: Double, val shifts: Int, val gymSessions: Int, val spend: Double)

    fun recap(
        weekStart: LocalDate,
        questLog: List<QuestLogEntity>,
        timeLogs: List<TimeLogEntity>,
        workoutLogs: List<WorkoutLogEntity>,
        groceries: List<GroceryEntity>
    ): Recap {
        val from = weekStart.toEpochDay()
        val to = from + 7
        val q = questLog.filter { it.day >= from && it.day < to }
        val tl = timeLogs.filter { it.shiftDay >= from && it.shiftDay < to && it.outMillis != null }
        val hours = tl.sumOf { l -> ((l.outMillis ?: l.inMillis) - l.inMillis) / 3600000.0 }
        val gym = workoutLogs.filter { it.day >= from && it.day < to }.map { it.day }.distinct().size
        val spend = groceries.sumOf { g ->
            val b = g.boughtDay
            if (b != null && b >= from && b < to) (g.price ?: 0.0) else 0.0
        }
        return Recap(q.size, q.sumOf { it.gold }, hours, tl.size, gym, spend)
    }

    /** Columns = weeks (oldest first), rows = Mon..Sun. Level 0..4, or -1 for days in the future. */
    fun heatmap(
        weeks: Int,
        today: LocalDate,
        questLog: List<QuestLogEntity>,
        timeLogs: List<TimeLogEntity>,
        workoutLogs: List<WorkoutLogEntity>
    ): List<List<Int>> {
        val questCount = questLog.groupBy { it.day }.mapValues { it.value.size }
        val worked = timeLogs.filter { it.outMillis != null }.map { it.shiftDay }.toSet()
        val gym = workoutLogs.map { it.day }.toSet()
        val start = weekStart(today).minusWeeks((weeks - 1).toLong())
        return (0 until weeks).map { col ->
            (0..6).map { row ->
                val d = start.plusDays((col * 7 + row).toLong())
                if (d.isAfter(today)) {
                    -1
                } else {
                    val ep = d.toEpochDay()
                    val score = (questCount[ep] ?: 0) + (if (ep in worked) 2 else 0) + (if (ep in gym) 2 else 0)
                    when {
                        score == 0 -> 0
                        score <= 2 -> 1
                        score <= 4 -> 2
                        score <= 6 -> 3
                        else -> 4
                    }
                }
            }
        }
    }

    class Budget(val shiftHours: Double, val sleepHours: Double, val freeHours: Double)

    fun timeBudget(weekStart: LocalDate, shifts: List<ShiftEntity>, s: AppSettings): Budget {
        val from = weekStart.toEpochDay()
        val to = from + 7
        val shiftHours = ShiftLogic.windows(shifts).count { it.rosterDay >= from && it.rosterDay < to } * 8.0
        val sleep = s.sleepHours * 7.0
        return Budget(shiftHours, sleep, maxOf(0.0, 168.0 - shiftHours - sleep))
    }

    // ------------------------------------------------------------------ gym

    fun bestWeight(logs: List<WorkoutLogEntity>, exerciseId: Long): Double? =
        logs.filter { it.exerciseId == exerciseId && it.weightKg != null }.maxOfOrNull { it.weightKg ?: 0.0 }

    /** The most recent earlier day on which this exercise has logged sets. */
    fun lastSession(logs: List<WorkoutLogEntity>, exerciseId: Long, beforeDay: Long): List<WorkoutLogEntity> {
        val sets = logs.filter { it.exerciseId == exerciseId && it.weightKg != null && it.day < beforeDay }
        val day = sets.maxOfOrNull { it.day } ?: return emptyList()
        return sets.filter { it.day == day }.sortedBy { it.id }
    }

    /** Simple progressive overload hint based on a "4 x 8" style target in the exercise details. */
    fun overloadHint(detail: String, last: List<WorkoutLogEntity>): String? {
        if (last.isEmpty()) return null
        val target = Regex("(\\d+)\\s*[xX]\\s*(\\d+)").find(detail)?.groupValues?.get(2)?.toIntOrNull()
        val top = last.maxOf { it.weightKg ?: 0.0 }
        if (target == null) return null
        val allHit = last.all { (it.reps ?: 0) >= target }
        return if (allHit) "Try ${fmtKg(top + 2.5)} kg next time" else "Aim for $target reps at ${fmtKg(top)} kg"
    }

    private fun countInWeek(days: Set<Long>, weekStart: LocalDate): Int {
        val from = weekStart.toEpochDay()
        return days.count { it >= from && it < from + 7 }
    }

    fun sessionsThisWeek(days: Set<Long>, today: LocalDate): Int = countInWeek(days, weekStart(today))

    /** Consecutive weeks (including this one if already met) with at least [target] sessions. */
    fun weeklyStreak(days: Set<Long>, today: LocalDate, target: Int = 2): Int {
        var ws = weekStart(today)
        var streak = 0
        if (countInWeek(days, ws) >= target) streak++
        ws = ws.minusWeeks(1)
        while (countInWeek(days, ws) >= target) {
            streak++
            ws = ws.minusWeeks(1)
        }
        return streak
    }

    fun gymNudge(days: Set<Long>, today: LocalDate): String? {
        val t = today.toEpochDay()
        val last7 = days.count { it > t - 7 && it <= t }
        if (last7 >= 5) return "You trained $last7 times in the last 7 days. A rest day would do you good."
        var weeks = 0
        var ws = weekStart(today).minusWeeks(1)
        while (weeks < 12 && countInWeek(days, ws) >= 3) {
            weeks++
            ws = ws.minusWeeks(1)
        }
        if (weeks >= 5) return "$weeks strong weeks in a row. Consider an easier deload week."
        return null
    }

    class DaySuggestion(val date: LocalDate, val reason: String)

    /** Picks up to three of the next seven days with the most free waking time, avoiding recovery after a night shift. */
    fun suggestGymDays(shifts: List<ShiftEntity>, trainedDays: Set<Long>, today: LocalDate): List<DaySuggestion> {
        val windows = ShiftLogic.windows(shifts)
        val scored = ArrayList<Pair<Double, DaySuggestion>>()
        for (i in 0..6) {
            val d = today.plusDays(i.toLong())
            if (d.toEpochDay() in trainedDays) continue
            val awakeFrom = TimeClock.millisOf(d.atTime(8, 0))
            val awakeTo = TimeClock.millisOf(d.atTime(22, 0))
            var busyMillis = 0L
            var recovering = false
            for (w in windows) {
                val s = TimeClock.millisOf(w.start)
                val e = TimeClock.millisOf(w.end)
                busyMillis += overlap(s, e, awakeFrom, awakeTo)
                if (w.end.toLocalDate() == d && w.end.hour in 3..10) recovering = true
            }
            val freeHours = 14.0 - busyMillis / 3600000.0
            if (freeHours < 4.0 || recovering) continue
            val reason = if (busyMillis == 0L) "free all day" else "${Math.round(freeHours)}h free around your shift"
            scored.add(Pair(freeHours, DaySuggestion(d, reason)))
        }
        return scored.sortedByDescending { it.first }.take(3).map { it.second }.sortedBy { it.date }
    }
}
