package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val date: String, // Format: "2024-11-18"
    val morning: String = "OFF",
    val evening: String = "OFF",
    val night: String = "OFF"
)
