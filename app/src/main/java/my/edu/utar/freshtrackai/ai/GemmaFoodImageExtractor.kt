package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import my.edu.utar.freshtrackai.ai.model.FoodDetectionResult

internal class GemmaFoodImageExtractor(
    context: Context
) : FoodImageExtractor {

    private val gemmaManager = GemmaManager(context)
    private val gson = Gson()

    override suspend fun detectFood(bitmap: Bitmap): FoodDetectionResult {
        val init = gemmaManager.ensureInitialized(enableImage = true)
        init.getOrElse { throw it }

        val prompt = """
You are identifying visible food items from an image.

Task:
Detect only food or drink items that are clearly visible in the image.
Do not guess.
Do not infer hidden items.
If uncertain, skip the item.

Return STRICT JSON only in this format:
{
  "items": [
    {
      "name": "<visible food item>",
      "category": "Other",
      "confidence": 0.50
    }
  ]
}

Rules:
- Include only visible food or drink items
- Ignore plates, table, hands, background, containers unless the food itself is visible
- If no food items are clearly visible, return {"items":[]}
- category must be one of:
  Dairy, Fruit, Vegetable, Meat, Beverage, Pantry, Snack, Other
- Return only JSON
- Do not wrap the JSON in markdown
""".trimIndent()

        val raw = gemmaManager.sendImagePrompt(bitmap, prompt)
            .getOrElse { throw it }

        Log.d("FOOD_RAW", raw)

        val cleaned = extractJson(raw)
        return gson.fromJson(cleaned, FoodDetectionResult::class.java)
    }

    private fun extractJson(raw: String): String {
        val trimmed = raw.trim()

        if (trimmed.startsWith("```")) {
            val withoutFenceStart = trimmed
                .removePrefix("```json")
                .removePrefix("```")
                .trim()

            return withoutFenceStart
                .removeSuffix("```")
                .trim()
        }

        return trimmed
    }
}