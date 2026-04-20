package my.edu.utar.freshtrackai.ai.model

import com.google.gson.annotations.SerializedName

// ─── Recipe suggestion models ─────────────────────────────────────────────────
// Note: FoodDetectionResult/FoodItemDto are in FoodDetectionResult.kt
// Note: ReceiptItemDto/ReceiptParseResult are in ReceiptParseModels.kt

data class RecipeIngredientDto(
    @SerializedName("name")     val name: String = "",
    @SerializedName("quantity") val quantity: String? = null
)

data class RecipeDto(
    @SerializedName("title")                val title: String = "",
    @SerializedName("description")          val description: String? = null,
    @SerializedName("availableIngredients") val availableIngredients: List<RecipeIngredientDto> = emptyList(),
    @SerializedName("missingIngredients")   val missingIngredients: List<RecipeIngredientDto> = emptyList(),
    @SerializedName("instructions")         val instructions: List<String> = emptyList()
)

data class RecipeSuggestionResult(
    @SerializedName("recipes") val recipes: List<RecipeDto> = emptyList()
)