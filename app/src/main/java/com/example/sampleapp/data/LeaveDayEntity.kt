package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A day off marked on the shift calendar. kind: "Vacation" or "Sick". */
@Entity(tableName = "leave_days")
data class LeaveDayEntity(
    @PrimaryKey val epochDay: Long,
    val kind: String
)
