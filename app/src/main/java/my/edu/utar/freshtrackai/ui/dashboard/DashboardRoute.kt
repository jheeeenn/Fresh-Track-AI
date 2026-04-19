package my.edu.utar.freshtrackai.ui.dashboard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import java.util.UUID
import kotlinx.coroutines.delay
import my.edu.utar.freshtrackai.ui.theme.FreshTrackAITheme

@Composable
fun FreshTrackDashboardScreen(modifier: Modifier = Modifier) {
    var screen by rememberSaveable { mutableStateOf(WiseScreen.AppLauncher) }
    val inventory = remember { mutableStateListOf<InventoryItem>().apply { addAll(seedInventoryItems()) } }
    val reviewItems = remember { mutableStateListOf<ReviewItemUi>().apply { addAll(seedReviewItems()) } }
    val recipesAll = remember { seedRecipes() }
    val recipeRecommendations = remember { mutableStateListOf<RecipeUi>() }
    val shoppingListItems = remember { mutableStateListOf<ShoppingListItemUi>().apply { addAll(seedShoppingListItems()) } }
    var expiringSearch by rememberSaveable { mutableStateOf("") }
    var expiringFilter by rememberSaveable { mutableStateOf<InventoryCategory?>(null) }
    var editingReviewItemId by remember { mutableStateOf<String?>(null) }
    var addItemOrigin by remember { mutableStateOf(AddItemOrigin.ItemReview) }
    var addFormDraft by remember { mutableStateOf(AddItemFormDraft()) }
    var selectedRecipeId by rememberSaveable { mutableStateOf<String?>(null) }
    var recipeBackTarget by rememberSaveable { mutableStateOf(WiseScreen.AiRecipes) }
    var recipePreferences by remember { mutableStateOf(RecipePreferencesUi()) }
    var recipeLoading by remember { mutableStateOf(false) }
    var recipeRefreshTick by rememberSaveable { mutableStateOf(0) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

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
                    editingReviewItemId = null
                    addFormDraft = AddItemFormDraft()
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
                onMarkUsed = { inventoryId -> removeInventoryItem(inventory, inventoryId) },
                onBack = { screen = WiseScreen.MainDashboard },
                onTabSelected = toRootTab
            )
            WiseScreen.SmartScan -> SmartScanScreen(
                onDone = {
                    if (reviewItems.isEmpty()) {
                        reviewItems.addAll(seedReviewItems())
                    }
                    screen = WiseScreen.ItemReview
                },
                onTabSelected = toRootTab
            )
            WiseScreen.AddMissingItem -> AddMissingItemScreen(
                draft = addFormDraft,
                isEditMode = editingReviewItemId != null,
                onDraftChange = { addFormDraft = it },
                onSubmit = {
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
                            inventory.add(draftToInventoryItem(addFormDraft))
                            toastMessage = "Item added to inventory."
                            addFormDraft = AddItemFormDraft()
                            editingReviewItemId = null
                            screen = WiseScreen.MainDashboard
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
                        inventory.addAll(reviewItems.map { it.toInventoryItem() })
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
                val recipe = recipesAll.firstOrNull { it.id == selectedRecipeId } ?: recipeRecommendations.firstOrNull() ?: recipesAll.first()
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
                recipes = recipeRecommendations.take(4),
                loading = recipeLoading,
                onGenerate = { recipeRefreshTick++ },
                onViewAll = { screen = WiseScreen.RecipeViewAll },
                onOpenRecipe = { recipeId ->
                    selectedRecipeId = recipeId
                    recipeBackTarget = WiseScreen.AiRecipes
                    screen = WiseScreen.RecipeDetails
                },
                onTabSelected = toRootTab
            )
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

