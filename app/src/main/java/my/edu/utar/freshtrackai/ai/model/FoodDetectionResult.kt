package my.edu.utar.freshtrackai.ai.model

// Root result returned by receipt parsing
data class FoodDetectionResult(
    val items: List<FoodItemDto> = emptyList()
)

// One detected food item from the scanned image
data class FoodItemDto(
    val name: String = "",
    val category: String? = null,
    val quantity: QuantityDto? = null,
    val expiry: ExpiryInfoDto? = null,
    val confidence: Double? = null
)

// Shared quantity structure for OCR and image detection
data class QuantityDto(
    val value: Double? = null,
    val unit: String? = null,
    val raw: String? = null
)

// Shared expiry information for OCR and image detection
data class ExpiryInfoDto(
    val date: String? = null,
    val dateFormat: String? = null,
    val isEstimated: Boolean? = null,
    val estimatedShelfLifeDays: Int? = null,
    val confidence: Double? = null
)
