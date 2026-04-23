package my.edu.utar.freshtrackai.ai

import android.graphics.Bitmap
import my.edu.utar.freshtrackai.ai.model.FoodDetectionResult

/**
 * Defines the contract for food image detection.
 * Implementations analyze an image and return detected food items.
 */

internal interface FoodImageExtractor {
    // Detects visible food items from the provided image.
    suspend fun detectFood(bitmap: Bitmap): FoodDetectionResult
}