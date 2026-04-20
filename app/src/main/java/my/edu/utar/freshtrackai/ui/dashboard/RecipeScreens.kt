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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
internal fun RecipeDetailsScreen(
    recipe: RecipeUi,
    onAddMissingToShoppingList: (RecipeUi) -> Int,
    onOpenList: () -> Unit,
    onBack: () -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    var selectedIngredient by remember { mutableStateOf<RecipeIngredientUi?>(null) }
    var showListShortcut by rememberSaveable(recipe.id) { mutableStateOf(false) }
    var lastAddedCount by rememberSaveable(recipe.id) { mutableStateOf(0) }

    Scaffold(
        topBar = { DashboardTopBar(showBack = true, onBack = onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(White)) {
                HorizontalDivider(color = Gray200)
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Button(
                        onClick = {
                            lastAddedCount = onAddMissingToShoppingList(recipe)
                            showListShortcut = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Outlined.FormatListBulleted, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Missing Items to Shopping List", fontWeight = FontWeight.Bold)
                    }
                    if (showListShortcut) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Added $lastAddedCount item(s).", color = Slate600, fontSize = 12.sp)
                            TextButton(onClick = onOpenList) {
                                Text("Open List", color = Emerald, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                BottomNav(RootTab.Recipe, onTabSelected)
            }
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    if (!recipe.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = recipe.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFEAF7EE),
                                            Color(0xFFD9F0E1),
                                            Color(0xFFC7E8D4)
                                        )
                                    )
                                )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC0F172A))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xAAFFFFFF))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = Slate900, modifier = Modifier.size(14.dp))
                        Text("${recipe.prepMinutes} mins", color = Slate900, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Text(
                        recipe.title,
                        color = White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Emerald)
                        Text("Ingredients", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Text("AVAILABLE", color = Slate600, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                    recipe.ingredientsAvailable.forEach { ingredient ->
                        IngredientRow(
                            ingredient = ingredient,
                            positive = true,
                            onClick = { selectedIngredient = ingredient }
                        )
                    }
                    Text("MISSING", color = Slate600, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                    recipe.ingredientsMissing.forEach { ingredient ->
                        IngredientRow(
                            ingredient = ingredient,
                            positive = false,
                            onClick = { selectedIngredient = ingredient }
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.RestaurantMenu, contentDescription = null, tint = Emerald)
                        Text("Instructions", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    recipe.steps.forEachIndexed { index, step ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = White),
                            border = BorderStroke(1.dp, Gray200),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${index + 1}", color = Emerald, fontWeight = FontWeight.Bold)
                                }
                                Text(step, color = Slate900, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedIngredient != null) {
        val ingredient = selectedIngredient!!
        AlertDialog(
            onDismissRequest = { selectedIngredient = null },
            title = { Text(ingredient.name, fontWeight = FontWeight.Bold) },
            text = { Text("Required: ${ingredient.quantityLabel}\nStatus: ${if (ingredient.isAvailable) "Available in inventory" else "Missing - add to shopping list"}") },
            confirmButton = {
                TextButton(onClick = { selectedIngredient = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun IngredientRow(
    ingredient: RecipeIngredientUi,
    positive: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ingredient.name, color = Slate900)
                Text(ingredient.quantityLabel, color = Slate600, fontSize = 12.sp)
            }
            Icon(
                imageVector = if (positive) Icons.Outlined.CheckCircle else Icons.Outlined.Delete,
                contentDescription = null,
                tint = if (positive) Emerald else RoseRed
            )
        }
    }
}

@Composable
internal fun AiRecipesScreen(
    inventory: List<InventoryItem>,
    recipes: List<RecipeUi>,
    loading: Boolean,
    onGenerate: () -> Unit,
    onViewAll: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    val expiringNames = inventory
        .filter { it.expiresInDays <= 2 }
        .sortedBy { it.expiresInDays }
        .take(3)
        .joinToString(", ") { it.name.lowercase() }
        .ifBlank { "chicken, spinach, and heavy cream" }

    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = { BottomNav(RootTab.Recipe, onTabSelected) }
    ) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SmartTipCard(
                    title = "INVENTORY INSIGHTS",
                    message = "Got ingredients about to expire? Our AI analyzed your pantry. You have $expiringNames that should be used soon.",
                    actionText = if (loading) "Generating..." else "Generate Recipe",
                    actionLoading = loading,
                    onAction = onGenerate
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recommended for You", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    if (recipes.isNotEmpty()) {
                        Text(
                            "View All",
                            color = Emerald,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable(onClick = onViewAll)
                        )
                    }
                }
            }

            if (loading && recipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Emerald)
                    }
                }
            } else if (recipes.isNotEmpty()) {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeRecommendationCard(
                        recipe = recipe,
                        onClick = { onOpenRecipe(recipe.id) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun RecipeViewAllScreen(
    inventory: List<InventoryItem>,
    recipes: List<RecipeUi>,
    preferences: RecipePreferencesUi,
    loading: Boolean,
    onPreferencesChange: (RecipePreferencesUi) -> Unit,
    onRegenerate: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    onBack: () -> Unit,
    onTabSelected: (RootTab) -> Unit
) {
    val avoidancePresets = listOf("Spicy", "Onion", "Coriander", "Dairy", "Nuts")

    Scaffold(
        topBar = { DashboardTopBar(showBack = true, onBack = onBack) },
        bottomBar = { BottomNav(RootTab.Recipe, onTabSelected) }
    ) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("View All Recipes", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Customize Generation", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Pick inventory anchors and food avoidances to steer AI recipe suggestions.", color = Slate600)

                        Text("Inventory focus", color = Slate600, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(inventory, key = { it.id }) { item ->
                                val selected = preferences.selectedInventoryItemIds.contains(item.id)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val next = preferences.selectedInventoryItemIds.toMutableSet()
                                        if (selected) next.remove(item.id) else next.add(item.id)
                                        onPreferencesChange(preferences.copy(selectedInventoryItemIds = next))
                                    },
                                    label = { Text(item.name) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Use inventory only", color = Slate900, fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = preferences.inventoryOnly,
                                onCheckedChange = { onPreferencesChange(preferences.copy(inventoryOnly = it)) }
                            )
                        }

                        Text("Avoid ingredients", color = Slate600, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(avoidancePresets) { preset ->
                                val key = preset.lowercase()
                                val selected = preferences.avoidancePresetSet.contains(key)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val next = preferences.avoidancePresetSet.toMutableSet()
                                        if (selected) next.remove(key) else next.add(key)
                                        onPreferencesChange(preferences.copy(avoidancePresetSet = next))
                                    },
                                    label = { Text(preset) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = preferences.avoidanceCustomText,
                            onValueChange = { onPreferencesChange(preferences.copy(avoidanceCustomText = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Custom avoidances") },
                            placeholder = { Text("e.g. mushrooms, shellfish") },
                            colors = freshOutlinedTextFieldColors()
                        )

                        Button(
                            onClick = onRegenerate,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = White)
                            } else {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(if (loading) "Regenerating..." else "Regenerate", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("Results (${recipes.size})", color = Slate900, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            if (loading && recipes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Emerald)
                    }
                }
            } else if (recipes.isEmpty()) {
                item {
                    RecipeEmptyState(
                        onGenerate = onRegenerate
                    )
                }
            } else {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeRecommendationCard(
                        recipe = recipe,
                        onClick = { onOpenRecipe(recipe.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeRecommendationCard(recipe: RecipeUi, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                if (!recipe.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFEAF7EE),
                                        Color(0xFFDDF4E4),
                                        Color(0xFFCDEFD8)
                                    )
                                )
                            )
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xE8DCFCE7))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${recipe.prepMinutes} MIN", color = Color(0xFF166534), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(recipe.title, color = Slate900, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp)
                Text(recipe.description, color = Slate600)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🥗", fontSize = 14.sp)
                    Text(recipe.pantryMatchText, color = Slate600, fontSize = 12.sp)
                }
                Badge(label = recipe.tag.uppercase(), textColor = Emerald, bg = Color(0xFFE8FBEF))
            }
        }
    }
}
@Composable
private fun RecipeEmptyState(
    onGenerate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAF7EE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestaurantMenu,
                    contentDescription = null,
                    tint = Emerald,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "No recipe generated yet",
                color = Slate900,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = "Tap the button below to generate one AI recipe from your current inventory.",
                color = Slate600,
                fontSize = 14.sp
            )

            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald,
                    contentColor = White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Generate Recipe", fontWeight = FontWeight.Bold)
            }
        }
    }
}

