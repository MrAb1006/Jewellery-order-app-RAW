package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Int): Flow<Order?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Int)

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()

    @Query("SELECT * FROM deleted_orders ORDER BY deletedAt DESC")
    fun getAllDeletedOrders(): Flow<List<DeletedOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedOrder(deletedOrder: DeletedOrder): Long

    @Query("DELETE FROM deleted_orders WHERE id = :id")
    suspend fun deleteDeletedOrderById(id: Int)

    @Query("DELETE FROM deleted_orders WHERE deletedAt < :cutoffTime")
    suspend fun deleteOldDeletedOrders(cutoffTime: Long)

    @Query("DELETE FROM deleted_orders")
    suspend fun deleteAllDeletedOrders()

    // Karigar Section
    @Query("SELECT * FROM karigars ORDER BY name ASC")
    fun getAllKarigars(): Flow<List<Karigar>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKarigar(karigar: Karigar): Long

    @Update
    suspend fun updateKarigar(karigar: Karigar)

    @Delete
    suspend fun deleteKarigar(karigar: Karigar)

    @Query("SELECT * FROM karigar_orders ORDER BY orderDate DESC")
    fun getAllKarigarOrders(): Flow<List<KarigarOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKarigarOrder(order: KarigarOrder): Long

    @Update
    suspend fun updateKarigarOrder(order: KarigarOrder)

    @Delete
    suspend fun deleteKarigarOrder(order: KarigarOrder)

    @Query("DELETE FROM karigar_orders WHERE id = :id")
    suspend fun deleteKarigarOrderById(id: Int)

    // Recycle Bin for Karigars
    @Query("SELECT * FROM deleted_karigar_orders ORDER BY deletedAt DESC")
    fun getAllDeletedKarigarOrders(): Flow<List<DeletedKarigarOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedKarigarOrder(order: DeletedKarigarOrder): Long

    @Query("DELETE FROM deleted_karigar_orders WHERE id = :id")
    suspend fun deleteDeletedKarigarOrderById(id: Int)

    @Query("DELETE FROM deleted_karigar_orders")
    suspend fun deleteAllDeletedKarigarOrders()

    @Query("DELETE FROM deleted_karigar_orders WHERE deletedAt < :cutoffTime")
    suspend fun deleteOldDeletedKarigarOrders(cutoffTime: Long)
}
