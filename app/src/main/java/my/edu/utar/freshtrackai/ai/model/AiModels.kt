package my.edu.utar.freshtrackai.ai.model

// app-side AI result models.
data class RecipeIngredientDto(
    val name: String,
    val quantity: String? = null
)

data class RecipeDto(
    val title: String,
    val description: String? = null,
    val availableIngredients: List<RecipeIngredientDto> = emptyList(),
    val missingIngredients: List<RecipeIngredientDto> = emptyList(),
    val instructions: List<String> = emptyList()
)

data class RecipeSuggestionResult(
    val recipes: List<RecipeDto> = emptyList()
)