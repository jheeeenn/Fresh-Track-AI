package my.edu.utar.freshtrackai.ui.dashboard

import androidx.compose.ui.graphics.Color
import java.util.UUID
import my.edu.utar.freshtrackai.logic.ExpiryCalculator
import my.edu.utar.freshtrackai.logic.ShelfLifeRules
import kotlin.math.ceil

internal fun InventoryItem.toExpiringOrNull(): ExpiringItem? {
    val badge = urgencyForDays(expiresInDays) ?: return null
    return ExpiringItem(
        id = "exp-$id",
        inventoryItemId = id,
        name = name,
        expiresInDays = expiresInDays,
        badge = badge,
        category = category
    )
}

internal fun urgencyForDays(days: Int): ExpiryBadge? {
    val status = ExpiryCalculator.getExpiryStatus(days)

    return when (status) {
        ExpiryCalculator.ExpiryStatus.EXPIRED -> ExpiryBadge.Warning   // distinct badge for expired
        ExpiryCalculator.ExpiryStatus.CRITICAL -> ExpiryBadge.Critical
        ExpiryCalculator.ExpiryStatus.WATCH -> ExpiryBadge.Watch
        else -> null
    }
}

internal fun daysLabel(days: Int): String = if (days <= 0) "Expires today" else "Expires in ${days}d"

internal fun categoryEmoji(category: InventoryCategory): String = when (category) {
    InventoryCategory.Dairy -> "🥛"
    InventoryCategory.Eggs -> "🥚"
    InventoryCategory.MeatPoultry -> "🥩"
    InventoryCategory.Seafood -> "🐟"
    InventoryCategory.Fruits -> "🍎"
    InventoryCategory.Vegetables -> "🥕"
    InventoryCategory.Bakery -> "🥐"
    InventoryCategory.GrainsPasta -> "🌾"
    InventoryCategory.CannedGoods -> "🥫"
    InventoryCategory.Frozen -> "🧊"
    InventoryCategory.Beverages -> "🧃"
    InventoryCategory.Condiments -> "🧂"
    InventoryCategory.Snacks -> "🥨"
    InventoryCategory.Leftovers -> "🥡"
    InventoryCategory.Other -> "📦"
}

// ─────────────────────────────────────────────────────────────
// SAFE MAPPER: UI Dropdown -> Logic Enums
// ─────────────────────────────────────────────────────────────
internal fun InventoryCategory.toLogicCategory(): ShelfLifeRules.FoodCategory {
    return when(this) {
        InventoryCategory.Dairy -> ShelfLifeRules.FoodCategory.DAIRY
        InventoryCategory.Eggs -> ShelfLifeRules.FoodCategory.EGGS
        InventoryCategory.MeatPoultry -> ShelfLifeRules.FoodCategory.MEAT_POULTRY
        InventoryCategory.Seafood -> ShelfLifeRules.FoodCategory.SEAFOOD
        InventoryCategory.Fruits -> ShelfLifeRules.FoodCategory.FRUITS
        InventoryCategory.Vegetables -> ShelfLifeRules.FoodCategory.VEGETABLES
        InventoryCategory.Bakery -> ShelfLifeRules.FoodCategory.BAKERY
        InventoryCategory.GrainsPasta -> ShelfLifeRules.FoodCategory.GRAINS_PASTA
        InventoryCategory.CannedGoods -> ShelfLifeRules.FoodCategory.CANNED_GOODS
        InventoryCategory.Frozen -> ShelfLifeRules.FoodCategory.FROZEN
        InventoryCategory.Beverages -> ShelfLifeRules.FoodCategory.BEVERAGES
        InventoryCategory.Condiments -> ShelfLifeRules.FoodCategory.CONDIMENTS
        InventoryCategory.Snacks -> ShelfLifeRules.FoodCategory.SNACKS
        InventoryCategory.Leftovers -> ShelfLifeRules.FoodCategory.LEFTOVERS
        InventoryCategory.Other -> ShelfLifeRules.FoodCategory.OTHER
    }
}

