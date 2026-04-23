package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipePreferencesUi

/**
 * Maps inventory data and recipe preferences into recipe-generation input.
 * This keeps selection logic separate from the use case itself.
 */

internal object InventoryRecipeInputMapper {

    // Simplified input passed into recipe generation.
    internal data class RecipeInput(
        val itemNames: List<String>,
        val selectedInventoryIds: Set<String>
    )

    // Selects the effective inventory items based on current preferences.
    internal fun map(
        inventory: List<InventoryItem>,
        preferences: RecipePreferencesUi
    ): RecipeInput {
        val availableIds = inventory.map { it.id }.toSet()

        val selectedIds = if (preferences.selectedInventoryItemIds.isEmpty()) {
            availableIds
        } else {
            preferences.selectedInventoryItemIds.intersect(availableIds)
        }

        val effectiveIds = if (selectedIds.isEmpty()) availableIds else selectedIds

        val selectedItems = inventory.filter { it.id in effectiveIds }

        val itemNames = selectedItems.map { it.name }

        return RecipeInput(
            itemNames = itemNames,
            selectedInventoryIds = effectiveIds
        )
    }
}