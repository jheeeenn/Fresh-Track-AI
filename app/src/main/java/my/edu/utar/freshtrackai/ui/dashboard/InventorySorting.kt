package my.edu.utar.freshtrackai.ui.dashboard

internal enum class InventorySortMode(val label: String) {
    Category("Category"),
    RecentlyAdded("Recently Added"),
    ExpirySoonest("Expiry Soonest"),
    NameAZ("Name A-Z")
}

internal fun sortInventory(
    items: List<InventoryItem>,
    mode: InventorySortMode
): List<InventoryItem> {
    return when (mode) {
        InventorySortMode.Category -> items
        InventorySortMode.RecentlyAdded -> items.sortedWith(
            compareByDescending<InventoryItem> { it.purchaseDateMillis }
                .thenBy { it.name.lowercase() }
        )
        InventorySortMode.ExpirySoonest -> items.sortedWith(
            compareBy<InventoryItem> { it.expiresInDays }
                .thenBy { it.name.lowercase() }
        )
        InventorySortMode.NameAZ -> items.sortedBy { it.name.lowercase() }
    }
}
