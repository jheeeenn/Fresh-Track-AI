package my.edu.utar.freshtrackai.ai

import android.graphics.Bitmap
import my.edu.utar.freshtrackai.ai.model.ReceiptParseResult

internal interface ReceiptOcrExtractor {
    suspend fun parseReceipt(bitmap: Bitmap): ReceiptParseResult
}