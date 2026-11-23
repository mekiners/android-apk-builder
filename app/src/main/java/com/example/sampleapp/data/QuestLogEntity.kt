package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per quest completion, so recaps and the heatmap keep history even for repeating quests. */
@Entity(tableName = "quest_log")
data class QuestLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: Long,
    val gold: Int,
    val taskId: Long
)
