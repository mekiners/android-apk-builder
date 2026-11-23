package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.RoutineEntity
import com.example.sampleapp.data.ShiftEntity
import com.example.sampleapp.data.WorkoutEntity
import com.example.sampleapp.data.WorkoutLogEntity
import com.example.sampleapp.util.Stats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.sampleapp.ui.theme.*

private val TEMPLATES: List<Pair<String, List<Pair<String, String>>>> = listOf(
    "Push Day" to listOf(
        "Bench Press" to "4 x 8", "Overhead Press" to "3 x 10", "Incline Dumbbell Press" to "3 x 10",
        "Triceps Pushdown" to "3 x 12", "Lateral Raise" to "3 x 15"
    ),
    "Pull Day" to listOf(
        "Pull-ups" to "4 x 8", "Barbell Row" to "4 x 8", "Lat Pulldown" to "3 x 10",
        "Face Pull" to "3 x 15", "Biceps Curl" to "3 x 12"
    ),
    "Leg Day" to listOf(
        "Squat" to "4 x 8", "Romanian Deadlift" to "3 x 10", "Leg Press" to "3 x 12",
        "Lunges" to "3 x 12", "Calf Raise" to "4 x 15"
    ),
    "Full Body" to listOf(
        "Squat" to "3 x 8", "Bench Press" to "3 x 8", "Row" to "3 x 10",
        "Overhead Press" to "3 x 10", "Plank" to "3 x 45 sec"
    ),
    "Cardio" to listOf(
        "Warm-up jog" to "10 min", "Intervals" to "8 x 1 min", "Cool-down walk" to "5 min"
    )
)

