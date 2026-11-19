package com.example.sampleapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.CategoryEntity
import com.example.sampleapp.data.TaskEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.Dates
import java.time.LocalDate

/** Quest difficulty look: Easy = leaves and beige, Medium = honey and amber, Hard = molten rock. */
class QuestAccent(val check: Color, val sprite: Sprite)

private val EASY_ACCENT = QuestAccent(check = Color(0xFF5B8C3E), sprite = Sprites.tree)
private val MEDIUM_ACCENT = QuestAccent(check = Color(0xFFC98A12), sprite = Sprites.honey)
private val HARD_ACCENT = QuestAccent(check = Color(0xFFB8321C), sprite = Sprites.flame)

fun accentFor(gold: Int): QuestAccent = when {
    gold >= 10 -> HARD_ACCENT
    gold >= 5 -> MEDIUM_ACCENT
    else -> EASY_ACCENT
}

val DIFFICULTIES = listOf(1, 5, 10)

fun difficultySprite(gold: Int): Sprite = when {
    gold >= 10 -> Sprites.bag
    gold >= 5 -> Sprites.coins
    else -> Sprites.coin
}

@Composable
fun QuestCard(
    task: TaskEntity,
    today: Long,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val a = accentFor(task.gold)
    val done = task.isCompleted
    val cardBackground = when {
        task.gold >= 10 -> Color(0xFFFBF0EC)
        task.gold >= 5 -> Color(0xFFFFF7E8)
        else -> Color(0xFFF1F6EE)
    }
    val cardBorder = when {
        task.gold >= 10 -> Color(0xFFEED7CE)
        task.gold >= 5 -> Color(0xFFF0E0BE)
        else -> Color(0xFFDCE8D7)
    }
    PanelBox(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 9.dp,
        backgroundColor = if (done) Color(0xFFF2F3EE) else cardBackground,
        borderColor = cardBorder,
        accentColor = if (done) Color(0xFFADB7A9) else a.check
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StardewCheckbox(checked = done, onCheckedChange = { onToggle() }, checkedColor = a.check)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = StardewTypography.bodyLarge.copy(
                        textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (done) StardewTextSecondary else StardewTextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(task.category, modifier = Modifier.weight(1f, fill = false))
                    GoldBadge(task.gold)
                    if (task.recurrence != "None") {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            PixelIcon(Sprites.loop, 12.dp)
                            Text(Dates.repeatLabel(task.recurrence, task.repeatDays), style = StardewTypography.labelSmall, color = StardewTextPrimary, maxLines = 1)
                        }
                    }
                }
                val due = Dates.dueLabel(task.dueDay, task.timeMinutes, today)
                if (due.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = due,
                        style = StardewTypography.bodySmall,
                        color = if (!done && task.dueDay != null && task.dueDay < today) Color(0xFF8E2A1C) else StardewTextPrimary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            PixelIcon(a.sprite, 20.dp, modifier = Modifier.padding(horizontal = 4.dp))
            PixelIconButton(Sprites.pencil, onEdit)
            PixelIconButton(Sprites.trash, onDelete)
        }
    }
}

@Composable
fun DifficultyChips(gold: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DIFFICULTIES.forEach { g ->
            StardewChip(
                text = "$g",
                sprite = difficultySprite(g),
                selected = gold == g,
                onClick = { onSelect(g) },
                selectedColor = StardewGold,
                selectedTextColor = StardewTextPrimary
            )
        }
    }
}

@Composable
fun RepeatChips(recurrence: String, repeatDays: Int, onChange: (String, Int) -> Unit) {
    var showCustom by remember { mutableStateOf(false) }
    val isCustom = recurrence == "Monthly" || recurrence == "Days"
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StardewChip("Once", recurrence == "None", { onChange("None", 0) })
        StardewChip("Daily", recurrence == "Daily", { onChange("Daily", 0) })
        StardewChip("Weekly", recurrence == "Weekly", { onChange("Weekly", 0) })
        StardewChip(
            text = if (isCustom) Dates.repeatLabel(recurrence, repeatDays) else "Custom",
            selected = isCustom,
            onClick = { showCustom = true }
        )
    }
    if (showCustom) {
        CustomRepeatDialog(
            initialRecurrence = recurrence,
            initialDays = repeatDays,
            onConfirm = { r, d -> onChange(r, d) },
            onDismiss = { showCustom = false }
        )
    }
}

private val DAY_SHORT = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

@Composable
private fun CustomRepeatDialog(
    initialRecurrence: String,
    initialDays: Int,
    onConfirm: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var monthly by remember { mutableStateOf(initialRecurrence == "Monthly") }
    var days by remember { mutableStateOf(if (initialRecurrence == "Days") initialDays else 0) }

    StardewDialog(
        title = "Custom repeat",
        confirmText = "Set",
        onDismiss = onDismiss,
        onConfirm = {
            if (monthly) onConfirm("Monthly", 0)
            else if (days != 0) onConfirm("Days", days)
            onDismiss()
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StardewChip("Days of the week", !monthly, { monthly = false })
            StardewChip("Monthly", monthly, { monthly = true })
        }
        if (monthly) {
            Text("Repeats on the same date every month.", style = StardewTypography.bodyMedium)
        } else {
            Text("Pick the days this quest repeats on:", style = StardewTypography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DAY_SHORT.forEachIndexed { i, label ->
                    StardewChip(
                        text = label,
                        selected = (days and (1 shl i)) != 0,
                        onClick = { days = days xor (1 shl i) },
                        modifier = Modifier.weight(1f),
                        selectedColor = StardewGreen
                    )
                }
            }
            if (days == 0) Text("Select at least one day.", style = StardewTypography.bodySmall)
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
                sprite = Sprites.calendar,
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
                text = if (timeMinutes != null) Dates.timeLabel(timeMinutes) else "Add time",
                sprite = Sprites.clock,
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
    var repeatDays by remember { mutableStateOf(task.repeatDays) }
    var dueDay by remember { mutableStateOf(task.dueDay) }
    var timeMinutes by remember { mutableStateOf(task.timeMinutes) }

    StardewDialog(
        title = "Edit Quest",
        onDismiss = onDismiss,
        onConfirm = {
            if (title.isNotBlank()) {
                var due = dueDay
                if (due == null && (recurrence != "None" || timeMinutes != null)) due = today
                onSave(
                    task.copy(
                        title = title.trim(), category = category, gold = gold,
                        recurrence = recurrence, repeatDays = repeatDays,
                        dueDay = due, timeMinutes = timeMinutes
                    )
                )
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
        SectionLabel("Reward")
        DifficultyChips(gold) { gold = it }
        SectionLabel("Repeat")
        RepeatChips(recurrence, repeatDays) { r, d -> recurrence = r; repeatDays = d }
        SectionLabel("When")
        WhenControls(today, dueDay, timeMinutes, { dueDay = it }, { timeMinutes = it })
    }
}
