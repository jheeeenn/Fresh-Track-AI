package my.edu.utar.freshtrackai.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FoodDao {
    @Insert
    fun insert(item: FoodItem)

    @Update
    fun update(item: FoodItem)

    @Delete
    fun delete(item: FoodItem)

    @Query("SELECT * FROM food_items ORDER BY expiry_date ASC")
    fun getAllItems(): List<FoodItem>

    @Query("SELECT * FROM food_items WHERE is_near_expiry = 1")
    fun getExpiringItems(): List<FoodItem>

    @Query("SELECT * FROM food_items WHERE category = :category")
    fun getItemsByCategory(category: String): List<FoodItem>

    @Query("SELECT * FROM food_items WHERE id = :itemId LIMIT 1")
    fun getItemById(itemId: Int): FoodItem?

    @Query("SELECT COUNT(*) FROM food_items")
    fun getItemCount(): Int

    @Query("DELETE FROM food_items")
    fun deleteAll()

    @Query("SELECT * FROM food_items WHERE name LIKE :searchQuery")
    fun searchItems(searchQuery: String): List<FoodItem>
}