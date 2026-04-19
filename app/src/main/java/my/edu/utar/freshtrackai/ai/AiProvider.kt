package my.edu.utar.freshtrackai.ai

import my.edu.utar.freshtrackai.BuildConfig

object AiProvider {
    val cloudFoodExtractor: CloudFoodExtractor by lazy {
        GeminiCloudFoodExtractor(BuildConfig.GEMINI_API_KEY)
    }
}