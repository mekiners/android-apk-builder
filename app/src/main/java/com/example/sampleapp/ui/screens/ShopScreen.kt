package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.RewardEntity
import com.example.sampleapp.ui.theme.*

private val REWARD_IDEAS = listOf(
    "Bubble tea" to 15, "Guilt-free nap" to 10, "Takeout night" to 40, "Movie night" to 35,
    "Sleep in (no alarm)" to 25, "One episode of a show" to 8, "30 min of gaming" to 10,
    "Fancy coffee" to 20, "New book" to 60, "Ice cream" to 12, "Slow morning with a cozy drink" to 15,
    "Buy a small treat" to 30, "Skip a chore" to 20, "Day trip" to 120, "Spa / massage" to 150
)

@Composable
fun ShopScreen(
    rewards: List<RewardEntity>,
    coins: Int,
    onAddReward: (String, Int) -> Unit,
    onDeleteReward: (RewardEntity) -> Unit,
    onRedeem: (RewardEntity) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var idea by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var toRedeem by remember { mutableStateOf<RewardEntity?>(null) }

    fun generate() {
        val fresh = REWARD_IDEAS.filter { p -> rewards.none { it.title.equals(p.first, ignoreCase = true) } }
        idea = (if (fresh.isEmpty()) REWARD_IDEAS else fresh).random()
    }

    Column(modifier = Modifier.fillMaxSize().background(StardewCanvasBg)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                StardewPanel(modifier = Modifier.fillMaxWidth()) {
                    Text("Welcome to the shop!", style = StardewTypography.titleMedium)
                    Text("You have ${coins}g to spend. Finish quests to earn more.", style = StardewTypography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    StardewButton("\u2728 Generate Reward Idea", { generate() }, modifier = Modifier.fillMaxWidth(), color = StardewGreen)
                }
            }

            idea?.let { (title, cost) ->
                item {
                    StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark) {
                        Text("How about...", style = StardewTypography.labelSmall)
                        Text(title, style = StardewTypography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        GoldBadge(cost)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StardewButton("Add to shop", { onAddReward(title, cost); idea = null })
                            StardewButton("Another", { generate() }, color = StardewWoodHighlight)
                        }
                    }
                }
            }

            if (rewards.isEmpty()) {
                item {
                    Text(
                        "The shelves are empty. Generate an idea or add your own reward!",
                        style = StardewTypography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }

            items(rewards, key = { it.id }) { reward ->
                val canAfford = coins >= reward.cost
                StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 10.dp) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reward.title, style = StardewTypography.bodyLarge)
                            Spacer(Modifier.height(4.dp))
                            GoldBadge(reward.cost)
                        }
                        StardewButton(
                            text = "Redeem",
                            onClick = { toRedeem = reward },
                            color = if (canAfford) StardewWoodPrimary else StardewTextSecondary
                        )
                        IconButton(onClick = { onDeleteReward(reward) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StardewTerracotta)
                        }
                    }
                }
            }
        }

        StardewButton(
            text = "+ Add Custom Reward",
            onClick = { showAdd = true },
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var cost by remember { mutableStateOf("") }
        StardewDialog(
            title = "New Reward",
            confirmText = "Add",
            onDismiss = { showAdd = false },
            onConfirm = {
                val c = cost.toIntOrNull()
                if (title.isNotBlank() && c != null && c > 0) onAddReward(title, c)
                showAdd = false
            }
        ) {
            StardewTextField(value = title, onValueChange = { title = it }, placeholder = "Reward (e.g. Ice cream)")
            StardewTextField(value = cost, onValueChange = { cost = it.filter { ch -> ch.isDigit() } }, placeholder = "Cost in gold", keyboardType = KeyboardType.Number)
        }
    }

    toRedeem?.let { reward ->
        StardewDialog(
            title = "Redeem reward?",
            confirmText = "Redeem",
            onDismiss = { toRedeem = null },
            onConfirm = { onRedeem(reward); toRedeem = null }
        ) {
            Text("Spend ${reward.cost}g on \"${reward.title}\"?", style = StardewTypography.bodyMedium)
        }
    }
}
