package my.edu.utar.freshtrackai.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "category")
    val category: String?,

    @ColumnInfo(name = "quantity")
    val quantity: String?,

    @ColumnInfo(name = "source_type")
    val sourceType: String?,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long,

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long,

    @ColumnInfo(name = "nutrition_info")
    val nutritionInfo: String?,

    @ColumnInfo(name = "is_near_expiry")
    val isNearExpiry: Boolean,

    @ColumnInfo(name = "notes")
    val notes: String?
)