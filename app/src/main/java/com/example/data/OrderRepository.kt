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

    // Karigar Section
    val allKarigars: Flow<List<Karigar>> = orderDao.getAllKarigars()
    suspend fun insertKarigar(karigar: Karigar) = orderDao.insertKarigar(karigar)
    suspend fun updateKarigar(karigar: Karigar) = orderDao.updateKarigar(karigar)
    suspend fun deleteKarigar(karigar: Karigar) = orderDao.deleteKarigar(karigar)

    val allKarigarOrders: Flow<List<KarigarOrder>> = orderDao.getAllKarigarOrders()
    suspend fun insertKarigarOrder(order: KarigarOrder) = orderDao.insertKarigarOrder(order)
    suspend fun updateKarigarOrder(order: KarigarOrder) = orderDao.updateKarigarOrder(order)
    suspend fun deleteKarigarOrder(order: KarigarOrder) = orderDao.deleteKarigarOrder(order)
    suspend fun deleteKarigarOrderById(id: Int) = orderDao.deleteKarigarOrderById(id)

    // Deleted Karigars
    val allDeletedKarigarOrders: Flow<List<DeletedKarigarOrder>> = orderDao.getAllDeletedKarigarOrders()
    suspend fun insertDeletedKarigar(order: DeletedKarigarOrder) = orderDao.insertDeletedKarigarOrder(order)
    suspend fun deleteDeletedKarigarById(id: Int) = orderDao.deleteDeletedKarigarOrderById(id)
    suspend fun deleteAllDeletedKarigars() = orderDao.deleteAllDeletedKarigarOrders()
    suspend fun deleteOldDeletedKarigars(cutoffTime: Long) = orderDao.deleteOldDeletedKarigarOrders(cutoffTime)
}
