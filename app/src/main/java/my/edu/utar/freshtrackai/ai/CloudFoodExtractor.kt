package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult

/**
 * Defines the contract for cloud-based recipe generation.
 * Implementations use the current inventory summary to return recipe suggestions.
 */
interface CloudFoodExtractor {

    // Requests recipe suggestions from the cloud AI service.
    suspend fun suggestRecipes(
        inventorySummary: String,
        onStatus: ((String) -> Unit)? = null
    ): RecipeSuggestionResult
}
