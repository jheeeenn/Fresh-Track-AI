package my.edu.utar.freshtrackai.ai.model

// result model for receipt parsing
data class ReceiptItemDto(
    val name: String = "",
    val category: String? = null,
    val quantity: QuantityDto? = null,
    val expiry: ExpiryInfoDto? = null,
    val price: PriceDto? = null,
    val confidence: Float? = null
)

data class PriceDto(
    val amount: Double? = null,
    val currency: String? = null,
    val raw: String? = null
)

data class ReceiptParseResult(
    val items: List<ReceiptItemDto> = emptyList()
)
