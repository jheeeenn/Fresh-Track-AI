package my.edu.utar.freshtrackai.ai.model

data class FoodDetectionResult(
    val items: List<FoodItemDto>
)

data class FoodItemDto(
    val name: String,
    val category: String?,
    val confidence: Double?
)