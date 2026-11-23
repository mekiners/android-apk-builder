package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per calendar day. slots is a bitmask: bit0 = Day, bit1 = Eve, bit2 = Mid. 0 rows are not stored. */
@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val epochDay: Long,
    val slots: Int
)
