package com.example.sampleapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

object Widgets {
    /** Tapping a widget opens the app on the given tab (0 Home, 1 Quests, 3 Market, 4 Shifts). */
    fun openApp(context: Context, tab: Int): PendingIntent =
        PendingIntent.getActivity(
            context, 100 + tab,
            Intent(context, MainActivity::class.java).putExtra("tab", tab).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    suspend fun refreshAllNow(context: Context) {
        CozyWidget.updateNow(context)
        GroceryWidget.updateNow(context)
        ShiftWidget.updateNow(context)
    }

    fun refreshAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch { refreshAllNow(context) }
    }

    fun todayEpoch(): Long = LocalDate.now().toEpochDay()
}
