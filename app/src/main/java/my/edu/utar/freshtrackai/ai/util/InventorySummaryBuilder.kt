package my.edu.utar.freshtrackai.ai.util

object InventorySummaryBuilder {

    fun fromNames(items: List<String>): String {
        return items
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(separator = ", ")
    }
}