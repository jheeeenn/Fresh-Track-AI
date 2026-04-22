package my.edu.utar.freshtrackai.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult
import my.edu.utar.freshtrackai.ai.util.PromptFactory

class GeminiCloudFoodExtractor(
    private val apiKey: String
) : CloudFoodExtractor {

    private val gson = Gson()

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    }

    override suspend fun suggestRecipes(inventorySummary: String): RecipeSuggestionResult {
        return withContext(Dispatchers.IO) {
            val prompt = PromptFactory.recipePrompt(inventorySummary)
            val maxAttempts = 3
            var lastError: Throwable? = null

            for (attempt in 0 until maxAttempts) {
                try {
                    val response = generativeModel.generateContent(prompt)
                    val text = response.text.orEmpty().trim()

                    if (text.isBlank()) {
                        error("Gemini returned an empty response")
                    }

                    return@withContext parseRecipeJson(text)
                } catch (t: Throwable) {
                    lastError = t

                    val shouldRetry = isServiceUnavailable(t) && attempt < maxAttempts - 1
                    if (!shouldRetry) {
                        break
                    }

                    val backoffMillis = 1000L * (attempt + 1)
                    Log.w(
                        "GEMINI_RECIPE",
                        "Gemini temporarily unavailable (attempt ${attempt + 1}/$maxAttempts). Retrying in ${backoffMillis}ms"
                    )
                    delay(backoffMillis)
                }
            }

            if (lastError != null && isServiceUnavailable(lastError)) {
                val localFallback = runCatching { generateWithLocalGemma(prompt) }
                localFallback.getOrNull()?.let { return@withContext it }

                Log.e(
                    "GEMINI_RECIPE",
                    "Gemini unavailable and local Gemma fallback failed.",
                    localFallback.exceptionOrNull()
                )
            }

            throw IllegalStateException(
                "Failed to generate recipe suggestions after retries.",
                lastError
            )
        }
    }

    private suspend fun generateWithLocalGemma(prompt: String): RecipeSuggestionResult {
        val context = AppContextProvider.get()
            ?: error("Application context unavailable for local Gemma fallback")

        val gemmaManager = GemmaManager(context)
        try {
            val init = gemmaManager.ensureInitialized(enableImage = false)
            init.getOrElse { throw it }

            val raw = gemmaManager.sendPrompt(prompt)
                .getOrElse { throw it }

            return parseRecipeJson(raw)
        } finally {
            gemmaManager.close()
        }
    }

    private fun parseRecipeJson(rawText: String): RecipeSuggestionResult {
        val cleanedJson = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return gson.fromJson(cleanedJson, RecipeSuggestionResult::class.java)
    }

    private fun isServiceUnavailable(throwable: Throwable): Boolean {
        val errorMessage = throwable.message.orEmpty()
        return errorMessage.contains("503") ||
            errorMessage.contains("UNAVAILABLE", ignoreCase = true)
    }
}
