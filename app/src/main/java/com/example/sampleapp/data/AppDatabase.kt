package com.example.sampleapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import androidx.room.*

@Database(entities = [TaskEntity::class, CategoryEntity::class, UserEntity::class, RewardEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): UserDao
    abstract fun rewardDao(): RewardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cozy_assistant_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, timestamp ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    @Update
    suspend fun updateTask(task: TaskEntity)
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUser(): Flow<UserEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<RewardEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity)
    @Delete
    suspend fun deleteReward(reward: RewardEntity)
}
