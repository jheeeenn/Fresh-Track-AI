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

    suspend fun insertShoppingItem(item: ShoppingItemEntity) = shoppingDao.insertItem(item)
    suspend fun updateShoppingItem(item: ShoppingItemEntity) = shoppingDao.updateItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = shoppingDao.deleteItem(item)
    suspend fun clearPurchasedShoppingItems() = shoppingDao.clearPurchasedItems()
}
