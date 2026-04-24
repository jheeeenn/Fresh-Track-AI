package my.edu.utar.freshtrackai.ai.util

/**
 * Stores reusable prompt templates for cloud and local AI features.
 * This keeps prompt text separate from extractor and networking logic.
 */

internal object PromptFactory {

    // Category labels expected by the AI response.
    private const val SUPPORTED_CATEGORIES =
        "Dairy, Fruit, Vegetable, Meat, Seafood, Beverage, Bakery, Pantry, Snack, Frozen, Canned, Condiment, Grains, Eggs, Leftover, Other"

    // Shared output rules to keep responses machine-readable.
    private const val JSON_ONLY_RULES = """
- Return valid JSON only
- Do not include markdown fences
- Do not include explanations or extra keys
"""

    // Prompt used for recipe generation from the current inventory.
    fun recipePrompt(inventorySummary: String): String {
        return """
You are a recipe assistant.

Task:
- Propose up to 10 distinct recipes using the provided inventory.
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
- If no practical recipe can be formed, return {"recipes": []}.
""".trimIndent()
    }

    fun nutritionLabelPrompt(): String = """
You are a nutrition label reader.

Task:
- Read the nutrition facts shown in the image.
- Return only the key nutrition values that are clearly visible.

Output format:
- Plain text only
- One value per line
- Keep it short and human readable

Example:
120 kcal / serving
Protein: 5g
Fat: 4g
Carbs: 18g
Sugar: 6g
Sodium: 210mg

Rules:
- Include serving size if it is clearly shown
- Do not include brand, barcode, marketing claims, or price
- If the label is unreadable or not a nutrition label, reply exactly: UNREADABLE
- Do not use markdown fences or extra explanation
""".trimIndent()

    fun nutritionEstimatePrompt(itemName: String, categoryName: String = ""): String {
        val categoryHint = if (categoryName.isBlank()) "" else " (category: $categoryName)"
        return """
You are a nutrition estimation assistant.

Task:
- Provide a typical nutrition estimate for "$itemName"$categoryHint.

Output format:
- Plain text only
- One value per line
- Keep it short and human readable

Example:
61 kcal / 100ml
Protein: 3.2g
Fat: 3.3g
Carbs: 4.8g
Sugar: 4.8g
Calcium: 120mg

Rules:
- Use per 100g or per 100ml as the base unit
- Include the 4-5 most relevant nutrients
- Use typical values for a standard version of the food
- Do not use markdown fences or extra explanation
""".trimIndent()
    }

    // Prompt used for food image analysis with Gemma.
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

    // Prompt used for receipt OCR with Gemma.
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
