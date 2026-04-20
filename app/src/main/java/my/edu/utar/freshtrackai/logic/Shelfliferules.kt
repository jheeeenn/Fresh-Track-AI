package my.edu.utar.freshtrackai.logic

/**
 * ShelfLifeRules.kt
 * Member 3 — Expiry Logic
 *
 * Owns the category-based shelf-life rule table.
 * All durations are in days, measured from the purchase/add date.
 * Used by ExpiryCalculator when no manufacturer expiry date is provided.
 */
object ShelfLifeRules {

    // ─────────────────────────────────────────────────────────────
    // Food Category Enum
    // ─────────────────────────────────────────────────────────────
    enum class FoodCategory {
        DAIRY,
        EGGS,
        MEAT_POULTRY,
        SEAFOOD,
        FRUITS,
        VEGETABLES,
        BAKERY,
        GRAINS_PASTA,
        CANNED_GOODS,
        FROZEN,
        BEVERAGES,
        CONDIMENTS,
        SNACKS,
        LEFTOVERS,
        OTHER
    }

    // ─────────────────────────────────────────────────────────────
    // Default shelf life per category (days from purchase date)
    // ─────────────────────────────────────────────────────────────
    private val categoryShelfLife: Map<FoodCategory, Int> = mapOf(
        FoodCategory.DAIRY to 7,
        FoodCategory.EGGS to 21,
        FoodCategory.MEAT_POULTRY to 3,
        FoodCategory.SEAFOOD to 2,
        FoodCategory.FRUITS to 5,
        FoodCategory.VEGETABLES to 7,
        FoodCategory.BAKERY to 5,
        FoodCategory.GRAINS_PASTA to 365,
        FoodCategory.CANNED_GOODS to 730,
        FoodCategory.FROZEN to 90,
        FoodCategory.BEVERAGES to 14,
        FoodCategory.CONDIMENTS to 180,
        FoodCategory.SNACKS to 30,
        FoodCategory.LEFTOVERS to 3,
        FoodCategory.OTHER to 14
    )

    // ─────────────────────────────────────────────────────────────
    // Keyword → Category mapping (used for auto-detect from name)
    // ─────────────────────────────────────────────────────────────
    private val keywordToCategoryMap: Map<String, FoodCategory> = mapOf(
        // Dairy
        "milk" to FoodCategory.DAIRY,
        "cheese" to FoodCategory.DAIRY,
        "yogurt" to FoodCategory.DAIRY,
        "butter" to FoodCategory.DAIRY,
        "cream" to FoodCategory.DAIRY,
        "tofu" to FoodCategory.DAIRY,

        // Eggs
        "egg" to FoodCategory.EGGS,
        "eggs" to FoodCategory.EGGS,

        // Meat & Poultry
        "chicken" to FoodCategory.MEAT_POULTRY,
        "beef" to FoodCategory.MEAT_POULTRY,
        "pork" to FoodCategory.MEAT_POULTRY,
        "lamb" to FoodCategory.MEAT_POULTRY,
        "turkey" to FoodCategory.MEAT_POULTRY,
        "sausage" to FoodCategory.MEAT_POULTRY,
        "bacon" to FoodCategory.MEAT_POULTRY,
        "ham" to FoodCategory.MEAT_POULTRY,

        // Seafood
        "fish" to FoodCategory.SEAFOOD,
        "shrimp" to FoodCategory.SEAFOOD,
        "prawn" to FoodCategory.SEAFOOD,
        "salmon" to FoodCategory.SEAFOOD,
        "tuna" to FoodCategory.SEAFOOD,
        "crab" to FoodCategory.SEAFOOD,
        "squid" to FoodCategory.SEAFOOD,

        // Fruits
        "apple" to FoodCategory.FRUITS,
        "banana" to FoodCategory.FRUITS,
        "orange" to FoodCategory.FRUITS,
        "grape" to FoodCategory.FRUITS,
        "mango" to FoodCategory.FRUITS,
        "strawberry" to FoodCategory.FRUITS,
        "watermelon" to FoodCategory.FRUITS,
        "papaya" to FoodCategory.FRUITS,
        "durian" to FoodCategory.FRUITS,
        "rambutan" to FoodCategory.FRUITS,

        // Vegetables
        "carrot" to FoodCategory.VEGETABLES,
        "spinach" to FoodCategory.VEGETABLES,
        "broccoli" to FoodCategory.VEGETABLES,
        "tomato" to FoodCategory.VEGETABLES,
        "lettuce" to FoodCategory.VEGETABLES,
        "onion" to FoodCategory.VEGETABLES,
        "garlic" to FoodCategory.VEGETABLES,
        "potato" to FoodCategory.VEGETABLES,
        "cabbage" to FoodCategory.VEGETABLES,
        "cucumber" to FoodCategory.VEGETABLES,

        // Bakery
        "bread" to FoodCategory.BAKERY,
        "bun" to FoodCategory.BAKERY,
        "cake" to FoodCategory.BAKERY,
        "pastry" to FoodCategory.BAKERY,
        "muffin" to FoodCategory.BAKERY,

        // Grains
        "rice" to FoodCategory.GRAINS_PASTA,
        "pasta" to FoodCategory.GRAINS_PASTA,
        "noodle" to FoodCategory.GRAINS_PASTA,
        "flour" to FoodCategory.GRAINS_PASTA,
        "oat" to FoodCategory.GRAINS_PASTA,
        "cereal" to FoodCategory.GRAINS_PASTA,

        // Canned
        "canned" to FoodCategory.CANNED_GOODS,
        "sardine" to FoodCategory.CANNED_GOODS,
        "pickle" to FoodCategory.CANNED_GOODS,

        // Frozen
        "frozen" to FoodCategory.FROZEN,
        "ice cream" to FoodCategory.FROZEN,

        // Beverages
        "juice" to FoodCategory.BEVERAGES,
        "drink" to FoodCategory.BEVERAGES,
        "soda" to FoodCategory.BEVERAGES,
        "water" to FoodCategory.BEVERAGES,
        "tea" to FoodCategory.BEVERAGES,
        "coffee" to FoodCategory.BEVERAGES,

        // Condiments
        "sauce" to FoodCategory.CONDIMENTS,
        "ketchup" to FoodCategory.CONDIMENTS,
        "mayonnaise" to FoodCategory.CONDIMENTS,
        "soy sauce" to FoodCategory.CONDIMENTS,
        "vinegar" to FoodCategory.CONDIMENTS,
        "jam" to FoodCategory.CONDIMENTS,
        "honey" to FoodCategory.CONDIMENTS,

        // Snacks
        "chips" to FoodCategory.SNACKS,
        "biscuit" to FoodCategory.SNACKS,
        "cookie" to FoodCategory.SNACKS,
        "cracker" to FoodCategory.SNACKS,
        "chocolate" to FoodCategory.SNACKS,
        "candy" to FoodCategory.SNACKS,

        // Leftovers
        "leftover" to FoodCategory.LEFTOVERS,
        "cooked" to FoodCategory.LEFTOVERS
    )

