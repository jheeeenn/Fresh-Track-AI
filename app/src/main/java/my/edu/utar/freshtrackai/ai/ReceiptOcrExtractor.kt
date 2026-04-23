package my.edu.utar.freshtrackai.ai

import android.graphics.Bitmap
import my.edu.utar.freshtrackai.ai.model.ReceiptParseResult

/**
 * Defines the contract for receipt OCR processing.
 * Implementations parse a receipt image into structured receipt data.
 */
internal interface ReceiptOcrExtractor {
    // Parses a receipt image and returns detected receipt items.
    suspend fun parseReceipt(bitmap: Bitmap): ReceiptParseResult
}