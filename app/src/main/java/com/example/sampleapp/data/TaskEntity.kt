package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "To Do",
    val timestamp: Long? = null,
    val recurrence: String = "None", // None, Daily, Weekly, Monthly, etc.
    val isCompleted: Boolean = false
)
