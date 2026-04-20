package my.edu.utar.freshtrackai.ai

import android.graphics.Bitmap
import my.edu.utar.freshtrackai.ai.model.FoodDetectionResult

internal interface FoodImageExtractor {
    suspend fun detectFood(bitmap: Bitmap): FoodDetectionResult
}