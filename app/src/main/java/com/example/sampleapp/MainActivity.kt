package com.example.sampleapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sampleapp.data.RewardEntity
import com.example.sampleapp.data.TaskEntity
import com.example.sampleapp.ui.CozyTheme
import com.example.sampleapp.ui.CozyTypography
import com.example.sampleapp.ui.CommandViewModel
import com.example.sampleapp.ui.PastelColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { 
            MaterialTheme(typography = CozyTypography) { 
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
    val rewards by viewModel.rewards.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("dashboard") } // dashboard, grocery, shop

    var inputText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = CozyTheme.Background) {
                Spacer(Modifier.height(24.dp))
                Text("🐻 Cozy Assistant", modifier = Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CozyTheme.TextPrimary)
                Divider(color = CozyTheme.Accent, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                
                NavigationDrawerItem(label = { Text("🏠 Dashboard") }, selected = currentScreen == "dashboard", onClick = { currentScreen = "dashboard"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CozyTheme.Accent))
                NavigationDrawerItem(label = { Text("🛒 Grocery List") }, selected = currentScreen == "grocery", onClick = { currentScreen = "grocery"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CozyTheme.Accent))
                NavigationDrawerItem(label = { Text("🎁 Rewards Shop") }, selected = currentScreen == "shop", onClick = { currentScreen = "shop"; scope.launch { drawerState.close() } }, colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = CozyTheme.Accent))
            }
        }
    ) {
        Scaffold(
            containerColor = CozyTheme.Background,
            topBar = {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = CozyTheme.Primary)
                    }
                    Text(
                        text = when(currentScreen) {
                            "dashboard" -> "Dashboard"
                            "grocery" -> "Grocery List"
                            "shop" -> "Rewards Shop"
                            else -> "Cozy Assistant"
                        }, 
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CozyTheme.TextPrimary, modifier = Modifier.weight(1f)
                    )
                    Surface(shape = RoundedCornerShape(20.dp), color = CozyTheme.Accent) {
                        Text("🪙 ${user?.coins ?: 0}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = CozyTheme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (currentScreen) {
                    "dashboard" -> DashboardScreen(tasks, categories, selectedCategory, { selectedCategory = it }, inputText, { inputText = it }, viewModel, { showAddCategoryDialog = true }, { taskToEdit = it })
                    "grocery" -> GroceryScreen(tasks, inputText, { inputText = it }, viewModel)
                    "shop" -> ShopScreen(rewards, user?.coins ?: 0, viewModel)
                }
            }
        }
    }

    // EDIT / RESCHEDULE DIALOG
    taskToEdit?.let { task ->
        val context = LocalContext.current
        var editTitle by remember { mutableStateOf(task.title) }
        var editRecurrence by remember { mutableStateOf(task.recurrence) }
        var editTimestamp by remember { mutableStateOf(task.timestamp) }

        AlertDialog(
            onDismissRequest = { taskToEdit = null },
            containerColor = CozyTheme.Surface,
            title = { Text("Edit Task", color = CozyTheme.TextPrimary, fontSize = 18.sp) },
            text = {
                Column {
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Task Name") }, textStyle = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val dateString = if (editTimestamp != null) SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(editTimestamp!!)) else "No Alarm Set"
                    Button(onClick = {
                        val cal = Calendar.getInstance()
                        if (editTimestamp != null) cal.timeInMillis = editTimestamp!!
                        DatePickerDialog(context, { _, year, month, day ->
                            cal.set(Calendar.YEAR, year)
                            cal.set(Calendar.MONTH, month)
                            cal.set(Calendar.DAY_OF_MONTH, day)
                            TimePickerDialog(context, { _, hour, minute ->
                                cal.set(Calendar.HOUR_OF_DAY, hour)
                                cal.set(Calendar.MINUTE, minute)
                                editTimestamp = cal.timeInMillis
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }, colors = ButtonDefaults.buttonColors(containerColor = CozyTheme.Accent)) {
                        Text("🕒 $dateString", color = CozyTheme.TextPrimary, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Repeat:", color = CozyTheme.TextPrimary, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("None", "Daily", "Weekly", "Monthly").forEach { rec ->
                            FilterChip(selected = editRecurrence == rec, onClick = { editRecurrence = rec }, label = { Text(rec, fontSize = 11.sp) })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateTaskDetails(task, editTitle, editRecurrence, editTimestamp)
                    taskToEdit = null 
                }) { Text("Save", color = CozyTheme.Primary) }
            }
        )
    }

    // ADD CATEGORY DIALOG (NOW WITH COLORS!)
    if (showAddCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        var newCatIcon by remember { mutableStateOf("📌") }
        var selectedColor by remember { mutableStateOf(PastelColors[0]) }
        
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = CozyTheme.Surface,
            title = { Text("New Category", color = CozyTheme.TextPrimary, fontSize = 18.sp) },
            text = {
                Column {
                    OutlinedTextField(value = newCatName, onValueChange = { newCatName = it }, label = { Text("Name") }, textStyle = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newCatIcon, onValueChange = { if(it.length <= 2) newCatIcon = it }, label = { Text("Emoji") }, textStyle = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Choose a Color:", fontSize = 14.sp, color = CozyTheme.TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PastelColors) { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, RoundedCornerShape(16.dp))
                                    .clickable { selectedColor = hex }
                                    .then(if (selectedColor == hex) Modifier.padding(2.dp) else Modifier)
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { if (newCatName.isNotBlank()) viewModel.addCategory(newCatName, newCatIcon, selectedColor); showAddCategoryDialog = false }) { Text("Add", color = CozyTheme.Primary) } }
        )
    }
}

@Composable
fun DashboardScreen(tasks: List<TaskEntity>, categories: List<com.example.sampleapp.data.CategoryEntity>, selectedCategory: String, onCategorySelect: (String) -> Unit, inputText: String, onInputChange: (String) -> Unit, viewModel: CommandViewModel, onAddCategoryClick: () -> Unit, onEditTask: (TaskEntity) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // Input Area
        Card(colors = CardDefaults.cardColors(containerColor = CozyTheme.Surface), shape = RoundedCornerShape(CozyTheme.CornerRadius)) {
            Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText, 
                    onValueChange = onInputChange, 
                    placeholder = { Text("Type a task...", fontSize = 14.sp, color = CozyTheme.TextPrimary.copy(alpha = 0.5f)) }, 
                    modifier = Modifier.weight(1f), 
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent)
                )
                VoiceButton { spoken -> onInputChange(spoken) }
                IconButton(onClick = { val cat = if (selectedCategory == "All") "To Do" else selectedCategory; viewModel.addTask(inputText, cat); onInputChange("") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = CozyTheme.Primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        // Category Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item { CategoryChip("All", selectedCategory == "All") { onCategorySelect("All") } }
            items(categories) { cat -> CategoryChip("${cat.icon} ${cat.name}", selectedCategory == cat.name) { onCategorySelect(cat.name) } }
            item { IconButton(onClick = onAddCategoryClick, modifier = Modifier.background(CozyTheme.Accent, RoundedCornerShape(50)).size(36.dp)) { Icon(Icons.Default.Add, contentDescription = "Add Category", tint = CozyTheme.TextPrimary) } }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        val filteredTasks = if (selectedCategory == "All") tasks else tasks.filter { it.category == selectedCategory }
        val pendingTasks = filteredTasks.filter { !it.isCompleted }
        val completedTasks = filteredTasks.filter { it.isCompleted }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pendingTasks.isEmpty() && completedTasks.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Nothing here! Time for a cozy break ☕", color = CozyTheme.Primary, fontSize = 15.sp, textAlign = TextAlign.Center) } }
            }

            items(pendingTasks) { task ->
                val catColor = categories.find { it.name == task.category }?.colorHex ?: "#FFFDF5"
                TaskCard(task, catColor, onToggle = { viewModel.toggleTaskCompletion(task) }, onDelete = { viewModel.deleteTask(task) }, onEdit = { onEditTask(task) })
            }
            if (completedTasks.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Completed", color = CozyTheme.Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.clearCompletedTasks() }) { Text("Clear All", color = Color.Gray, fontSize = 12.sp) }
                    }
                }
                items(completedTasks) { task ->
                    val catColor = categories.find { it.name == task.category }?.colorHex ?: "#FFFDF5"
                    TaskCard(task, catColor, onToggle = { viewModel.toggleTaskCompletion(task) }, onDelete = { viewModel.deleteTask(task) }, onEdit = { onEditTask(task) })
                }
            }
        }
    }
}

