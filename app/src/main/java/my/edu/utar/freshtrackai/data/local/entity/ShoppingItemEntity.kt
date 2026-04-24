package my.edu.utar.freshtrackai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val itemId: Long = 0,
    val name: String,
    val normalizedName: String,
    val sourceRecipeId: String? = null,
    val sourceRecipeName: String? = null,
    val quantityCount: Int = 1,
    val checked: Boolean = false
)
