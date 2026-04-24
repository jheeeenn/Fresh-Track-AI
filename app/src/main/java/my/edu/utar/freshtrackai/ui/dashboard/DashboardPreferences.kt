package my.edu.utar.freshtrackai.ui.dashboard

import android.content.Context

internal object DashboardPreferencesStore {
    private const val PREFS_NAME = "dashboard_prefs"
    private const val KEY_INVENTORY_SORT_MODE = "inventory_sort_mode"

    fun loadInventorySortMode(context: Context): InventorySortMode {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_INVENTORY_SORT_MODE, InventorySortMode.Category.name)
            .orEmpty()
        return InventorySortMode.entries.firstOrNull { it.name == raw } ?: InventorySortMode.Category
    }

    fun saveInventorySortMode(context: Context, mode: InventorySortMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_INVENTORY_SORT_MODE, mode.name)
            .apply()
    }
}
