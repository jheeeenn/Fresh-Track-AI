package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.util.InventorySummaryBuilder
import my.edu.utar.freshtrackai.ai.util.PromptFactory
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi

/**
 * Coordinates the recipe generation flow for the UI layer.
 * It prepares inventory data, requests recipes, and maps the result for display.
 */

internal class GenerateRecipeUiUseCase(
    private val extractorProvider: () -> CloudFoodExtractor = { AiProvider.cloudFoodExtractor() }
) {

    // Generates recipe UI models from the current inventory.
    suspend fun generateFromInventory(
        inventory: List<InventoryItem>,
        onStatus: ((String) -> Unit)? = null
    ): List<RecipeUi> {
        val extractor = extractorProvider()
        val mappedInput = InventoryRecipeInputMapper.map(inventory)
        val summary = InventorySummaryBuilder.fromNames(mappedInput.allItemNames)
        val prompt = PromptFactory.recipePrompt(inventorySummary = summary)
        val result = extractor.suggestRecipes(prompt, onStatus)

        return RecipeUiMapper.mapRecipes(
            recipes = result.recipes
        )
    }
}