internal fun removeInventoryItem(list: MutableList<InventoryItem>, inventoryId: String) {
    val idx = list.indexOfFirst { it.id == inventoryId }
    if (idx >= 0) list.removeAt(idx)
}

internal fun seedInventoryItems(): List<InventoryItem> = listOf(
    InventoryItem("it-001", "Whole Milk", InventoryCategory.Dairy, "0.5 gal", 2, 1, "milk"),
    InventoryItem("it-002", "Baby Spinach", InventoryCategory.Vegetables, "1 bunch", 1, 3, "spinach"),
    InventoryItem("it-003", "Ribeye Steak", InventoryCategory.MeatPoultry, "0.8 kg", 2, 4, "steak"),
    InventoryItem("it-004", "Orange Juice", InventoryCategory.Beverages, "1.0 L", 3, 6, "juice"),
    InventoryItem("it-005", "Brown Rice", InventoryCategory.GrainsPasta, "2.0 kg", 7, 24, "rice"),
    InventoryItem("it-006", "Frozen Peas", InventoryCategory.Frozen, "1 pack", 5, 30, "peas"),
    InventoryItem("it-007", "Sourdough Bread", InventoryCategory.Bakery, "1 loaf", 1, 2, "bread"),
    InventoryItem("it-008", "Hot Sauce", InventoryCategory.Condiments, "250 ml", 12, 120, "sauce"),
    InventoryItem("it-009", "Greek Yogurt", InventoryCategory.Dairy, "500 g", 1, 8, "yogurt"),
    InventoryItem("it-010", "Chicken Breast", InventoryCategory.MeatPoultry, "1.2 kg", 1, 2, "chicken"),
    InventoryItem("it-011", "Carrots", InventoryCategory.Vegetables, "1.2 kg", 2, 12, "carrot"),
    InventoryItem("it-012", "Avocados", InventoryCategory.Fruits, "3 pcs", 3, 5, "avocado")
)

internal fun seedReviewItems(): List<ReviewItemUi> = listOf(
    ReviewItemUi(
        id = "rev-001",
        name = "Organic Baby Spinach",
        category = InventoryCategory.Vegetables,
        quantityLabel = "1 box",
        expiresLabel = "Oct 24, 2026",
        expiresInDays = 6,
        nutritionLabel = "23 kcal / 100g",
        thumbnailRef = "spinach"
    ),
    ReviewItemUi(
        id = "rev-002",
        name = "Whole Milk",
        category = InventoryCategory.Dairy,
        quantityLabel = "2L",
        expiresLabel = "Oct 18, 2026",
        expiresInDays = 1,
        nutritionLabel = "61 kcal / 100ml",
        thumbnailRef = "milk"
    ),
    ReviewItemUi(
        id = "rev-003",
        name = "Red Bell Pepper",
        category = InventoryCategory.Vegetables,
        quantityLabel = "3 units",
        expiresLabel = "Oct 28, 2026",
        expiresInDays = 10,
        nutritionLabel = "31 kcal / 100g",
        thumbnailRef = "pepper"
    ),
    ReviewItemUi(
        id = "rev-004",
        name = "Chicken Breast",
        category = InventoryCategory.MeatPoultry,
        quantityLabel = "500g",
        expiresLabel = "Oct 21, 2026",
        expiresInDays = 4,
        nutritionLabel = "165 kcal / 100g",
        thumbnailRef = "chicken"
    )
)

internal fun ReviewItemUi.toInventoryItem(): my.edu.utar.freshtrackai.data.local.entity.InventoryItem {
    val expiryMillis = System.currentTimeMillis() + (this.expiresInDays * 24L * 60 * 60 * 1000L)
    val qtyData = parseQuantity(this.quantityLabel)
    return my.edu.utar.freshtrackai.data.local.entity.InventoryItem(
        name = this.name,
        category = this.category.name,
        quantity = qtyData.first,
        unit = qtyData.second,
        expiryDate = expiryMillis
    )
}

