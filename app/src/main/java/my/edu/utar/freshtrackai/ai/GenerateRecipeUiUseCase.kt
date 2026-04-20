package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.util.InventorySummaryBuilder
import my.edu.utar.freshtrackai.ui.dashboard.InventoryItem
import my.edu.utar.freshtrackai.ui.dashboard.RecipePreferencesUi
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi

internal class GenerateRecipeUiUseCase(
    private val extractor: CloudFoodExtractor = AiProvider.cloudFoodExtractor
) {
    suspend fun generateFromInventory(
        inventory: List<InventoryItem>,
        preferences: RecipePreferencesUi
    ): List<RecipeUi> {
        val mappedInput = InventoryRecipeInputMapper.map(
            inventory = inventory,
            preferences = preferences
        )

        val summary = InventorySummaryBuilder.fromNames(mappedInput.itemNames)
        val result = extractor.suggestRecipes(summary)

        return RecipeUiMapper.mapRecipes(
            recipes = result.recipes,
            selectedInventoryIds = mappedInput.selectedInventoryIds
        )
    }

    suspend fun generateFromItemNames(
        itemNames: List<String>,
        selectedInventoryIds: Set<String> = emptySet()
    ): List<RecipeUi> {
        val summary = InventorySummaryBuilder.fromNames(itemNames)
        val result = extractor.suggestRecipes(summary)

        return RecipeUiMapper.mapRecipes(
            recipes = result.recipes,
            selectedInventoryIds = selectedInventoryIds
        )
    }
}