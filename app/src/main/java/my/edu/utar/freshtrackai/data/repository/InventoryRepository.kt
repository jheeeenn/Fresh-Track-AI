package my.edu.utar.freshtrackai.data.repository

import kotlinx.coroutines.flow.Flow
import my.edu.utar.freshtrackai.data.local.dao.InventoryDao
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem

class InventoryRepository(private val inventoryDao: InventoryDao) {

    val allItems: Flow<List<InventoryItem>> = inventoryDao.getAllItems()

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
}
