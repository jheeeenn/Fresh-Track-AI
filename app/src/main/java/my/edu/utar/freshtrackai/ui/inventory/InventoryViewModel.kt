package my.edu.utar.freshtrackai.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem
import my.edu.utar.freshtrackai.data.repository.InventoryRepository

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    // Dashboard retrieval logic: Getting all items
    val allItems: StateFlow<List<InventoryItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Retrieving expiring-soon items explicitly for dashboard alerts
    val expiringItems: StateFlow<List<InventoryItem>> = repository.getItemsByExpiryStatus("EXPIRING_SOON")
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
}
