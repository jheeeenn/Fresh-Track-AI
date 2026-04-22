package my.edu.utar.freshtrackai.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import my.edu.utar.freshtrackai.ai.model.ReceiptParseResult
import my.edu.utar.freshtrackai.ai.util.PromptFactory

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