package my.edu.utar.freshtrackai.ai

internal enum class GeminiFailureKind {
    MissingKey,
    InvalidKey,
    QuotaExhausted,
    Busy,
    Unknown
}

internal fun resolveGeminiApiKey(primaryKey: String, fallbackKey: String): String {
    return primaryKey.ifBlank { fallbackKey }
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
