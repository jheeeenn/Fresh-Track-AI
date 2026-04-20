package my.edu.utar.freshtrackai.ai

import java.util.UUID
import my.edu.utar.freshtrackai.ai.model.ReceiptItemDto
import my.edu.utar.freshtrackai.ai.model.ReceiptParseResult
import my.edu.utar.freshtrackai.ui.dashboard.InventoryCategory
import my.edu.utar.freshtrackai.ui.dashboard.ReviewItemUi

internal object ReceiptReviewMapper {

    fun map(result: ReceiptParseResult): List<ReviewItemUi> {
        return result.items.map { it.toReviewItem() }
    }

    private fun ReceiptItemDto.toReviewItem(): ReviewItemUi {
        val resolvedCategory = mapCategory(category)
        val resolvedDays = estimateExpiryDays(resolvedCategory)
        val resolvedQuantity = buildQuantityLabel(quantity, unit)

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

    private fun buildQuantityLabel(quantity: String?, unit: String?): String {
        val q = quantity?.trim().orEmpty()
        val u = unit?.trim().orEmpty()

        return when {
            q.isNotBlank() && u.isNotBlank() -> "$q $u"
            q.isNotBlank() -> q
            else -> "1 unit"
        }
    }

    private fun mapCategory(raw: String?): InventoryCategory {
        return when (raw?.trim()?.lowercase()) {
            "dairy" -> InventoryCategory.Dairy
            "fruit", "vegetable", "produce" -> InventoryCategory.Produce
            "meat", "protein" -> InventoryCategory.MeatProtein
            "beverage", "drink" -> InventoryCategory.Beverages
            "pantry", "snack" -> InventoryCategory.PantryDryGoods
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