@Composable
fun WorkoutScreen(
    routines: List<RoutineEntity>,
    workouts: List<WorkoutEntity>,
    workoutLogs: List<WorkoutLogEntity>,
    shifts: List<ShiftEntity>,
    today: Long,
    onLogSet: (WorkoutEntity, Double, Int) -> Unit,
    onDeleteLog: (WorkoutLogEntity) -> Unit,
    onAddRoutine: (String, List<Pair<String, String>>) -> Unit,
    onRenameRoutine: (RoutineEntity, String) -> Unit,
    onDeleteRoutine: (RoutineEntity) -> Unit,
    onAddWorkout: (Long, String, String) -> Unit,
    onUpdateWorkout: (WorkoutEntity) -> Unit,
    onToggleWorkout: (WorkoutEntity) -> Unit,
    onDeleteWorkout: (WorkoutEntity) -> Unit
) {
    var expandedId by remember { mutableStateOf<Long?>(null) }
    var showAddRoutine by remember { mutableStateOf(false) }
    var addExerciseFor by remember { mutableStateOf<Long?>(null) }
    var editingExercise by remember { mutableStateOf<WorkoutEntity?>(null) }
    var renaming by remember { mutableStateOf<RoutineEntity?>(null) }
    var deleting by remember { mutableStateOf<RoutineEntity?>(null) }
    var logging by remember { mutableStateOf<WorkoutEntity?>(null) }

    val todayDate = LocalDate.ofEpochDay(today)
    val gymDays = remember(workoutLogs) { workoutLogs.map { it.day }.toSet() }
    val sessionsWeek = Stats.sessionsThisWeek(gymDays, todayDate)
    val weekStreak = Stats.weeklyStreak(gymDays, todayDate)
    val nudge = Stats.gymNudge(gymDays, todayDate)
    val suggestions = Stats.suggestGymDays(shifts, gymDays, todayDate)
    val dayFmt = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 26.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 6.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PixelIcon(Sprites.dumbbell, 28.dp)
                        Column {
                            Text("My Routines", style = StardewTypography.titleMedium)
                            Text("Tap a routine to open it and customize.", style = StardewTypography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This week: $sessionsWeek gym days" + if (weekStreak > 0) "   Streak: $weekStreak weeks" else "",
                        style = StardewTypography.bodyMedium
                    )
                    if (nudge != null) {
                        Text(nudge, style = StardewTypography.bodySmall, color = StardewTerracotta)
                    }
                    if (suggestions.isNotEmpty()) {
                        Text(
                            "Good days to train: " + suggestions.joinToString(", ") { it.date.format(dayFmt) + " (" + it.reason + ")" },
                            style = StardewTypography.bodySmall,
                            color = StardewTextPrimary
                        )
                    }
                }
            }

            if (routines.isEmpty()) {
                item {
                    NoteTag("No routines yet. Add one below, or start from a template.")
                }
            }

            items(routines, key = { it.id }) { routine ->
                val exercises = workouts.filter { it.routineId == routine.id }
                val doneCount = exercises.count { it.doneDay == today }
                val expanded = expandedId == routine.id

                StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedId = if (expanded) null else routine.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelIcon(Sprites.dumbbell, 24.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(routine.name, style = StardewTypography.titleSmall)
                            Text("$doneCount of ${exercises.size} done today", style = StardewTypography.bodySmall)
                        }
                        PixelIcon(if (expanded) Sprites.upDark else Sprites.downDark, 20.dp)
                    }

                    if (expanded) {
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(2.dp).background(StardewParchmentDark))
                        Spacer(Modifier.height(6.dp))

                        if (exercises.isEmpty()) {
                            Text("No exercises yet.", style = StardewTypography.bodySmall)
                        }
                        exercises.forEach { w ->
                            val isDone = w.doneDay == today
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StardewCheckbox(isDone, { onToggleWorkout(w) })
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        w.name,
                                        style = StardewTypography.bodyLarge.copy(
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (isDone) StardewTextSecondary else StardewTextPrimary
                                    )
                                    val best = Stats.bestWeight(workoutLogs, w.id)
                                    val info = listOfNotNull(
                                        w.detail.takeIf { it.isNotBlank() },
                                        best?.let { "PR ${Stats.fmtKg(it)} kg" }
                                    ).joinToString("  \u00B7  ")
                                    if (info.isNotEmpty()) Text(info, style = StardewTypography.bodySmall)
                                }
                                StardewChip("Log", false, { logging = w }, sprite = Sprites.dumbbell)
                                Spacer(Modifier.width(2.dp))
                                PixelIconButton(Sprites.pencil, { editingExercise = w })
                                PixelIconButton(Sprites.trash, { onDeleteWorkout(w) })
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StardewChip("Exercise", false, { addExerciseFor = routine.id }, sprite = Sprites.plusDark, selectedColor = StardewGreen)
                            StardewChip("Rename", false, { renaming = routine }, sprite = Sprites.pencil)
                            StardewChip("Delete", false, { deleting = routine }, sprite = Sprites.trash)
                        }
                    }
                }
            }
        }
        StardewButton("+ Add Routine", { showAddRoutine = true }, modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 12.dp))
    }

    if (showAddRoutine) {
        var name by remember { mutableStateOf("") }
        var chosen by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
        StardewDialog(
            title = "New Routine",
            confirmText = "Add",
            onDismiss = { showAddRoutine = false },
            onConfirm = {
                onAddRoutine(name, chosen)
                showAddRoutine = false
            }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Routine name (e.g. Push Day)")
            SectionLabel("Start from a template")
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TEMPLATES.forEach { (tName, tExercises) ->
                    StardewChip(
                        text = tName,
                        selected = chosen == tExercises,
                        onClick = { name = tName; chosen = tExercises }
                    )
                }
            }
            Text(
                if (chosen.isEmpty()) "Blank routine. Add your own exercises after." else "Includes ${chosen.size} exercises you can edit later.",
                style = StardewTypography.bodySmall
            )
        }
    }

    addExerciseFor?.let { routineId ->
        var name by remember { mutableStateOf("") }
        var detail by remember { mutableStateOf("") }
        StardewDialog(
            title = "New Exercise",
            confirmText = "Add",
            onDismiss = { addExerciseFor = null },
            onConfirm = { onAddWorkout(routineId, name, detail); addExerciseFor = null }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Exercise (e.g. Push-ups)")
            StardewTextField(value = detail, onValueChange = { detail = it }, placeholder = "Sets / reps (e.g. 3 x 12)")
        }
    }

    editingExercise?.let { w ->
        var name by remember(w.id) { mutableStateOf(w.name) }
        var detail by remember(w.id) { mutableStateOf(w.detail) }
        StardewDialog(
            title = "Edit Exercise",
            onDismiss = { editingExercise = null },
            onConfirm = {
                if (name.isNotBlank()) onUpdateWorkout(w.copy(name = name.trim(), detail = detail.trim()))
                editingExercise = null
            }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Exercise")
            StardewTextField(value = detail, onValueChange = { detail = it }, placeholder = "Sets / reps")
        }
    }

    renaming?.let { r ->
        var name by remember(r.id) { mutableStateOf(r.name) }
        StardewDialog(
            title = "Rename Routine",
            onDismiss = { renaming = null },
            onConfirm = { onRenameRoutine(r, name); renaming = null }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Routine name")
        }
    }

    deleting?.let { r ->
        StardewDialog(
            title = "Delete routine?",
            confirmText = "Delete",
            onDismiss = { deleting = null },
            onConfirm = {
                onDeleteRoutine(r)
                if (expandedId == r.id) expandedId = null
                deleting = null
            }
        ) {
            Text("Delete \"${r.name}\" and all of its exercises?", style = StardewTypography.bodyMedium)
        }
    }

    logging?.let { w ->
        LogSetsDialog(
            exercise = w,
            logs = workoutLogs,
            today = today,
            onAdd = { weight, reps -> onLogSet(w, weight, reps) },
            onDelete = onDeleteLog,
            onDismiss = { logging = null }
        )
    }
}

