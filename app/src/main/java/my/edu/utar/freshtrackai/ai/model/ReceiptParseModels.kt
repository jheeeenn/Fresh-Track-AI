package my.edu.utar.freshtrackai.ai.model

// result model for receipt parsing
// One grocery item extracted from a receipt image.
data class ReceiptItemDto(
    val name: String = "",
    val category: String? = null,
    val quantity: QuantityDto? = null,
    val expiry: ExpiryInfoDto? = null,
    val price: PriceDto? = null,
    val confidence: Float? = null
)

// Optional price information from the receipt line.
data class PriceDto(
    val amount: Double? = null,
    val currency: String? = null,
    val raw: String? = null
)

// Root result returned by receipt parsing.
data class ReceiptParseResult(
    val items: List<ReceiptItemDto> = emptyList()
)
