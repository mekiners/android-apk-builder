package com.example.sampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
        setContent { StardewTheme { CozyAssistantApp(viewModel) } }
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
    TabInfo(Sprites.dumbbell, "Gym", "Workout Log")
)

@Composable
fun CozyAssistantApp(vm: CommandViewModel) {
    val tasks by vm.tasks.collectAsState()
    val todayTasks by vm.todayTasks.collectAsState()
    val categories by vm.categories.collectAsState()
    val user by vm.user.collectAsState()
    val rewards by vm.rewards.collectAsState()
    val groceries by vm.groceries.collectAsState()
    val workouts by vm.workouts.collectAsState()
    val routines by vm.routines.collectAsState()
    val shifts by vm.shifts.collectAsState()
    val today by vm.today.collectAsState()

    var tab by rememberSaveable { mutableStateOf(0) }
    val coins = user?.coins ?: 0

    Scaffold(
        containerColor = StardewCanvasBg,
        topBar = { StardewBanner(title = TABS[tab].title, coinBalance = coins, onCoinClick = { tab = 2 }, titleIcon = TABS[tab].sprite) },
        bottomBar = { StardewDock(TABS.map { it.sprite to it.label }, tab, { tab = it }) }
    ) { padding ->
        val m = Modifier.padding(padding)
        androidx.compose.foundation.layout.Box(modifier = m) {
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
                    onAdd = vm::addGrocery,
                    onToggle = vm::toggleGrocery,
                    onDelete = vm::deleteGrocery,
                    onSendToQuest = vm::sendGroceryToQuest,
                    onClearBought = vm::clearBoughtGroceries
                )
                4 -> ShiftScreen(shifts = shifts, onSetSlots = vm::setShiftSlots)
                else -> WorkoutScreen(
                    routines = routines,
                    workouts = workouts,
                    today = today,
                    onAddRoutine = vm::addRoutine,
                    onRenameRoutine = vm::renameRoutine,
                    onDeleteRoutine = vm::deleteRoutine,
                    onAddWorkout = vm::addWorkout,
                    onUpdateWorkout = vm::updateWorkout,
                    onToggleWorkout = vm::toggleWorkout,
                    onDeleteWorkout = vm::deleteWorkout
                )
            }
        }
    }
}
