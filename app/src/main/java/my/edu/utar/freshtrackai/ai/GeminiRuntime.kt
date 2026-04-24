package my.edu.utar.freshtrackai.ai

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import my.edu.utar.freshtrackai.ui.dashboard.DashboardPreferencesStore

internal enum class GeminiFailureKind {
    MissingKey,
    InvalidKey,
    QuotaExhausted,
    Busy,
    Unknown
}

internal enum class GeminiApiKeyValidationResult {
    NotSet,
    Valid,
    InvalidKey,
    QuotaExhausted,
    RequestFailed
}

internal fun resolveConfiguredGeminiApiKey(savedKey: String): String {
    return savedKey.trim()
}

internal fun loadConfiguredGeminiApiKey(context: Context?): String {
    val savedKey = context?.let(DashboardPreferencesStore::loadGeminiApiKey).orEmpty()
    return resolveConfiguredGeminiApiKey(savedKey)
}

internal suspend fun validateGeminiApiKey(
    apiKey: String,
    validationCall: suspend (String, String) -> String = { key, prompt ->
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = key
        ).generateContent(prompt).text.orEmpty().trim()
    }
): GeminiApiKeyValidationResult {
    val normalizedKey = resolveConfiguredGeminiApiKey(apiKey)
    if (normalizedKey.isBlank()) {
        return GeminiApiKeyValidationResult.NotSet
    }

    return runCatching {
        validationCall(
            normalizedKey,
            "Reply with OK only."
        )
    }.fold(
        onSuccess = { response ->
            if (response.isNotBlank()) {
                GeminiApiKeyValidationResult.Valid
            } else {
                GeminiApiKeyValidationResult.RequestFailed
            }
        },
        onFailure = { throwable ->
            when (classifyGeminiFailure(throwable)) {
                GeminiFailureKind.InvalidKey -> GeminiApiKeyValidationResult.InvalidKey
                GeminiFailureKind.QuotaExhausted -> GeminiApiKeyValidationResult.QuotaExhausted
                GeminiFailureKind.MissingKey,
                GeminiFailureKind.Busy,
                GeminiFailureKind.Unknown -> GeminiApiKeyValidationResult.RequestFailed
            }
        }
    )
}

internal fun classifyGeminiFailure(throwable: Throwable): GeminiFailureKind {
    val message = throwable.message.orEmpty()
    return when {
        message.contains("not configured", ignoreCase = true) ->
            GeminiFailureKind.MissingKey

        message.contains("API key", ignoreCase = true) ||
            message.contains("API_KEY_INVALID", ignoreCase = true) ||
            message.contains("permission denied", ignoreCase = true) ||
            message.contains("authentication", ignoreCase = true) ->
            GeminiFailureKind.InvalidKey

        message.contains("429") ||
            message.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
            message.contains("quota", ignoreCase = true) ||
            message.contains("credits are depleted", ignoreCase = true) ->
            GeminiFailureKind.QuotaExhausted

        message.contains("503") ||
            message.contains("UNAVAILABLE", ignoreCase = true) ||
            message.contains("busy", ignoreCase = true) ->
            GeminiFailureKind.Busy

        else -> GeminiFailureKind.Unknown
    }
}

internal fun fallbackStatusMessage(failureKind: GeminiFailureKind): String {
    return when (failureKind) {
        GeminiFailureKind.MissingKey ->
            "Gemini is not configured. Switching to local Gemma model..."

        GeminiFailureKind.InvalidKey ->
            "Gemini authentication failed. Switching to local Gemma model..."

        GeminiFailureKind.QuotaExhausted ->
            "Gemini unavailable or quota exhausted. Switching to local Gemma model..."

        GeminiFailureKind.Busy ->
            "Gemini is busy. Switching to local Gemma model..."

        GeminiFailureKind.Unknown ->
            "Gemini failed. Switching to local Gemma model..."
    }
}
