package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.ai.model.RecipeDto
import my.edu.utar.freshtrackai.ui.dashboard.RecipeIngredientUi
import my.edu.utar.freshtrackai.ui.dashboard.RecipeUi
import java.util.UUID

internal object RecipeUiMapper {

    internal fun mapRecipes(
        recipes: List<RecipeDto>,
        selectedInventoryIds: Set<String> = emptySet()
    ): List<RecipeUi> {
        return recipes.mapIndexed { index, recipe ->
            recipe.toRecipeUi(
                generatedId = "ai-${index + 1}-${UUID.randomUUID().toString().take(6)}",
                selectedInventoryIds = selectedInventoryIds
            )
        }
    }

    private fun RecipeDto.toRecipeUi(
        generatedId: String,
        selectedInventoryIds: Set<String>
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
            usedInventoryItemIds = selectedInventoryIds,
            ingredientsAvailable = available,
            ingredientsMissing = missing,
            steps = if (instructions.isEmpty()) listOf("No instructions provided.") else instructions,
            avoidanceTokens = emptySet()
        )
    }

    private fun estimatePrepMinutes(steps: List<String>): Int {
        return when {
            steps.size <= 2 -> 10
            steps.size <= 4 -> 15
            steps.size <= 6 -> 20
            else -> 25
        }
    }

    private fun buildPantryMatchText(availableCount: Int, missingCount: Int): String {
        return when {
            availableCount > 0 && missingCount == 0 -> "Uses all ingredients from inventory"
            availableCount > 0 -> "Uses $availableCount inventory item(s)"
            else -> "AI suggested recipe"
        }
    }

    private fun buildTag(availableCount: Int, missingCount: Int): String {
        return when {
            availableCount > 0 && missingCount == 0 -> "Ready to cook"
            availableCount > missingCount -> "Good match"
            else -> "Needs ingredients"
        }
    }


}