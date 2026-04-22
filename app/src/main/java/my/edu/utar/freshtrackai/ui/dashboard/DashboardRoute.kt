package my.edu.utar.freshtrackai.ui.dashboard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.UUID

import my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import my.edu.utar.freshtrackai.ai.RecipeGenerationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.ai.ReceiptOcrProvider
import my.edu.utar.freshtrackai.ai.ReceiptReviewMapper
import my.edu.utar.freshtrackai.ai.ScanCaptureBitmapResolver
import my.edu.utar.freshtrackai.ai.FoodExtractorProvider
import my.edu.utar.freshtrackai.ai.FoodReviewMapper

@Composable
fun FreshTrackDashboardScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    var screen by rememberSaveable { mutableStateOf(WiseScreen.AppLauncher) }
    
    val inventoryVm: my.edu.utar.freshtrackai.ui.inventory.InventoryViewModel = viewModel(
        factory = my.edu.utar.freshtrackai.ui.inventory.InventoryViewModel.Factory(LocalContext.current.applicationContext)
    )
    val dbItems by inventoryVm.allItems.collectAsState()
    
    val inventory = remember(dbItems) { 
        mutableStateListOf<InventoryItem>().apply { addAll(dbItems.map { it.toUiModel() }) } 
    }
    val reviewItems = remember { mutableStateListOf<ReviewItemUi>().apply { addAll(seedReviewItems()) } }
    val recipesAll = remember { seedRecipes() }
    val recipeRecommendations = remember { mutableStateListOf<RecipeUi>() }
    val shoppingListItems = remember { mutableStateListOf<ShoppingListItemUi>().apply { addAll(seedShoppingListItems()) } }
    var expiringSearch by rememberSaveable { mutableStateOf("") }
    var expiringFilter by rememberSaveable { mutableStateOf<InventoryCategory?>(null) }
    var editingReviewItemId by remember { mutableStateOf<String?>(null) }
    var editingInventoryItemId by remember { mutableStateOf<String?>(null) }
    var addItemOrigin by remember { mutableStateOf(AddItemOrigin.ItemReview) }
    var addFormDraft by remember { mutableStateOf(AddItemFormDraft()) }
    var selectedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    var recipeBackTarget by rememberSaveable { mutableStateOf(WiseScreen.AiRecipes) }
    var recipePreferences by remember { mutableStateOf(RecipePreferencesUi()) }
    var recipeLoading by remember { mutableStateOf(false) }
    var recipeRefreshTick by rememberSaveable { mutableStateOf(0) }
    val recipeViewModel: RecipeGenerationViewModel = viewModel() // new
    val recipeUiState by recipeViewModel.uiState.collectAsState() // new

    fun refreshRecipes() {
        recipeViewModel.generateRecipes(
            inventory = inventory.toList(),
            preferences = recipePreferences
        )
    }

    LaunchedEffect(recipeUiState.recipes) {
        if (recipeUiState.recipes.isNotEmpty()) {
            if (selectedRecipeId == null || recipeUiState.recipes.none { it.id == selectedRecipeId }) {
                selectedRecipeId = recipeUiState.recipes.first().id
            }
        }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(recipeUiState.errorMessage) {
        val message = recipeUiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        recipeViewModel.clearError()
    }

    LaunchedEffect(toastMessage) {
        val message = toastMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        toastMessage = null
    }

    LaunchedEffect(recipeRefreshTick, recipePreferences, inventory.joinToString("|") { it.id }) {
        recipeLoading = true
        delay(450)
        val generated = generateRecipesForPreferences(
            recipesAll = recipesAll,
            inventory = inventory,
            preferences = recipePreferences,
            refreshTick = recipeRefreshTick
        )
        recipeRecommendations.clear()
        recipeRecommendations.addAll(generated)
        if (selectedRecipeId == null || recipesAll.none { it.id == selectedRecipeId }) {
            selectedRecipeId = recipeRecommendations.firstOrNull()?.id
        }
        recipeLoading = false
    }

    val toRootTab: (RootTab) -> Unit = { tab ->
        screen = when (tab) {
            RootTab.Home -> WiseScreen.MainDashboard
            RootTab.Scan -> WiseScreen.SmartScan
            RootTab.Recipe -> WiseScreen.AiRecipes
            RootTab.List -> WiseScreen.ShoppingList
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (screen) {
            WiseScreen.AppLauncher -> AppLauncherScreen { screen = WiseScreen.MainDashboard }
            WiseScreen.MainDashboard -> MainDashboardScreen(
                inventory = inventory,
                onViewAllExpiring = { screen = WiseScreen.ExpiringSoonAll },
                onRecipe = { screen = WiseScreen.AiRecipes },
                onScan = { screen = WiseScreen.SmartScan },
                onAddItem = {
                    addItemOrigin = AddItemOrigin.Dashboard
                    editingInventoryItemId = null
                    editingReviewItemId = null
                    addFormDraft = AddItemFormDraft()
                    screen = WiseScreen.AddMissingItem
                },
                onEditItem = { item ->
                    addItemOrigin = AddItemOrigin.Dashboard
                    editingInventoryItemId = item.id
                    addFormDraft = AddItemFormDraft(
                        name = item.name,
                        quantity = item.quantityLabel,
                        category = item.category,
                        expiryDate = "${item.expiresInDays}d"
                    )
                    screen = WiseScreen.AddMissingItem
                },
                onTabSelected = toRootTab
            )
            WiseScreen.ExpiringSoonAll -> ExpiringSoonAllScreen(
                inventory = inventory,
                searchQuery = expiringSearch,
                selectedCategory = expiringFilter,
                onSearchQueryChange = { expiringSearch = it },
                onCategoryChange = { expiringFilter = it },
                onRecipe = { screen = WiseScreen.AiRecipes },
                onMarkUsed = { inventoryId -> 
                    inventoryId.toLongOrNull()?.let { inventoryVm.deleteItemById(it) }
                },
                onBack = { screen = WiseScreen.MainDashboard },
                onTabSelected = toRootTab
            )
            WiseScreen.SmartScan -> SmartScanScreen(
                onDone = { mode, captures ->
                    if (captures.isEmpty()) {
                        toastMessage = "No image captured."
                        return@SmartScanScreen
                    }

                    if (mode == ScanMode.Receipt) {
                        android.util.Log.d("RECEIPT_FLOW", "Mode = $mode, captures = ${captures.size}")
                        scope.launch {
                            try {
                                val bitmap = ScanCaptureBitmapResolver.resolve(context, captures.first())
                                val parsed = ReceiptOcrProvider.get(context).parseReceipt(bitmap)
                                android.util.Log.d("RECEIPT_FLOW", "Parsed item count = ${parsed.items.size}")
                                android.util.Log.d("RECEIPT_FLOW", "Parsed items = ${parsed.items}")

                                val mapped = ReceiptReviewMapper.map(parsed)
                                android.util.Log.d("RECEIPT_FLOW", "Mapped review count = ${mapped.size}")
                                android.util.Log.d("RECEIPT_FLOW", "Mapped review items = ${mapped.map { it.name }}")

                                reviewItems.clear()
                                reviewItems.addAll(mapped)

                                toastMessage = "Parsed ${mapped.size} item(s) from receipt."
                                screen = WiseScreen.ItemReview
                            } catch (e: Exception) {
                                toastMessage = e.message ?: "Failed to parse receipt."
                            }
                        }
                    } else {
                        android.util.Log.d("FOOD_FLOW", "Mode = $mode, captures = ${captures.size}")
                        scope.launch {
                            try {
                                val bitmap = ScanCaptureBitmapResolver.resolve(context, captures.first())
                                val parsed = FoodExtractorProvider.get(context).detectFood(bitmap)
                                android.util.Log.d("FOOD_FLOW", "Parsed item count = ${parsed.items.size}")
                                android.util.Log.d("FOOD_FLOW", "Parsed items = ${parsed.items}")

                                val mapped = FoodReviewMapper.map(parsed)
                                android.util.Log.d("FOOD_FLOW", "Mapped review count = ${mapped.size}")
                                android.util.Log.d("FOOD_FLOW", "Mapped review items = ${mapped.map { it.name }}")

                                reviewItems.clear()
                                reviewItems.addAll(mapped)

                                toastMessage = "Detected ${mapped.size} food item(s)."
                                screen = WiseScreen.ItemReview
                            } catch (e: Exception) {
                                toastMessage = e.message ?: "Failed to scan food image."
                            }
                        }
                    }
                },
                onTabSelected = toRootTab
            )
            WiseScreen.AddMissingItem -> AddMissingItemScreen(
                draft = addFormDraft,
                isEditMode = editingReviewItemId != null || editingInventoryItemId != null,
                onDraftChange = { addFormDraft = it },
                onSubmit = {
                    coroutineScope.launch {
                        val existingItem = if (addItemOrigin == AddItemOrigin.ItemReview) {
                            reviewItems.firstOrNull { it.id == editingReviewItemId }
                        } else null

                        if (requiresAiCheck(addFormDraft, existingItem)) {
                            isSaving = true
                        }

                        try {
                            when (addItemOrigin) {
                                AddItemOrigin.ItemReview -> {
                                    val existing = reviewItems.firstOrNull { it.id == editingReviewItemId }
                                    val next = draftToReviewItem(
                                        draft = addFormDraft,
                                        existing = existing,
                                        forcedId = editingReviewItemId
                                    )
                                    val existingIndex = reviewItems.indexOfFirst { it.id == next.id }
                                    if (existingIndex >= 0) {
                                        reviewItems[existingIndex] = next
                                        toastMessage = "Scanned item updated."
                                    } else {
                                        reviewItems.add(next)
                                        toastMessage = "Missing item added."
                                    }
                                    addFormDraft = AddItemFormDraft()
                                    editingReviewItemId = null
                                    screen = WiseScreen.ItemReview
                                }
                                AddItemOrigin.Dashboard -> {
                                    if (editingInventoryItemId != null) {
                                        val numId = editingInventoryItemId!!.toLongOrNull()
                                        if (numId != null) {
                                            val updated = draftToInventoryItem(addFormDraft).copy(itemId = numId)
                                            inventoryVm.confirmAndEditItem(updated, updated.quantity, updated.expiryDate, updated.notes)
                                            toastMessage = "Food details updated."
                                        }
                                    } else {
                                        inventoryVm.insertItem(draftToInventoryItem(addFormDraft))
                                        toastMessage = "Item added to inventory."
                                    }
                                    addFormDraft = AddItemFormDraft()
                                    editingInventoryItemId = null
                                    screen = WiseScreen.MainDashboard
                                }
                            }
                        } finally {
                            isSaving = false
                        }
                    }
                },
                onBack = {
                    screen = if (addItemOrigin == AddItemOrigin.ItemReview) {
                        WiseScreen.ItemReview
                    } else {
                        WiseScreen.MainDashboard
                    }
                },
                onTabSelected = toRootTab
            )
            WiseScreen.ItemReview -> ItemReviewScreen(
                reviewItems = reviewItems,
                onAddMissingItem = {
                    addItemOrigin = AddItemOrigin.ItemReview
                    editingReviewItemId = null
                    addFormDraft = AddItemFormDraft()
                    screen = WiseScreen.AddMissingItem
                },
                onEditItem = { reviewId ->
                    val current = reviewItems.firstOrNull { it.id == reviewId }
                    if (current != null) {
                        addItemOrigin = AddItemOrigin.ItemReview
                        editingReviewItemId = reviewId
                        addFormDraft = current.toDraft()
                        screen = WiseScreen.AddMissingItem
                    }
                },
                onDeleteItem = { reviewId ->
                    val idx = reviewItems.indexOfFirst { it.id == reviewId }
                    if (idx >= 0) {
                        reviewItems.removeAt(idx)
                        toastMessage = "Scanned item removed."
                    }
                },
                onSaveToInventory = {
                    if (reviewItems.isNotEmpty()) {
                        inventoryVm.insertBulk(reviewItems.map { it.toInventoryItem() })
                        val savedCount = reviewItems.size
                        reviewItems.clear()
                        toastMessage = "$savedCount item(s) saved to inventory."
                        screen = WiseScreen.MainDashboard
                    }
                },
                onTabSelected = toRootTab
            )
            WiseScreen.RecipeViewAll -> RecipeViewAllScreen(
                inventory = inventory,
                recipes = recipeRecommendations,
                preferences = recipePreferences,
                loading = recipeLoading,
                onPreferencesChange = { recipePreferences = it },
                onRegenerate = { recipeRefreshTick++ },
                onOpenRecipe = { recipeId ->
                    selectedRecipeId = recipeId
                    recipeBackTarget = WiseScreen.RecipeViewAll
                    screen = WiseScreen.RecipeDetails
                },
                onBack = { screen = WiseScreen.AiRecipes },
                onTabSelected = toRootTab
            )
            WiseScreen.ShoppingList -> ShoppingListScreen(
                items = shoppingListItems,
                onItemCheckedChange = { itemId, checked ->
                    val idx = shoppingListItems.indexOfFirst { it.id == itemId }
                    if (idx >= 0) {
                        shoppingListItems[idx] = shoppingListItems[idx].copy(checked = checked)
                    }
                },
                onAddGeneralItem = { itemName ->
                    shoppingListItems.add(
                        ShoppingListItemUi(
                            id = "shop-${UUID.randomUUID().toString().take(8)}",
                            name = itemName
                        )
                    )
                    toastMessage = "Added to shopping list."
                },
                onClearPurchased = {
                    val before = shoppingListItems.size
                    shoppingListItems.removeAll { it.checked }
                    val removed = before - shoppingListItems.size
                    toastMessage = if (removed > 0) {
                        "Cleared $removed purchased item(s)."
                    } else {
                        "No purchased items to clear."
                    }
                    removed
                },
                onTabSelected = toRootTab
            )
            WiseScreen.RecipeDetails -> {
                val recipe = recipeUiState.recipes.firstOrNull { it.id == selectedRecipeId }
                    ?: recipeUiState.recipes.firstOrNull()

                if (recipe == null) {
                    LaunchedEffect(Unit) {
                        screen = recipeBackTarget
                    }
                    return@Box
                }
                RecipeDetailsScreen(
                    recipe = recipe,
                    onAddMissingToShoppingList = { current ->
                        val added = addMissingItemsToShoppingList(
                            shoppingListItems = shoppingListItems,
                            recipe = current
                        )
                        toastMessage = if (added > 0) {
                            "$added missing item(s) added to shopping list."
                        } else {
                            "All missing ingredients are already in your shopping list."
                        }
                        added
                    },
                    onOpenList = { screen = WiseScreen.ShoppingList },
                    onBack = { screen = recipeBackTarget },
                    onTabSelected = toRootTab
                )
            }
            WiseScreen.AiRecipes -> AiRecipesScreen(
                inventory = inventory,
                recipes = recipeUiState.recipes.take(4),
                loading = recipeUiState.isLoading,
                onGenerate = { refreshRecipes() },
                onViewAll = { screen = WiseScreen.RecipeViewAll },
                onOpenRecipe = { recipeId ->
                    selectedRecipeId = recipeId
                    recipeBackTarget = WiseScreen.AiRecipes
                    screen = WiseScreen.RecipeDetails
                },
                onTabSelected = toRootTab
            )
        }

        if (isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF14532D))
                        Text(
                            text = "Gemini AI is analyzing...",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    val handleDeviceBack: (() -> Unit)? = when (screen) {
        WiseScreen.ExpiringSoonAll -> ({ screen = WiseScreen.MainDashboard })
        WiseScreen.ItemReview -> ({ screen = WiseScreen.SmartScan })
        WiseScreen.AddMissingItem -> ({
            screen = if (addItemOrigin == AddItemOrigin.ItemReview) {
                WiseScreen.ItemReview
            } else {
                WiseScreen.MainDashboard
            }
        })
        WiseScreen.RecipeViewAll -> ({ screen = WiseScreen.AiRecipes })
        WiseScreen.RecipeDetails -> ({ screen = recipeBackTarget })
        WiseScreen.SmartScan, WiseScreen.AiRecipes, WiseScreen.ShoppingList -> ({ screen = WiseScreen.MainDashboard })
        WiseScreen.MainDashboard, WiseScreen.AppLauncher -> null
    }

    BackHandler(enabled = handleDeviceBack != null) {
        handleDeviceBack?.invoke()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PreviewFreshTrackDashboard() {
    FreshTrackAITheme {
        FreshTrackDashboardScreen()
    }
}