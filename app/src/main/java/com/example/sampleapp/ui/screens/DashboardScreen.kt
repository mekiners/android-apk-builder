package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sampleapp.VoiceButton
import com.example.sampleapp.data.CategoryEntity
import com.example.sampleapp.data.ShiftEntity
import com.example.sampleapp.data.TaskEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.Dates
import com.example.sampleapp.util.ShiftLogic
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    tasks: List<TaskEntity>,
    todayTasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    shifts: List<ShiftEntity>,
    today: Long,
    onAddQuest: (String, String, Int, String, Long?, Int?) -> Unit,
    onToggle: (TaskEntity) -> Unit,
    onUpdate: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onClearCompleted: () -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var gold by remember { mutableStateOf(1) }
    var recurrence by remember { mutableStateOf("None") }
    var dueDay by remember { mutableStateOf<Long?>(today) }
    var timeMinutes by remember { mutableStateOf<Int?>(null) }
    var showOptions by remember { mutableStateOf(false) }

    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var showAddCategory by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    val targetCategory = if (selectedCategory == "All") "To Do" else selectedCategory

    fun submit() {
        if (input.isBlank()) return
        onAddQuest(input, targetCategory, gold, recurrence, dueDay, timeMinutes)
        input = ""
        gold = 1
        recurrence = "None"
        dueDay = today
        timeMinutes = null
    }

    val filtered = tasks.filter { selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true) }
    val pending = filtered.filter { !it.isCompleted }
    val done = filtered.filter { it.isCompleted }

    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning, Farmer!"
        hour < 18 -> "Good afternoon, Farmer!"
        else -> "Good evening, Farmer!"
    }
    val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    val questsDone = todayTasks.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(StardewCanvasBg),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Greeting + at-a-glance
        item {
            StardewPanel(modifier = Modifier.fillMaxWidth()) {
                Text(dateText, style = StardewTypography.labelSmall)
                Text(greeting, style = StardewTypography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Today's quests: $questsDone of ${todayTasks.size} done", style = StardewTypography.bodyMedium)
                Text(
                    "\uD83D\uDCC5 " + ShiftLogic.summary(shifts, LocalDateTime.now()),
                    style = StardewTypography.bodyMedium
                )
            }
        }

        // Quick add
        item {
            StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StardewTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = "Type or speak a quest...",
                        modifier = Modifier.weight(1f),
                        onDone = { submit() }
                    )
                    VoiceButton { spoken -> input = spoken }
                    StardewButton(text = "+", onClick = { submit() })
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    val diffName = DIFFICULTIES.first { it.second == gold }.first
                    val repeatName = REPEATS.first { it.first == recurrence }.second
                    Text(
                        "$targetCategory \u00B7 $diffName \u00B7 $repeatName",
                        style = StardewTypography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    StardewChip(if (showOptions) "Options \u25B2" else "Options \u25BC", showOptions, { showOptions = !showOptions })
                }
                if (showOptions) {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionLabel("Difficulty")
                        DifficultyChips(gold) { gold = it }
                        SectionLabel("Repeat")
                        RepeatChips(recurrence) { recurrence = it }
                        SectionLabel("When")
                        WhenControls(today, dueDay, timeMinutes, { dueDay = it }, { timeMinutes = it })
                        Text(
                            "Tip: you can also say \"buy milk tomorrow at 5pm\", \"stretch daily\" or \"file report hard quest\".",
                            style = StardewTypography.bodySmall
                        )
                    }
                }
            }
        }

        // Category filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StardewChip("All", selectedCategory == "All", { selectedCategory = "All" })
                categories.forEach { c ->
                    StardewChip(
                        text = "${c.icon} ${c.name}",
                        selected = selectedCategory == c.name,
                        onClick = { selectedCategory = c.name },
                        onLongClick = { categoryToDelete = c.name }
                    )
                }
                StardewChip("+", false, { showAddCategory = true }, selectedColor = StardewGold)
            }
        }

        if (pending.isEmpty() && done.isEmpty()) {
            item {
                Text(
                    "Nothing here! Time for a cozy break \u2615",
                    style = StardewTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
        }

        items(pending, key = { it.id }) { task ->
            QuestCard(task, today, { onToggle(task) }, { editing = task }, { onDelete(task) })
        }

        if (done.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel("Completed")
                    StardewChip("Clear done", false, onClearCompleted)
                }
            }
            items(done, key = { it.id }) { task ->
                QuestCard(task, today, { onToggle(task) }, { editing = task }, { onDelete(task) })
            }
        }
    }

    editing?.let { task ->
        QuestEditorDialog(task, categories, today, onSave = onUpdate, onDismiss = { editing = null })
    }

    if (showAddCategory) {
        var name by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("\uD83D\uDCCC") }
        StardewDialog(
            title = "New Category",
            confirmText = "Add",
            onDismiss = { showAddCategory = false },
            onConfirm = { onAddCategory(name, icon); showAddCategory = false }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Name")
            StardewTextField(value = icon, onValueChange = { if (it.length <= 4) icon = it }, placeholder = "Emoji")
        }
    }

    categoryToDelete?.let { name ->
        StardewDialog(
            title = "Delete category?",
            confirmText = "Delete",
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                onDeleteCategory(name)
                if (selectedCategory == name) selectedCategory = "All"
                categoryToDelete = null
            }
        ) {
            Text("Remove \"$name\" from your categories? Quests keep their label.", style = StardewTypography.bodyMedium)
        }
    }
}
