package com.example.sampleapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.example.sampleapp.data.AppDatabase
import com.example.sampleapp.util.GroceryLogic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 2x2 "To Buy" grocery widget. */
class GroceryWidget : AppWidgetProvider() {
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
        private val LINES = intArrayOf(R.id.g_line1, R.id.g_line2, R.id.g_line3, R.id.g_line4, R.id.g_line5)

        suspend fun updateNow(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, GroceryWidget::class.java))
            if (ids.isEmpty()) return

            val db = AppDatabase.getDatabase(context)
            val pending = db.groceryDao().getAllOnce()
                .filter { !it.isBought && !it.inStock }
                .sortedBy { GroceryLogic.sections.indexOf(it.section) }

            val views = RemoteViews(context.packageName, R.layout.widget_grocery)
            views.setTextViewText(R.id.g_count, "${pending.size} left")
            for (i in LINES.indices) {
                if (i < pending.size) {
                    views.setTextViewText(LINES[i], pending[i].name)
                    views.setViewVisibility(LINES[i], View.VISIBLE)
                } else {
                    views.setViewVisibility(LINES[i], View.GONE)
                }
            }
            if (pending.size > LINES.size) {
                views.setTextViewText(R.id.g_more, "+${pending.size - LINES.size} more")
                views.setViewVisibility(R.id.g_more, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.g_more, View.GONE)
            }
            views.setViewVisibility(R.id.g_empty, if (pending.isEmpty()) View.VISIBLE else View.GONE)
            views.setOnClickPendingIntent(R.id.g_root, Widgets.openApp(context, 3))
            for (id in ids) manager.updateAppWidget(id, views)
        }
    }
}
