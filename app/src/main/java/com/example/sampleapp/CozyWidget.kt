package com.example.sampleapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.example.sampleapp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 2x2 "Daily Quests" widget. */
class CozyWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateNow(context)
            } catch (e: Exception) {
                android.util.Log.e("CozyWidget", "Widget update failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val LINES = intArrayOf(R.id.w_line1, R.id.w_line2, R.id.w_line3, R.id.w_line4)

        suspend fun updateNow(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CozyWidget::class.java))
            if (ids.isEmpty()) return

            val db = AppDatabase.getDatabase(context)
            val today = LocalDate.now().toEpochDay()
            val todays = db.taskDao().getAllOnce().filter {
                it.dueDay != null && it.dueDay <= today && (!it.isCompleted || it.completedDay == today)
            }
            val pending = todays.filter { !it.isCompleted }
                .sortedWith(compareBy({ it.dueDay ?: 0L }, { it.timeMinutes ?: 24 * 60 }))
            val done = todays.count { it.isCompleted }

            val views = RemoteViews(context.packageName, R.layout.widget_quests)
            views.setTextViewText(R.id.w_count, "$done/${todays.size}")
            for (i in LINES.indices) {
                if (i < pending.size) {
                    val t = pending[i]
                    views.setTextViewText(LINES[i], t.title)
                    views.setTextViewCompoundDrawablesRelative(
                        LINES[i],
                        when {
                            t.gold >= 10 -> R.drawable.dot_hard
                            t.gold >= 5 -> R.drawable.dot_medium
                            else -> R.drawable.dot_easy
                        },
                        0, 0, 0
                    )
                    views.setViewVisibility(LINES[i], View.VISIBLE)
                } else {
                    views.setViewVisibility(LINES[i], View.GONE)
                }
            }
            if (pending.size > LINES.size) {
                views.setTextViewText(R.id.w_more, "+${pending.size - LINES.size} more")
                views.setViewVisibility(R.id.w_more, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.w_more, View.GONE)
            }
            if (pending.isEmpty()) {
                views.setTextViewText(R.id.w_empty, if (todays.isEmpty()) "No quests today" else "All quests done!")
                views.setViewVisibility(R.id.w_empty, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.w_empty, View.GONE)
            }
            views.setOnClickPendingIntent(R.id.w_root, Widgets.openApp(context, 1))
            for (id in ids) manager.updateAppWidget(id, views)
        }
    }
}
