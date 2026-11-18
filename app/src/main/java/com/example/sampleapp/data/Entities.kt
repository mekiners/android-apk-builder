package com.example.cozyassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val isCompleted: Boolean = false
)

@Entity(tableName = "market_items")
data class MarketItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val cost: Int
)

@Entity(tableName = "shifts")
data class ShiftRecord(
    @PrimaryKey val dateString: String, // Store as "YYYY-MM-DD" for easy sorting
    val morning: Int = 0, // 0=Off, 1=Day, 2=Evening, 3=Night
    val evening: Int = 0,
    val night: Int = 0
)
