package my.edu.utar.freshtrackai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val name: String,
    val category: String = "General",
    val quantity: Double,
    val unit: String = "pcs",
    val purchaseDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = 0,
    val expiryStatus: String = "FRESH", // Options: FRESH, EXPIRING_SOON, EXPIRED
    val notes: String = ""
)
