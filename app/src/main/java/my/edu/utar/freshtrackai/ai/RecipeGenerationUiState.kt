package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi


/*
 * Holds the UI state for recipe generation.
 * Used to represent loading, result, and error states in the screen.
*/

internal data class RecipeGenerationUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipeUi> = emptyList(),
    val errorMessage: String? = null,
    val processingMessage: String? = null
)
