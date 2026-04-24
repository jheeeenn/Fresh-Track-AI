package my.edu.utar.freshtrackai.ai

import android.content.Context

/**
 * Provides a shared food image extractor instance.
 * The extractor uses the application context to avoid activity leaks.
 */

internal object FoodExtractorProvider {

    @Volatile
    private var extractor: FoodImageExtractor? = null

    // Returns the existing extractor or creates it on first use.
    fun get(context: Context): FoodImageExtractor {
        return extractor ?: synchronized(this) {
            extractor ?: GemmaFoodImageExtractor(context.applicationContext).also {
                extractor = it
            }
        }
    }
}