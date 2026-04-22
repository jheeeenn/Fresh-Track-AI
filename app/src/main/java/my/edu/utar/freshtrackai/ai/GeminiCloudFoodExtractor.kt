package my.edu.utar.freshtrackai.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult
import my.edu.utar.freshtrackai.ai.util.PromptFactory
import com.google.gson.Gson

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

                    val cleanedJson = text
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()

                    return@withContext gson.fromJson(cleanedJson, RecipeSuggestionResult::class.java)
                } catch (t: Throwable) {
                    lastError = t
                    val errorMessage = t.message.orEmpty()
                    val isServiceUnavailable =
                        errorMessage.contains("503") || errorMessage.contains("UNAVAILABLE", ignoreCase = true)

                    if (!isServiceUnavailable || attempt == maxAttempts - 1) {
                        break
                    }

                    val backoffMillis = (1000L * (attempt + 1))
                    Log.w(
                        "GEMINI_RECIPE",
                        "Gemini temporarily unavailable (attempt ${attempt + 1}/$maxAttempts). Retrying in ${backoffMillis}ms"
                    )
                    delay(backoffMillis)
                }
            }

            throw IllegalStateException(
                "Failed to generate recipe suggestions after retries.",
                lastError
            )
        }
    }
}
