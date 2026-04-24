package my.edu.utar.freshtrackai.ai

/**
 * Provides shared AI-related service instances.
 * This currently exposes the Gemini-based cloud recipe extractor.
 */

object AiProvider {
    fun cloudFoodExtractor(): CloudFoodExtractor {
        return GeminiCloudFoodExtractor(
            loadConfiguredGeminiApiKey(AppContextProvider.get())
        )
    }
}
