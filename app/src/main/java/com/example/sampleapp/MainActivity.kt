package com.example.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sampleapp.data.CategoryEntity
import com.example.sampleapp.data.TaskEntity
import com.example.sampleapp.ui.CozyTheme
import com.example.sampleapp.ui.CommandViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CozyAssistantApp()
            }
        }
    }
}

@Composable
fun CozyAssistantApp(viewModel: CommandViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val user by viewModel.user.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CozyTheme.Background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🐻 Cozy Assistant", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CozyTheme.TextPrimary)
                Surface(shape = RoundedCornerShape(20.dp), color = CozyTheme.Accent) {
                    Text("🪙 ${user?.coins ?: 0}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = CozyTheme.TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            
            // INPUT AREA
            Card(colors = CardDefaults.cardColors(containerColor = CozyTheme.Surface), shape = RoundedCornerShape(CozyTheme.CornerRadius)) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a task...", color = CozyTheme.TextPrimary.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent)
                    )
                    VoiceButton { spoken -> inputText = spoken }
                    IconButton(onClick = {
                        val cat = if (selectedCategory == "All") "To Do" else selectedCategory
                        viewModel.addTask(inputText, cat)
                        inputText = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = CozyTheme.Primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORIES (Tabs)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryChip("All", selectedCategory == "All") { selectedCategory = "All" }
                }
                items(categories) { cat ->
                    CategoryChip("${cat.icon} ${cat.name}", selectedCategory == cat.name) { selectedCategory = cat.name }
                }
                item {
                    IconButton(onClick = { showAddCategoryDialog = true }, modifier = Modifier.background(CozyTheme.Accent, RoundedCornerShape(50))) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category", tint = CozyTheme.TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TASK LIST
            val filteredTasks = if (selectedCategory == "All") tasks else tasks.filter { it.category == selectedCategory }
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredTasks) { task ->
                    TaskCard(task, 
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }

    // ADD CATEGORY DIALOG
    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var newCatIcon by remember { mutableStateOf("📌") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = CozyTheme.Surface,
            title = { Text("New Category", color = CozyTheme.TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(value = newCatName, onValueChange = { newCatName = it }, label = { Text("Name") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newCatIcon, onValueChange = { if(it.length <= 2) newCatIcon = it }, label = { Text("Emoji (e.g., 🍎)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCatName.isNotBlank()) viewModel.addCategory(newCatName, newCatIcon)
                    showAddCategoryDialog = false
                }) { Text("Add", color = CozyTheme.Primary) }
            }
        )
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) CozyTheme.Primary else CozyTheme.Surface,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else CozyTheme.TextPrimary
        )
    }
}

@Composable
fun TaskCard(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CozyTheme.Surface),
        shape = RoundedCornerShape(CozyTheme.CornerRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = CozyTheme.Primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    color = CozyTheme.TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Row {
                    Text(task.category, fontSize = 12.sp, color = CozyTheme.Primary)
                    if (task.recurrence != "None") {
                        Text(" • 🔄 ${task.recurrence}", fontSize = 12.sp, color = CozyTheme.Primary)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}
