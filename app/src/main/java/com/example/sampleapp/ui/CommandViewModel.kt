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
import com.example.sampleapp.util.AppSettings
import com.example.sampleapp.util.GroceryLogic
import com.example.sampleapp.util.SettingsStore
import com.example.sampleapp.util.Stats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val groceryRules = db.groceryRuleDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val timeLogs = db.timeLogDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val routines = db.routineDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val questLog = db.questLogDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val leaveDays = db.leaveDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val workoutLogs = db.workoutLogDao().getAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val settingsStore = SettingsStore(application)
    private val _settings = MutableStateFlow(settingsStore.load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun updateSettings(s: AppSettings) {
        settingsStore.save(s)
        _settings.value = s
    }

    // ---------------------------------------------------------------- Undo for deletes

    class UndoState(val message: String, val restore: suspend () -> Unit)

    private val _undo = MutableStateFlow<UndoState?>(null)
    val undo: StateFlow<UndoState?> = _undo.asStateFlow()
    private var undoJob: Job? = null

    private fun offerUndo(message: String, restore: suspend () -> Unit) {
        val state = UndoState(message, restore)
        _undo.value = state
        undoJob?.cancel()
        undoJob = viewModelScope.launch {
            delay(7000)
            if (_undo.value === state) _undo.value = null
        }
    }

    fun performUndo() {
        val state = _undo.value ?: return
        _undo.value = null
        undoJob?.cancel()
        viewModelScope.launch {
            state.restore()
            refreshWidget()
            toast("Restored")
        }
    }

    fun dismissUndo() {
        _undo.value = null
        undoJob?.cancel()
    }

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
                val day = _today.value
                db.taskDao().updateTask(task.copy(isCompleted = true, completedDay = day))
                db.userDao().addCoins(task.gold)
                db.questLogDao().insert(QuestLogEntity(day = day, gold = task.gold, taskId = task.id))
                AlarmScheduler.cancel(app, task.id.toInt())
                toast("Quest complete! +${task.gold}g")
            } else {
                val reopened = task.copy(isCompleted = false, completedDay = null)
                db.taskDao().updateTask(reopened)
                db.userDao().addCoins(-task.gold)
                db.questLogDao().deleteLatest(task.id, task.completedDay ?: _today.value)
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
            offerUndo("Deleted \"${task.title}\"") {
                db.taskDao().insertTask(task)
                scheduleAlarm(task)
            }
        }
    }

    fun clearCompletedTasks() {
        val removed = tasks.value.filter { it.isCompleted && it.recurrence == "None" }
        viewModelScope.launch {
            db.taskDao().deleteCompletedTasks()
            refreshWidget()
            if (removed.isNotEmpty()) {
                offerUndo("Cleared ${removed.size} finished quests") {
                    removed.forEach { db.taskDao().insertTask(it) }
                }
            }
        }
    }

    fun addCategory(name: String, icon: String) {
        val n = name.trim()
        if (n.isBlank()) return
        viewModelScope.launch { db.categoryDao().insertCategory(CategoryEntity(n, icon)) }
    }

    fun deleteCategory(name: String) {
        val cat = categories.value.firstOrNull { it.name == name }
        viewModelScope.launch {
            db.categoryDao().deleteCategory(name)
            if (cat != null) offerUndo("Deleted category \"$name\"") { db.categoryDao().insertCategory(cat) }
        }
    }

    // ---------------------------------------------------------------- Rewards

    fun addReward(title: String, cost: Int) {
        if (title.isBlank() || cost <= 0) return
        viewModelScope.launch { db.rewardDao().insertReward(RewardEntity(title = title.trim(), cost = cost)) }
    }

    fun deleteReward(reward: RewardEntity) {
        viewModelScope.launch {
            db.rewardDao().deleteReward(reward)
            offerUndo("Deleted reward \"${reward.title}\"") { db.rewardDao().insertReward(reward) }
        }
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

    fun addGrocery(name: String, section: String, quantity: String = "", price: Double? = null) {
        val n = name.trim()
        if (n.isBlank()) return
        viewModelScope.launch {
            val key = GroceryLogic.normalize(n)
            val inPantry = groceries.value.firstOrNull { it.inStock && GroceryLogic.normalize(it.name) == key }
            if (inPantry != null) {
                toast("\"$n\" is already in your pantry")
                return@launch
            }
            val learned = db.groceryRuleDao().getAllOnce().associate { it.ruleKey to it.section }
            val sec = if (section == "Auto") GroceryLogic.detect(n, learned) else section
            if (section != "Auto") learnSection(n, section)
            db.groceryDao().insert(GroceryEntity(name = n, section = sec, quantity = quantity.trim(), price = price))
            refreshWidget()
        }
    }

    /** Remembers a hand-picked section so the same item name lands there automatically next time. */
    private suspend fun learnSection(name: String, section: String) {
        val key = GroceryLogic.normalize(name)
        if (key.isBlank()) return
        val existing = db.groceryRuleDao().getAllOnce().firstOrNull { it.ruleKey == key }
        if (existing?.section == section) return
        // Nothing to remember if the built-in guess already agrees and there is no older rule to replace.
        if (existing == null && GroceryLogic.detect(name) == section) return
        db.groceryRuleDao().upsert(GroceryRuleEntity(key, section))
        toast("Learned: \"$name\" goes to $section")
    }

    fun updateGroceryDetails(item: GroceryEntity, section: String, quantity: String, price: Double?) {
        viewModelScope.launch {
            db.groceryDao().update(item.copy(section = section, quantity = quantity.trim(), price = price))
            if (section != item.section) learnSection(item.name, section)
            refreshWidget()
        }
    }

    fun toggleGrocery(item: GroceryEntity) {
        viewModelScope.launch {
            val nowBought = !item.isBought
            db.groceryDao().update(item.copy(isBought = nowBought, boughtDay = if (nowBought) _today.value else null))
            refreshWidget()
        }
    }

    fun deleteGrocery(item: GroceryEntity) {
        viewModelScope.launch {
            db.groceryDao().delete(item)
            refreshWidget()
            offerUndo("Removed \"${item.name}\"") { db.groceryDao().insert(item) }
        }
    }

    /** "Done shopping": everything in the basket moves to the pantry (in stock). */
    fun stockUpBasket() {
        val basket = groceries.value.filter { it.isBought && !it.inStock }
        viewModelScope.launch {
            basket.forEach { db.groceryDao().update(it.copy(isBought = false, inStock = true)) }
            refreshWidget()
            if (basket.isNotEmpty()) toast("${basket.size} items moved to the pantry")
        }
    }

    /** Pantry toggle: mark an item as in stock, or put it back on the to-buy list. */
    fun toggleStock(item: GroceryEntity) {
        viewModelScope.launch {
            if (item.inStock) {
                db.groceryDao().update(item.copy(inStock = false, isBought = false, boughtDay = null))
            } else {
                db.groceryDao().update(item.copy(inStock = true, isBought = false))
            }
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
        val exercises = workouts.value.filter { it.routineId == r.id }
        viewModelScope.launch {
            db.workoutDao().deleteForRoutine(r.id)
            db.routineDao().delete(r)
            offerUndo("Deleted routine \"${r.name}\"") {
                db.routineDao().insert(r)
                exercises.forEach { db.workoutDao().insert(it) }
            }
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

    /** Ticking an exercise also records a gym day in the history. */
    fun toggleWorkout(w: WorkoutEntity) {
        val t = _today.value
        viewModelScope.launch {
            val done = w.doneDay == t
            db.workoutDao().update(w.copy(doneDay = if (done) null else t))
            if (done) {
                db.workoutLogDao().deleteTicks(w.id, t)
            } else if (db.workoutLogDao().countTicks(w.id, t) == 0) {
                db.workoutLogDao().insert(WorkoutLogEntity(day = t, exerciseId = w.id, exerciseName = w.name))
            }
        }
    }

    fun deleteWorkout(w: WorkoutEntity) {
        viewModelScope.launch {
            db.workoutDao().delete(w)
            offerUndo("Deleted \"${w.name}\"") { db.workoutDao().insert(w) }
        }
    }

    fun logSet(w: WorkoutEntity, weightKg: Double, reps: Int) {
        viewModelScope.launch {
            val t = _today.value
            val previousBest = Stats.bestWeight(workoutLogs.value, w.id)
            db.workoutLogDao().insert(
                WorkoutLogEntity(day = t, exerciseId = w.id, exerciseName = w.name, weightKg = weightKg, reps = reps)
            )
            if (w.doneDay != t) db.workoutDao().update(w.copy(doneDay = t))
            if (previousBest != null && weightKg > previousBest) {
                toast("New PR on ${w.name}: ${Stats.fmtKg(weightKg)} kg!")
            }
        }
    }

    fun deleteWorkoutLog(log: WorkoutLogEntity) {
        viewModelScope.launch { db.workoutLogDao().delete(log) }
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
            offerUndo("Deleted punch") { db.timeLogDao().insert(log) }
        }
    }

    // ---------------------------------------------------------------- Leave

    /** kind = "Vacation" or "Sick"; null clears the day. */
    fun setLeave(epochDay: Long, kind: String?) {
        viewModelScope.launch {
            if (kind == null) db.leaveDao().delete(epochDay)
            else db.leaveDao().upsert(LeaveDayEntity(epochDay, kind))
        }
    }
}
