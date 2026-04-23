package my.edu.utar.freshtrackai.ai

import java.util.UUID
import my.edu.utar.freshtrackai.ai.model.ReceiptItemDto
import my.edu.utar.freshtrackai.ai.model.ReceiptParseResult
import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.ReviewItemUi

/**
 * Converts receipt parsing results into review screen items.
 * This keeps receipt DTO models separate from UI models.
 */

internal object ReceiptReviewMapper {

    // Maps parsed receipt items into UI models for review.
    fun map(result: ReceiptParseResult): List<ReviewItemUi> {
        return result.items.map { it.toReviewItem() }
    }

    // Converts a single parsed receipt item into a review item.
    private fun ReceiptItemDto.toReviewItem(): ReviewItemUi {
        val resolvedCategory = mapCategory(category)
        val resolvedDays = expiry?.estimatedShelfLifeDays ?: estimateExpiryDays(resolvedCategory)
        val resolvedQuantity = buildQuantityLabel(quantity?.raw, quantity?.value, quantity?.unit)

        return ReviewItemUi(
            id = "rev-${UUID.randomUUID().toString().take(8)}",
            name = name.ifBlank { "Unknown Item" },
            category = resolvedCategory,
            quantityLabel = resolvedQuantity,
            expiresLabel = "Estimated ${resolvedDays}d",
            expiresInDays = resolvedDays,
            nutritionLabel = "OCR parsed from receipt",
            thumbnailRef = name.ifBlank { "item" }.lowercase().replace(" ", "_")
        )
    }

    // Builds a readable quantity label from parsed receipt data.
    private fun buildQuantityLabel(raw: String?, value: Double?, unit: String?): String {
        val parsedRaw = raw?.trim().orEmpty()
        val parsedUnit = unit?.trim().orEmpty()
        val parsedValue = value?.let {
            if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
        }.orEmpty()

        return when {
            parsedRaw.isNotBlank() -> parsedRaw
            parsedValue.isNotBlank() && parsedUnit.isNotBlank() -> "$parsedValue $parsedUnit"
            parsedValue.isNotBlank() -> parsedValue
            else -> "1 unit"
        }
    }

    // Converts receipt category text into the app's inventory category.
    private fun mapCategory(raw: String?): InventoryCategory {
        return when (raw?.trim()?.lowercase()) {
            "dairy" -> InventoryCategory.Dairy
            "fruit", "fruits" -> InventoryCategory.Fruits
            "vegetable", "vegetables", "produce" -> InventoryCategory.Vegetables
            "meat", "poultry", "protein" -> InventoryCategory.MeatPoultry
            "seafood" -> InventoryCategory.Seafood
            "beverage", "beverages", "drink" -> InventoryCategory.Beverages
            "bakery" -> InventoryCategory.Bakery
            "snack", "snacks", "pantry" -> InventoryCategory.Snacks
            "frozen" -> InventoryCategory.Frozen
            "canned", "canned_goods" -> InventoryCategory.CannedGoods
            "condiment", "condiments", "sauce" -> InventoryCategory.Condiments
            "grains", "pasta", "grains_pasta" -> InventoryCategory.GrainsPasta
            "leftover", "leftovers" -> InventoryCategory.Leftovers
            "eggs", "egg" -> InventoryCategory.Eggs
            else -> InventoryCategory.Other
        }
    }

    // Provides a fallback expiry estimate when OCR does not return one.
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
