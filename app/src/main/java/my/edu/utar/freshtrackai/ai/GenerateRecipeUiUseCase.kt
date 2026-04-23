package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.util.InventorySummaryBuilder
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipePreferencesUi
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi

/**
 * Coordinates the recipe generation flow for the UI layer.
 * It prepares inventory data, requests recipes, and maps the result for display.
 */

internal class GenerateRecipeUiUseCase(
    private val extractor: CloudFoodExtractor = AiProvider.cloudFoodExtractor
) {

    // Generates recipe UI models from the current inventory and user preferences.
    suspend fun generateFromInventory(
        inventory: List<InventoryItem>,
        preferences: RecipePreferencesUi,
        onStatus: ((String) -> Unit)? = null
    ): List<RecipeUi> {
        val mappedInput = InventoryRecipeInputMapper.map(
            inventory = inventory,
            preferences = preferences
        )

        val summary = InventorySummaryBuilder.fromNames(mappedInput.itemNames)
        val result = extractor.suggestRecipes(summary, onStatus)

        return RecipeUiMapper.mapRecipes(
            recipes = result.recipes,
            selectedInventoryIds = mappedInput.selectedInventoryIds
        )
    }


}
