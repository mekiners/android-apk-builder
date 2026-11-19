package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groceries")
data class GroceryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val section: String = "Misc",
    val isBought: Boolean = false
)
