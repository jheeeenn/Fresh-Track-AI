package my.edu.utar.freshtrackai.ai

import android.content.Context

internal object ReceiptOcrProvider {

    @Volatile
    private var extractor: ReceiptOcrExtractor? = null

    fun get(context: Context): ReceiptOcrExtractor {
        return extractor ?: synchronized(this) {
            extractor ?: GemmaReceiptOcrExtractor(context.applicationContext).also {
                extractor = it
            }
        }
    }
}