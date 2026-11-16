package com.example.sampleapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.sampleapp.*
import com.example.sampleapp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CommandViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "assistant-db"
    ).build()

    val reminders: StateFlow<List<ReminderEntity>> = db.reminderDao()
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<ReminderEntity>> = reminders.map { list ->
        list.filter { it.type == "NOTE" || it.type == "TODO" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun executeCommand(raw: String) {
        val parsed = CommandParser.parse(raw) ?: return

        val timeLabel = if (parsed.timestamp != null) {
            SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                .format(Date(parsed.timestamp))
        } else ""

        val entity = ReminderEntity(
            text = parsed.text,
            time = timeLabel,
            timestamp = parsed.timestamp,
            type = parsed.type.name
        )

        viewModelScope.launch {
            db.reminderDao().insert(entity)

            if (parsed.type == CommandType.REMINDER && parsed.timestamp != null) {
                AlarmScheduler.schedule(
                    getApplication(),
                    parsed.timestamp,
                    parsed.text,
                    entity.id.toInt()
                )
            }
        }
    }
}
