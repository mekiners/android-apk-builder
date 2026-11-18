package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.WorkoutEntity
import com.example.sampleapp.ui.theme.*

@Composable
fun WorkoutScreen(
    workouts: List<WorkoutEntity>,
    today: Long,
    onAdd: (String, String) -> Unit,
    onUpdate: (WorkoutEntity) -> Unit,
    onToggle: (WorkoutEntity) -> Unit,
    onDelete: (WorkoutEntity) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<WorkoutEntity?>(null) }
    val doneCount = workouts.count { it.doneDay == today }

    Column(modifier = Modifier.fillMaxSize().background(StardewCanvasBg)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark) {
                    Text("My Routine", style = StardewTypography.titleMedium)
                    Text("$doneCount of ${workouts.size} exercises done today", style = StardewTypography.bodyMedium)
                }
            }
            if (workouts.isEmpty()) {
                item {
                    Text(
                        "No exercises yet. Add your current routine below!",
                        style = StardewTypography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            }
            items(workouts, key = { it.id }) { w ->
                val isDone = w.doneDay == today
                StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 10.dp) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        StardewCheckbox(isDone, { onToggle(w) })
                        Spacer(Modifier.width(12.dp))
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
                        IconButton(onClick = { editing = w }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = StardewTextSecondary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { onDelete(w) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StardewTerracotta, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        StardewButton("+ Add Exercise", { showAdd = true }, modifier = Modifier.fillMaxWidth().padding(12.dp))
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var detail by remember { mutableStateOf("") }
        StardewDialog(
            title = "New Exercise",
            confirmText = "Add",
            onDismiss = { showAdd = false },
            onConfirm = { onAdd(name, detail); showAdd = false }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Exercise (e.g. Push-ups)")
            StardewTextField(value = detail, onValueChange = { detail = it }, placeholder = "Sets / reps (e.g. 3 x 12)")
        }
    }

    editing?.let { w ->
        var name by remember(w.id) { mutableStateOf(w.name) }
        var detail by remember(w.id) { mutableStateOf(w.detail) }
        StardewDialog(
            title = "Edit Exercise",
            onDismiss = { editing = null },
            onConfirm = {
                if (name.isNotBlank()) onUpdate(w.copy(name = name.trim(), detail = detail.trim()))
                editing = null
            }
        ) {
            StardewTextField(value = name, onValueChange = { name = it }, placeholder = "Exercise")
            StardewTextField(value = detail, onValueChange = { detail = it }, placeholder = "Sets / reps")
        }
    }
}
