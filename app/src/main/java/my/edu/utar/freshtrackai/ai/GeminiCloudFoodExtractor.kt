package my.edu.utar.freshtrackai.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult
import my.edu.utar.freshtrackai.ai.util.PromptFactory


/**
 * Uses Gemini to generate recipe suggestions from the current inventory.
 * If Gemini is temporarily unavailable, it retries and can fall back to local Gemma.
 */

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

    override suspend fun suggestRecipes(
        inventorySummary: String,
        onStatus: ((String) -> Unit)?
    ): RecipeSuggestionResult {
        return withContext(Dispatchers.IO) {
            val prompt = PromptFactory.recipePrompt(inventorySummary)
            val maxAttempts = 3
            var lastError: Throwable? = null
            var geminiAttempts = 0

            for (attempt in 0 until maxAttempts) {
                try {
                    geminiAttempts += 1
                    Log.d("GEMINI_RECIPE", "Calling Gemini attempt ${attempt + 1}/$maxAttempts")
                    onStatus?.invoke("Generating recipes with Gemini (${attempt + 1}/$maxAttempts)...")
                    val response = generativeModel.generateContent(prompt)
                    val text = response.text.orEmpty().trim()

                    if (text.isBlank()) {
                        error("Gemini returned an empty response")
                    }

                    Log.d("GEMINI_RECIPE", "Gemini recipe generation succeeded")
                    return@withContext parseRecipeJson(text)
                } catch (t: Throwable) {
                    lastError = t
                    Log.w("GEMINI_RECIPE", "Gemini attempt ${attempt + 1} failed", t)

                    val shouldRetry = isServiceUnavailable(t) && attempt < maxAttempts - 1
                    if (!shouldRetry) {
                        break
                    }

                    val backoffMillis = 1000L * (attempt + 1)
                    onStatus?.invoke("Gemini is busy, retrying...")
                    Log.w(
                        "GEMINI_RECIPE",
                        "Gemini temporarily unavailable (attempt ${attempt + 1}/$maxAttempts). Retrying in ${backoffMillis}ms"
                    )
                    delay(backoffMillis)
                }
            }

            // Fallback to local Gemma if Gemini failed after retry attempts.
            //
            onStatus?.invoke("Gemini failed. Switching to local Gemma model...")
            val localFallback = runCatching { generateWithLocalGemma(prompt, onStatus) }
            localFallback.getOrNull()?.let { return@withContext it }

            Log.e(
                "GEMINI_RECIPE",
                "Gemini failed and local Gemma fallback failed.",
                localFallback.exceptionOrNull()
            )

            throw IllegalStateException(
                "Failed to generate recipe suggestions after retries.",
                lastError
            )
        }
    }

    // Generates recipes locally when the cloud service is unavailable.
    private suspend fun generateWithLocalGemma(
        prompt: String,
        onStatus: ((String) -> Unit)?
    ): RecipeSuggestionResult {
        val context = AppContextProvider.get()
            ?: error("Application context unavailable for local Gemma fallback")

        val gemmaManager = GemmaManager(context)
        try {
            onStatus?.invoke("Loading local Gemma model...")
            val init = gemmaManager.ensureInitialized(enableImage = false)
            init.getOrElse { throw it }

            onStatus?.invoke("Generating recipes with local Gemma...")
            val raw = gemmaManager.sendPrompt(prompt)
                .getOrElse { throw it }

            return parseRecipeJson(raw)
        } finally {
            gemmaManager.close()
        }
    }

    // Cleans and parses the JSON returned by the model.
    private fun parseRecipeJson(rawText: String): RecipeSuggestionResult {
        val cleanedJson = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return gson.fromJson(cleanedJson, RecipeSuggestionResult::class.java)
    }

    // Checks whether the error is a temporary service-side availability issue --> error 503.
    private fun isServiceUnavailable(throwable: Throwable): Boolean {
        val errorMessage = throwable.message.orEmpty()
        return errorMessage.contains("503") ||
            errorMessage.contains("UNAVAILABLE", ignoreCase = true)
    }
}
