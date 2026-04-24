package my.edu.utar.freshtrackai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("SELECT * FROM inventory_items ORDER BY expiryDate ASC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE itemId = :id LIMIT 1")
    fun getItemById(id: Long): Flow<InventoryItem?>

    @Query("SELECT * FROM inventory_items WHERE expiryStatus = :status ORDER BY expiryDate ASC")
    fun getItemsByExpiryStatus(status: String): Flow<List<InventoryItem>>
    
    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%'")
    fun searchItemsByName(query: String): Flow<List<InventoryItem>>
}
