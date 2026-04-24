package my.edu.utar.freshtrackai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import my.edu.utar.freshtrackai.data.local.entity.ShoppingItemEntity

@Dao
interface ShoppingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("SELECT * FROM shopping_items")
    fun getAllShoppingItems(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun getItemByNormalizedName(normalizedName: String): ShoppingItemEntity?

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun clearPurchasedItems()
}
