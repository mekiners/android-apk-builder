package com.example.sampleapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.RewardEntity

@Composable
fun ShopScreen(
    rewards: List<RewardEntity>,
    coinBalance: Int,
    onMenuClick: () -> Unit,
    onAddReward: (String, Int) -> Unit,
    onClaimReward: (RewardEntity) -> Unit,
    onDeleteReward: (RewardEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(StardewCanvasBg)) {
        StardewWoodenBanner(title = "Rewards Shop", coinBalance = coinBalance, onMenuClick = onMenuClick)
        
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rewards) { reward ->
                    StardewPanel(contentPadding = 12.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(reward.title, modifier = Modifier.weight(1f), style = StardewTypography.bodyLarge)
                            Text("${reward.cost} 🪙", style = StardewTypography.bodyMedium, color = StardewGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            StardewButton(
                                text = "Claim",
                                onClick = { onClaimReward(reward) },
                                enabled = coinBalance >= reward.cost
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("🗑️", modifier = Modifier.padding(8.dp).then(Modifier).also { /* Add clickable modifier here if needed */ })
                        }
                    }
                }
            }
            
            StardewButton(
                text = "+ Add Custom Reward",
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var cost by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = StardewParchment,
            title = { Text("New Reward", style = StardewTypography.titleMedium) },
            text = { Column {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Reward Name") })
                TextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost (Coins)") })
            } },
            confirmButton = { 
                TextButton(onClick = { 
                    if (title.isNotBlank() && cost.toIntOrNull() != null) onAddReward(title, cost.toInt())
                    showAddDialog = false 
                }) { Text("Add", color = StardewWoodPrimary) } 
            }
        )
    }
}
