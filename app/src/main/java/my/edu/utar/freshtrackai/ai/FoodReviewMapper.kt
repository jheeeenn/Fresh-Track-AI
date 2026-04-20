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