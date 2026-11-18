package com.example.cozyassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cozyassistant.ui.screens.DashboardScreen
import com.example.cozyassistant.ui.screens.ShiftAlmanacScreen
import com.example.cozyassistant.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StardewTheme {
                CozyAppMain()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CozyAppMain() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Fake coin balance for now (will connect to DB later)
    var coinBalance by remember { mutableStateOf(150) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = StardewParchment,
                drawerContentColor = StardewTextPrimary,
                modifier = Modifier.width(280.dp)
            ) {
                // Sidebar Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StardewWoodPrimary)
                        .padding(16.dp)
                ) {
                    Text("Cozy Assistant", style = StardewTypography.titleLarge, color = StardewParchment)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sidebar Buttons
                DrawerButton("Daily Quests") {
                    navController.navigate("dashboard")
                    scope.launch { drawerState.close() }
                }
                DrawerButton("Shift Almanac") {
                    navController.navigate("almanac")
                    scope.launch { drawerState.close() }
                }
                DrawerButton("Market List") {
                    navController.navigate("market")
                    scope.launch { drawerState.close() }
                }
                DrawerButton("Rewards Shop") {
                    navController.navigate("rewards")
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        // Main App Area
        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                // We will build the full screen here later
                PlaceholderScreen("Daily Quests", coinBalance) { scope.launch { drawerState.open() } }
            }
            composable("almanac") {
                PlaceholderScreen("Shift Almanac", coinBalance) { scope.launch { drawerState.open() } }
            }
            composable("market") {
                PlaceholderScreen("Market List", coinBalance) { scope.launch { drawerState.open() } }
            }
            composable("rewards") {
                PlaceholderScreen("Rewards Shop", coinBalance) { scope.launch { drawerState.open() } }
            }
        }
    }
}

@Composable
fun DrawerButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text, style = StardewTypography.titleMedium)
    }
}

@Composable
fun PlaceholderScreen(title: String, coins: Int, onMenuClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(StardewCanvasBg)) {
        StardewWoodenBanner(title = title, coinBalance = coins, onMenuClick = onMenuClick)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Build $title Here", style = StardewTypography.titleLarge)
        }
    }
}
