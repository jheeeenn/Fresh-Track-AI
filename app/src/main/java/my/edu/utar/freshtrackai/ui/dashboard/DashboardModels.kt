package my.edu.utar.freshtrackai.ui.dashboard

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color

internal enum class InventoryCategory(val label: String) {
    Produce("Produce"),
    Dairy("Dairy"),
    MeatProtein("Meat & Protein"),
    Beverages("Beverages"),
    PantryDryGoods("Pantry & Dry Goods"),
    Frozen("Frozen"),
    Bakery("Bakery"),
    CondimentsSauces("Condiments & Sauces")
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
    val thumbnailRef: String
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
    val category: InventoryCategory = InventoryCategory.Produce,
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

internal data class RecipePreferencesUi(
    val selectedInventoryItemIds: Set<String> = emptySet(),
    val inventoryOnly: Boolean = false,
    val avoidancePresetSet: Set<String> = emptySet(),
    val avoidanceCustomText: String = ""
)

internal data class ShoppingListItemUi(
    val id: String,
    val name: String,
    val sourceRecipeId: String? = null,
    val sourceRecipeName: String? = null,
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
