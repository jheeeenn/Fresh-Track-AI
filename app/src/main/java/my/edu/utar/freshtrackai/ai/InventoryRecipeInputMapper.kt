package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem

/**
 * Maps inventory data into recipe-generation input.
 */

internal object InventoryRecipeInputMapper {

    internal data class RecipeInput(
        val allItemNames: List<String>,
        val preferredItemNames: List<String> = emptyList(),
        val selectedInventoryIds: Set<String> = emptySet(),
        val inventoryOnly: Boolean = false,
        val avoidanceTokens: Set<String> = emptySet()
    )

    internal fun map(inventory: List<InventoryItem>): RecipeInput {
        return RecipeInput(
            allItemNames = inventory.map { it.name }
        )
    }
}
