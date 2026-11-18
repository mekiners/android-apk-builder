package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.GroceryEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.GroceryLogic

@Composable
fun MarketScreen(
    items: List<GroceryEntity>,
    onAdd: (String, String) -> Unit,
    onToggle: (GroceryEntity) -> Unit,
    onDelete: (GroceryEntity) -> Unit,
    onSendToQuest: (GroceryEntity) -> Unit,
    onClearBought: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("Auto") }

    fun submit() {
        if (input.isBlank()) return
        onAdd(input, section)
        input = ""
    }

    val pending = items.filter { !it.isBought }
    val bought = items.filter { it.isBought }
    val grouped = pending.groupBy { it.section }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(StardewCanvasBg),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StardewTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = "Add an item...",
                        modifier = Modifier.weight(1f),
                        onDone = { submit() }
                    )
                    StardewButton("+", { submit() })
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StardewChip("Auto", section == "Auto", { section = "Auto" }, selectedColor = StardewGold, selectedTextColor = StardewTextPrimary)
                    GroceryLogic.sections.forEach { s ->
                        StardewChip("${GroceryLogic.emoji(s)} $s", section == s, { section = s })
                    }
                }
            }
        }

        if (items.isEmpty()) {
            item {
                Text(
                    "Your basket is empty. Add what you need from the market!",
                    style = StardewTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
        }

        GroceryLogic.sections.forEach { s ->
            val list = grouped[s].orEmpty()
            if (list.isNotEmpty()) {
                item(key = "header_$s") { SectionLabel("${GroceryLogic.emoji(s)} $s") }
                items(list, key = { it.id }) { g ->
                    GroceryRow(g, onToggle, onDelete, onSendToQuest)
                }
            }
        }

        if (bought.isNotEmpty()) {
            item(key = "header_bought") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel("In the basket")
                    StardewChip("Clear", false, onClearBought)
                }
            }
            items(bought, key = { it.id }) { g ->
                GroceryRow(g, onToggle, onDelete, onSendToQuest)
            }
        }

        if (items.isNotEmpty()) {
            item(key = "footer") {
                Text(
                    "${bought.size} of ${items.size} items gathered!",
                    style = StardewTypography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun GroceryRow(
    item: GroceryEntity,
    onToggle: (GroceryEntity) -> Unit,
    onDelete: (GroceryEntity) -> Unit,
    onSendToQuest: (GroceryEntity) -> Unit
) {
    StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StardewCheckbox(item.isBought, { onToggle(item) })
            Spacer(Modifier.width(10.dp))
            Text(GroceryLogic.emoji(item.section))
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.name,
                style = StardewTypography.bodyLarge.copy(
                    textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (item.isBought) StardewTextSecondary else StardewTextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (!item.isBought) {
                StardewChip("\u2192 Quest", false, { onSendToQuest(item) })
            }
            IconButton(onClick = { onDelete(item) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StardewTerracotta)
            }
        }
    }
}