internal fun my.edu.utar.freshtrackai.data.local.entity.InventoryItem.toUiModel(): InventoryItem {
    val currentMillis = System.currentTimeMillis()
    val diff = this.expiryDate - currentMillis
    val days = ceil(diff.toDouble() / (1000.0 * 60 * 60 * 24)).toInt()
    
    val addedDiff = currentMillis - this.purchaseDate
    val addedDays = (addedDiff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    val formattedExpiryDate = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(this.expiryDate))

    val cat = InventoryCategory.values().firstOrNull { it.name.equals(this.category, ignoreCase = true) } ?: InventoryCategory.Other
    
    return InventoryItem(
        id = this.itemId.toString(),
        name = this.name,
        category = cat,
        quantityLabel = formatStoredQuantityLabel(this.quantity, this.unit),
        addedDaysAgo = addedDays,
        expiresInDays = days,
        thumbnailRef = this.name.lowercase().replace(" ", "_"),
        nutritionNotes = this.notes,
        formattedExpiryDate = formattedExpiryDate,
        formattedAddedDate = formatAddedDateLabel(this.purchaseDate),
        purchaseDateMillis = this.purchaseDate
    )
}

internal fun parseQuantity(value: String): Pair<Double, String> {
    val digitsStr = value.takeWhile { it.isDigit() || it == '.' }
    val digits = digitsStr.toDoubleOrNull() ?: 1.0
    val letters = value.dropWhile { it.isDigit() || it == '.' || it.isWhitespace() }.trim()
    val finalUnit = if (letters.isEmpty()) "unit" else letters
    return digits to finalUnit
}


internal fun ReviewItemUi.toDraft(): AddItemFormDraft = AddItemFormDraft(
    name = name,
    quantity = quantityLabel,
    category = category,
    expiryDate = expiresLabel,
    nutritionNotes = nutritionLabel
)

// ─────────────────────────────────────────────────────────────
// NEW FIX: Tells the UI whether we actually need to call the AI
// ─────────────────────────────────────────────────────────────
internal fun requiresAiCheck(draft: AddItemFormDraft, existing: ReviewItemUi? = null): Boolean {
    val resolvedLabel = draft.expiryDate.trim().ifBlank { existing?.expiresLabel ?: "Not set" }

    // 1. If user typed a date (or didn't change the existing date), we don't need AI
    if (existing != null && resolvedLabel == existing.expiresLabel) return false
    if (resolvedLabel != "Not set" && resolvedLabel.isNotBlank()) return false

    // 2. If it perfectly matches our offline dictionary, we don't need AI
    val resolvedName = draft.name.trim().ifBlank { "Unnamed Item" }
    val localCategory = ShelfLifeRules.detectCategory(resolvedName)
    val selectedCategory = draft.category.toLogicCategory()

    if (localCategory == selectedCategory && localCategory != ShelfLifeRules.FoodCategory.OTHER) {
        return false // Instant offline match!
    }

    // 3. Otherwise, we MUST wake up the AI!
    return true
}

