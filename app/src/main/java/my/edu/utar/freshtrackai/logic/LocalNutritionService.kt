package my.edu.utar.freshtrackai.logic

import android.graphics.Bitmap
import my.edu.utar.freshtrackai.ai.AppContextProvider
import my.edu.utar.freshtrackai.ai.GemmaManager
import my.edu.utar.freshtrackai.ai.util.PromptFactory

internal object LocalNutritionService {

    suspend fun readLabel(bitmap: Bitmap): Result<String> {
        return runWithGemma(enableImage = true, action = NutritionAction.LabelScan) { gemmaManager ->
            gemmaManager.sendImagePrompt(bitmap, PromptFactory.nutritionLabelPrompt())
                .getOrElse { throw it }
        }.mapCatching { raw ->
            normalizeNutritionResponse(raw)
                ?.takeUnless { it.equals("UNREADABLE", ignoreCase = true) }
                ?: error("Label was unreadable. Please try a clearer photo.")
        }
    }

    suspend fun estimateByName(itemName: String, categoryName: String = ""): Result<String> {
        return runWithGemma(enableImage = false, action = NutritionAction.Estimate) { gemmaManager ->
            gemmaManager.sendPrompt(
                PromptFactory.nutritionEstimatePrompt(
                    itemName = itemName,
                    categoryName = categoryName
                )
            ).getOrElse { throw it }
        }.mapCatching { raw ->
            normalizeNutritionResponse(raw)
                ?: error("Local Gemma did not return nutrition data. Try entering it manually.")
        }
    }

    private suspend fun runWithGemma(
        enableImage: Boolean,
        action: NutritionAction,
        block: suspend (GemmaManager) -> String
    ): Result<String> {
        val context = AppContextProvider.get()
            ?: return Result.failure(
                IllegalStateException("App context is unavailable. Please reopen the app.")
            )

        val gemmaManager = GemmaManager(context)
        return try {
            gemmaManager.ensureInitialized(enableImage = enableImage).getOrElse { throw it }
            Result.success(block(gemmaManager))
        } catch (e: Exception) {
            Result.failure(IllegalStateException(toFailureMessage(e, action), e))
        } finally {
            gemmaManager.close()
        }
    }

    private fun normalizeNutritionResponse(raw: String): String? {
        val cleaned = raw
            .trim()
            .removePrefix("```text")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return cleaned.ifBlank { null }
    }

    private fun toFailureMessage(error: Throwable, action: NutritionAction): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("No Gemma model selected yet", ignoreCase = true) ->
                "Local Gemma model is not configured."

            message.contains("no longer exists", ignoreCase = true) ->
                "Selected local Gemma model file is missing."

            action == NutritionAction.LabelScan &&
                (message.contains("vision", ignoreCase = true) ||
                    message.contains("image", ignoreCase = true)) ->
                "Selected local Gemma model does not support nutrition label scans."

            action == NutritionAction.LabelScan ->
                "Local Gemma could not read the nutrition label. Try a clearer photo or enter it manually."

            else ->
                "Local Gemma could not estimate nutrition. Try entering it manually."
        }
    }
}

internal enum class NutritionAction {
    LabelScan,
    Estimate
}
