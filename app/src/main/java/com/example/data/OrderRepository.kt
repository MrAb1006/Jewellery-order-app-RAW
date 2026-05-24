package com.example.data

import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    fun getOrderById(id: Int): Flow<Order?> = orderDao.getOrderById(id)

    suspend fun insert(order: Order): Long = orderDao.insertOrder(order)

    suspend fun update(order: Order) = orderDao.updateOrder(order)

    suspend fun delete(order: Order) = orderDao.deleteOrder(order)

    suspend fun deleteById(id: Int) = orderDao.deleteOrderById(id)
}
