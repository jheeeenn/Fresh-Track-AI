package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult

/**
 * Defines the contract for AI-backed recipe generation.
 * Implementations receive a fully prepared prompt and return recipe suggestions.
 */
interface CloudFoodExtractor {

    // Requests recipe suggestions from the AI service using the prepared prompt text.
    suspend fun suggestRecipes(
        promptText: String,
        onStatus: ((String) -> Unit)? = null
    ): RecipeSuggestionResult
}
