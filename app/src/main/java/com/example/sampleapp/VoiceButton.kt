package com.example.sampleapp

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sampleapp.ui.theme.Sprites
import com.example.sampleapp.ui.theme.StardewIconButton
import java.util.Locale

@Composable
fun VoiceButton(modifier: Modifier = Modifier, size: Dp = 40.dp, onResult: (String) -> Unit) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) onResult(spoken)
        }
    }

    StardewIconButton(
        sprite = Sprites.mic,
        modifier = modifier,
        size = size,
        onClick = {
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
    )
}
