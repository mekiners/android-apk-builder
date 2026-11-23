package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A section the user picked by hand for an item name (e.g. "pc" -> Tech). Used before the built-in guesses. */
@Entity(tableName = "grocery_rules")
data class GroceryRuleEntity(
    @PrimaryKey val ruleKey: String,
    val section: String
)
