package com.example.sampleapp.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sampleapp.AlarmScheduler
import com.example.sampleapp.CommandParser
import com.example.sampleapp.ShiftReminders
import com.example.sampleapp.Widgets
import com.example.sampleapp.util.TimeClock
import com.example.sampleapp.data.*
import com.example.sampleapp.util.Dates
import com.example.sampleapp.util.GroceryLogic
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class CommandViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application
    private val db = AppDatabase.getDatabase(application)

    private val _today = MutableStateFlow(LocalDate.now().toEpochDay())
    val today: StateFlow<Long> = _today.asStateFlow()

    val tasks = db.taskDao().getAllTasks().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val categories = db.categoryDao().getAllCategories().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val user = db.userDao().getUser().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val rewards = db.rewardDao().getAllRewards().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val groceries = db.groceryDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val workouts = db.workoutDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val shifts = db.shiftDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val timeLogs = db.timeLogDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val routines = db.routineDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Quests for today's list: due today or overdue, plus anything finished today. */
    val todayTasks: StateFlow<List<TaskEntity>> = combine(tasks, today) { list, t ->
        list.filter { it.dueDay != null && it.dueDay <= t && (!it.isCompleted || it.completedDay == t) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            firstRunSetup()
            rolloverRepeatingQuests()
            ShiftReminders.reschedule(app)
            Widgets.refreshAllNow(app)
        }
    }

    /** Called from MainActivity.onResume so the day rolls over even if the app stayed in memory. */
    fun refreshDay() {
        _today.value = LocalDate.now().toEpochDay()
        viewModelScope.launch { rolloverRepeatingQuests() }
    }

    private suspend fun firstRunSetup() {
        if (db.userDao().getUserOnce() != null) return
        db.userDao().ensureUser(UserEntity())
        listOf("maknae" to "", "To Do" to "", "To Buy" to "", "Work" to "")
            .forEach { db.categoryDao().insertCategory(CategoryEntity(it.first, it.second)) }
        listOf("Bubble tea" to 15, "Guilt-free nap" to 10, "Takeout night" to 40)
            .forEach { db.rewardDao().insertReward(RewardEntity(title = it.first, cost = it.second)) }
    }

    /** Repeating quests whose day has passed move to their next date and become unchecked again. */
    private suspend fun rolloverRepeatingQuests() {
        val now = LocalDate.now().toEpochDay()
        val stale = db.taskDao().getAllOnce().filter { it.recurrence != "None" && it.dueDay != null && it.dueDay < now }
        for (t in stale) {
            var d = t.dueDay ?: continue
            while (d < now) d = Dates.nextOccurrence(d, t.recurrence, t.repeatDays)
            val next = t.copy(dueDay = d, isCompleted = false)
            db.taskDao().updateTask(next)
            scheduleAlarm(next)
        }
        if (stale.isNotEmpty()) refreshWidget()
    }

    private fun toast(msg: String) = Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()

    private fun refreshWidget() {
        Widgets.refreshAll(app)
    }

    private fun scheduleAlarm(task: TaskEntity) {
        if (task.isCompleted) return
        val d = task.dueDay ?: return
        val m = task.timeMinutes ?: return
        val millis = Dates.toMillis(d, m)
        if (millis > System.currentTimeMillis()) {
            AlarmScheduler.schedule(app, millis, task.title, task.id.toInt())
        }
    }

    // ---------------------------------------------------------------- Quests

    fun addQuest(raw: String, category: String, gold: Int, recurrence: String, repeatDays: Int, dueDay: Long?, timeMinutes: Int?) {
        if (raw.isBlank()) return
        val p = CommandParser.parse(raw)
        val t = _today.value
        val rec = p.recurrence ?: recurrence
        val days = if (p.recurrence != null) 0 else repeatDays
        val time = p.timeMinutes ?: timeMinutes
        var due = p.dayOffset?.let { t + it } ?: dueDay
        if (due == null && (rec != "None" || time != null)) due = t
        val task = TaskEntity(
            title = p.title,
            category = category,
            gold = p.gold ?: gold,
            recurrence = rec,
            repeatDays = days,
            dueDay = due,
            timeMinutes = time
        ).let { normalize(it) }
        viewModelScope.launch {
            val id = db.taskDao().insertTask(task)
            scheduleAlarm(task.copy(id = id))
            toast("Quest added to $category!")
            refreshWidget()
        }
    }

    /** Custom weekday repeats start on the first matching day. */
    private fun normalize(task: TaskEntity): TaskEntity {
        val d = task.dueDay
        return if (task.recurrence == "Days" && d != null) {
            task.copy(dueDay = Dates.alignToRepeatDays(d, task.repeatDays))
        } else task
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            if (!task.isCompleted) {
                db.taskDao().updateTask(task.copy(isCompleted = true, completedDay = _today.value))
                db.userDao().addCoins(task.gold)
                AlarmScheduler.cancel(app, task.id.toInt())
                toast("Quest complete! +${task.gold}g")
            } else {
                val reopened = task.copy(isCompleted = false, completedDay = null)
                db.taskDao().updateTask(reopened)
                db.userDao().addCoins(-task.gold)
                scheduleAlarm(reopened)
            }
            refreshWidget()
        }
    }

    fun updateTask(edited: TaskEntity) {
        val task = normalize(edited)
        viewModelScope.launch {
            db.taskDao().updateTask(task)
            AlarmScheduler.cancel(app, task.id.toInt())
            scheduleAlarm(task)
            refreshWidget()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancel(app, task.id.toInt())
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
        val n = name.trim()
        if (n.isBlank()) return
        viewModelScope.launch { db.categoryDao().insertCategory(CategoryEntity(n, icon)) }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch { db.categoryDao().deleteCategory(name) }
    }

    // ---------------------------------------------------------------- Rewards

    fun addReward(title: String, cost: Int) {
        if (title.isBlank() || cost <= 0) return
        viewModelScope.launch { db.rewardDao().insertReward(RewardEntity(title = title.trim(), cost = cost)) }
    }

    fun deleteReward(reward: RewardEntity) {
        viewModelScope.launch { db.rewardDao().deleteReward(reward) }
    }

    fun redeemReward(reward: RewardEntity) {
        val coins = user.value?.coins ?: 0
        if (coins >= reward.cost) {
            viewModelScope.launch {
                db.userDao().addCoins(-reward.cost)
                Toast.makeText(app, "Redeemed: ${reward.title}! Enjoy!", Toast.LENGTH_LONG).show()
            }
        } else {
            toast("Need ${reward.cost - coins}g more!")
        }
    }

    // ---------------------------------------------------------------- Market

    fun addGrocery(name: String, section: String) {
        val n = name.trim()
        if (n.isBlank()) return
        val sec = if (section == "Auto") GroceryLogic.detect(n) else section
        viewModelScope.launch {
            db.groceryDao().insert(GroceryEntity(name = n, section = sec))
            refreshWidget()
        }
    }

    fun toggleGrocery(item: GroceryEntity) {
        viewModelScope.launch {
            db.groceryDao().update(item.copy(isBought = !item.isBought))
            refreshWidget()
        }
    }

    fun deleteGrocery(item: GroceryEntity) {
        viewModelScope.launch {
            db.groceryDao().delete(item)
            refreshWidget()
        }
    }

    fun clearBoughtGroceries() {
        viewModelScope.launch {
            db.groceryDao().deleteBought()
            refreshWidget()
        }
    }

    /** "Buy X" appears as a quest on the dashboard and in today's quests. */
    fun sendGroceryToQuest(item: GroceryEntity) {
        viewModelScope.launch {
            db.taskDao().insertTask(
                TaskEntity(title = "Buy ${item.name}", category = "To Buy", gold = 1, dueDay = _today.value)
            )
            toast("Added to your quests!")
            refreshWidget()
        }
    }

    // ---------------------------------------------------------------- Workout

    fun addRoutine(name: String, exercises: List<Pair<String, String>>) {
        val n = name.trim()
        if (n.isBlank()) return
        viewModelScope.launch {
            val id = db.routineDao().insert(RoutineEntity(name = n))
            exercises.forEach { (ex, detail) ->
                db.workoutDao().insert(WorkoutEntity(name = ex, detail = detail, routineId = id))
            }
        }
    }

    fun renameRoutine(r: RoutineEntity, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch { db.routineDao().update(r.copy(name = newName.trim())) }
    }

    fun deleteRoutine(r: RoutineEntity) {
        viewModelScope.launch {
            db.workoutDao().deleteForRoutine(r.id)
            db.routineDao().delete(r)
        }
    }

    fun addWorkout(routineId: Long, name: String, detail: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            db.workoutDao().insert(WorkoutEntity(name = name.trim(), detail = detail.trim(), routineId = routineId))
        }
    }

    fun updateWorkout(w: WorkoutEntity) {
        viewModelScope.launch { db.workoutDao().update(w) }
    }

    fun toggleWorkout(w: WorkoutEntity) {
        val t = _today.value
        viewModelScope.launch { db.workoutDao().update(w.copy(doneDay = if (w.doneDay == t) null else t)) }
    }

    fun deleteWorkout(w: WorkoutEntity) {
        viewModelScope.launch { db.workoutDao().delete(w) }
    }

    // ---------------------------------------------------------------- Shifts

    fun setShiftSlots(epochDay: Long, slots: Int) {
        viewModelScope.launch {
            if (slots == 0) db.shiftDao().delete(epochDay)
            else db.shiftDao().upsert(ShiftEntity(epochDay, slots))
            ShiftReminders.reschedule(app)
            refreshWidget()
        }
    }

    // ---------------------------------------------------------------- Time clock

    fun punch() {
        viewModelScope.launch {
            val result = TimeClock.punch(db)
            toast(result.message)
            refreshWidget()
        }
    }

    fun addTimeLog(shiftDay: Long, slot: Int, inMillis: Long, outMillis: Long?) {
        viewModelScope.launch {
            db.timeLogDao().insert(TimeLogEntity(shiftDay = shiftDay, slot = slot, inMillis = inMillis, outMillis = outMillis))
            refreshWidget()
        }
    }

    fun deleteTimeLog(log: TimeLogEntity) {
        viewModelScope.launch {
            db.timeLogDao().delete(log)
            refreshWidget()
        }
    }
}
