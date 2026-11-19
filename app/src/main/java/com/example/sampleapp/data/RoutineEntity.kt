package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A named workout routine (e.g. "Push Day"). Its exercises are WorkoutEntity rows with routineId = id. */
@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
