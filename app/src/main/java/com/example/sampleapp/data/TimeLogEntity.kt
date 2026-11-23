package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One time-in / time-out session. shiftDay = epoch day the shift started on (a Mid shift that ends the next
 * morning still belongs to the day it started). slot: 0 Day, 1 Eve, 2 Mid, -1 = not matched to a scheduled shift.
 * outMillis == null means still timed in.
 */
@Entity(tableName = "time_logs")
data class TimeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shiftDay: Long,
    val slot: Int,
    val inMillis: Long,
    val outMillis: Long? = null
)
