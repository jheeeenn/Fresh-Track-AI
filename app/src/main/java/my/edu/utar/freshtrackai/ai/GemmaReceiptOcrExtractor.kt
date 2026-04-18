package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.gson.Gson
import my.edu.utar.freshtrackai.ai.model.ReceiptParseResult
import java.io.ByteArrayOutputStream

internal class GemmaReceiptOcrExtractor(
    context: Context
) : ReceiptOcrExtractor {

    private val gemmaManager = GemmaManager(context)
    private val gson = Gson()

    override suspend fun parseReceipt(bitmap: Bitmap): ReceiptParseResult {
        val init = gemmaManager.ensureInitialized(enableImage = true)
        init.getOrElse { throw it }

        val prompt = """
You are reading a grocery receipt image.

Task:
Extract only item names that are visibly printed on the receipt.
Do not guess.
Do not infer missing items.
If text is unclear, skip it.

Return STRICT JSON only in this format:
{
  "items": [
    {
      "name": "Strawberries",
      "category": "Fruit",
      "quantity": null,
      "unit": null,
      "confidence": 0.82
    }
  ]
}

Rules:
- Extract only grocery or food-related line items that are clearly visible
- Ignore subtotal, total, tax, cashier name, store info, codes, prices, and payment text
- Ignore non-food items
- If no food items are readable, return:
  {"items":[]}
- category must be one of:
  Dairy, Fruit, Vegetable, Meat, Beverage, Pantry, Snack, Other
- quantity and unit should usually be null unless explicitly shown in the line item
- confidence should be lower when text is uncertain
- Return only JSON
- Do not wrap the JSON in markdown
""".trimIndent()

        val raw = gemmaManager.sendImagePrompt(bitmap, prompt)
            .getOrElse { throw it }

        android.util.Log.d("RECEIPT_RAW", raw)
        val cleaned = extractJson(raw)

        return gson.fromJson(cleaned, ReceiptParseResult::class.java)
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