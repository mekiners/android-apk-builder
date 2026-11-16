package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val time: String,
    val timestamp: Long? = null,
    val type: String = "REMINDER"
)
