package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult

interface CloudFoodExtractor {
    suspend fun suggestRecipes(inventorySummary: String): RecipeSuggestionResult
}