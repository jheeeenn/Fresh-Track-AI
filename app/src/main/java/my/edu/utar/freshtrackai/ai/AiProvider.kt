package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.BuildConfig

/**
 * Provides shared AI-related service instances.
 * This currently exposes the Gemini-based cloud recipe extractor.
 */

object AiProvider {
    // Lazily creates the cloud extractor only when first needed.
    val cloudFoodExtractor: CloudFoodExtractor by lazy {
        GeminiCloudFoodExtractor(BuildConfig.GEMINI_API_KEY)
    }
}