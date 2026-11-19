package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A "quest". gold: Easy = 1, Medium = 5, Hard = 10.
 * dueDay is a java.time epoch day (null = no date). timeMinutes = minutes after midnight (null = no time).
 * recurrence: None, Daily, Weekly, Monthly, Days (custom weekdays, see repeatDays bitmask: bit0 = Mon ... bit6 = Sun).
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "To Do",
    val gold: Int = 1,
    val dueDay: Long? = null,
    val timeMinutes: Int? = null,
    val recurrence: String = "None",
    val repeatDays: Int = 0,
    val isCompleted: Boolean = false,
    val completedDay: Long? = null
)
