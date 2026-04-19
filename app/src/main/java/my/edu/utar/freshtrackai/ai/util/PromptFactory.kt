package my.edu.utar.freshtrackai.ai.util

object PromptFactory {

    // gemini recipe generation prompt
    fun recipePrompt(inventorySummary: String): String {
        return """
        You are a recipe suggestion assistant.

        Based only on this inventory:
        $inventorySummary

        Return STRICT JSON only in this format:
        {
          "recipes": [
            {
              "title": "Recipe name",
              "description": "Short description",
              "availableIngredients": [
                {"name": "Eggs", "quantity": "2"}
              ],
              "missingIngredients": [
                {"name": "Butter", "quantity": "1 tbsp"}
              ],
              "instructions": [
                "Step 1",
                "Step 2"
              ]
            }
          ]
        }

        Rules:
        - Return exactly 1 recipe only.
        - Use available inventory ingredients as much as possible.
        - Keep missing ingredients minimal, at most 3.
        - Keep instructions short and practical.
        - Do not include markdown.
        - Do not include any extra explanation outside JSON.
    """.trimIndent()
    }


}
