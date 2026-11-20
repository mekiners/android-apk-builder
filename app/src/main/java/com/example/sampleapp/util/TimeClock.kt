package com.example.sampleapp.util

import com.example.sampleapp.data.AppDatabase
import com.example.sampleapp.data.ShiftEntity
import com.example.sampleapp.data.TimeLogEntity
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** Time in / time out logic shared by the app, the widget and the notification button. */
object TimeClock {
    class Result(val clockedIn: Boolean, val message: String)

    private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun millisOf(dt: LocalDateTime): Long = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun toDateTime(millis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

    fun hhmm(millis: Long): String = toDateTime(millis).format(timeFmt)

    fun durationLabel(fromMillis: Long, toMillis: Long): String {
        val minutes = ((toMillis - fromMillis) / 60000L).coerceAtLeast(0L)
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    /** Which scheduled shift does a punch at this moment belong to? Returns (shiftDay, slot), slot -1 if none. */
    fun matchShift(shifts: List<ShiftEntity>, now: LocalDateTime): Pair<Long, Int> {
        val candidates = ShiftLogic.windows(shifts).filter {
            now.isAfter(it.start.minusHours(3)) && now.isBefore(it.end.plusHours(1))
        }
        val best = candidates.minByOrNull { abs(Duration.between(it.start, now).toMinutes()) }
        return if (best != null) {
            Pair(best.start.toLocalDate().toEpochDay(), best.slot)
        } else {
            Pair(now.toLocalDate().toEpochDay(), -1)
        }
    }

    /** Toggles: if timed in, time out; otherwise time in. */
    suspend fun punch(db: AppDatabase, now: LocalDateTime = LocalDateTime.now()): Result {
        val nowMillis = millisOf(now)
        val open = db.timeLogDao().getOpenOnce()
        var note = ""
        if (open != null) {
            val age = Duration.between(toDateTime(open.inMillis), now)
            if (age.toHours() < 18) {
                db.timeLogDao().update(open.copy(outMillis = nowMillis))
                return Result(false, "Timed out at ${now.format(timeFmt)} (${durationLabel(open.inMillis, nowMillis)})")
            }
            // They forgot to time out last time: close that session at the end of its shift.
            val autoOut = if (open.slot >= 0) {
                millisOf(LocalDate.ofEpochDay(open.shiftDay).atTime(ShiftSlots.startHours[open.slot], 0).plusHours(8))
            } else {
                open.inMillis + 8L * 3600_000L
            }
            db.timeLogDao().update(open.copy(outMillis = autoOut))
            note = " (previous session was auto-closed)"
        }
        val (day, slot) = matchShift(db.shiftDao().getAllOnce(), now)
        db.timeLogDao().insert(TimeLogEntity(shiftDay = day, slot = slot, inMillis = nowMillis))
        return Result(true, "Timed in at ${now.format(timeFmt)}$note")
    }
}
