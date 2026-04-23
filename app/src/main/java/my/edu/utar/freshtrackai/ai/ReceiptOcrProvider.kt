package my.edu.utar.freshtrackai.ai

import android.content.Context

/**
 * Provides a shared receipt OCR extractor instance.
 * The current implementation uses the local Gemma receipt parser.
 */

internal object ReceiptOcrProvider {

    @Volatile
    private var extractor: ReceiptOcrExtractor? = null

    // Returns the existing extractor or creates it on first use.
    fun get(context: Context): ReceiptOcrExtractor {
        return extractor ?: synchronized(this) {
            extractor ?: GemmaReceiptOcrExtractor(context.applicationContext).also {
                extractor = it
            }
        }
    }
}