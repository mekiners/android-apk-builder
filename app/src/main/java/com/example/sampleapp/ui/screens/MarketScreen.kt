package com.example.sampleapp.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.GroceryEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.GroceryLogic
import com.example.sampleapp.util.Stats

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

private fun priceText(p: Double): String = if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()

private fun detailLine(item: GroceryEntity, currency: String): String =
    listOfNotNull(
        item.quantity.takeIf { it.isNotBlank() },
        item.price?.let { Stats.money(currency, it) }
    ).joinToString(" \u00B7 ")

@Composable
fun MarketScreen(
    groceries: List<GroceryEntity>,
    currency: String,
    onAdd: (String, String, String, Double?) -> Unit,
    onToggle: (GroceryEntity) -> Unit,
    onDelete: (GroceryEntity) -> Unit,
    onSendToQuest: (GroceryEntity) -> Unit,
    onEditItem: (GroceryEntity, String, String, Double?) -> Unit,
    onToggleStock: (GroceryEntity) -> Unit,
    onStockUp: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var qtyInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("Auto") }
    var editing by remember { mutableStateOf<GroceryEntity?>(null) }

    fun submit() {
        if (input.isBlank()) return
        onAdd(input, section, qtyInput, priceInput.replace(',', '.').toDoubleOrNull())
        input = ""
        qtyInput = ""
        priceInput = ""
    }

    val pending = groceries.filter { !it.isBought && !it.inStock }
    val bought = groceries.filter { it.isBought && !it.inStock }
    val pantry = groceries.filter { it.inStock }
    val grouped = pending.groupBy { it.section }
    val estimate = (pending + bought).sumOf { it.price ?: 0.0 }

    val tex = LocalTextures.current
    val corkArt = tex.art.bgMarket
    // Wooden frame around a cork bulletin board
    Box(modifier = Modifier.fillMaxSize().tiledBackground(tex.wood, 3.dp).bevel(inset = 0.dp, thickness = 2.dp).padding(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (corkArt != null) Modifier.artTile(corkArt, 240.dp) else Modifier.tiledBackground(tex.cork, 3.dp))
                .border(3.dp, StardewWoodDark)
                .bevel(inset = 3.dp, thickness = 3.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
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
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StardewTextField(
                                value = qtyInput,
                                onValueChange = { qtyInput = it },
                                placeholder = "Qty (2 kg)",
                                modifier = Modifier.weight(1f),
                                height = 36.dp
                            )
                            StardewTextField(
                                value = priceInput,
                                onValueChange = { priceInput = it },
                                placeholder = "Price",
                                modifier = Modifier.weight(1f),
                                height = 36.dp,
                                keyboardType = KeyboardType.Decimal
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Pick a section once and I'll remember it for that item.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
                        Spacer(Modifier.height(4.dp))
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
                        item(key = "header_$s") { SectionLabel(s, sprite = sectionSprite(s)) }
                        items(list, key = { it.id }) { g ->
                            GroceryNote(g, currency, onToggle, onDelete, onSendToQuest) { editing = g }
                        }
                    }
                }

                if (bought.isNotEmpty()) {
                    item(key = "header_bought") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            SectionLabel("In the basket", sprite = Sprites.basket)
                            StardewChip("Stock up", false, onStockUp, sprite = Sprites.jar)
                        }
                    }
                    items(bought, key = { it.id }) { g ->
                        GroceryNote(g, currency, onToggle, onDelete, onSendToQuest) { editing = g }
                    }
                }

                if (pantry.isNotEmpty()) {
                    item(key = "header_pantry") { SectionLabel("Pantry (in stock)", sprite = Sprites.jar) }
                    items(pantry, key = { it.id }) { g ->
                        PantryNote(g, currency, onToggleStock, onDelete)
                    }
                }

                if (pending.isNotEmpty() || bought.isNotEmpty()) {
                    item(key = "footer") {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionLabel("${bought.size} of ${pending.size + bought.size} items gathered!")
                            if (estimate > 0.0) SectionLabel("Estimate: " + Stats.money(currency, estimate))
                        }
                    }
                }
            }
        }
    }

    editing?.let { item ->
        EditGroceryDialog(
            item = item,
            onSave = { sec, qty, price -> onEditItem(item, sec, qty, price) },
            onToggleStock = { onToggleStock(item) },
            onDismiss = { editing = null }
        )
    }
}

