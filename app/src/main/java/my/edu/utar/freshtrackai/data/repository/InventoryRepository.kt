package my.edu.utar.freshtrackai.data.repository

import kotlinx.coroutines.flow.Flow
import my.edu.utar.freshtrackai.data.local.dao.InventoryDao
import my.edu.utar.freshtrackai.data.local.dao.ShoppingDao
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem
import my.edu.utar.freshtrackai.data.local.entity.ShoppingItemEntity

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val shoppingDao: ShoppingDao
) {
    private fun normalizeShoppingName(name: String): String = name.trim().lowercase()

    private fun mergeRecipeSource(
        existing: String?,
        incoming: String?
    ): String? {
        return when {
            incoming.isNullOrBlank() -> existing
            existing.isNullOrBlank() -> incoming
            existing.equals(incoming, ignoreCase = true) -> existing
            else -> "Multiple Recipes"
        }
    }

    val allItems: Flow<List<InventoryItem>> = inventoryDao.getAllItems()
    val allShoppingItems: Flow<List<ShoppingItemEntity>> = shoppingDao.getAllShoppingItems()

    fun getItemById(id: Long): Flow<InventoryItem?> {
        return inventoryDao.getItemById(id)
    }

    fun getItemsByExpiryStatus(status: String): Flow<List<InventoryItem>> {
        return inventoryDao.getItemsByExpiryStatus(status)
    }

    fun searchItemsByName(query: String): Flow<List<InventoryItem>> {
        return inventoryDao.searchItemsByName(query)
    }

    suspend fun insertItem(item: InventoryItem): Long {
        return inventoryDao.insertItem(item)
    }

    suspend fun updateItem(item: InventoryItem) {
        inventoryDao.updateItem(item)
    }

    suspend fun deleteItem(item: InventoryItem) {
        inventoryDao.deleteItem(item)
    }

    suspend fun addOrMergeShoppingItem(
        name: String,
        sourceRecipeId: String? = null,
        sourceRecipeName: String? = null
    ): Long {
        val normalizedName = normalizeShoppingName(name)
        val existing = shoppingDao.getItemByNormalizedName(normalizedName)

        return if (existing != null) {
            shoppingDao.updateItem(
                existing.copy(
                    quantityCount = existing.quantityCount + 1,
                    sourceRecipeId = existing.sourceRecipeId ?: sourceRecipeId,
                    sourceRecipeName = mergeRecipeSource(existing.sourceRecipeName, sourceRecipeName),
                    checked = false
                )
            )
            existing.itemId
        } else {
            shoppingDao.insertItem(
                ShoppingItemEntity(
                    name = name,
                    normalizedName = normalizedName,
                    sourceRecipeId = sourceRecipeId,
                    sourceRecipeName = sourceRecipeName
                )
            )
        }
    }

    suspend fun insertShoppingItem(item: ShoppingItemEntity) = shoppingDao.insertItem(item)
    suspend fun updateShoppingItem(item: ShoppingItemEntity) = shoppingDao.updateItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = shoppingDao.deleteItem(item)
    suspend fun clearPurchasedShoppingItems() = shoppingDao.clearPurchasedItems()
}
