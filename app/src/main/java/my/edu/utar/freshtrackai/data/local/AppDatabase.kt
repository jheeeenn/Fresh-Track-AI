package my.edu.utar.freshtrackai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import my.edu.utar.freshtrackai.data.local.dao.InventoryDao
import my.edu.utar.freshtrackai.data.local.dao.ShoppingDao
import my.edu.utar.freshtrackai.data.local.entity.InventoryItem
import my.edu.utar.freshtrackai.data.local.entity.ShoppingItemEntity

@Database(entities = [InventoryItem::class, ShoppingItemEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao
    abstract fun shoppingDao(): ShoppingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "freshtrack_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