// ─────────────────────────────────────────────────────────────
// UPDATED: Now a suspend function that asks AI for exact days!
// ─────────────────────────────────────────────────────────────
internal suspend fun draftToReviewItem(
    draft: AddItemFormDraft,
    existing: ReviewItemUi?,
    forcedId: String?
): ReviewItemUi {
    val resolvedName = draft.name.trim().ifBlank { "Unnamed Item" }
    val resolvedQuantity = draft.quantity.trim().ifBlank { "1 unit" }
    val resolvedLabel = draft.expiryDate.trim().ifBlank { existing?.expiresLabel ?: "Not set" }
    val finalCategory = draft.category // We trust the user's category dropdown

    val resolvedDays = when {
        existing != null && resolvedLabel == existing.expiresLabel -> existing.expiresInDays
        resolvedLabel == "Not set" || resolvedLabel.isBlank() ->
            // 1. Asks AI for exact days based on the name.
            // 2. Passes the selected Dropdown Category as context and fallback.
            ShelfLifeRules.getShelfLifeByNameAI(
                resolvedName, finalCategory.toLogicCategory()
            )
        else -> ExpiryCalculator.estimateExpiresInDays(resolvedLabel).toInt()
    }

    return ReviewItemUi(
        id = forcedId ?: existing?.id ?: "rev-${UUID.randomUUID().toString().take(8)}",
        name = resolvedName,
        category = finalCategory,
        quantityLabel = resolvedQuantity,
        expiresLabel = resolvedLabel,
        expiresInDays = resolvedDays,
        nutritionLabel = draft.nutritionNotes.trim().ifBlank { "Not provided" },
        thumbnailRef = existing?.thumbnailRef ?: resolvedName.lowercase().replace(" ", "_")
    )
}

// ─────────────────────────────────────────────────────────────
// UPDATED: Now a suspend function that asks AI for exact days!
// ─────────────────────────────────────────────────────────────
internal suspend fun draftToInventoryItem(draft: AddItemFormDraft): my.edu.utar.freshtrackai.data.local.entity.InventoryItem {
    val resolvedName = draft.name.trim().ifBlank { "Unnamed Item" }
    val finalCategory = draft.category

    val resolvedDays = if (draft.expiryDate.isBlank() || draft.expiryDate == "Not set") {
        // 1. Asks AI for exact days based on the name.
        // 2. Passes the selected Dropdown Category as context and fallback.
        ShelfLifeRules.getShelfLifeByNameAI(
            resolvedName, finalCategory.toLogicCategory()
        )
    } else {
        ExpiryCalculator.estimateExpiresInDays(draft.expiryDate).toInt()
    }

    val expiryMillis = System.currentTimeMillis() + (resolvedDays * 24L * 60 * 60 * 1000L)
    val qtyData = parseQuantity(draft.quantity.trim().ifBlank { "1 unit" })

    return my.edu.utar.freshtrackai.data.local.entity.InventoryItem(
        name = resolvedName,
        category = finalCategory.name,
        quantity = qtyData.first,
        unit = qtyData.second,
        expiryDate = expiryMillis,
        notes = draft.nutritionNotes
    )
}

internal fun estimateExpiresInDays(input: String, fallback: Int): Int {
    return ExpiryCalculator.estimateExpiresInDays(input).toInt()
}

internal fun reviewThumbBackground(ref: String): Color {
    val colors = listOf(
        Color(0xFF14532D),
        Color(0xFF1E3A8A),
        Color(0xFF7C2D12),
        Color(0xFF312E81),
        Color(0xFF0F766E),
        Color(0xFF3F3F46)
    )
    val idx = (ref.hashCode() and Int.MAX_VALUE) % colors.size
    return colors[idx]
}

internal fun addMissingItemsToShoppingList(
    shoppingListItems: MutableList<ShoppingListItemUi>,
    recipe: RecipeUi
): Int {
    val existingNames = shoppingListItems.map { normalizeFoodName(it.name) }.toMutableSet()
    var added = 0
    recipe.ingredientsMissing.forEach { ingredient ->
        val normalized = normalizeFoodName(ingredient.name)
        if (normalized.isNotEmpty() && normalized !in existingNames) {
            shoppingListItems.add(
                ShoppingListItemUi(
                    id = "shop-${UUID.randomUUID().toString().take(8)}",
                    name = ingredient.name,
                    sourceRecipeId = recipe.id,
                    sourceRecipeName = recipe.title
                )
            )
            existingNames.add(normalized)
            added++
        }
    }
    return added
}

internal fun normalizeFoodName(name: String): String = name.lowercase().trim()

