package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi

internal data class RecipeGenerationUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipeUi> = emptyList(),
    val errorMessage: String? = null
)