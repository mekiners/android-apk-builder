package com.example.cozyassistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CozyDao {
    @Query("SELECT * FROM quests") fun getQuests(): Flow<List<Quest>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertQuest(quest: Quest)
    @Update suspend fun updateQuest(quest: Quest)
    @Delete suspend fun deleteQuest(quest: Quest)

    @Query("SELECT * FROM market_items") fun getMarketItems(): Flow<List<MarketItem>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMarketItem(item: MarketItem)
    @Update suspend fun updateMarketItem(item: MarketItem)

    @Query("SELECT * FROM rewards") fun getRewards(): Flow<List<Reward>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertReward(reward: Reward)

    @Query("SELECT * FROM shifts") fun getShifts(): Flow<List<ShiftRecord>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertShift(shift: ShiftRecord)
}
