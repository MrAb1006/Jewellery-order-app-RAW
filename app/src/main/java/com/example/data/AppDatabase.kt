package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Order::class, DeletedOrder::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Safely verify if database file is corrupt or has schema mismatch.
                // We use allowMainThreadQueries() temporarily to perform startup verification
                // without crashing with Room's main-thread warning exception.
                var isCorruptOrIncompatible = false
                var tempDb: AppDatabase? = null
                try {
                    tempDb = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "jewellery_orders_database"
                    )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration(true)
                    .build()
                    
                    // Force the database to open and run schema verification
                    tempDb.openHelper.writableDatabase
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Database verification/migration failed. Deleting and recreating.", e)
                    isCorruptOrIncompatible = true
                } finally {
                    try {
                        tempDb?.close()
                    } catch (ex: Exception) {}
                }

                if (isCorruptOrIncompatible) {
                    try {
                        context.deleteDatabase("jewellery_orders_database")
                    } catch (ex: Exception) {
                        android.util.Log.e("AppDatabase", "Failed to delete database file", ex)
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jewellery_orders_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