internal fun seedRecipes(): List<RecipeUi> = listOf(
    RecipeUi(
        id = "rcp-001",
        title = "Quick Chicken Stir-fry",
        description = "A high-protein, veggie-packed meal using your current chicken breast stock.",
        prepMinutes = 15,
        imageUrl = "https://images.unsplash.com/photo-1512058564366-18510be2db19?auto=format&fit=crop&w=1200&q=80",
        pantryMatchText = "Uses 3 pantry items",
        tag = "High protein option",
        usedInventoryItemIds = setOf("it-010", "it-002", "it-011"),
        ingredientsAvailable = listOf(
            RecipeIngredientUi("Chicken Breast", true, "500g"),
            RecipeIngredientUi("Baby Spinach", true, "1 handful"),
            RecipeIngredientUi("Carrots", true, "1 cup sliced")
        ),
        ingredientsMissing = listOf(
            RecipeIngredientUi("Soy Sauce", false, "2 tbsp"),
            RecipeIngredientUi("Red Bell Pepper", false, "1 unit")
        ),
        steps = listOf(
            "Slice chicken and vegetables into bite-sized pieces.",
            "Sear chicken for 4-5 minutes over medium-high heat.",
            "Add vegetables and cook until just tender-crisp.",
            "Stir in soy sauce and serve immediately."
        ),
        avoidanceTokens = setOf("onion", "spicy")
    ),
    RecipeUi(
        id = "rcp-002",
        title = "One-Pot Creamy Pasta",
        description = "A quick pasta dinner that uses dairy already in your inventory.",
        prepMinutes = 25,
        imageUrl = "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=1200&q=80",
        pantryMatchText = "Uses 2 expiring items",
        tag = "Comfort meal",
        usedInventoryItemIds = setOf("it-001", "it-009"),
        ingredientsAvailable = listOf(
            RecipeIngredientUi("Whole Milk", true, "1 cup"),
            RecipeIngredientUi("Greek Yogurt", true, "2 tbsp"),
            RecipeIngredientUi("Brown Rice Pasta", true, "250g")
        ),
        ingredientsMissing = listOf(
            RecipeIngredientUi("Parmesan Cheese", false, "50g")
        ),
        steps = listOf(
            "Boil pasta in salted water until al dente.",
            "Simmer milk and yogurt in a pan until slightly thickened.",
            "Toss cooked pasta in the sauce and fold to coat.",
            "Top with grated cheese and finish with black pepper."
        ),
        avoidanceTokens = setOf("dairy")
    ),
    RecipeUi(
        id = "rcp-003",
        title = "Crispy Chickpea Kale Bowl",
        description = "Fresh, filling, and low calorie with a citrus finish.",
        prepMinutes = 10,
        imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80",
        pantryMatchText = "Uses 2 pantry items",
        tag = "Low calorie option",
        usedInventoryItemIds = setOf("it-011", "it-012"),
        ingredientsAvailable = listOf(
            RecipeIngredientUi("Carrots", true, "1 cup shredded"),
            RecipeIngredientUi("Avocado", true, "1/2 fruit")
        ),
        ingredientsMissing = listOf(
            RecipeIngredientUi("Chickpeas", false, "1 can"),
            RecipeIngredientUi("Kale", false, "2 cups")
        ),
        steps = listOf(
            "Rinse and dry chickpeas, then roast until crisp.",
            "Massage chopped kale with lemon and a pinch of salt.",
            "Assemble bowl with kale, chickpeas, carrot, and avocado.",
            "Finish with lemon juice and olive oil."
        ),
        avoidanceTokens = emptySet()
    ),
    RecipeUi(
        id = "rcp-004",
        title = "Obsidian Breakfast Toast",
        description = "Quick breakfast using ripe avocado and high-protein toppings.",
        prepMinutes = 5,
        imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=1200&q=80",
        pantryMatchText = "Uses 2 pantry items",
        tag = "High protein breakfast",
        usedInventoryItemIds = setOf("it-007", "it-012"),
        ingredientsAvailable = listOf(
            RecipeIngredientUi("Sourdough Bread", true, "1 slice"),
            RecipeIngredientUi("Avocado", true, "1/2 fruit")
        ),
        ingredientsMissing = listOf(
            RecipeIngredientUi("Eggs", false, "2 units")
        ),
        steps = listOf(
            "Toast sourdough until golden and crisp.",
            "Mash avocado with salt and lemon.",
            "Top toast with avocado and a poached or fried egg.",
            "Finish with chili flakes if desired."
        ),
        avoidanceTokens = setOf("spicy")
    ),
    RecipeUi(
        id = "rcp-005",
        title = "Sun-Dried Tomato & Basil Penne",
        description = "A rich pasta recipe that balances bright herbs and umami.",
        prepMinutes = 25,
        imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=1200&q=80",
        pantryMatchText = "Uses 2 pantry items",
        tag = "Restaurant style",
        usedInventoryItemIds = setOf("it-005"),
        ingredientsAvailable = listOf(
            RecipeIngredientUi("Penne Pasta", true, "250g"),
            RecipeIngredientUi("Olive Oil", true, "2 tbsp"),
            RecipeIngredientUi("Garlic Cloves", true, "2 cloves")
        ),
        ingredientsMissing = listOf(
            RecipeIngredientUi("Sun-dried Tomatoes", false, "60g"),
            RecipeIngredientUi("Fresh Basil", false, "1 handful"),
            RecipeIngredientUi("Parmesan Cheese", false, "50g")
        ),
        steps = listOf(
            "Cook penne in salted water until al dente.",
            "Saute garlic in olive oil over medium heat.",
            "Add chopped sun-dried tomatoes and toss with cooked pasta.",
            "Stir in basil and finish with grated parmesan."
        ),
        avoidanceTokens = setOf("dairy")
    ),
    RecipeUi(
        id = "rcp-006",
        title = "Green Power Smoothie",
        description = "A clean post-workout smoothie focused on produce and hydration.",
        prepMinutes = 8,
        imageUrl = "https://images.unsplash.com/photo-1610970881699-44a5587cabec?auto=format&fit=crop&w=1200&q=80",
        pantryMatchText = "Uses 2 inventory items",
        tag = "No-cook option",
        usedInventoryItemIds = setOf("it-002", "it-004"),
        ingredientsAvailable = listOf(
            RecipeIngredientUi("Baby Spinach", true, "1 cup"),
            RecipeIngredientUi("Orange Juice", true, "300ml")
        ),
        ingredientsMissing = listOf(
            RecipeIngredientUi("Banana", false, "1 unit")
        ),
        steps = listOf(
            "Add spinach, orange juice, and banana to a blender.",
            "Blend for 30-45 seconds until smooth.",
            "Taste and add water or ice to adjust texture.",
            "Serve immediately."
        ),
        avoidanceTokens = emptySet()
    )
)

internal fun seedShoppingListItems(): List<ShoppingListItemUi> = listOf(
    ShoppingListItemUi(
        id = "shop-r-001",
        name = "Greek Yogurt (500g)",
        sourceRecipeId = "rcp-seed-1",
        sourceRecipeName = "Morning Protein Bowl"
    ),
    ShoppingListItemUi(
        id = "shop-r-002",
        name = "Fresh Basil",
        sourceRecipeId = "rcp-seed-2",
        sourceRecipeName = "Pesto Pasta"
    ),
    ShoppingListItemUi(
        id = "shop-r-003",
        name = "Red Onions",
        sourceRecipeId = "rcp-seed-3",
        sourceRecipeName = "Multiple Recipes"
    ),
    ShoppingListItemUi(id = "shop-g-001", name = "Oat Milk"),
    ShoppingListItemUi(id = "shop-g-002", name = "Sparkling Water", checked = true),
    ShoppingListItemUi(id = "shop-g-003", name = "Paper Towels"),
    ShoppingListItemUi(id = "shop-g-004", name = "Avocados (x2)", checked = true)
)
