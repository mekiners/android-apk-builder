package com.example.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sampleapp.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StardewTheme {
                MainAppFrame()
            }
        }
    }
}

@Composable
fun MainAppFrame(viewModel: CommandViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val user by viewModel.user.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    
    // NEW: Get the shifts from the database
    val shifts by viewModel.shifts.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf("Quests") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = StardewParchment) {
                Spacer(Modifier.height(24.dp))
                Text("🐻 Cozy Assistant", modifier = Modifier.padding(16.dp), style = StardewTypography.titleLarge)
                Divider(color = StardewWoodHighlight, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                
                NavigationDrawerItem(label = { Text("🌾 Quests", style = StardewTypography.bodyLarge) }, selected = currentTab == "Quests", onClick = { currentTab = "Quests"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
                NavigationDrawerItem(label = { Text("📅 Shift Almanac", style = StardewTypography.bodyLarge) }, selected = currentTab == "Shifts", onClick = { currentTab = "Shifts"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
                NavigationDrawerItem(label = { Text("🛒 Market List", style = StardewTypography.bodyLarge) }, selected = currentTab == "Market", onClick = { currentTab = "Market"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
                NavigationDrawerItem(label = { Text("🎁 Rewards Shop", style = StardewTypography.bodyLarge) }, selected = currentTab == "Shop", onClick = { currentTab = "Shop"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
            }
        }
    ) {
        Scaffold(
            containerColor = StardewCanvasBg
            // NO BOTTOM BAR
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentTab) {
                    "Quests" -> DashboardScreen(
                        tasks = tasks, categories = categories, coinBalance = user?.coins ?: 0,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onAddTask = { text, cat -> viewModel.addTask(text, cat) },
                        onVoiceInputClick = { /* Will add later */ },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = { /* Will add later */ },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                    
                    // NEW: Passing the shifts and update function!
                    "Shifts" -> ShiftAlmanacScreen(
                        shifts = shifts,
                        coinBalance = user?.coins ?: 0, 
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onUpdateShift = { date, index, status -> viewModel.updateShift(date, index, status) }
                    )
                    
                    "Market" -> GroceryScreen(
                        tasks = tasks, coinBalance = user?.coins ?: 0, onMenuClick = { scope.launch { drawerState.open() } },
                        onAddTask = { text -> viewModel.addTask(text, "To Buy") },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                    
                    "Shop" -> ShopScreen(
                        rewards = rewards, coinBalance = user?.coins ?: 0, onMenuClick = { scope.launch { drawerState.open() } },
                        onAddReward = { title, cost -> viewModel.addReward(title, cost) },
                        onClaimReward = { viewModel.claimReward(it) },
                        onDeleteReward = { viewModel.deleteReward(it) }
                    )
                }
            }
        }
    }
}
