package my.edu.utar.freshtrackai.ai.model

// result model for receipt parsing
data class ReceiptItemDto(
    val name: String,
    val category: String? = null,
    val quantity: String? = null,
    val unit: String? = null,
    val confidence: Float? = null
)

data class ReceiptParseResult(
    val items: List<ReceiptItemDto> = emptyList()
)