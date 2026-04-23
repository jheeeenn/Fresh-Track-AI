package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import my.edu.utar.freshtrackai.ai.model.ReceiptParseResult
import my.edu.utar.freshtrackai.ai.util.PromptFactory
import android.util.Log
/**
 * Uses the local Gemma model to parse grocery receipt images.
 * The model output is cleaned and converted into structured receipt data.
 */

internal class GemmaReceiptOcrExtractor(
    context: Context
) : ReceiptOcrExtractor {

    private val gemmaManager = GemmaManager(context)
    private val gson = Gson()

    override suspend fun parseReceipt(bitmap: Bitmap): ReceiptParseResult {
        val init = gemmaManager.ensureInitialized(enableImage = true)
        init.getOrElse { throw it }

        val prompt = PromptFactory.receiptOcrPrompt()

        val raw = gemmaManager.sendImagePrompt(bitmap, prompt)
            .getOrElse { throw it }

        Log.d("RECEIPT_RAW", raw) // Log the raw response

        val cleaned = extractJson(raw)

        return gson.fromJson(cleaned, ReceiptParseResult::class.java)
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