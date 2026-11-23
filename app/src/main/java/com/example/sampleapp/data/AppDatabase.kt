package com.example.sampleapp.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        TaskEntity::class, CategoryEntity::class, UserEntity::class, RewardEntity::class,
        GroceryEntity::class, WorkoutEntity::class, ShiftEntity::class, RoutineEntity::class,
        TimeLogEntity::class, GroceryRuleEntity::class, QuestLogEntity::class, LeaveDayEntity::class,
        WorkoutLogEntity::class
    ],
    version = 8,
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
    abstract fun routineDao(): RoutineDao
    abstract fun timeLogDao(): TimeLogDao
    abstract fun groceryRuleDao(): GroceryRuleDao
    abstract fun questLogDao(): QuestLogDao
    abstract fun leaveDao(): LeaveDao
    abstract fun workoutLogDao(): WorkoutLogDao

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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/** Keeps your existing quests, shifts, gold, etc. when updating from the previous version. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN repeatDays INTEGER NOT NULL DEFAULT 0")
        database.execSQL("CREATE TABLE IF NOT EXISTS `routines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        database.execSQL("ALTER TABLE workouts ADD COLUMN routineId INTEGER NOT NULL DEFAULT 0")
        // Existing exercises move into a default routine so nothing is lost.
        database.execSQL("INSERT INTO routines (name) SELECT 'My Routine' WHERE EXISTS (SELECT 1 FROM workouts)")
        database.execSQL("UPDATE workouts SET routineId = (SELECT id FROM routines ORDER BY id LIMIT 1)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `time_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shiftDay` INTEGER NOT NULL, `slot` INTEGER NOT NULL, `inMillis` INTEGER NOT NULL, `outMillis` INTEGER)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `grocery_rules` (`ruleKey` TEXT NOT NULL, `section` TEXT NOT NULL, PRIMARY KEY(`ruleKey`))")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE groceries ADD COLUMN quantity TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE groceries ADD COLUMN price REAL")
        database.execSQL("ALTER TABLE groceries ADD COLUMN inStock INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE groceries ADD COLUMN boughtDay INTEGER")
        database.execSQL("CREATE TABLE IF NOT EXISTS `quest_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `day` INTEGER NOT NULL, `gold` INTEGER NOT NULL, `taskId` INTEGER NOT NULL)")
        database.execSQL("CREATE TABLE IF NOT EXISTS `leave_days` (`epochDay` INTEGER NOT NULL, `kind` TEXT NOT NULL, PRIMARY KEY(`epochDay`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `workout_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `day` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `exerciseName` TEXT NOT NULL, `weightKg` REAL, `reps` INTEGER)")
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

    @Query("SELECT * FROM groceries")
    suspend fun getAllOnce(): List<GroceryEntity>

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
    suspend fun insert(w: WorkoutEntity): Long

    @Query("DELETE FROM workouts WHERE routineId = :routineId")
    suspend fun deleteForRoutine(routineId: Long)

    @Update
    suspend fun update(w: WorkoutEntity)

    @Delete
    suspend fun delete(w: WorkoutEntity)
}

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts")
    fun getAll(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts")
    suspend fun getAllOnce(): List<ShiftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(s: ShiftEntity)

    @Query("DELETE FROM shifts WHERE epochDay = :day")
    suspend fun delete(day: Long)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY id ASC")
    fun getAll(): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(r: RoutineEntity): Long

    @Update
    suspend fun update(r: RoutineEntity)

    @Delete
    suspend fun delete(r: RoutineEntity)
}

@Dao
interface TimeLogDao {
    @Query("SELECT * FROM time_logs ORDER BY inMillis DESC")
    fun getAll(): Flow<List<TimeLogEntity>>

    @Query("SELECT * FROM time_logs ORDER BY inMillis DESC")
    suspend fun getAllOnce(): List<TimeLogEntity>

    @Query("SELECT * FROM time_logs WHERE outMillis IS NULL ORDER BY inMillis DESC LIMIT 1")
    suspend fun getOpenOnce(): TimeLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: TimeLogEntity): Long

    @Update
    suspend fun update(log: TimeLogEntity)

    @Delete
    suspend fun delete(log: TimeLogEntity)
}

@Dao
interface GroceryRuleDao {
    @Query("SELECT * FROM grocery_rules")
    fun getAll(): Flow<List<GroceryRuleEntity>>

    @Query("SELECT * FROM grocery_rules")
    suspend fun getAllOnce(): List<GroceryRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: GroceryRuleEntity)
}

@Dao
interface QuestLogDao {
    @Query("SELECT * FROM quest_log")
    fun getAll(): Flow<List<QuestLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: QuestLogEntity): Long

    @Query("DELETE FROM quest_log WHERE id = (SELECT id FROM quest_log WHERE taskId = :taskId AND day = :day ORDER BY id DESC LIMIT 1)")
    suspend fun deleteLatest(taskId: Long, day: Long)
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_days")
    fun getAll(): Flow<List<LeaveDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: LeaveDayEntity)

    @Query("DELETE FROM leave_days WHERE epochDay = :day")
    suspend fun delete(day: Long)
}

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY day DESC, id DESC")
    fun getAll(): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WorkoutLogEntity): Long

    @Delete
    suspend fun delete(log: WorkoutLogEntity)

    @Query("DELETE FROM workout_logs WHERE exerciseId = :exerciseId AND day = :day AND weightKg IS NULL")
    suspend fun deleteTicks(exerciseId: Long, day: Long)

    @Query("SELECT COUNT(*) FROM workout_logs WHERE exerciseId = :exerciseId AND day = :day AND weightKg IS NULL")
    suspend fun countTicks(exerciseId: Long, day: Long): Int

    @Query("DELETE FROM workout_logs WHERE exerciseId = :exerciseId")
    suspend fun deleteForExercise(exerciseId: Long)
}
