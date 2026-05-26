package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Order
import com.example.data.DeletedOrder
import com.example.data.toDeletedOrder
import com.example.data.toOrder
import com.example.data.OrderRepository
import com.example.data.getItems
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    init {
        // App starts fresh for real-world launch; no automatic seeding.
        viewModelScope.launch {
            try {
                // Retention window layout of 60 days
                val sixtyDaysMs = 60L * 24L * 60L * 60L * 1000L
                val cutoffTime = System.currentTimeMillis() - sixtyDaysMs
                repository.deleteOldDeleted(cutoffTime)
            } catch (e: Exception) {
                android.util.Log.e("OrderViewModel", "Failed to clean old activity log entries on launch", e)
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("Newest") // Newest, Oldest, Delivery Soonest, Delivery Latest, Value Highest, Value Lowest
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    // Reactive list of orders matching current criteria
    val filteredOrders: StateFlow<List<Order>> = combine(
        repository.allOrders,
        _searchQuery,
        _statusFilter,
        _sortBy
    ) { orders, query, status, sort ->
        var result = orders

        // Apply Search Query
        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            result = result.filter {
                it.customerName.lowercase().contains(q) ||
                it.customerPhone.contains(q) ||
                it.jewelleryType.lowercase().contains(q) ||
                it.metalType.lowercase().contains(q) ||
                it.purity.lowercase().contains(q) ||
                it.notes.lowercase().contains(q)
            }
        }

        // Apply Status Filter
        if (status != "All") {
            result = result.filter { order ->
                order.status == status || order.getItems().any { item -> item.status == status }
            }
        }

        // Apply Sorting
        result = when (sort) {
            "Newest" -> result.sortedByDescending { it.orderDate }
            "Oldest" -> result.sortedBy { it.orderDate }
            "Delivery Soonest" -> result.sortedBy { it.expectedDeliveryDate }
            "Delivery Latest" -> result.sortedByDescending { it.expectedDeliveryDate }
            "Value Highest" -> result.sortedByDescending { it.totalAmount }
            "Value Lowest" -> result.sortedBy { it.totalAmount }
            else -> result.sortedByDescending { it.orderDate }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reactive business dashboard metrics
    val metrics: StateFlow<OrderMetrics> = repository.allOrders.map { orders ->
        var activeCount = 0
        var totalAmountVal = 0.0
        var totalAdvanceVal = 0.0
        var totalPendingBalanceVal = 0.0
        var dueSoonCount = 0

        val now = System.currentTimeMillis()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

        orders.forEach { order ->
            if (order.status != "Delivered") {
                activeCount++
                val balance = order.totalAmount - order.advancePaid
                totalPendingBalanceVal += if (balance > 0) balance else 0.0

                if (order.expectedDeliveryDate - now in 0..threeDaysMs) {
                    dueSoonCount++
                }
            }
            totalAmountVal += order.totalAmount
            totalAdvanceVal += order.advancePaid
        }

        OrderMetrics(
            activeOrders = activeCount,
            totalAdvanceCollected = totalAdvanceVal,
            pendingCollection = totalPendingBalanceVal,
            dueSoonOrders = dueSoonCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OrderMetrics()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setSortBy(sortOption: String) {
        _sortBy.value = sortOption
    }

    fun addOrder(order: Order) {
        viewModelScope.launch {
            repository.insert(order)
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            repository.update(order)
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            try {
                repository.insertDeleted(order.toDeletedOrder())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.delete(order)
        }
    }

    fun deleteOrderById(id: Int) {
        viewModelScope.launch {
            try {
                repository.allOrders.first().find { it.id == id }?.let { order ->
                    repository.insertDeleted(order.toDeletedOrder())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository.deleteById(id)
        }
    }

    // Retained Activity Log of Deleted Orders
    val deletedOrders: StateFlow<List<DeletedOrder>> = repository.allDeletedOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun restoreDeletedOrder(deletedOrder: DeletedOrder) {
        viewModelScope.launch {
            try {
                repository.insert(deletedOrder.toOrder())
                repository.deleteDeletedById(deletedOrder.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun permanentlyDeleteDeletedOrder(deletedOrderId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteDeletedById(deletedOrderId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllDeletedOrders() {
        viewModelScope.launch {
            try {
                repository.deleteAllDeleted()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.allOrders.firstOrNull()?.find { it.id == orderId }?.let { order ->
                repository.update(order.copy(status = newStatus))
            }
        }
    }
}

data class OrderMetrics(
    val activeOrders: Int = 0,
    val totalAdvanceCollected: Double = 0.0,
    val pendingCollection: Double = 0.0,
    val dueSoonOrders: Int = 0
)

class OrderViewModelFactory(private val repository: OrderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
