package my.edu.utar.freshtrackai.ai.util

internal object PromptFactory {

    private const val SUPPORTED_CATEGORIES =
        "Dairy, Fruit, Vegetable, Meat, Seafood, Beverage, Bakery, Pantry, Snack, Frozen, Canned, Condiment, Grains, Eggs, Leftover, Other"

    private const val JSON_ONLY_RULES = """
- Return valid JSON only
- Do not include markdown fences
- Do not include explanations or extra keys
"""

    fun recipePrompt(inventorySummary: String): String = """
You are a recipe assistant.

Task:
- Propose exactly 2 practical recipes using the provided inventory.
- Prefer recipes that consume soon-to-expire ingredients first.
- Keep recipes simple for home cooking.

Inventory:
$inventorySummary

Output requirements:
- Return STRICT JSON only.
- No markdown fences, no prose, no additional keys.
- Schema:
{
  "recipes": [
    {
      "title": "Recipe Name",
      "description": "One short sentence.",
      "availableIngredients": [
        { "name": "Ingredient Name", "quantity": "amount" }
      ],
      "missingIngredients": [
        { "name": "Ingredient Name", "quantity": "amount" }
      ],
      "instructions": [
        "1. Step one.",
        "2. Step two."
      ]
    }
  ]
}

Rules:
- availableIngredients can only include items from inventory.
- missingIngredients must include only items not present in inventory.
- instructions must be concise and actionable.
""".trimIndent()

    fun foodImageOcrPrompt(): String = """
You are identifying visible food items from an image.

Task:
Detect only food or drink items that are clearly visible in the image.
Do not guess hidden products.
If uncertain, skip the item.

Return STRICT JSON only in this schema:
{
  "items": [
    {
      "name": "Milk",
      "category": "Dairy",
      "quantity": {
        "value": 1,
        "unit": "bottle",
        "raw": "1 bottle"
      },
      "expiry": {
        "date": null,
        "dateFormat": null,
        "isEstimated": true,
        "estimatedShelfLifeDays": 5,
        "confidence": 0.52
      },
      "confidence": 0.92
    }
  ]
}

Rules:
- Include only visible edible items (food or drink)
- Ignore utensils, containers, tables, labels, and background unless edible item is clearly visible
- If no items are clearly visible, return {"items":[]}
- category must be one of: $SUPPORTED_CATEGORIES
- quantity fields must be null when not visible
- expiry.date should be null unless an actual printed date is clearly visible in image
- confidence values are decimals in range 0.0 to 1.0
$JSON_ONLY_RULES
""".trimIndent()

    fun receiptOcrPrompt(): String = """
You are reading a grocery receipt image.

Task:
Extract only grocery or food-related line items that are clearly printed.
Do not guess missing lines.
If text is unclear, skip it.

Return STRICT JSON only in this schema:
{
  "items": [
    {
      "name": "Strawberries",
      "category": "Fruit",
      "quantity": {
        "value": 2,
        "unit": "pack",
        "raw": "2 pack"
      },
      "expiry": {
        "date": null,
        "dateFormat": null,
        "isEstimated": true,
        "estimatedShelfLifeDays": 5,
        "confidence": 0.47
      },
      "price": {
        "amount": 7.90,
        "currency": null,
        "raw": "7.90"
      },
      "confidence": 0.86
    }
  ]
}

Rules:
- Ignore subtotal, total, tax, payment lines, cashier/store metadata, and product codes
- Ignore non-food items
- If no readable food items exist, return {"items":[]}
- category must be one of: $SUPPORTED_CATEGORIES
- quantity should be null when not explicitly shown for a line item
- expiry.date should be null unless explicitly printed
- price fields are optional and may be null when uncertain
- confidence values are decimals in range 0.0 to 1.0
$JSON_ONLY_RULES
""".trimIndent()
}
