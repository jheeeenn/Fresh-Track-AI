package my.edu.utar.freshtrackai.ai

import android.content.Context

internal object FoodExtractorProvider {

    @Volatile
    private var extractor: FoodImageExtractor? = null

    fun get(context: Context): FoodImageExtractor {
        return extractor ?: synchronized(this) {
            extractor ?: GemmaFoodImageExtractor(context.applicationContext).also {
                extractor = it
            }
        }
    }
}