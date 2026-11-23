package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Workout history. A row with weightKg == null is a plain "done" tick for that exercise on that day;
 * a row with weight and reps is one logged set.
 */
@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val weightKg: Double? = null,
    val reps: Int? = null
)
