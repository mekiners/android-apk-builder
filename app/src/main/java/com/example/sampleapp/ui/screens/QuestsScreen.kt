package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.CategoryEntity
import com.example.sampleapp.data.TaskEntity
import com.example.sampleapp.ui.theme.*

@Composable
fun QuestsScreen(
    todayTasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    today: Long,
    onToggle: (TaskEntity) -> Unit,
    onUpdate: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    var editing by remember { mutableStateOf<TaskEntity?>(null) }

    val overdue = todayTasks.filter { !it.isCompleted && (it.dueDay ?: today) < today }
    val due = todayTasks.filter { !it.isCompleted && it.dueDay == today }
    val done = todayTasks.filter { it.isCompleted }
    val totalGold = todayTasks.sumOf { it.gold }
    val earnedGold = done.sumOf { it.gold }
    val fraction = if (totalGold == 0) 0f else earnedGold.toFloat() / totalGold

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(StardewCanvasBg),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark) {
                Text("${done.size} of ${todayTasks.size} daily quests complete!", style = StardewTypography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(14.dp)
                        .background(StardewParchment).border(2.dp, StardewWoodDark)
                ) {
                    Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(StardewGreen))
                }
                Spacer(Modifier.height(4.dp))
                Text("Gold earned: ${earnedGold}g of ${totalGold}g", style = StardewTypography.bodySmall)
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                Text(
                    "No quests for today! Add some on the Home tab, or enjoy a cozy break \u2615",
                    style = StardewTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
        }

        if (overdue.isNotEmpty()) {
            item { SectionLabel("Overdue") }
            items(overdue, key = { it.id }) { t ->
                QuestCard(t, today, { onToggle(t) }, { editing = t }, { onDelete(t) })
            }
        }
        if (due.isNotEmpty()) {
            item { SectionLabel("Today") }
            items(due, key = { it.id }) { t ->
                QuestCard(t, today, { onToggle(t) }, { editing = t }, { onDelete(t) })
            }
        }
        if (done.isNotEmpty()) {
            item { SectionLabel("Done") }
            items(done, key = { it.id }) { t ->
                QuestCard(t, today, { onToggle(t) }, { editing = t }, { onDelete(t) })
            }
        }
    }

    editing?.let { task ->
        QuestEditorDialog(task, categories, today, onSave = onUpdate, onDismiss = { editing = null })
    }
}
