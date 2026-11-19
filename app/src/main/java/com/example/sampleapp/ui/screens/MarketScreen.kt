package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.GroceryEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.GroceryLogic

fun sectionSprite(section: String): Sprite = when (section) {
    "Produce" -> Sprites.apple
    "Meat" -> Sprites.meat
    "Dairy" -> Sprites.milk
    "Bakery" -> Sprites.bread
    "Pantry" -> Sprites.jar
    "Household" -> Sprites.sponge
    "Tech" -> Sprites.phone
    else -> Sprites.crate
}

private val PaperEdge = androidx.compose.ui.graphics.Color(0xFF8A6A3A)

@Composable
fun MarketScreen(
    groceries: List<GroceryEntity>,
    onAdd: (String, String) -> Unit,
    onToggle: (GroceryEntity) -> Unit,
    onDelete: (GroceryEntity) -> Unit,
    onSendToQuest: (GroceryEntity) -> Unit,
    onClearBought: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("Auto") }

    fun submit() {
        if (input.isBlank()) return
        onAdd(input, section)
        input = ""
    }

    val pending = groceries.filter { !it.isBought }
    val bought = groceries.filter { it.isBought }
    val grouped = pending.groupBy { it.section }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
                item {
                    // The "add a note" slip
                    PinnedNote {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StardewTextField(
                                value = input,
                                onValueChange = { input = it },
                                placeholder = "Pin a new item...",
                                modifier = Modifier.weight(1f),
                                height = 40.dp,
                                onDone = { submit() }
                            )
                            StardewIconButton(Sprites.plus, { submit() }, size = 40.dp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            StardewChip("Auto", section == "Auto", { section = "Auto" }, selectedColor = StardewGold, selectedTextColor = StardewTextPrimary)
                            GroceryLogic.sections.forEach { s ->
                                StardewChip(s, section == s, { section = s }, sprite = sectionSprite(s))
                            }
                        }
                    }
                }

                if (groceries.isEmpty()) {
                    item {
                        PinnedNote {
                            Text("The board is empty. Pin what you need from the market!", style = StardewTypography.bodyMedium)
                        }
                    }
                }

                GroceryLogic.sections.forEach { s ->
                    val list = grouped[s].orEmpty()
                    if (list.isNotEmpty()) {
                        item(key = "header_$s") { PlankLabel(s, sectionSprite(s)) }
                        items(list, key = { it.id }) { g ->
                            GroceryNote(g, onToggle, onDelete, onSendToQuest)
                        }
                    }
                }

                if (bought.isNotEmpty()) {
                    item(key = "header_bought") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            PlankLabel("In the basket", Sprites.basket)
                            StardewChip("Clear", false, onClearBought)
                        }
                    }
                    items(bought, key = { it.id }) { g ->
                        GroceryNote(g, onToggle, onDelete, onSendToQuest)
                    }
                }

                if (groceries.isNotEmpty()) {
                    item(key = "footer") {
                        SectionLabel("${bought.size} of ${groceries.size} items gathered!")
                    }
                }
    }
}

/** A paper slip with a red pin at the top. */
@Composable
private fun PinnedNote(content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFFFFF1D5), Color(0xFFEAD8B6))), RoundedCornerShape(8.dp))
                .border(1.dp, PaperEdge, RoundedCornerShape(8.dp))
                .padding(horizontal = 11.dp, vertical = 12.dp),
            content = content
        )
        PixelIcon(Sprites.pin, 14.dp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-6).dp))
    }
}

/** Section header: a small wooden sign hanging on the board. */
@Composable
private fun PlankLabel(text: String, sprite: Sprite) {
    SectionLabel(text, sprite = sprite)
}

@Composable
private fun GroceryNote(
    item: GroceryEntity,
    onToggle: (GroceryEntity) -> Unit,
    onDelete: (GroceryEntity) -> Unit,
    onSendToQuest: (GroceryEntity) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (item.isBought) Color(0xFFE2D7C0) else Color(0xFFFFF1D5), RoundedCornerShape(8.dp))
                .border(1.dp, PaperEdge, RoundedCornerShape(8.dp))
                .padding(start = 10.dp, end = 4.dp, top = 9.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StardewCheckbox(item.isBought, { onToggle(item) })
            Spacer(Modifier.width(8.dp))
            PixelIcon(sectionSprite(item.section), 22.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = item.name,
                style = StardewTypography.bodyLarge.copy(
                    textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (item.isBought) StardewTextSecondary else StardewTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!item.isBought) {
                StardewChip("Quest", false, { onSendToQuest(item) }, sprite = Sprites.scroll)
                Spacer(Modifier.width(2.dp))
            }
            PixelIconButton(Sprites.trash, { onDelete(item) })
        }
        PixelIcon(Sprites.pin, 12.dp, modifier = Modifier.align(Alignment.TopStart).offset(x = 8.dp, y = (-5).dp))
    }
}
