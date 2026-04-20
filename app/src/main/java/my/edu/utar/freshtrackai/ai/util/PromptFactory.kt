package my.edu.utar.freshtrackai.ai.util

internal object PromptFactory {

    fun recipePrompt(inventorySummary: String): String = """
You are a professional chef and recipe generator AI.

Given the following food inventory, suggest 3-4 practical recipes the user can cook.
Prioritize recipes that use items likely to expire soon.

Inventory:
$inventorySummary

Return STRICT JSON only in the following format. No markdown, no explanation, no preamble:
{
  "recipes": [
    {
      "title": "Recipe Name",
      "description": "Brief one-sentence description of the dish.",
      "availableIngredients": [
        { "name": "Ingredient Name", "quantity": "amount" }
      ],
      "missingIngredients": [
        { "name": "Ingredient Name", "quantity": "amount" }
      ],
      "instructions": [
        "Step 1 description.",
        "Step 2 description."
      ]
    }
  ]
}

Rules:
- availableIngredients must only contain items from the provided inventory
- missingIngredients should list any additional items needed that are NOT in inventory
- instructions should be clear, numbered steps as plain strings
- Return only valid JSON, no markdown fences
""".trimIndent()
}