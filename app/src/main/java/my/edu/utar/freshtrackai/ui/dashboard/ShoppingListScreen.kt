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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ShoppingListScreen(
    items: List<ShoppingListItemUi>,
    onItemCheckedChange: (String, Boolean) -> Unit,
    onAddGeneralItem: (String) -> Unit,
    onClearPurchased: () -> Int,
    onTabSelected: (RootTab) -> Unit
) {
    var quickAddText by rememberSaveable { mutableStateOf("") }
    var showClearConfirm by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val remainingCount = items.count { !it.checked }
    val recipeItems = items.filter { it.sourceRecipeName != null }
    val generalItems = items.filter { it.sourceRecipeName == null }

    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = { BottomNav(RootTab.List, onTabSelected) }
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quickAddText,
                        onValueChange = { quickAddText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add Item to list...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = {
                            val name = quickAddText.trim()
                            if (name.isNotBlank()) {
                                onAddGeneralItem(name)
                                quickAddText = ""
                            }
                        },
                        enabled = quickAddText.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("ADD", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Shopping List", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("$remainingCount items remaining", color = Slate600)
                    }
                    TextButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = RoseRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("CLEAR", color = RoseRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { HorizontalDivider(color = Gray200) }

            if (recipeItems.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
                        Text(
                            "MISSING FROM RECIPES",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
                items(recipeItems, key = { it.id }) { item ->
                    ShoppingListRow(
                        item = item,
                        onCheckedChange = { checked ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemCheckedChange(item.id, checked)
                        }
                    )
                    HorizontalDivider(color = Gray200)
                }
            }

            if (generalItems.isNotEmpty()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.FormatListBulleted, contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
                        Text(
                            "GENERAL GROCERY LIST",
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
                items(generalItems, key = { it.id }) { item ->
                    ShoppingListRow(
                        item = item,
                        onCheckedChange = { checked ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onItemCheckedChange(item.id, checked)
                        }
                    )
                    HorizontalDivider(color = Gray200)
                }
            }

            if (items.isEmpty()) {
                item {
                    Text(
                        "No shopping items yet. Add manually or from recipe details.",
                        color = Slate600
                    )
                }
            }

            item {
                SmartTipCard(
                    title = "SMART PLANNING",
                    message = "Based on your recent scans, your milk is likely to expire in 2 days. Added to list automatically."
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Purchased?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all checked items from your shopping list.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearPurchased()
                    showClearConfirm = false
                }) { Text("Clear", color = RoseRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ShoppingListRow(
    item: ShoppingListItemUi,
    onCheckedChange: (Boolean) -> Unit
) {
    val titleColor = if (item.checked) Color(0xFF9CA3AF) else Slate900
    val titleDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
    val contextColor = if (item.checked) Color(0xFF86EFAC) else Emerald

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { checked -> onCheckedChange(checked) },
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = Emerald,
                uncheckedColor = Gray200,
                checkmarkColor = White
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                textDecoration = titleDecoration
            )
            if (item.sourceRecipeName != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("RECIPE:", color = Slate600, fontSize = 12.sp, letterSpacing = 1.sp)
                    Text(item.sourceRecipeName.uppercase(), color = contextColor, fontSize = 12.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}


