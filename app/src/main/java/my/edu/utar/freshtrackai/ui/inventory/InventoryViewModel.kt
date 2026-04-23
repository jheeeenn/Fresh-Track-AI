package my.edu.utar.freshtrackai.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem
import my.edu.utar.freshtrackai.data.local.entity.ShoppingItemEntity
import my.edu.utar.freshtrackai.data.repository.InventoryRepository
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import my.edu.utar.freshtrackai.data.local.AppDatabase
import android.content.Context

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    // Dashboard retrieval logic: Getting all items
    val allItems: StateFlow<List<InventoryItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Retrieving expiring-soon items explicitly for dashboard alerts
    val expiringItems: StateFlow<List<InventoryItem>> = repository.getItemsByExpiryStatus("EXPIRING_SOON")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = repository.allShoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Add missing item logic
    fun addMissingItem(
        name: String,
        category: String,
        quantity: Double,
        unit: String,
        expiryDate: Long,
        notes: String = ""
    ) {
        val newItem = InventoryItem(
            name = name,
            category = category,
            quantity = quantity,
            unit = unit,
            purchaseDate = System.currentTimeMillis(),
            expiryDate = expiryDate,
            expiryStatus = calculateExpiryStatus(expiryDate),
            notes = notes
        )
        viewModelScope.launch {
            repository.insertItem(newItem)
        }
    }

    // Manual confirm and edit logic
    fun confirmAndEditItem(item: InventoryItem, newQuantity: Double, newExpiryDate: Long, newNotes: String) {
        val updatedItem = item.copy(
            quantity = newQuantity,
            expiryDate = newExpiryDate,
            expiryStatus = calculateExpiryStatus(newExpiryDate),
            notes = newNotes
        )
        viewModelScope.launch {
            repository.updateItem(updatedItem)
        }
    }
    
    fun insertItem(item: InventoryItem) {
        viewModelScope.launch { repository.insertItem(item) }
    }

    // Item delete flow
    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    // Simple helper to calculate expiry status (e.g., Expiry in next 3 days = EXPIRING_SOON)
    private fun calculateExpiryStatus(expiryDate: Long): String {
        val currentTime = System.currentTimeMillis()
        val threeDaysInMillis = 3L * 24 * 60 * 60 * 1000
        
        return when {
            expiryDate <= currentTime -> "EXPIRED"
            expiryDate - currentTime <= threeDaysInMillis -> "EXPIRING_SOON"
            else -> "FRESH"
        }
    }

    fun deleteItemById(itemId: Long) {
        viewModelScope.launch {
            val item = repository.getItemById(itemId).firstOrNull()
            if (item != null) {
                repository.deleteItem(item)
            }
        }
    }

    fun insertBulk(items: List<InventoryItem>) {
        viewModelScope.launch {
            items.forEach { repository.insertItem(it) }
        }
    }

    fun addShoppingItem(name: String, sourceRecipeId: String? = null, sourceRecipeName: String? = null) {
        viewModelScope.launch {
            repository.insertShoppingItem(ShoppingItemEntity(name = name, sourceRecipeId = sourceRecipeId, sourceRecipeName = sourceRecipeName))
        }
    }
    
    fun toggleShoppingItem(item: ShoppingItemEntity, checked: Boolean) {
        viewModelScope.launch { repository.updateShoppingItem(item.copy(checked = checked)) }
    }

    fun clearPurchasedShoppingItems() {
        viewModelScope.launch { repository.clearPurchasedShoppingItems() }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
                val db = AppDatabase.getDatabase(context)
                @Suppress("UNCHECKED_CAST")
                return InventoryViewModel(
                    InventoryRepository(db.inventoryDao(), db.shoppingDao())
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
