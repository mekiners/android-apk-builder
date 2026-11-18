package com.example.sampleapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra("text") ?: return
        val id = intent.getIntExtra("id", 0)
        AlarmScheduler.showNotification(context, text, id)
    }
}