    // ─────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns shelf-life days for a given category.
     */
    fun getShelfLifeDays(category: FoodCategory): Int {
        return categoryShelfLife[category] ?: 14
    }

    /**
     * Auto-detects the FoodCategory from an item name string.
     * Matches any keyword found within the item name (case-insensitive).
     * Falls back to OTHER if nothing matches.
     */
    fun detectCategory(itemName: String): FoodCategory {
        val lower = itemName.lowercase()
        for ((keyword, category) in keywordToCategoryMap) {
            if (lower.contains(keyword)) return category
        }
        return FoodCategory.OTHER
    }

    /**
     * Convenience: given just a food name, return its estimated shelf-life in days.
     */
    suspend fun detectCategoryWithAI(itemName: String): FoodCategory {
        val localResult = detectCategory(itemName)
        if (localResult != FoodCategory.OTHER) {
            return localResult
        }
        // Assuming your AiCategorizer still has categorize() for fallback/other uses
        return AiCategorizer.categorize(itemName)
    }

    /**
     * UPDATED: Implements the exact context-aware flow.
     * 1. Check if name matches locally AND matches the selected category.
     * 2. If no, ask AI and provide the selected category.
     * 3. Fallback to standard days if offline.
     */
    suspend fun getShelfLifeByNameAI(
        itemName: String,
        selectedCategory: FoodCategory = FoodCategory.OTHER
    ): Int {
        // Step 1: keyword lookup
        val localCategory = detectCategory(itemName)

        // Only trust keyword match if it AGREES with user's selected category
        // OR if user didn't select a category (OTHER)
        if (localCategory != FoodCategory.OTHER &&
            (selectedCategory == FoodCategory.OTHER || localCategory == selectedCategory)
        ) {
            android.util.Log.d(
                "ShelfLife",
                "[$itemName] Found in rules → $localCategory = ${getShelfLifeDays(localCategory)} days"
            )
            return getShelfLifeDays(localCategory)
        }

        // Step 2: keyword conflicts with user selection, or not in rules → call AI
        android.util.Log.d("ShelfLife", "[$itemName] Calling AI (hint: $selectedCategory)")
        val aiDays = AiCategorizer.getEstimatedDays(itemName, selectedCategory.name)
        if (aiDays > 0) {
            android.util.Log.d("ShelfLife", "[$itemName] AI returned $aiDays days")
            return aiDays
        }

        // Step 3: AI failed → fallback to user's selected category default
        val fallback = getShelfLifeDays(selectedCategory)
        android.util.Log.d(
            "ShelfLife",
            "[$itemName] AI failed → fallback $selectedCategory = $fallback days"
        )
        return fallback
    }
}
//hi