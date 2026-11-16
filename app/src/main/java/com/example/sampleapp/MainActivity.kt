package com.example.sampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sampleapp.ui.CommandViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CommandScreen()
            }
        }
    }
}

@Composable
fun CommandScreen(viewModel: CommandViewModel = viewModel()) {
    var input by remember { mutableStateOf("") }
    val reminders by viewModel.reminders.collectAsState()
    val notes by viewModel.notes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Command Assistant",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a command…") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (input.isNotBlank()) {
                    viewModel.executeCommand(input)
                    input = ""
                }
            }) {
                Text("Go")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        VoiceButton(onCommand = { spoken ->
            viewModel.executeCommand(spoken)
        })

        Spacer(modifier = Modifier.height(24.dp))

        Text("Today", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(reminders) { reminder ->
                ListItem(
                    headlineContent = { Text(reminder.text) },
                    supportingContent = { Text(reminder.time) }
                )
            }
            items(notes) { note ->
                ListItem(
                    headlineContent = { Text(note.text) },
                    supportingContent = { Text("Note") }
                )
            }
        }
    }
}
