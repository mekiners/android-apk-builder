package com.example.sampleapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sampleapp.R
import com.example.sampleapp.data.TaskEntity
import com.example.sampleapp.ui.theme.*

@Composable
fun QuestCard(
    task: TaskEntity,
    today: Long,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tex = LocalTextures.current
    val a = accentFor(task.gold)
    val done = task.isCompleted
    
    val look = when {
        task.gold >= 10 -> PanelLook(tex.rock, if (done) tex.parchmentDark else tex.lava, 5.dp, true)
        task.gold >= 5 -> PanelLook(tex.honeyFrame, if (done) tex.parchmentDark else tex.honey, 5.dp, true)
        else -> PanelLook(tex.leafFrame, if (done) tex.parchmentDark else tex.beige, 5.dp, false)
    }
    
    val overlay: (DrawScope.(Float, Float, Int) -> Unit)? = when {
        task.gold >= 10 -> null
        task.gold >= 5 -> null // Removed the old flat honey
        else -> { w, h, _ ->
            // moss creeping in from the corners
            val ms = pxScale(2.dp)
            drawPixels(tex.mossTl, 6.dp.toPx(), 6.dp.toPx(), ms)
            drawPixels(tex.mossBr, w - tex.mossBr.width * ms - 6.dp.toPx(), h - tex.mossBr.height * ms - 6.dp.toPx(), ms)
        }
    }

    // Wrapper Box to allow overlapping the honey image
    Box(modifier = Modifier.fillMaxWidth()) {
        
        PanelBox(
            // Added top padding for Medium tasks to make room for honey
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (task.gold >= 5 && task.gold < 10) 12.dp else 0.dp), 
            look = look, 
            contentPadding = 3.dp, 
            overlay = overlay
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StardewCheckbox(checked = done, onCheckedChange = { onToggle() }, checkedColor = a.check)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = StardewTypography.bodyLarge.copy(
                            textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (done) StardewTextSecondary else StardewTextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Pill(task.category, modifier = Modifier.weight(1f, fill = false))
                        GoldBadge(task.gold)
                        if (task.recurrence != "None") {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                PixelIcon(Sprites.loop, 12.dp)
                                Text(Dates.repeatLabel(task.recurrence, task.repeatDays), style = StardewTypography.labelSmall, color = StardewTextPrimary, maxLines = 1)
                            }
                        }
                    }
                    val due = Dates.dueLabel(task.dueDay, task.timeMinutes, today)
                    if (due.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = due,
                            style = StardewTypography.bodySmall,
                            color = if (!done && task.dueDay != null && task.dueDay < today) Color(0xFF8E2A1C) else StardewTextPrimary,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
                PixelIcon(a.sprite, 20.dp, modifier = Modifier.padding(horizontal = 4.dp))
                PixelIconButton(Sprites.pencil, onEdit)
                PixelIconButton(Sprites.trash, onDelete)
            }
        }

        // This draws your custom honey_drip.xml on top of the card!
        if (task.gold >= 5 && task.gold < 10) {
            Image(
                painter = painterResource(id = R.drawable.honey_drip),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }
    }
}
