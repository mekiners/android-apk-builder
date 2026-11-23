package com.example.sampleapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One list item. States: to buy (isBought=false, inStock=false), in the basket (isBought=true),
 * in the pantry (inStock=true). quantity is free text ("2 kg"), price is an optional estimate.
 */
@Entity(tableName = "groceries")
data class GroceryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val section: String = "Misc",
    val isBought: Boolean = false,
    val quantity: String = "",
    val price: Double? = null,
    val inStock: Boolean = false,
    val boughtDay: Long? = null
)
