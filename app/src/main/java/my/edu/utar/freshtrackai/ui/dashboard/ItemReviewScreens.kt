package my.edu.utar.freshtrackai.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AddMissingItemScreen(
    draft: AddItemFormDraft,
    isEditMode: Boolean,
    onDraftChange: (AddItemFormDraft) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    var categoryExpanded by remember { mutableStateOf(false) }
    val canSubmit = draft.name.isNotBlank()

    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(White)) {
                HorizontalDivider(color = Gray200)
                Button(
                    onClick = onSubmit,
                    enabled = canSubmit,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .height(56.dp)
                ) {
                    Icon(Icons.Outlined.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditMode) "Save Changes" else "Add Item", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                BottomNav(RootTab.Scan, onTabSelected)
            }
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Slate900)
                    }
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Add Missing Item", color = Slate900, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 32.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Manually catalog items that weren't captured during your last scan.",
                            color = Slate600
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, Gray200),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { onDraftChange(draft.copy(name = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Item Name") },
                            placeholder = { Text("e.g. Organic Baby Spinach") },
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = draft.quantity,
                                onValueChange = { onDraftChange(draft.copy(quantity = it)) },
                                modifier = Modifier.weight(1f),
                                label = { Text("Quantity") },
                                placeholder = { Text("e.g. 1 box") },
                                singleLine = true
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { categoryExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Gray200),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                ) {
                                    Text(
                                        text = "${categoryEmoji(draft.category)} ${draft.category.label}",
                                        color = Slate900,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1
                                    )
                                    Text("▾", color = Slate600, fontWeight = FontWeight.Bold)
                                }

                                DropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false }
                                ) {
                                    InventoryCategory.entries.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text("${categoryEmoji(category)} ${category.label}") },
                                            onClick = {
                                                onDraftChange(draft.copy(category = category))
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = draft.expiryDate,
                            onValueChange = { onDraftChange(draft.copy(expiryDate = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Expiry Date") },
                            placeholder = { Text("e.g. Oct 24, 2026") },
                            leadingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null, tint = Slate600) },
                            singleLine = true
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = BorderStroke(1.dp, Gray200),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🍎", fontSize = 18.sp)
                            Text("Nutritional Info", color = Slate900, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = draft.nutritionNotes,
                            onValueChange = { onDraftChange(draft.copy(nutritionNotes = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. 23 kcal / 100g, high fiber, low sodium") },
                            minLines = 4
                        )
                        OutlinedButton(
                            onClick = {
                                if (draft.nutritionNotes.isBlank()) {
                                    onDraftChange(draft.copy(nutritionNotes = "Quick scan label (mock): 120 kcal / serving"))
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Gray200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Slate600)
                            Spacer(Modifier.width(8.dp))
                            Text("Quick Scan Label", color = Slate900, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ItemReviewScreen(
    reviewItems: List<ReviewItemUi>,
    onAddMissingItem: () -> Unit,
    onEditItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onSaveToInventory: () -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(White)) {
                HorizontalDivider(color = Gray200)
                Button(
                    onClick = onSaveToInventory,
                    enabled = reviewItems.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .height(56.dp)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save to Inventory", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                BottomNav(RootTab.Scan, onTabSelected)
            }
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Review Scanned Items", color = Slate900, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                Text("${reviewItems.size} items detected. Verify details before saving.", color = Slate600)
            }

            items(reviewItems, key = { it.id }) { item ->
                ReviewScannedItemCard(
                    item = item,
                    onEdit = { onEditItem(item.id) },
                    onDelete = { onDeleteItem(item.id) }
                )
            }

            item {
                DashedAddMissingButton(onClick = onAddMissingItem)
            }
        }
    }
}

@Composable
private fun ReviewScannedItemCard(item: ReviewItemUi, onEdit: () -> Unit, onDelete: () -> Unit) {
    val urgent = item.expiresInDays <= 1
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ReviewThumb(item = item)
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, color = Slate900, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 26.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Badge(label = item.category.label.uppercase(), textColor = Slate600, bg = Gray100)
                        Text("Qty: ${item.quantityLabel}", color = Slate600)
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Slate600)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Slate600)
                    }
                }
            }

            HorizontalDivider(color = Gray200)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ReviewMetaTile(
                    modifier = Modifier.weight(1f),
                    label = "EXPIRES",
                    value = item.expiresLabel,
                    icon = if (urgent) Icons.Outlined.WarningAmber else Icons.Outlined.DateRange,
                    valueColor = if (urgent) RoseRed else Slate900,
                    iconColor = if (urgent) RoseRed else Emerald
                )
                ReviewMetaTile(
                    modifier = Modifier.weight(1f),
                    label = "NUTRITION",
                    value = item.nutritionLabel,
                    icon = Icons.Outlined.AutoAwesome,
                    valueColor = Slate900,
                    iconColor = Emerald
                )
            }
        }
    }
}

@Composable
private fun ReviewMetaTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color,
    iconColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Gray50)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, color = Slate600, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Text(value, color = valueColor)
        }
    }
}

@Composable
private fun ReviewThumb(item: ReviewItemUi) {
    val bg = reviewThumbBackground(item.thumbnailRef)
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(categoryEmoji(item.category), fontSize = 34.sp)
    }
}

@Composable
private fun DashedAddMissingButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(White)
            .clickable(onClick = onClick)
            .drawBehind {
                val strokePx = 1.dp.toPx()
                drawRoundRect(
                    color = Gray200,
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(
                        width = strokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f))
                    )
                )
            }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AddCircle, contentDescription = null, tint = Slate600)
            Text("Add Missing Item", color = Slate600, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

