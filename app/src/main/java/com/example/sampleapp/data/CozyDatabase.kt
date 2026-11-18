package com.example.cozyassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Quest::class, MarketItem::class, Reward::class, ShiftRecord::class], version = 1, exportSchema = false)
abstract class CozyDatabase : RoomDatabase() {
    abstract fun cozyDao(): CozyDao

    companion object {
        @Volatile private var INSTANCE: CozyDatabase? = null
        fun getDatabase(context: Context): CozyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CozyDatabase::class.java,
                    "cozy_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
