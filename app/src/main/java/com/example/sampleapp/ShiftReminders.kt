package com.example.sampleapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sampleapp.data.AppDatabase
import com.example.sampleapp.util.ShiftSlots
import com.example.sampleapp.util.TimeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Reminds you to time in shortly before a scheduled shift and to time out when it ends. */
object ShiftReminders {
    private const val CHANNEL = "shift_reminders"
    const val NOTIF_ID_BASE = 800000

    private fun requestId(day: Long, slot: Int, kind: Int): Int =
        700000 + ((day - 18000L).toInt() * 12 + slot * 2 + kind)

    private fun intentFor(context: Context, day: Long, slot: Int, kind: Int): Intent =
        Intent(context, ShiftReminderReceiver::class.java).apply {
            putExtra("day", day)
            putExtra("slot", slot)
            putExtra("kind", kind)
        }

    private fun schedule(am: AlarmManager, millis: Long, pi: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }

    /** kind 0 = time-in reminder (15 min before the shift), kind 1 = time-out reminder (at shift end). */
    suspend fun reschedule(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val scheduled = db.shiftDao().getAllOnce().associate { it.epochDay to it.slots }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val today = LocalDate.now().toEpochDay()
        for (day in (today - 1)..(today + 45)) {
            for (slot in 0 until ShiftSlots.count) {
                val on = ((scheduled[day] ?: 0) and (1 shl slot)) != 0
                for (kind in 0..1) {
                    val rid = requestId(day, slot, kind)
                    val intent = intentFor(context, day, slot, kind)
                    val existing = PendingIntent.getBroadcast(
                        context, rid, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (existing != null) {
                        am.cancel(existing)
                        existing.cancel()
                    }
                    if (!on) continue
                    val start = ShiftSlots.startOf(day, slot)
                    val fireAt = if (kind == 0) start.minusMinutes(15) else start.plusHours(8)
                    val millis = TimeClock.millisOf(fireAt)
                    if (millis <= System.currentTimeMillis()) continue
                    val pi = PendingIntent.getBroadcast(
                        context, rid, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    schedule(am, millis, pi)
                }
            }
        }
    }

    fun notifyReminder(context: Context, notifId: Int, title: String, text: String, actionLabel: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "Shift reminders", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val open = PendingIntent.getActivity(
            context, 4,
            Intent(context, MainActivity::class.java).putExtra("tab", 4),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val punch = PendingIntent.getBroadcast(
            context, notifId,
            Intent(context, ShiftWidget::class.java).setAction(ShiftWidget.ACTION_PUNCH).putExtra("notif", notifId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(0, actionLabel, punch)
            .build()
        manager.notify(notifId, n)
    }
}

class ShiftReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val day = intent.getLongExtra("day", -1L)
        val slot = intent.getIntExtra("slot", -1)
        val kind = intent.getIntExtra("kind", -1)
        if (day < 0 || slot < 0 || kind < 0) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val logs = db.timeLogDao().getAllOnce().filter { it.shiftDay == day && it.slot == slot }
                val name = ShiftSlots.longNames[slot]
                val window = ShiftSlots.windows[slot]
                val notifId = ShiftReminders.NOTIF_ID_BASE + slot * 2 + kind
                if (kind == 0 && logs.isEmpty()) {
                    ShiftReminders.notifyReminder(context, notifId, "Time in soon", "$name starts soon ($window).", "TIME IN")
                } else if (kind == 1 && logs.any { it.outMillis == null }) {
                    ShiftReminders.notifyReminder(context, notifId, "Time out", "$name has ended. Don't forget to time out.", "TIME OUT")
                }
            } finally {
                pending.finish()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ShiftReminders.reschedule(context)
            } finally {
                pending.finish()
            }
        }
    }
}
