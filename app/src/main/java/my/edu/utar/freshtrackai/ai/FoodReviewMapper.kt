package my.edu.utar.freshtrackai.ai

import java.util.UUID
import my.edu.utar.freshtrackai.ai.model.FoodDetectionResult
import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.ReviewItemUi
import my.edu.utar.freshtrackai.ui.dashboard.normalizeScannedQuantityLabel

/**
 * Converts food detection results into review screen items.
 * This keeps AI response models separate from UI models.
 */

internal object FoodReviewMapper {

    // Maps detected food items into UI models for review.
    fun map(result: FoodDetectionResult): List<ReviewItemUi> {
        return result.items.map { item ->
            val category = mapCategory(item.category)
            val quantityLabel = resolveQuantityLabel(
                raw = item.quantity?.raw,
                value = item.quantity?.value,
                unit = item.quantity?.unit
            )
            val expiryDays = item.expiry?.estimatedShelfLifeDays ?: estimateExpiryDays(category)

            ReviewItemUi(
                id = "food-${UUID.randomUUID().toString().take(8)}",
                name = item.name.ifBlank { "Unknown Item" },
                category = category,
                quantityLabel = quantityLabel,
                expiresLabel = "Estimated ${expiryDays}d",
                expiresInDays = expiryDays,
                nutritionLabel = "Not available",
                thumbnailRef = item.name.ifBlank { "item" }.lowercase().replace(" ", "_")
            )
        }
    }

    // Builds a readable quantity label from AI output.
    private fun resolveQuantityLabel(raw: String?, value: Double?, unit: String?): String {
        return normalizeScannedQuantityLabel(raw, value, unit)
    }

    // Converts AI category text into the app's inventory category.
    private fun mapCategory(raw: String?): InventoryCategory {
        return when (raw?.trim()?.lowercase()) {
            "dairy" -> InventoryCategory.Dairy
            "fruit", "fruits" -> InventoryCategory.Fruits
            "vegetable", "vegetables", "produce" -> InventoryCategory.Vegetables
            "meat", "poultry", "protein" -> InventoryCategory.MeatPoultry
            "seafood" -> InventoryCategory.Seafood
            "beverage", "beverages", "drink" -> InventoryCategory.Beverages
            "bakery" -> InventoryCategory.Bakery
            "snack", "snacks" -> InventoryCategory.Snacks
            "frozen" -> InventoryCategory.Frozen
            "canned", "canned_goods" -> InventoryCategory.CannedGoods
            "condiment", "condiments", "sauce" -> InventoryCategory.Condiments
            "grains", "pasta", "grains_pasta" -> InventoryCategory.GrainsPasta
            "leftover", "leftovers" -> InventoryCategory.Leftovers
            "eggs", "egg" -> InventoryCategory.Eggs
            else -> InventoryCategory.Other
        }
    }

    // Provides a fallback expiry estimate when AI does not return one.
    private fun estimateExpiryDays(category: InventoryCategory): Int {
        return when (category) {
            InventoryCategory.Dairy -> 5
            InventoryCategory.Eggs -> 21
            InventoryCategory.MeatPoultry -> 3
            InventoryCategory.Seafood -> 2
            InventoryCategory.Fruits -> 5
            InventoryCategory.Vegetables -> 7
            InventoryCategory.Bakery -> 4
            InventoryCategory.GrainsPasta -> 365
            InventoryCategory.CannedGoods -> 730
            InventoryCategory.Frozen -> 90
            InventoryCategory.Beverages -> 14
            InventoryCategory.Condiments -> 180
            InventoryCategory.Snacks -> 30
            InventoryCategory.Leftovers -> 3
            InventoryCategory.Other -> 14
        }
    }
}
