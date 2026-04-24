package my.edu.utar.freshtrackai.ai.model

import com.google.gson.annotations.SerializedName

/**
 * DTO models for recipe suggestion responses returned by the cloud AI service.

 * These classes are used by Gson to deserialize the JSON returned from Gemini
 * into structured Kotlin objects. They only represent the external response
 * format and should not contain UI logic or business logic.

 */


/**
 * Represents one ingredient entry in a generated recipe.
 *
 * Example JSON:
 * {
 *   "name": "Eggs",
 *   "quantity": "2"
 * }
 */
data class RecipeIngredientDto(
    @SerializedName("name")     val name: String = "",
    @SerializedName("quantity") val quantity: String? = null
)

/**
 * Represents a single recipe returned by the AI service.
 *
 * A recipe contains:
 * - a title
 * - a short description
 * - ingredients already available in the user's inventory
 * - missing ingredients that the user may need to buy
 * - step-by-step cooking instructions
 */
data class RecipeDto(
    @SerializedName("title")
    val title: String = "",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("availableIngredients")
    val availableIngredients: List<RecipeIngredientDto> = emptyList(),

    @SerializedName("missingIngredients")
    val missingIngredients: List<RecipeIngredientDto> = emptyList(),

    @SerializedName("instructions")
    val instructions: List<String> = emptyList()
)

/**
 * Root response model for recipe generation.
 *
 * Example JSON:
 * {
 *   "recipes": [ ... ]
 * }
 */
data class RecipeSuggestionResult(
    @SerializedName("recipes")
    val recipes: List<RecipeDto> = emptyList()
)