package com.example.sampleapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.sampleapp.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CozyWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    // NEW: Listen for a manual refresh signal from the app
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_REFRESH_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context.packageName, CozyWidget::class.java.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
        views.setOnClickPendingIntent(R.id.task_1, pendingIntent)
        views.setOnClickPendingIntent(R.id.task_2, pendingIntent)
        views.setOnClickPendingIntent(R.id.task_3, pendingIntent)

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            val allTasks = db.taskDao().getAllTasks().first()
            val today = java.time.LocalDate.now().toEpochDay()
            val pendingTasks = allTasks
                .filter { !it.isCompleted && it.dueDay != null && it.dueDay <= today }
                .take(3)

            views.setTextViewText(R.id.task_1, if (pendingTasks.isNotEmpty()) "- ${pendingTasks[0].title}" else "All quests done!")
            views.setTextViewText(R.id.task_2, if (pendingTasks.size > 1) "- ${pendingTasks[1].title}" else "")
            views.setTextViewText(R.id.task_3, if (pendingTasks.size > 2) "- ${pendingTasks[2].title}" else "")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
