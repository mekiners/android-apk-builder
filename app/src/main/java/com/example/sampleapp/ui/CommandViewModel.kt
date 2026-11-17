package com.example.sampleapp.ui

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.AlarmScheduler
import com.example.sampleapp.CommandParser
import com.example.sampleapp.CozyWidget
import com.example.sampleapp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class CommandViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    
    val tasks = db.taskDao().getAllTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = db.categoryDao().getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val user = db.userDao().getUser().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserEntity())
    val rewards = db.rewardDao().getAllRewards().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            categories.collect { list ->
                if (list.isEmpty()) {
                    db.categoryDao().insertCategory(CategoryEntity("To Do", "📝"))
                    db.categoryDao().insertCategory(CategoryEntity("To Buy", "🛒"))
                    db.categoryDao().insertCategory(CategoryEntity("To Make", "🛠️"))
                    db.categoryDao().insertCategory(CategoryEntity("Work", "💼"))
                }
            }
        }
    }

    private fun refreshWidget() {
        val context = getApplication<Application>()
        val intent = Intent(context, CozyWidget::class.java).apply { action = "ACTION_REFRESH_WIDGET" }
        context.sendBroadcast(intent)
    }

    fun addTask(rawText: String, categoryName: String) {
        if (rawText.isBlank()) return
        val parsed = CommandParser.parse(rawText)
        val task = TaskEntity(
            title = parsed?.text ?: rawText,
            category = categoryName,
            timestamp = parsed?.timestamp,
            recurrence = parsed?.recurrence ?: "None"
        )

        viewModelScope.launch {
            db.taskDao().insertTask(task)
            if (task.timestamp != null) {
                AlarmScheduler.schedule(getApplication(), task.timestamp, task.title, task.id.toInt())
            }
            Toast.makeText(getApplication(), "Added to $categoryName", Toast.LENGTH_SHORT).show()
            refreshWidget() 
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            db.taskDao().updateTask(updatedTask)

            if (updatedTask.isCompleted) {
                val currentUser = user.value ?: UserEntity()
                db.userDao().updateUser(currentUser.copy(coins = currentUser.coins + 1))
                
                if (task.recurrence != "None" && task.timestamp != null) {
                    val nextTimestamp = calculateNextDate(task.timestamp, task.recurrence)
                    val nextTask = task.copy(id = 0, isCompleted = false, timestamp = nextTimestamp)
                    db.taskDao().insertTask(nextTask)
                }
            } else {
                val currentUser = user.value ?: UserEntity()
                if (currentUser.coins > 0) {
                    db.userDao().updateUser(currentUser.copy(coins = currentUser.coins - 1))
                }
            }
            refreshWidget() 
        }
    }

    fun updateTaskDetails(task: TaskEntity, newTitle: String, newRecurrence: String, newTimestamp: Long?) {
        viewModelScope.launch {
            val updated = task.copy(title = newTitle, recurrence = newRecurrence, timestamp = newTimestamp)
            db.taskDao().updateTask(updated)
            if (newTimestamp != null) {
                AlarmScheduler.schedule(getApplication(), newTimestamp, newTitle, task.id.toInt())
            }
            refreshWidget() 
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { 
            db.taskDao().deleteTask(task)
            refreshWidget() 
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            db.taskDao().deleteCompletedTasks()
            refreshWidget()
        }
    }

    fun addCategory(name: String, icon: String) {
        viewModelScope.launch { db.categoryDao().insertCategory(CategoryEntity(name, icon)) }
    }

    fun addReward(title: String, cost: Int) {
        viewModelScope.launch { db.rewardDao().insertReward(RewardEntity(title = title, cost = cost)) }
    }

    // NEW: Delete Reward Function
    fun deleteReward(reward: RewardEntity) {
        viewModelScope.launch { db.rewardDao().deleteReward(reward) }
    }

    fun claimReward(reward: RewardEntity) {
        val currentUser = user.value ?: UserEntity()
        if (currentUser.coins >= reward.cost) {
            viewModelScope.launch {
                db.userDao().updateUser(currentUser.copy(coins = currentUser.coins - reward.cost))
                Toast.makeText(getApplication(), "Claimed: ${reward.title}!", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(getApplication(), "Not enough coins!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateNextDate(currentTime: Long, recurrence: String): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentTime }
        when (recurrence) {
            "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "Bi-weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 2)
            "Monthly" -> cal.add(Calendar.MONTH, 1)
            "Yearly" -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}
