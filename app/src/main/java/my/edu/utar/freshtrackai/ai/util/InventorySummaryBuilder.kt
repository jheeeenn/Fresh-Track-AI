package my.edu.utar.freshtrackai.ai.util

internal object InventorySummaryBuilder {

    /**
     * Builds a plain-text inventory summary from a list of item names.
     * Used as context when prompting the AI for recipe suggestions.
     *
     * Example output:
     * "- Chicken Breast\n- Baby Spinach\n- Whole Milk"
     */
    fun fromNames(itemNames: List<String>): String {
        if (itemNames.isEmpty()) return "No items in inventory."
        return itemNames.joinToString(separator = "\n") { "- $it" }
    }
}