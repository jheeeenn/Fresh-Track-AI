package my.edu.utar.freshtrackai.ui.dashboard

import android.net.Uri
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// FIX: Aligned UI Categories with Member 3's ShelfLifeRules.kt
// Now both the UI and Backend have the exact same 15 categories!
// ─────────────────────────────────────────────────────────────
internal enum class InventoryCategory(val label: String) {
    Dairy("Dairy"),
    Eggs("Eggs"),
    MeatPoultry("Meat & Poultry"),
    Seafood("Seafood"),
    Fruits("Fruits"),
    Vegetables("Vegetables"),
    Bakery("Bakery"),
    GrainsPasta("Grains & Pasta"),
    CannedGoods("Canned Goods"),
    Frozen("Frozen"),
    Beverages("Beverages"),
    Condiments("Condiments & Sauces"),
    Snacks("Snacks"),
    Leftovers("Leftovers"),
    Other("Other")
}

internal enum class ExpiryBadge(val label: String, val textColor: Color, val bgColor: Color) {
    Critical("CRITICAL", RoseRed, Color(0xFFFFE4E6)),
    Warning("WARNING", Color(0xFFB45309), Color(0xFFFEF3C7)),
    Watch("WATCH", Color(0xFF15803D), Color(0xFFDCFCE7))
}

internal data class InventoryItem(
    val id: String,
    val name: String,
    val category: InventoryCategory,
    val quantityLabel: String,
    val addedDaysAgo: Int,
    val expiresInDays: Int,
    val thumbnailRef: String,
    val nutritionNotes: String = "",
    val formattedExpiryDate: String = "",
    val formattedAddedDate: String = "",
    val purchaseDateMillis: Long = 0L
)

internal data class ExpiringItem(
    val id: String,
    val inventoryItemId: String,
    val name: String,
    val expiresInDays: Int,
    val badge: ExpiryBadge,
    val category: InventoryCategory,
    val canRecipe: Boolean = true
)

internal data class ReviewItemUi(
    val id: String,
    val name: String,
    val category: InventoryCategory,
    val quantityLabel: String,
    val expiresLabel: String,
    val expiresInDays: Int,
    val nutritionLabel: String,
    val thumbnailRef: String
)

internal enum class AddItemOrigin {
    ItemReview,
    Dashboard
}

internal data class AddItemFormDraft(
    val name: String = "",
    val quantity: String = "",
    // FIX: Updated the default category from 'Produce' to 'Vegetables' to match the new enum
    val category: InventoryCategory = InventoryCategory.Vegetables,
    val expiryDate: String = "",
    val nutritionNotes: String = ""
)

internal data class RecipeIngredientUi(
    val name: String,
    val isAvailable: Boolean,
    val quantityLabel: String
)

internal data class RecipeUi(
    val id: String,
    val title: String,
    val description: String,
    val prepMinutes: Int,
    val imageUrl: String?,
    val pantryMatchText: String,
    val tag: String,
    val usedInventoryItemIds: Set<String>,
    val ingredientsAvailable: List<RecipeIngredientUi>,
    val ingredientsMissing: List<RecipeIngredientUi>,
    val steps: List<String>,
    val avoidanceTokens: Set<String>
)

internal data class ShoppingListItemUi(
    val id: String,
    val name: String,
    val sourceRecipeId: String? = null,
    val sourceRecipeName: String? = null,
    val quantityCount: Int = 1,
    val checked: Boolean = false
)

internal enum class ScanMode {
    Food,
    Receipt
}

internal sealed class ScanCapture(val id: String) {
    class Camera(id: String, val uri: Uri) : ScanCapture(id)
    class Gallery(id: String, val uri: Uri) : ScanCapture(id)
}
