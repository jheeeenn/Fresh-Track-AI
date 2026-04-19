package my.edu.utar.freshtrackai.ai

import java.util.UUID
import my.edu.utar.freshtrackai.ai.model.FoodDetectionResult
import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.ReviewItemUi

internal object FoodReviewMapper {

    fun map(result: FoodDetectionResult): List<ReviewItemUi> {
        return result.items.map { item ->
            val category = mapCategory(item.category)

            ReviewItemUi(
                id = "food-${UUID.randomUUID().toString().take(8)}",
                name = item.name.ifBlank { "Unknown Item" },
                category = category,
                quantityLabel = "Detected item",
                expiresLabel = "Estimate after review",
                expiresInDays = estimateExpiryDays(category),
                nutritionLabel = "AI food scan",
                thumbnailRef = item.name.lowercase().replace(" ", "_")
            )
        }
    }

    private fun mapCategory(raw: String?): InventoryCategory {
        return when (raw?.trim()?.lowercase()) {
            "dairy" -> InventoryCategory.Dairy
            "fruit" -> InventoryCategory.Produce
            "vegetable" -> InventoryCategory.Produce
            "meat" -> InventoryCategory.MeatProtein
            "beverage" -> InventoryCategory.Beverages
            "pantry" -> InventoryCategory.PantryDryGoods
            "snack" -> InventoryCategory.PantryDryGoods
            else -> InventoryCategory.PantryDryGoods
        }
    }

    private fun estimateExpiryDays(category: InventoryCategory): Int {
        return when (category) {
            InventoryCategory.Dairy -> 5
            InventoryCategory.Produce -> 7
            InventoryCategory.MeatProtein -> 3
            InventoryCategory.Beverages -> 14
            InventoryCategory.PantryDryGoods -> 30
            InventoryCategory.Frozen -> 30
            InventoryCategory.Bakery -> 4
            InventoryCategory.CondimentsSauces -> 60
        }
    }
}