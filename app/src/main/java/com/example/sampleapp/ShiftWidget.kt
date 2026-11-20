package com.example.sampleapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.sampleapp.data.AppDatabase
import com.example.sampleapp.util.ShiftLogic
import com.example.sampleapp.util.ShiftSlots
import com.example.sampleapp.util.TimeClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 4x2 shift widget: next shifts plus a TIME IN / TIME OUT button. */
class ShiftWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateNow(context)
            } finally {
                pending.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_PUNCH) {
            val notifId = intent.getIntExtra("notif", -1)
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = TimeClock.punch(AppDatabase.getDatabase(context))
                    if (notifId >= 0) NotificationManagerCompat.from(context).cancel(notifId)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                    Widgets.refreshAllNow(context)
                } finally {
                    pending.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_PUNCH = "com.example.sampleapp.ACTION_PUNCH"

        private val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())

        suspend fun updateNow(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ShiftWidget::class.java))
            if (ids.isEmpty()) return

            val db = AppDatabase.getDatabase(context)
            val shifts = db.shiftDao().getAllOnce()
            val open = db.timeLogDao().getOpenOnce()
            val now = LocalDateTime.now()
            val next = ShiftLogic.nextOrCurrent(shifts, now)
            val upcoming = ShiftLogic.windows(shifts)
                .filter { it.start.isAfter(now) && it != next }
                .sortedBy { it.start }
                .take(2)

            val views = RemoteViews(context.packageName, R.layout.widget_shift)
            views.setTextViewText(R.id.s_title, ShiftLogic.headline(next, now))
            views.setTextViewText(R.id.s_detail, ShiftLogic.detail(next, now))

            val up = intArrayOf(R.id.s_up1, R.id.s_up2)
            for (i in up.indices) {
                if (i < upcoming.size) {
                    val w = upcoming[i]
                    views.setTextViewText(
                        up[i],
                        "${w.start.format(dayFmt)}  ${ShiftSlots.names[w.slot]}  " +
                            String.format(Locale.getDefault(), "%02d-%02d", w.start.hour, w.end.hour)
                    )
                    views.setViewVisibility(up[i], View.VISIBLE)
                } else {
                    views.setViewVisibility(up[i], View.GONE)
                }
            }

            if (open != null) {
                views.setTextViewText(R.id.s_status, "Timed in ${TimeClock.hhmm(open.inMillis)}")
                views.setTextViewText(R.id.s_button, "TIME OUT")
                views.setInt(R.id.s_button, "setBackgroundResource", R.drawable.btn_out)
            } else {
                views.setTextViewText(R.id.s_status, "Off the clock")
                views.setTextViewText(R.id.s_button, "TIME IN")
                views.setInt(R.id.s_button, "setBackgroundResource", R.drawable.btn_in)
            }

            val punch = PendingIntent.getBroadcast(
                context, 200,
                Intent(context, ShiftWidget::class.java).setAction(ACTION_PUNCH),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.s_button, punch)
            views.setOnClickPendingIntent(R.id.s_root, Widgets.openApp(context, 4))
            views.setViewVisibility(R.id.s_up1, if (upcoming.isNotEmpty()) View.VISIBLE else View.GONE)
            for (id in ids) manager.updateAppWidget(id, views)
        }
    }
}
