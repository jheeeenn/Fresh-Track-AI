package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.model.RecipeDto
import my.edu.utar.freshtrackai.ui.dashboard.RecipeIngredientUi
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi
import java.util.UUID

/**
 * Converts recipe DTO models into UI models.
 * This keeps API response structures separate from screen-ready data.
 */

internal object RecipeUiMapper {
    // Maps all generated recipes into UI models.
    internal fun mapRecipes(recipes: List<RecipeDto>): List<RecipeUi> {
        return recipes.mapIndexed { index, recipe ->
            recipe.toRecipeUi(
                generatedId = "ai-${index + 1}-${UUID.randomUUID().toString().take(6)}"
            )
        }
    }

    // Converts one recipe DTO into a RecipeUi object.
    private fun RecipeDto.toRecipeUi(
        generatedId: String
    ): RecipeUi {
        val available = availableIngredients.map {
            RecipeIngredientUi(
                name = it.name,
                isAvailable = true,
                quantityLabel = it.quantity ?: "As needed"
            )
        }

        val missing = missingIngredients.map {
            RecipeIngredientUi(
                name = it.name,
                isAvailable = false,
                quantityLabel = it.quantity ?: "As needed"
            )
        }

        val availableCount = available.size
        val missingCount = missing.size

        return RecipeUi(
            id = generatedId,
            title = title.ifBlank { "Untitled Recipe" },
            description = description ?: "AI-generated recipe suggestion.",
            prepMinutes = estimatePrepMinutes(instructions),
            imageUrl = null,
            pantryMatchText = buildPantryMatchText(availableCount, missingCount),
            tag = buildTag(availableCount, missingCount),
            usedInventoryItemIds = emptySet(),
            ingredientsAvailable = available,
            ingredientsMissing = missing,
            steps = if (instructions.isEmpty()) listOf("No instructions provided.") else instructions,
            avoidanceTokens = emptySet()
        )
    }

    // Estimates a simple preparation time based on instruction count.
    private fun estimatePrepMinutes(steps: List<String>): Int {
        return when {
            steps.size <= 2 -> 10
            steps.size <= 4 -> 15
            steps.size <= 6 -> 20
            else -> 25
        }
    }

    // Builds a short pantry match summary for the recipe card.
    private fun buildPantryMatchText(availableCount: Int, missingCount: Int): String {
        return when {
            availableCount > 0 && missingCount == 0 -> "Uses all ingredients from inventory"
            availableCount > 0 -> "Uses $availableCount inventory item(s)"
            else -> "AI suggested recipe"
        }
    }

    // Builds a small status tag for the recipe card.
    private fun buildTag(availableCount: Int, missingCount: Int): String {
        return when {
            availableCount > 0 && missingCount == 0 -> "Ready to cook"
            availableCount > missingCount -> "Good match"
            else -> "Needs ingredients"
        }
    }


}