@Composable
private fun LogSetsDialog(
    exercise: WorkoutEntity,
    logs: List<WorkoutLogEntity>,
    today: Long,
    onAdd: (Double, Int) -> Unit,
    onDelete: (WorkoutLogEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val last = Stats.lastSession(logs, exercise.id, today)
    val todaySets = logs.filter { it.exerciseId == exercise.id && it.day == today && it.weightKg != null }.sortedBy { it.id }
    var weight by remember { mutableStateOf(last.maxOfOrNull { it.weightKg ?: 0.0 }?.let { Stats.fmtKg(it) } ?: "") }
    var reps by remember { mutableStateOf(last.firstOrNull()?.reps?.toString() ?: "") }

    StardewDialog(
        title = exercise.name,
        confirmText = "Done",
        onDismiss = onDismiss,
        onConfirm = onDismiss
    ) {
        if (last.isNotEmpty()) {
            Text(
                "Last time: " + last.joinToString(", ") { Stats.fmtKg(it.weightKg ?: 0.0) + " x " + (it.reps ?: 0) },
                style = StardewTypography.bodySmall,
                color = StardewTextPrimary
            )
            val hint = Stats.overloadHint(exercise.detail, last)
            if (hint != null) Text(hint, style = StardewTypography.bodyMedium, color = StardewGreen)
        } else {
            Text("No sets logged yet. Add your first one below.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
        }
        SectionLabel("Today")
        if (todaySets.isEmpty()) Text("No sets yet.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
        todaySets.forEachIndexed { i, set ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Set ${i + 1}:  ${Stats.fmtKg(set.weightKg ?: 0.0)} kg x ${set.reps ?: 0}",
                    style = StardewTypography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                PixelIconButton(Sprites.trash, { onDelete(set) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            StardewTextField(
                value = weight,
                onValueChange = { weight = it },
                placeholder = "kg",
                modifier = Modifier.weight(1f),
                height = 40.dp,
                keyboardType = KeyboardType.Decimal
            )
            StardewTextField(
                value = reps,
                onValueChange = { reps = it },
                placeholder = "reps",
                modifier = Modifier.weight(1f),
                height = 40.dp,
                keyboardType = KeyboardType.Number
            )
            StardewButton(
                text = "Add set",
                onClick = {
                    val wv = weight.replace(',', '.').toDoubleOrNull()
                    val rv = reps.trim().toIntOrNull()
                    if (wv != null && rv != null && rv > 0) onAdd(wv, rv)
                }
            )
        }
    }
}
