package my.edu.utar.freshtrackai.ai.model

data class FoodDetectionResult(
    val items: List<FoodItemDto> = emptyList()
)

data class FoodItemDto(
    val name: String = "",
    val category: String? = null,
    val quantity: QuantityDto? = null,
    val expiry: ExpiryInfoDto? = null,
    val confidence: Double? = null
)

/**
 * Common quantity structure used by image and receipt OCR.
 */
data class QuantityDto(
    val value: Double? = null,
    val unit: String? = null,
    val raw: String? = null
)

/**
 * Optional expiry metadata extracted from OCR or estimated by the model.
 */
data class ExpiryInfoDto(
    val date: String? = null,
    val dateFormat: String? = null,
    val isEstimated: Boolean? = null,
    val estimatedShelfLifeDays: Int? = null,
    val confidence: Double? = null
)
