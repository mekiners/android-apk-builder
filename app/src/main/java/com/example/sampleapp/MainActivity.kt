package com.example.sampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sampleapp.ui.CommandViewModel
import com.example.sampleapp.ui.screens.*
import com.example.sampleapp.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: CommandViewModel by viewModels()

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val startTab = intent.getIntExtra("tab", 0).coerceIn(0, 6)
        setContent { StardewTheme { CozyAssistantApp(viewModel, startTab) } }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDay()
    }
}

private class TabInfo(val sprite: Sprite, val label: String, val title: String)

private val TABS = listOf(
    TabInfo(Sprites.house, "Home", "Dashboard"),
    TabInfo(Sprites.scroll, "Quests", "Daily Quests"),
    TabInfo(Sprites.bag, "Shop", "Rewards Shop"),
    TabInfo(Sprites.basket, "Market", "Market List"),
    TabInfo(Sprites.calendar, "Shifts", "Shift Almanac"),
    TabInfo(Sprites.dumbbell, "Gym", "Workout Log"),
    TabInfo(Sprites.chart, "Stats", "Insights")
)

@Composable
fun CozyAssistantApp(vm: CommandViewModel, initialTab: Int = 0) {
    val tasks by vm.tasks.collectAsState()
    val todayTasks by vm.todayTasks.collectAsState()
    val categories by vm.categories.collectAsState()
    val user by vm.user.collectAsState()
    val rewards by vm.rewards.collectAsState()
    val groceries by vm.groceries.collectAsState()
    val workouts by vm.workouts.collectAsState()
    val routines by vm.routines.collectAsState()
    val shifts by vm.shifts.collectAsState()
    val timeLogs by vm.timeLogs.collectAsState()
    val leaveDays by vm.leaveDays.collectAsState()
    val questLog by vm.questLog.collectAsState()
    val workoutLogs by vm.workoutLogs.collectAsState()
    val settings by vm.settings.collectAsState()
    val undo by vm.undo.collectAsState()
    val today by vm.today.collectAsState()

    var tab by rememberSaveable { mutableStateOf(initialTab) }
    val coins = user?.coins ?: 0
    val tex = LocalTextures.current
    val kind = ScreenKind.values()[tab]
    val look = if (kind == ScreenKind.GYM) tex.stoneLook() else tex.woodLook()

    CompositionLocalProvider(LocalPanelLook provides look) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { StardewBanner(title = TABS[tab].title, coinBalance = coins, onCoinClick = { tab = 2 }, titleIcon = TABS[tab].sprite) },
                bottomBar = { StardewDock(TABS.map { it.sprite to it.label }, tab, { tab = it }) }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize().screenBackground(kind, tex)) {
                    when (tab) {
                        0 -> DashboardScreen(
                            tasks = tasks,
                            todayTasks = todayTasks,
                            categories = categories,
                            shifts = shifts,
                            today = today,
                            onAddQuest = vm::addQuest,
                            onToggle = vm::toggleTask,
                            onUpdate = vm::updateTask,
                            onDelete = vm::deleteTask,
                            onClearCompleted = vm::clearCompletedTasks,
                            onAddCategory = vm::addCategory,
                            onDeleteCategory = vm::deleteCategory
                        )
                        1 -> QuestsScreen(
                            todayTasks = todayTasks,
                            categories = categories,
                            today = today,
                            onToggle = vm::toggleTask,
                            onUpdate = vm::updateTask,
                            onDelete = vm::deleteTask
                        )
                        2 -> ShopScreen(
                            rewards = rewards,
                            coins = coins,
                            onAddReward = vm::addReward,
                            onDeleteReward = vm::deleteReward,
                            onRedeem = vm::redeemReward
                        )
                        3 -> MarketScreen(
                            groceries = groceries,
                            currency = settings.currency,
                            onAdd = vm::addGrocery,
                            onToggle = vm::toggleGrocery,
                            onDelete = vm::deleteGrocery,
                            onSendToQuest = vm::sendGroceryToQuest,
                            onEditItem = vm::updateGroceryDetails,
                            onToggleStock = vm::toggleStock,
                            onStockUp = vm::stockUpBasket
                        )
                        4 -> ShiftScreen(
                            shifts = shifts,
                            timeLogs = timeLogs,
                            leaveDays = leaveDays,
                            overtimeAfter = settings.overtimeAfterHours,
                            onSetSlots = vm::setShiftSlots,
                            onSetLeave = vm::setLeave,
                            onPunch = vm::punch,
                            onAddLog = vm::addTimeLog,
                            onDeleteLog = vm::deleteTimeLog,
                            onSetOvertime = vm::setOvertime
                        )
                        5 -> WorkoutScreen(
                            routines = routines,
                            workouts = workouts,
                            workoutLogs = workoutLogs,
                            shifts = shifts,
                            today = today,
                            onLogSet = vm::logSet,
                            onDeleteLog = vm::deleteWorkoutLog,
                            onAddRoutine = vm::addRoutine,
                            onRenameRoutine = vm::renameRoutine,
                            onDeleteRoutine = vm::deleteRoutine,
                            onAddWorkout = vm::addWorkout,
                            onUpdateWorkout = vm::updateWorkout,
                            onToggleWorkout = vm::toggleWorkout,
                            onDeleteWorkout = vm::deleteWorkout
                        )
                        else -> StatsScreen(
                            shifts = shifts,
                            timeLogs = timeLogs,
                            questLog = questLog,
                            workoutLogs = workoutLogs,
                            groceries = groceries,
                            leaveDays = leaveDays,
                            settings = settings,
                            onSaveSettings = vm::updateSettings
                        )
                    }
                    undo?.let { u ->
                        UndoBar(
                            message = u.message,
                            onUndo = vm::performUndo,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                        )
                    }
                }
            }
            VineOverlay(kind)
        }
    }
}
