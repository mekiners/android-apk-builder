package com.example.sampleapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.CategoryEntity
import com.example.sampleapp.data.TaskEntity

@Composable
fun DashboardScreen(
    tasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    coinBalance: Int,
    onMenuClick: () -> Unit,
    onAddTask: (String, String) -> Unit,
    onVoiceInputClick: () -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var taskInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredTasks = if (selectedCategory == "All") tasks else tasks.filter { it.category.equals(selectedCategory, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().background(StardewCanvasBg)) {
        StardewWoodenBanner(title = "Daily Quests", coinBalance = coinBalance, onMenuClick = onMenuClick)

        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            
            // Input Bar
            StardewPanel(contentPadding = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = taskInput,
                        onValueChange = { taskInput = it },
                        placeholder = { Text("Type a quest...", style = StardewTypography.bodyMedium, color = StardewTextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = StardewTextPrimary, unfocusedTextColor = StardewTextPrimary
                        ),
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                    IconButton(onClick = onVoiceInputClick) { Text("🎤", style = StardewTypography.titleMedium) }
                    StardewButton(text = "+", onClick = {
                        if (taskInput.isNotBlank()) {
                            val targetCat = if (selectedCategory == "All") "To Do" else selectedCategory
                            onAddTask(taskInput.trim(), targetCat)
                            taskInput = ""
                        }
                    })
                }
            }

            // Category Tabs
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { 
                    Box(modifier = Modifier.background(if (selectedCategory == "All") StardewWoodPrimary else StardewParchmentDark).border(2.dp, StardewWoodDark).clickable { selectedCategory = "All" }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("All", style = StardewTypography.labelSmall, color = if (selectedCategory == "All") StardewParchment else StardewTextPrimary)
                    }
                }
                items(categories) { cat ->
                    val isSelected = cat.name == selectedCategory
                    Box(modifier = Modifier.background(if (isSelected) StardewWoodPrimary else StardewParchmentDark).border(2.dp, StardewWoodDark).clickable { selectedCategory = cat.name }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("${cat.icon} ${cat.name}", style = StardewTypography.labelSmall, color = if (isSelected) StardewParchment else StardewTextPrimary)
                    }
                }
            }

            // Task List
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredTasks, key = { it.id }) { task ->
                    StardewPanel(contentPadding = 10.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            StardewCheckbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = StardewTypography.bodyLarge.copy(textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None),
                                    color = if (task.isCompleted) StardewTextSecondary else StardewTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.background(StardewParchmentDark).border(1.dp, StardewWoodHighlight).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(task.category, style = StardewTypography.labelSmall)
                                    }
                                    if (task.recurrence != "None") {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("🔄 ${task.recurrence}", style = StardewTypography.labelSmall)
                                    }
                                }
                            }
                            IconButton(onClick = { onEditTask(task) }) { Text("✏️") }
                            IconButton(onClick = { onDeleteTask(task) }) { Text("🗑️") }
                        }
                    }
                }
            }

            // Footer
            val completedCount = tasks.count { it.isCompleted }
            StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark, contentPadding = 8.dp) {
                Text("$completedCount of ${tasks.size} daily quests complete!", style = StardewTypography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
