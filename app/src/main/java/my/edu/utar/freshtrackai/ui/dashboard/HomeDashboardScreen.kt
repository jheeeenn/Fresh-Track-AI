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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MainDashboardScreen(
    inventory: List<InventoryItem>,
    onViewAllExpiring: () -> Unit,
    onRecipe: () -> Unit,
    onScan: () -> Unit,
    onAddItem: () -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    val expiring = inventory.mapNotNull { it.toExpiringOrNull() }.sortedBy { it.expiresInDays }
    val grouped = InventoryCategory.entries.associateWith { category -> inventory.filter { it.category == category } }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = White,
            topBar = { DashboardTopBar() },
            bottomBar = { BottomNav(active = RootTab.Home, onTabSelected = onTabSelected) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                Text("KITCHEN OVERVIEW", color = Slate600, fontSize = 10.sp, letterSpacing = 1.sp)
                Text(
                    buildAnnotatedString {
                        append("You have ")
                        pushStyle(SpanStyle(color = Emerald, fontWeight = FontWeight.ExtraBold))
                        append(inventory.size.toString())
                        pop()
                        append(" items trackable")
                    },
                    fontSize = 30.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Expiring Soon", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Slate900)
                    Text("View all", color = Emerald, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onViewAllExpiring))
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(expiring) { item ->
                        ExpiringCarouselCard(item = item, onRecipe = onRecipe)
                    }
                }
            }

            item {
                Button(
                    onClick = onScan,
                    modifier = Modifier.fillMaxWidth().height(88.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Scan Groceries", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text("AI vision auto-import", fontSize = 13.sp)
                        }
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Add Item",
                        icon = Icons.Outlined.AddCircle,
                        iconTint = RoseRed,
                        onClick = onAddItem
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Checklist",
                        icon = Icons.Outlined.FormatListBulleted,
                        iconTint = Emerald,
                        onClick = { onTabSelected(RootTab.List) }
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Current Inventory", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Slate900)
                    Row {
                        IconButton(onClick = {}) { Icon(Icons.Outlined.Sort, contentDescription = "Sort", tint = Slate600) }
                        IconButton(onClick = {}) { Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = Slate600) }
                    }
                }
            }

                InventoryCategory.entries.forEach { category ->
                    val categoryItems = grouped[category].orEmpty()
                    if (categoryItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "${categoryEmoji(category)} ${category.label.uppercase()} (${categoryItems.size})",
                                color = Slate600,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(categoryItems) { item ->
                            InventoryRow(item = item)
                        }
                    }
                }
            }
        }

        DraggableAddFab(
            onClick = onAddItem,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
internal fun ExpiringSoonAllScreen(
    inventory: List<InventoryItem>,
    searchQuery: String,
    selectedCategory: InventoryCategory?,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (InventoryCategory?) -> Unit,
    onRecipe: () -> Unit,
    onMarkUsed: (String) -> Unit,
    onBack: () -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    val expiring = inventory.mapNotNull { it.toExpiringOrNull() }.sortedBy { it.expiresInDays }
    val filtered = expiring.filter { item ->
        val categoryOk = selectedCategory == null || item.category == selectedCategory
        val searchOk = searchQuery.isBlank() ||
            item.name.contains(searchQuery, ignoreCase = true) ||
            item.category.label.contains(searchQuery, ignoreCase = true)
        categoryOk && searchOk
    }

    Scaffold(
        containerColor = White,
        topBar = { DashboardTopBar(showBack = true, onBack = onBack) },
        bottomBar = { BottomNav(active = RootTab.Home, onTabSelected = onTabSelected) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("All Expiring Soon", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Slate900)
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search expiring items") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategoryChange(null) },
                            label = { Text("All") }
                        )
                    }
                    items(InventoryCategory.entries) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategoryChange(category) },
                            label = { Text("${categoryEmoji(category)} ${category.label}") }
                        )
                    }
                }
            }

            ExpiryBadge.entries.forEach { band ->
                val bandItems = filtered.filter { it.badge == band }
                if (bandItems.isNotEmpty()) {
                    item {
                        Text(
                            "${band.label} (${bandItems.size})",
                            color = band.textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    items(bandItems) { item ->
                        ExpiringListCard(
                            item = item,
                            onRecipe = onRecipe,
                            onMarkUsed = { onMarkUsed(item.inventoryItemId) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Gray50), border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)) {
                        Text(
                            "No expiring items match your filter.",
                            color = Slate600,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpiringCarouselCard(item: ExpiringItem, onRecipe: () -> Unit) {
    Card(
        modifier = Modifier.width(268.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)).background(Gray100), contentAlignment = Alignment.Center) {
                Text(categoryEmoji(item.category), fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, color = Slate900)
                Text("${categoryEmoji(item.category)} ${daysLabel(item.expiresInDays)}", color = item.badge.textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Badge(label = item.badge.label, textColor = item.badge.textColor, bg = item.badge.bgColor)
                    TextButton(onClick = onRecipe, contentPadding = PaddingValues(0.dp)) {
                        Icon(Icons.Outlined.RestaurantMenu, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Recipe", color = Emerald, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpiringListCard(item: ExpiringItem, onRecipe: () -> Unit, onMarkUsed: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("${categoryEmoji(item.category)} ${item.category.label} • ${daysLabel(item.expiresInDays)}", fontSize = 12.sp, color = Slate600)
                }
                Badge(label = item.badge.label, textColor = item.badge.textColor, bg = item.badge.bgColor)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRecipe,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.RestaurantMenu, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Recipe")
                }
                OutlinedButton(
                    onClick = onMarkUsed,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Emerald, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Mark Used", color = Slate900)
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(modifier: Modifier, title: String, icon: ImageVector, iconTint: Color, onClick: () -> Unit) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = Gray100),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
            Text(title, fontWeight = FontWeight.SemiBold, color = Slate900)
        }
    }
}

@Composable
private fun InventoryRow(item: InventoryItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Gray100), contentAlignment = Alignment.Center) {
                    Text(categoryEmoji(item.category), fontSize = 18.sp)
                }
                Column {
                    Text(item.name, fontWeight = FontWeight.SemiBold, color = Slate900)
                    Text("Added ${item.addedDaysAgo} days ago", color = Slate600, fontSize = 12.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(item.quantityLabel, fontWeight = FontWeight.SemiBold, color = Slate900)
                val band = urgencyForDays(item.expiresInDays)
                val tagColor = band?.textColor ?: Slate600
                Text(daysLabel(item.expiresInDays), color = tagColor, fontSize = 12.sp)
            }
        }
    }
}