@Composable
fun GroceryScreen(tasks: List<TaskEntity>, inputText: String, onInputChange: (String) -> Unit, viewModel: CommandViewModel) {
    val groceryTasks = tasks.filter { it.category.equals("To Buy", ignoreCase = true) }
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = CozyTheme.Surface), shape = RoundedCornerShape(CozyTheme.CornerRadius)) {
            Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = inputText, onValueChange = onInputChange, placeholder = { Text("Add grocery item...", fontSize = 14.sp) }, modifier = Modifier.weight(1f), textStyle = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { viewModel.addTask(inputText, "To Buy"); onInputChange("") }) { Icon(Icons.Default.Add, contentDescription = "Add", tint = CozyTheme.Primary) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(groceryTasks) { task ->
                TaskCard(task, "#E5F0FF", onToggle = { viewModel.toggleTaskCompletion(task) }, onDelete = { viewModel.deleteTask(task) }, onEdit = {})
            }
        }
    }
}

@Composable
fun ShopScreen(rewards: List<RewardEntity>, currentCoins: Int, viewModel: CommandViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rewards) { reward ->
                Card(colors = CardDefaults.cardColors(containerColor = CozyTheme.Surface), shape = RoundedCornerShape(CozyTheme.CornerRadius)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(reward.title, modifier = Modifier.weight(1f), color = CozyTheme.TextPrimary, fontSize = 15.sp)
                        Text("${reward.cost} 🪙", color = CozyTheme.TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.claimReward(reward) }, colors = ButtonDefaults.buttonColors(containerColor = CozyTheme.Primary)) { Text("Claim", fontSize = 13.sp) }
                        IconButton(onClick = { viewModel.deleteReward(reward) }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray) }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CozyTheme.Accent)) { Text("+ Add Custom Reward", color = CozyTheme.TextPrimary, fontSize = 14.sp) }
    }
    
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var cost by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showAddDialog = false }, containerColor = CozyTheme.Surface, title = { Text("New Reward") }, text = { Column {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Reward") })
            OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost") })
        } }, confirmButton = { TextButton(onClick = { if (title.isNotBlank() && cost.toIntOrNull() != null) viewModel.addReward(title, cost.toInt()); showAddDialog = false }) { Text("Add") } })
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = if (isSelected) CozyTheme.Primary else CozyTheme.Surface, modifier = Modifier.clickable { onClick() }) {
        Text(text = text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if (isSelected) Color.White else CozyTheme.TextPrimary, fontSize = 13.sp)
    }
}

@Composable
fun TaskCard(task: TaskEntity, colorHex: String, onToggle: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    val bgColor = Color(android.graphics.Color.parseColor(colorHex))
    
    Card(colors = CardDefaults.cardColors(containerColor = bgColor), shape = RoundedCornerShape(CozyTheme.CornerRadius), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = CozyTheme.Primary))
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontSize = 14.sp, color = CozyTheme.TextPrimary, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                Row {
                    Text(task.category, fontSize = 11.sp, color = CozyTheme.Primary.copy(alpha = 0.8f))
                    if (task.recurrence != "None") Text(" • 🔄 ${task.recurrence}", fontSize = 11.sp, color = CozyTheme.Primary.copy(alpha = 0.8f))
                    if (task.timestamp != null) Text(" • " + SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(task.timestamp)), fontSize = 11.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CozyTheme.Primary, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp)) }
        }
    }
}
