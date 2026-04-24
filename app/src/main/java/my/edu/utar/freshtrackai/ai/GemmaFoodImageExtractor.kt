package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import my.edu.utar.freshtrackai.ai.model.FoodDetectionResult
import my.edu.utar.freshtrackai.ai.util.PromptFactory

/**
 * Uses the local Gemma model to detect visible food items from an image.
 * The model response is parsed into structured food detection results.
 */

internal class GemmaFoodImageExtractor(
    context: Context
) : FoodImageExtractor {

    private val gemmaManager = GemmaManager(context)
    private val gson = Gson()

    override suspend fun detectFood(bitmap: Bitmap): FoodDetectionResult {
        val init = gemmaManager.ensureInitialized(enableImage = true)
        init.getOrElse { throw it }

        val prompt = PromptFactory.foodImageOcrPrompt()

        val raw = gemmaManager.sendImagePrompt(bitmap, prompt)
            .getOrElse { throw it }

        Log.d("FOOD_RAW", raw)

        val cleaned = extractJson(raw)
        return gson.fromJson(cleaned, FoodDetectionResult::class.java)
    }

    // Removes optional markdown fences before JSON parsing.
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