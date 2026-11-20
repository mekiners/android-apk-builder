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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.RoutineEntity
import com.example.sampleapp.data.WorkoutEntity
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
    today: Long,
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
                                    if (w.detail.isNotBlank()) Text(w.detail, style = StardewTypography.bodySmall)
                                }
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
}
