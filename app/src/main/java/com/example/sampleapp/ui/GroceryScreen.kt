package com.example.sampleapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.TaskEntity

@Composable
fun GroceryScreen(
    tasks: List<TaskEntity>,
    coinBalance: Int,
    onMenuClick: () -> Unit,
    onAddTask: (String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var taskInput by remember { mutableStateOf("") }
    val groceryTasks = tasks.filter { it.category == "To Buy" }

    Column(modifier = Modifier.fillMaxSize().background(StardewCanvasBg)) {
        StardewWoodenBanner(title = "Market List", coinBalance = coinBalance, onMenuClick = onMenuClick)

        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            
            // Input Bar
            StardewPanel(contentPadding = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = taskInput,
                        onValueChange = { taskInput = it },
                        placeholder = { Text("Add grocery item...", style = StardewTypography.bodyMedium, color = StardewTextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = StardewTextPrimary, unfocusedTextColor = StardewTextPrimary
                        ),
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                    StardewButton(text = "+", onClick = {
                        if (taskInput.isNotBlank()) {
                            onAddTask(taskInput.trim())
                            taskInput = ""
                        }
                    })
                }
            }

            // List
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groceryTasks, key = { it.id }) { task ->
                    StardewPanel(contentPadding = 10.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            StardewCheckbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = task.title,
                                style = StardewTypography.bodyLarge.copy(textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None),
                                color = if (task.isCompleted) StardewTextSecondary else StardewTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onDeleteTask(task) }) { Text("🗑️") }
                        }
                    }
                }
            }
        }
    }
}
