package com.example.sampleapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.CategoryEntity
import com.example.sampleapp.data.TaskEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.Dates
import java.time.LocalDate

val DIFFICULTIES = listOf("Easy" to 1, "Medium" to 5, "Hard" to 10)
val REPEATS = listOf("None" to "Once", "Daily" to "Daily", "Weekly" to "Weekly", "Monthly" to "Monthly")

@Composable
fun QuestCard(
    task: TaskEntity,
    today: Long,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 10.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            StardewCheckbox(checked = task.isCompleted, onCheckedChange = { onToggle() })
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = StardewTypography.bodyLarge.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) StardewTextSecondary else StardewTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(task.category, modifier = Modifier.weight(1f, fill = false))
                    GoldBadge(task.gold)
                    if (task.recurrence != "None") Text("\uD83D\uDD04 ${task.recurrence}", style = StardewTypography.labelSmall, maxLines = 1)
                }
                val due = Dates.dueLabel(task.dueDay, task.timeMinutes, today)
                if (due.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = due,
                        style = StardewTypography.bodySmall,
                        color = if (!task.isCompleted && task.dueDay != null && task.dueDay < today) StardewTerracotta else StardewTextSecondary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = StardewTextSecondary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StardewTerracotta, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DifficultyChips(gold: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DIFFICULTIES.forEach { (label, g) ->
            StardewChip(
                text = "$label ${g}g",
                selected = gold == g,
                onClick = { onSelect(g) },
                selectedColor = StardewGold,
                selectedTextColor = StardewTextPrimary
            )
        }
    }
}

@Composable
fun RepeatChips(recurrence: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        REPEATS.forEach { (key, label) ->
            StardewChip(text = label, selected = recurrence == key, onClick = { onSelect(key) })
        }
    }
}

/** Date + time pickers: Today / Tomorrow / Pick / None, plus a time button. */
@Composable
fun WhenControls(
    today: Long,
    dueDay: Long?,
    timeMinutes: Int?,
    onDueDay: (Long?) -> Unit,
    onTime: (Int?) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StardewChip("Today", dueDay == today, { onDueDay(today) })
            StardewChip("Tomorrow", dueDay == today + 1, { onDueDay(today + 1) })
            StardewChip(
                text = if (dueDay != null && dueDay != today && dueDay != today + 1) Dates.dayLabel(dueDay, today) else "Pick date",
                selected = dueDay != null && dueDay != today && dueDay != today + 1,
                onClick = {
                    val base = LocalDate.ofEpochDay(dueDay ?: today)
                    DatePickerDialog(context, { _, y, m, d ->
                        onDueDay(LocalDate.of(y, m + 1, d).toEpochDay())
                    }, base.year, base.monthValue - 1, base.dayOfMonth).show()
                }
            )
            StardewChip("No date", dueDay == null, { onDueDay(null) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StardewChip(
                text = if (timeMinutes != null) "\u23F0 " + Dates.timeLabel(timeMinutes) else "\u23F0 Add time",
                selected = timeMinutes != null,
                onClick = {
                    val m = timeMinutes ?: 9 * 60
                    TimePickerDialog(context, { _, h, min -> onTime(h * 60 + min) }, m / 60, m % 60, false).show()
                }
            )
            if (timeMinutes != null) StardewChip("Clear time", false, { onTime(null) })
        }
    }
}

@Composable
fun QuestEditorDialog(
    task: TaskEntity,
    categories: List<CategoryEntity>,
    today: Long,
    onSave: (TaskEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var category by remember { mutableStateOf(task.category) }
    var gold by remember { mutableStateOf(task.gold) }
    var recurrence by remember { mutableStateOf(task.recurrence) }
    var dueDay by remember { mutableStateOf(task.dueDay) }
    var timeMinutes by remember { mutableStateOf(task.timeMinutes) }

    StardewDialog(
        title = "Edit Quest",
        onDismiss = onDismiss,
        onConfirm = {
            if (title.isNotBlank()) {
                var due = dueDay
                if (due == null && (recurrence != "None" || timeMinutes != null)) due = today
                onSave(task.copy(title = title.trim(), category = category, gold = gold, recurrence = recurrence, dueDay = due, timeMinutes = timeMinutes))
            }
            onDismiss()
        }
    ) {
        StardewTextField(value = title, onValueChange = { title = it }, placeholder = "Quest name")
        SectionLabel("Category")
        val cats = (categories.map { it.name } + category).distinct()
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            cats.forEach { c -> StardewChip(c, category == c, { category = c }) }
        }
        SectionLabel("Difficulty")
        DifficultyChips(gold) { gold = it }
        SectionLabel("Repeat")
        RepeatChips(recurrence) { recurrence = it }
        SectionLabel("When")
        WhenControls(today, dueDay, timeMinutes, { dueDay = it }, { timeMinutes = it })
    }
}
