package com.example.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Navigation state: Quests, Shifts, Market, Shop
    var currentTab by remember { mutableStateOf("Quests") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = StardewParchment) {
                Spacer(Modifier.height(24.dp))
                Text("🐻 Cozy Assistant", modifier = Modifier.padding(16.dp), style = StardewTypography.titleLarge)
                Divider(color = StardewWoodHighlight, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                
                NavigationDrawerItem(label = { Text("🌾 Quests") }, selected = currentTab == "Quests", onClick = { currentTab = "Quests"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
                NavigationDrawerItem(label = { Text("📅 Shift Almanac") }, selected = currentTab == "Shifts", onClick = { currentTab = "Shifts"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
                NavigationDrawerItem(label = { Text("🛒 Market List") }, selected = currentTab == "Market", onClick = { currentTab = "Market"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
                NavigationDrawerItem(label = { Text("🎁 Rewards Shop") }, selected = currentTab == "Shop", onClick = { currentTab = "Shop"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = StardewParchmentDark))
            }
        }
    ) {
        Scaffold(
            containerColor = StardewCanvasBg,
            bottomBar = {
                // Bottom Navigation Dock
                Row(
                    modifier = Modifier.fillMaxWidth().background(StardewWoodPrimary).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Quests", "Shifts", "Market", "Shop").forEach { tab ->
                        val isSelected = currentTab == tab
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) StardewGold else StardewWoodPrimary)
                                .clickable { currentTab = tab }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab,
                                style = StardewTypography.labelSmall,
                                color = if (isSelected) StardewWoodDark else StardewParchment
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentTab) {
                    "Quests" -> DashboardScreen(
                        tasks = tasks, categories = categories, coinBalance = user?.coins ?: 0,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onAddTask = { text, cat -> viewModel.addTask(text, cat) },
                        onVoiceInputClick = { /* Add voice intent later if needed */ },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = { /* Re-implement Edit Dialog if needed, omitted for brevity in this massive UI shift */ },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                    "Shifts" -> ShiftAlmanacScreen(coinBalance = user?.coins ?: 0, onMenuClick = { scope.launch { drawerState.open() } })
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
