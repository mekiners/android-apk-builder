package com.example.sampleapp

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.sampleapp.ui.theme.StardewParchmentDark
import com.example.sampleapp.ui.theme.StardewWoodDark
import java.util.Locale

@Composable
fun VoiceButton(onResult: (String) -> Unit) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) onResult(spoken)
        }
    }

    Box(
        modifier = Modifier
            .background(StardewParchmentDark)
            .border(2.dp, StardewWoodDark)
            .clickable {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "What's the quest?")
                }
                try {
                    launcher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Voice input isn't available on this device", Toast.LENGTH_SHORT).show()
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(text = "\uD83C\uDFA4", fontSize = 22.sp)
    }
}
