package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
private fun SkyHeader(greeting: String, dateText: String, questLine: String, shiftLine: String, hour: Int) {
    val (top, bottom, orb) = when {
        hour in 5..11 -> Triple(Color(0xFFBFE6F2), Color(0xFFFBEFC8), Sprites.sun)
        hour in 12..16 -> Triple(Color(0xFF9ED6F0), Color(0xFFF8E8B8), Sprites.sun)
        hour in 17..19 -> Triple(Color(0xFFF2B07A), Color(0xFFF6DDB5), Sprites.sunset)
        else -> Triple(Color(0xFF8FA0D8), Color(0xFFE3D4E6), Sprites.moon)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(top, bottom)))
            .border(3.dp, StardewWoodDark)
            .padding(3.dp)
            .border(2.dp, StardewWoodHighlight)
    ) {
        PixelIcon(Sprites.cloud, 40.dp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 44.dp, top = 6.dp))
        PixelIcon(orb, 26.dp, modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 8.dp))
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(greeting, style = StardewTypography.titleMedium)
                Text(dateText, style = StardewTypography.bodySmall, color = StardewTextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PixelIcon(Sprites.scroll, 14.dp)
                Text(questLine, style = StardewTypography.bodySmall, color = StardewTextPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PixelIcon(Sprites.calendar, 14.dp)
                Text(shiftLine, style = StardewTypography.bodySmall, color = StardewTextPrimary)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    tasks: List<TaskEntity>,
    todayTasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    shifts: List<ShiftEntity>,
    today: Long,
    onAddQuest: (String, String, Int, String, Int, Long?, Int?) -> Unit,
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
    var repeatDays by remember { mutableStateOf(0) }
    var dueDay by remember { mutableStateOf<Long?>(today) }
    var timeMinutes by remember { mutableStateOf<Int?>(null) }
    var showOptions by remember { mutableStateOf(false) }

    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var showAddCategory by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    val targetCategory = if (selectedCategory == "All") "To Do" else selectedCategory

    fun submit() {
        if (input.isBlank()) return
        onAddQuest(input, targetCategory, gold, recurrence, repeatDays, dueDay, timeMinutes)
        input = ""
        gold = 1
        recurrence = "None"
        repeatDays = 0
        dueDay = today
        timeMinutes = null
    }

    val filtered = tasks.filter { selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true) }
    val pending = filtered.filter { !it.isCompleted }
    val done = filtered.filter { it.isCompleted }

    val hour = LocalTime.now().hour
    val greeting = if (hour in 5..11) "Good morning" else if (hour in 12..16) "Good afternoon" else "Good evening"
    val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    val questsDone = todayTasks.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(StardewCanvasBg),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SkyHeader(
                greeting = greeting,
                dateText = dateText,
                questLine = "Today's quests: $questsDone of ${todayTasks.size} done",
                shiftLine = ShiftLogic.summary(shifts, LocalDateTime.now()),
                hour = hour
            )
        }

        // Quick add
        item {
            StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 6.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StardewTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = "Type or speak a quest...",
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        onDone = { submit() }
                    )
                    VoiceButton(size = 40.dp) { spoken -> input = spoken }
                    StardewIconButton(Sprites.plus, { submit() }, size = 40.dp)
                }
                Spacer(Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PixelIcon(difficultySprite(gold), 13.dp)
                        Text("$gold", style = StardewTypography.labelSmall)
                        if (recurrence != "None") PixelIcon(Sprites.loop, 11.dp)
                        Text(
                            Dates.repeatLabel(recurrence, repeatDays) + " \u00B7 " + targetCategory,
                            style = StardewTypography.labelSmall,
                            maxLines = 1
                        )
                    }
                    StardewIconButton(
                        sprite = if (showOptions) Sprites.up else Sprites.down,
                        onClick = { showOptions = !showOptions },
                        size = 28.dp,
                        iconSize = 14.dp,
                        color = StardewWoodHighlight
                    )
                }
                if (showOptions) {
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SectionLabel("Reward")
                        DifficultyChips(gold) { gold = it }
                        SectionLabel("Repeat")
                        RepeatChips(recurrence, repeatDays) { r, d -> recurrence = r; repeatDays = d }
                        SectionLabel("When")
                        WhenControls(today, dueDay, timeMinutes, { dueDay = it }, { timeMinutes = it })
                        Text(
                            "Tip: say \"buy milk tomorrow at 5pm\" or \"stretch daily\".",
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
                        text = c.name,
                        selected = selectedCategory == c.name,
                        onClick = { selectedCategory = c.name },
                        onLongClick = { categoryToDelete = c.name }
                    )
                }
                StardewChip("", false, { showAddCategory = true }, sprite = Sprites.plusDark)
            }
        }

        if (pending.isEmpty() && done.isEmpty()) {
            item {
                Text(
                    "Nothing here yet. Add a quest above!",
                    style = StardewTypography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
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

        item { RestingCat() }
    }

    editing?.let { task ->
        QuestEditorDialog(task, categories, today, onSave = onUpdate, onDismiss = { editing = null })
    }

    if (showAddCategory) {
        var name by remember { mutableStateOf("") }
        StardewDialog(
            title = "New Category",
            confirmText = "Add",
            onDismiss = { showAddCategory = false },
            onConfirm = { onAddCategory(name, ""); showAddCategory = false }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Category name")
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
