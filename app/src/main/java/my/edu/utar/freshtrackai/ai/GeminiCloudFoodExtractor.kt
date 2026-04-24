package my.edu.utar.freshtrackai.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import my.edu.utar.freshtrackai.ai.model.RecipeSuggestionResult


/**
 * Uses Gemini to generate recipe suggestions from the current inventory.
 * If Gemini is temporarily unavailable, it retries and can fall back to local Gemma.
 */

class GeminiCloudFoodExtractor(
    private val apiKey: String,
    private val generateContentBlock: suspend GeminiCloudFoodExtractor.(String) -> String = { promptText ->
        generativeModel.generateContent(promptText).text.orEmpty().trim()
    },
    private val localFallbackBlock:
    suspend GeminiCloudFoodExtractor.(String, ((String) -> Unit)?) -> RecipeSuggestionResult = { promptText, onStatus ->
        generateWithLocalGemma(promptText, onStatus)
    }
) : CloudFoodExtractor {

    private val gson = Gson()

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    }

    override suspend fun suggestRecipes(
        promptText: String,
        onStatus: ((String) -> Unit)?
    ): RecipeSuggestionResult {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext fallbackToLocalGemma(
                    promptText = promptText,
                    failureKind = GeminiFailureKind.MissingKey,
                    onStatus = onStatus,
                    cause = null
                )
            }

            val maxAttempts = 3
            var lastError: Throwable? = null
            var failureKind = GeminiFailureKind.Unknown

            for (attempt in 0 until maxAttempts) {
                try {
                    Log.d("GEMINI_RECIPE", "Calling Gemini attempt ${attempt + 1}/$maxAttempts")
                    onStatus?.invoke("Generating recipes with Gemini (${attempt + 1}/$maxAttempts)...")
                    val text = generateContentBlock(promptText)

                    if (text.isBlank()) {
                        error("Gemini returned an empty response")
                    }

                    Log.d("GEMINI_RECIPE", "Gemini recipe generation succeeded")
                    return@withContext parseRecipeJson(text, source = "Gemini")
                } catch (t: Throwable) {
                    lastError = t
                    failureKind = classifyGeminiFailure(t)
                    Log.w("GEMINI_RECIPE", "Gemini attempt ${attempt + 1} failed", t)

                    val shouldRetry = failureKind == GeminiFailureKind.Busy && attempt < maxAttempts - 1
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

            return@withContext fallbackToLocalGemma(
                promptText = promptText,
                failureKind = failureKind,
                onStatus = onStatus,
                cause = lastError
            )
        }
    }

    private suspend fun fallbackToLocalGemma(
        promptText: String,
        failureKind: GeminiFailureKind,
        onStatus: ((String) -> Unit)?,
        cause: Throwable?
    ): RecipeSuggestionResult {
        onStatus?.invoke(fallbackStatusMessage(failureKind))
        val localFallback = runCatching { localFallbackBlock(promptText, onStatus) }
        localFallback.getOrNull()?.let { return it }

        val fallbackError = localFallback.exceptionOrNull() ?: cause
        Log.e(
            "GEMINI_RECIPE",
            "Gemini path failed with $failureKind and local Gemma fallback failed.",
            fallbackError
        )

        val message = when (failureKind) {
            GeminiFailureKind.MissingKey ->
                "Gemini is not configured and local Gemma fallback failed."

            GeminiFailureKind.InvalidKey ->
                "Gemini authentication failed and local Gemma fallback failed."

            GeminiFailureKind.QuotaExhausted ->
                "Gemini quota is exhausted and local Gemma fallback failed."

            GeminiFailureKind.Busy ->
                "Gemini remained unavailable and local Gemma fallback failed."

            GeminiFailureKind.Unknown ->
                "Gemini failed and local Gemma fallback failed."
        }

        throw IllegalStateException(message, fallbackError)
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

            return parseRecipeJson(raw, source = "local Gemma")
        } finally {
            gemmaManager.close()
        }
    }

    // Cleans and parses the JSON returned by the model.
    private fun parseRecipeJson(rawText: String, source: String): RecipeSuggestionResult {
        val extractedJson = extractFirstJsonObject(rawText, source)
        val jsonElement = try {
            JsonParser.parseString(extractedJson)
        } catch (e: Exception) {
            throw IllegalStateException("$source returned malformed recipe JSON.", e)
        }

        if (!jsonElement.isJsonObject) {
            throw IllegalStateException("$source returned malformed recipe JSON.")
        }

        val jsonObject = jsonElement.asJsonObject
        if (!jsonObject.has("recipes") || !jsonObject.get("recipes").isJsonArray) {
            throw IllegalStateException("$source returned malformed recipe JSON.")
        }

        return try {
            gson.fromJson(extractedJson, RecipeSuggestionResult::class.java)
        } catch (e: Exception) {
            throw IllegalStateException("$source returned malformed recipe JSON.", e)
        }
    }

    private fun extractFirstJsonObject(rawText: String, source: String): String {
        val cleanedText = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        var startIndex = -1
        var depth = 0
        var inString = false
        var escaped = false

        cleanedText.forEachIndexed { index, char ->
            if (startIndex == -1) {
                if (char == '{') {
                    startIndex = index
                    depth = 1
                }
                return@forEachIndexed
            }

            if (escaped) {
                escaped = false
                return@forEachIndexed
            }

            if (char == '\\' && inString) {
                escaped = true
                return@forEachIndexed
            }

            if (char == '"') {
                inString = !inString
                return@forEachIndexed
            }

            if (!inString) {
                when (char) {
                    '{' -> depth += 1
                    '}' -> {
                        depth -= 1
                        if (depth == 0) {
                            return cleanedText.substring(startIndex, index + 1)
                        }
                    }
                }
            }
        }

        throw IllegalStateException("$source returned malformed recipe JSON.")
    }

}
