package com.example.data

import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    fun getOrderById(id: Int): Flow<Order?> = orderDao.getOrderById(id)

    suspend fun insert(order: Order): Long = orderDao.insertOrder(order)

    suspend fun update(order: Order) = orderDao.updateOrder(order)

    suspend fun delete(order: Order) = orderDao.deleteOrder(order)

    suspend fun deleteById(id: Int) = orderDao.deleteOrderById(id)

    suspend fun deleteAll() = orderDao.deleteAllOrders()

    val allDeletedOrders: Flow<List<DeletedOrder>> = orderDao.getAllDeletedOrders()

    suspend fun insertDeleted(deletedOrder: DeletedOrder): Long = orderDao.insertDeletedOrder(deletedOrder)

    suspend fun deleteDeletedById(id: Int) = orderDao.deleteDeletedOrderById(id)

    suspend fun deleteOldDeleted(cutoffTime: Long) = orderDao.deleteOldDeletedOrders(cutoffTime)

    suspend fun deleteAllDeleted() = orderDao.deleteAllDeletedOrders()
}
