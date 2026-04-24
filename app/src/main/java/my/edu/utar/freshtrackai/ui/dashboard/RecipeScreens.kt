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
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    val previewLabels = recipeHeroPreviewLabels(recipe)

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
                        Icon(
                            Icons.AutoMirrored.Outlined.FormatListBulleted,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Add Missing Items to Shopping List", fontWeight = FontWeight.Bold)
                    }
                    if (showListShortcut) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                recipeAddMissingConfirmationText(lastAddedCount),
                                color = Slate600,
                                fontSize = 12.sp
                            )
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
                RecipeDetailsHero(
                    recipe = recipe,
                    previewLabels = previewLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(304.dp)
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
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
                    modifier = Modifier.padding(horizontal = 16.dp),
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
private fun RecipeDetailsHero(
    recipe: RecipeUi,
    previewLabels: List<String>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (!recipe.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            RecipeArtworkPlaceholder(
                recipe = recipe,
                modifier = Modifier.fillMaxSize(),
                compact = false
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x140F172A), Color(0xE60F172A))
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xCCFFFFFF))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = Slate900,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "${recipe.prepMinutes} mins",
                    color = Slate900,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (previewLabels.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(previewLabels) { label ->
                            Badge(
                                label = label.take(22).uppercase(),
                                textColor = Color(0xFF14532D),
                                bg = Color.White.copy(alpha = 0.82f)
                            )
                        }
                    }
                }
                Text(
                    recipe.title,
                    color = White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    lineHeight = 36.sp
                )
                recipeDescriptionText(recipe.description)?.let { description ->
                    Text(
                        description,
                        color = Color(0xFFF8FAFC),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
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
                imageVector = if (positive) Icons.Outlined.CheckCircle else Icons.Outlined.Close,
                contentDescription = null,
                tint = if (positive) Emerald else RoseRed
            )
        }
    }
}

@Composable
internal fun AiRecipesScreen(
    recipes: List<RecipeUi>,
    loading: Boolean,
    loadingMessage: String?,
    onGenerate: () -> Unit,
    onOpenRecipe: (String) -> Unit,
    onTabSelected: (RootTab) -> Unit,
    showGuide: Boolean,
    onDismissGuide: () -> Unit
) {
    Scaffold(
        topBar = { DashboardTopBar() },
        bottomBar = { BottomNav(RootTab.Recipe, onTabSelected) }
    ) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!loadingMessage.isNullOrBlank()) {
                item {
                    RecipeProcessingBanner(message = loadingMessage)
                }
            }

            if (showGuide) {
                item {
                    SmartTipCard(
                        title = "RECIPE GUIDE",
                        message = "Generate multiple recipe ideas from your inventory, open one to review the steps, then add missing ingredients to your shopping list if needed.",
                        onDismiss = onDismissGuide
                    )
                }
            }

            item {
                Button(
                    onClick = onGenerate,
                    enabled = !loading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (loading) "Generating…" else "Generate Recipe", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Generated Recipes",
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            if (recipes.isEmpty() && !loading) {
                item {
                    RecipeEmptyState(
                        onGenerate = onGenerate
                    )
                }
            } else if (!loading || recipes.isNotEmpty()) {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeRecommendationCard(
                        recipe = recipe,
                        onClick = { onOpenRecipe(recipe.id) })
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
                    RecipeArtworkPlaceholder(
                        recipe = recipe,
                        modifier = Modifier.fillMaxSize(),
                        compact = true
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
                    Text(
                        "${recipe.prepMinutes} MIN",
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    recipe.title,
                    color = Slate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    lineHeight = 26.sp
                )
                Text(recipe.description, color = Slate600)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🥗", fontSize = 14.sp)
                    Text(recipe.pantryMatchText, color = Slate600, fontSize = 12.sp)
                }
                Badge(label = recipe.tag.uppercase(), textColor = Emerald, bg = Color(0xFFE8FBEF))
            }
        }
    }
}

@Composable
private fun RecipeArtworkPlaceholder(
    recipe: RecipeUi,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    val palette = recipePlaceholderPalette(recipe.id)
    val previewLabels = recipeHeroPreviewLabels(recipe, maxCount = 2)

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = palette
            )
        )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(if (compact) 10.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (compact) {
                Badge(
                    label = "AI RECIPE",
                    textColor = Slate900,
                    bg = Color.White.copy(alpha = 0.72f)
                )
                if (previewLabels.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        previewLabels.forEach { label ->
                            Badge(
                                label = label.take(18).uppercase(),
                                textColor = Color(0xFF14532D),
                                bg = Color.White.copy(alpha = 0.68f)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun recipeHeroPreviewLabels(
    recipe: RecipeUi,
    maxCount: Int = 3
): List<String> {
    if (maxCount <= 0) return emptyList()

    val orderedLabels = linkedMapOf<String, String>()
    (recipe.ingredientsAvailable + recipe.ingredientsMissing).forEach { ingredient ->
        val label = ingredient.name.trim()
        val normalized = label.lowercase()
        if (label.isNotEmpty() && normalized !in orderedLabels && orderedLabels.size < maxCount) {
            orderedLabels[normalized] = label
        }
    }
    return orderedLabels.values.toList()
}

internal fun recipeAddMissingConfirmationText(count: Int): String = "Added $count item(s)."

private fun recipeDescriptionText(description: String): String? =
    description.trim().takeIf { it.isNotEmpty() }

private fun recipePlaceholderPalette(recipeKey: String): List<Color> {
    return when ((recipeKey.hashCode() and Int.MAX_VALUE) % 4) {
        0 -> listOf(Color(0xFFE6F7EE), Color(0xFFD3F2DE), Color(0xFFB7E4C7))
        1 -> listOf(Color(0xFFF5F3E7), Color(0xFFECE6C8), Color(0xFFD9D6A5))
        2 -> listOf(Color(0xFFE8F4F8), Color(0xFFD7EBF3), Color(0xFFC1DFEB))
        else -> listOf(Color(0xFFF5ECE7), Color(0xFFEFD9CC), Color(0xFFE8C2A6))
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
                text = "No recipes generated yet",
                color = Slate900,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = "Tap the button below to generate recipe ideas from your current inventory.",
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
