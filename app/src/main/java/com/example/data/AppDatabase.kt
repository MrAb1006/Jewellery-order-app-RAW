package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Order::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jewellery_orders_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                try {
                    // Force the database to open and verify the schema early
                    instance.openHelper.writableDatabase
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Room connection verification failed. Deleting corrupted database to rebuild from scratch.", e)
                    try {
                        instance.close()
                    } catch (closeEx: Exception) {}
                    context.deleteDatabase("jewellery_orders_database")
                    
                    // Recreate cleanly from scratch
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "jewellery_orders_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                }
                
                INSTANCE = instance
                instance
            }
        }
    }
}
