package my.edu.utar.freshtrackai.ui.dashboard

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val placeholderQuantityTokens = setOf(
    "",
    "detected item",
    "detected",
    "item",
    "unknown",
    "not specified",
    "n/a",
    "na",
    "null"
)

internal fun normalizeScannedQuantityLabel(raw: String?, value: Double?, unit: String?): String {
    val parsedRaw = raw?.trim().orEmpty()
    val parsedUnit = unit?.trim().orEmpty()
    val parsedValue = value

    if (parsedRaw.isNotBlank() && !isPlaceholderQuantityText(parsedRaw)) {
        return parsedRaw
    }

    val safeUnit = parsedUnit.takeUnless(::isPlaceholderQuantityText).orEmpty()
    val safeValue = parsedValue ?: 1.0

    return when {
        parsedValue != null && safeUnit.isNotBlank() -> "${formatQuantityNumber(parsedValue)} $safeUnit"
        parsedValue != null -> "${formatQuantityNumber(parsedValue)} ${defaultUnitLabel(parsedValue)}"
        safeUnit.isNotBlank() -> "1 $safeUnit"
        else -> "1 unit"
    }
}

internal fun categoryEmojiFor(category: InventoryCategory): String = when (category) {
    InventoryCategory.Dairy -> "\uD83E\uDD5B"
    InventoryCategory.Eggs -> "\uD83E\uDD5A"
    InventoryCategory.MeatPoultry -> "\uD83E\uDD69"
    InventoryCategory.Seafood -> "\uD83D\uDC1F"
    InventoryCategory.Fruits -> "\uD83C\uDF4E"
    InventoryCategory.Vegetables -> "\uD83E\uDD66"
    InventoryCategory.Bakery -> "\uD83C\uDF5E"
    InventoryCategory.GrainsPasta -> "\uD83C\uDF3E"
    InventoryCategory.CannedGoods -> "\uD83E\uDD6B"
    InventoryCategory.Frozen -> "\uD83E\uDDCA"
    InventoryCategory.Beverages -> "\uD83E\uDDC3"
    InventoryCategory.Condiments -> "\uD83E\uDDC2"
    InventoryCategory.Snacks -> "\uD83C\uDF6B"
    InventoryCategory.Leftovers -> "\uD83E\uDD61"
    InventoryCategory.Other -> "\uD83D\uDCE6"
}

internal fun formatStoredQuantityLabel(quantity: Double, unit: String): String {
    val normalizedUnit = unit.trim()
    val safeUnit = normalizedUnit.takeUnless(::isPlaceholderQuantityText)
        ?: defaultUnitLabel(quantity)

    return "${formatQuantityNumber(quantity)} $safeUnit"
}

internal fun formatAddedDateLabel(purchaseDateMillis: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(Date(purchaseDateMillis))
}

internal fun isPlaceholderQuantityText(value: String): Boolean {
    return value
        .trim()
        .lowercase()
        .let { normalized -> normalized in placeholderQuantityTokens }
}

private fun formatQuantityNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

private fun defaultUnitLabel(value: Double): String {
    return if (value == 1.0) "unit" else "units"
}
