package my.edu.utar.freshtrackai.ai.util

object PromptFactory {

    fun receiptPrompt(): String {
        return """
        You are a receipt parsing assistant.

        From this receipt image, extract only grocery or food-related items.

        Return STRICT JSON only in this format:
        {
          "items": [
            {
              "name": "Milk",
              "category": "Dairy",
              "quantity": "1",
              "unit": "carton",
              "confidence": 0.95
            }
          ]
        }

        Rules:
        - Include only food or drink items.
        - Ignore prices, totals, store name, address, payment details, and dates.
        - category must be one of:
          Dairy, Fruit, Vegetable, Meat, Beverage, Pantry, Snack, Other
        - quantity and unit can be null if unclear.
        - confidence should be between 0.0 and 1.0 if possible.
        - Return only JSON.
        - Do not use markdown.
    """.trimIndent()
    }

    fun foodImagePrompt(): String {
        return """
            You are a food detection assistant.
            Analyze this fridge/food image and identify visible food items.

            Return STRICT JSON only in this format:
            {
              "items": [
                {
                  "name": "Eggs",
                  "category": "Dairy",
                  "quantity": "6",
                  "unit": "pieces",
                  "confidence": 0.90
                }
              ]
            }

            Rules:
            - Detect only visible food-related items.
            - category must be one of:
              Dairy, Fruit, Vegetable, Meat, Beverage, Pantry, Snack, Other
            - quantity and unit can be null if unclear.
            - Do not include markdown.
        """.trimIndent()
    }

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
