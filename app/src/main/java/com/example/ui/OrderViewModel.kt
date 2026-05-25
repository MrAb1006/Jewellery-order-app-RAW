package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Order
import com.example.data.OrderRepository
import com.example.data.getItems
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                repository.allOrders.first().let { currentOrders ->
                    if (currentOrders.isEmpty()) {
                        seedInitialData()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("OrderViewModel", "Failed to retrieve or seed initial database data", e)
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
            repository.delete(order)
        }
    }

    fun deleteOrderById(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.allOrders.firstOrNull()?.find { it.id == orderId }?.let { order ->
                repository.update(order.copy(status = newStatus))
            }
        }
    }

    private suspend fun seedInitialData() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        val order1 = Order(
            customerName = "Samantha Reed",
            customerPhone = "+1 555-8932",
            jewelleryType = "Gold Bridal Necklace",
            metalType = "Gold",
            purity = "91.6",
            approxWeight = 42.5,
            agreedRate = 72.0,
            makingCharges = 12.0, // 12% making charges
            otherCharges = 120.0,
            advancePaid = 1500.0,
            totalAmount = (42.5 * 72.0 * (91.6 / 100.0)) * (1 + 12.0 / 100.0) + 120.0, // 3259.32
            orderDate = now - (3 * 24 * 3600 * 1000L), // 3 days ago
            expectedDeliveryDate = now + (10 * 24 * 3600 * 1000L), // 10 days from now
            notes = "Engagement necklace. Traditional paisley design with central ruby stone. Smooth high polish backing.",
            status = "In Progress"
        )

        val order2 = Order(
            customerName = "Michael Chen",
            customerPhone = "+1 555-0149",
            jewelleryType = "Diamond Wedding Ring",
            metalType = "Platinum",
            purity = "95.0",
            approxWeight = 6.2,
            agreedRate = 95.0,
            makingCharges = 15.0, // 15% making charges
            otherCharges = 850.0, // Solitaire diamond
            advancePaid = 500.0,
            totalAmount = (6.2 * 95.0 * (95.0 / 100.0)) * (1 + 15.0 / 100.0) + 850.0, // 1493.48
            orderDate = now - (1 * 24 * 3600 * 1000L), // 1 day ago
            expectedDeliveryDate = now + (5 * 24 * 3600 * 1000L), // 5 days from now
            notes = "Prong setting for central solitaire diamond 0.5ct. Inscribe inside band: 'M & S - Eternal'. Ring size: 6.5.",
            status = "Pending"
        )

        val order3 = Order(
            customerName = "Sophia Martinez",
            customerPhone = "+1 555-4421",
            jewelleryType = "Teardrop Emerald Earrings",
            metalType = "Rose Gold",
            purity = "75.0",
            approxWeight = 12.8,
            agreedRate = 60.0,
            makingCharges = 10.0, // 10% making charges
            otherCharges = 500.0, // Genuine Brazilian emeralds
            advancePaid = 1000.0,
            totalAmount = (12.8 * 60.0 * (75.0 / 100.0)) * (1 + 10.0 / 100.0) + 500.0, // 1133.6
            orderDate = now - (5 * 24 * 3600 * 1000L), // 5 days ago
            expectedDeliveryDate = now + (1 * 24 * 3600 * 1000L), // tomorrow (1 day from now, showing due soon/ready)
            notes = "Matched pair. Hanging teardrop emeralds with micro pave diamonds surround. Comfort screw backing.",
            status = "Completed"
        )

        val order4 = Order(
            customerName = "Robert Taylor",
            customerPhone = "+1 555-9012",
            jewelleryType = "Heavy Gold Kada",
            metalType = "Gold",
            purity = "100.0",
            approxWeight = 55.0,
            agreedRate = 75.0,
            makingCharges = 8.0, // 8% making charges
            otherCharges = 0.0,
            advancePaid = 2000.0,
            totalAmount = (55.0 * 75.0 * (100.0 / 100.0)) * (1 + 8.0 / 100.0), // 4455.0
            orderDate = now - (14 * 24 * 3600 * 1000L), // 14 days ago
            expectedDeliveryDate = now - (2 * 24 * 3600 * 1000L), // delivered 2 days ago
            notes = "Solid gold custom hand carved floral motif. Internal engraving ID '999 AU'. Weight exactly 55.0 grams.",
            status = "Delivered"
        )

        repository.insert(order1)
        repository.insert(order2)
        repository.insert(order3)
        repository.insert(order4)
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
