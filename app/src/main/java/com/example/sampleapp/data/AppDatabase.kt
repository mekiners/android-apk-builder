package com.example.sampleapp.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        TaskEntity::class, CategoryEntity::class, UserEntity::class, RewardEntity::class,
        GroceryEntity::class, WorkoutEntity::class, ShiftEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): UserDao
    abstract fun rewardDao(): RewardDao
    abstract fun groceryDao(): GroceryDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun shiftDao(): ShiftDao

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
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, (dueDay IS NULL) ASC, dueDay ASC, (timeMinutes IS NULL) ASC, timeMinutes ASC, id ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllOnce(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Repeating quests are kept (they roll over to their next day), only one-off completed quests are cleared.
    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND recurrence = 'None'")
    suspend fun deleteCompletedTasks()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun deleteCategory(name: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserOnce(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ensureUser(user: UserEntity)

    @Query("UPDATE user_profile SET coins = MAX(0, coins + :delta) WHERE id = 1")
    suspend fun addCoins(delta: Int)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards ORDER BY cost ASC")
    fun getAllRewards(): Flow<List<RewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity)

    @Delete
    suspend fun deleteReward(reward: RewardEntity)
}

@Dao
interface GroceryDao {
    @Query("SELECT * FROM groceries ORDER BY isBought ASC, section ASC, name ASC")
    fun getAll(): Flow<List<GroceryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GroceryEntity)

    @Update
    suspend fun update(item: GroceryEntity)

    @Delete
    suspend fun delete(item: GroceryEntity)

    @Query("DELETE FROM groceries WHERE isBought = 1")
    suspend fun deleteBought()
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY id ASC")
    fun getAll(): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(w: WorkoutEntity)

    @Update
    suspend fun update(w: WorkoutEntity)

    @Delete
    suspend fun delete(w: WorkoutEntity)
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts")
    fun getAll(): Flow<List<ShiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: ShiftEntity)

    @Query("DELETE FROM shifts WHERE epochDay = :day")
    suspend fun delete(day: Long)
}