/** A paper slip with a red pin at the top. */
@Composable
private fun PinnedNote(content: @Composable ColumnScope.() -> Unit) {
    val tex = LocalTextures.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .tiledBackground(tex.parchment, 3.dp)
                .border(2.dp, PaperEdge)
                .bevel(inset = 2.dp, thickness = 1.dp)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            content = content
        )
        PixelIcon(Sprites.pin, 14.dp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-6).dp))
    }
}

@Composable
private fun GroceryNote(
    item: GroceryEntity,
    currency: String,
    onToggle: (GroceryEntity) -> Unit,
    onDelete: (GroceryEntity) -> Unit,
    onSendToQuest: (GroceryEntity) -> Unit,
    onEdit: () -> Unit
) {
    val tex = LocalTextures.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tiledBackground(if (item.isBought) tex.parchmentDark else tex.parchment, 3.dp)
                .border(2.dp, PaperEdge)
                .bevel(inset = 2.dp, thickness = 1.dp)
                .padding(start = 10.dp, end = 4.dp, top = 9.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StardewCheckbox(item.isBought, { onToggle(item) })
            Spacer(Modifier.width(8.dp))
            PixelIcon(sectionSprite(item.section), 22.dp, modifier = Modifier.clickable(onClick = onEdit))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f).clickable(onClick = onEdit)) {
                Text(
                    text = item.name,
                    style = StardewTypography.bodyLarge.copy(
                        textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (item.isBought) StardewTextSecondary else StardewTextPrimary
                )
                val detail = detailLine(item, currency)
                if (detail.isNotEmpty()) Text(detail, style = StardewTypography.bodySmall, color = StardewTextPrimary)
            }
            if (!item.isBought) {
                StardewChip("Quest", false, { onSendToQuest(item) }, sprite = Sprites.scroll)
                Spacer(Modifier.width(2.dp))
            }
            PixelIconButton(Sprites.trash, { onDelete(item) })
        }
        PixelIcon(Sprites.pin, 12.dp, modifier = Modifier.align(Alignment.TopStart).offset(x = 8.dp, y = (-5).dp))
    }
}

@Composable
private fun PantryNote(
    item: GroceryEntity,
    currency: String,
    onToggleStock: (GroceryEntity) -> Unit,
    onDelete: (GroceryEntity) -> Unit
) {
    val tex = LocalTextures.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tiledBackground(tex.parchmentDark, 3.dp)
            .border(2.dp, PaperEdge)
            .bevel(inset = 2.dp, thickness = 1.dp)
            .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PixelIcon(sectionSprite(item.section), 22.dp)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = StardewTypography.bodyLarge)
            val detail = detailLine(item, currency)
            if (detail.isNotEmpty()) Text(detail, style = StardewTypography.bodySmall, color = StardewTextPrimary)
        }
        StardewChip("Need more", false, { onToggleStock(item) }, sprite = Sprites.basket)
        PixelIconButton(Sprites.trash, { onDelete(item) })
    }
}

@Composable
private fun EditGroceryDialog(
    item: GroceryEntity,
    onSave: (String, String, Double?) -> Unit,
    onToggleStock: () -> Unit,
    onDismiss: () -> Unit
) {
    var section by remember { mutableStateOf(item.section) }
    var qty by remember { mutableStateOf(item.quantity) }
    var price by remember { mutableStateOf(item.price?.let { priceText(it) } ?: "") }
    StardewDialog(
        title = "Edit \"${item.name}\"",
        confirmText = "Save",
        onDismiss = onDismiss,
        onConfirm = {
            onSave(section, qty, price.replace(',', '.').toDoubleOrNull())
            onDismiss()
        }
    ) {
        Text("Pick a section. I'll remember it for next time.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
        GroceryLogic.sections.chunked(4).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowItems.forEach { sec ->
                    StardewChip(sec, section == sec, { section = sec }, sprite = sectionSprite(sec))
                }
            }
        }
        StardewTextField(value = qty, onValueChange = { qty = it }, placeholder = "Quantity (e.g. 2 kg)", modifier = Modifier.fillMaxWidth())
        StardewTextField(
            value = price,
            onValueChange = { price = it },
            placeholder = "Price estimate",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Decimal
        )
        StardewChip(
            text = if (item.inStock) "Need more (back to the list)" else "I already have it (pantry)",
            selected = false,
            onClick = { onToggleStock(); onDismiss() },
            sprite = Sprites.jar
        )
    }
}
