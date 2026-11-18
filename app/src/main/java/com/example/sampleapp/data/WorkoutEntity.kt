package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val detail: String = "",
    val doneDay: Long? = null // epoch day it was last ticked; counts as "done" only when == today
)
