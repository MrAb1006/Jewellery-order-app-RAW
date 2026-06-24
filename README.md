# Lumina Atelier / Aurelia Jewel Vault - Technical Replication Blueprint

Welcome to the **Aurelia Jewel Vault** (Lumina Atelier) system specification and reproduction guide. This document contains the entire blueprint, dependencies, database schema, state management architecture, color scheme configurations, and detailed layout specifications of this premium, offline-first custom jewelry order tracking system.

You can paste this entire blueprint file directly into Cursor, Bolt.new, v0, Replit, or other AI builders to instantly recreate this application with the exact same functionality, database layer, and highly-polished **Sophisticated Dark** premium interface with dynamic gold accents.

---

## 1. Directory Structure Blueprint

Ensure your target project is structured as follows:

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── Order.kt
│   │   ├── OrderDao.kt
│   │   ├── AppDatabase.kt
│   │   └── OrderRepository.kt
│   └── ui/
│       ├── DashboardScreen.kt
│       ├── OrderViewModel.kt
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           ├── Type.kt
│           └── Shape.kt
└── res/
    └── values/
        └── strings.xml
```

---

## 2. Dependencies & Build Configuration (`build.gradle.kts`)

Make sure your `libs.versions.toml` or module-level `build.gradle.kts` has the following dependencies enabled:

### Gradle Configurations
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp) // Used for Room compilation
}

android {
    namespace = "com.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aistudio.jewelleryorders"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // Room Database Architecture
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Jetpack Compose & Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended) // Extra luxury icons
    implementation(libs.androidx.lifecycle.runtime.compose) // collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
```

---

## 3. The Core Data Layer (Room Offline DB)

This application utilizes a robust SQLite relational layer managed by Room, which executes all database updates on IO threads via Kotlin Flow data streams.

### `Order.kt` (State Model Definition)
Create this model in `com.example.data` to represent custom luxury orders:
```kotlin
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val jewelleryType: String, // Ring, Necklace, Earrings, Bracelet, Bangle, Custom, etc.
    val metalType: String, // Gold, Silver, Platinum, Rose Gold
    val purity: String, // 22K, 18K, 24K, 92.5%, Pt950
    val approxWeight: Double, // in grams
    val agreedRate: Double, // rate per gram
    val makingCharges: Double, // manufacturing, design labour charges
    val otherCharges: Double, // gems, diamonds, other accessory charges
    val advancePaid: Double, // deposit given in advance
    val totalAmount: Double, // total calculated billing estimate
    val orderDate: Long = System.currentTimeMillis(),
    val expectedDeliveryDate: Long,
    val notes: String = "",
    val status: String = "Pending" // Pending, In Progress, Completed, Delivered
)
```

### `OrderDao.kt` (Data Access Interface)
```kotlin
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
}
```

### `AppDatabase.kt` (Local Database Engine)
```kotlin
package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Order::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jewellery_orders_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### `OrderRepository.kt` (Data Scoping Layer)
```kotlin
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
```

---

## 4. State Management Layer

This is the stateful bridge controlling dynamic metrics analysis, filters, real-time query aggregation, and automated database seeding on first startup.

### `OrderViewModel.kt`
```kotlin
package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Order
import com.example.data.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.allOrders.first().let { currentOrders ->
                if (currentOrders.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("Newest") // Newest, Oldest, Delivery Soonest, Delivery Latest, Value Highest, Value Lowest
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    // Combined Flow: reactive order filtering and sorting
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
            result = result.filter { it.status == status }
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

    // Real-Time Analytics Dashboard States
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

    fun addOrder(order: Order) = viewModelScope.launch { repository.insert(order) }
    fun updateOrder(order: Order) = viewModelScope.launch { repository.update(order) }
    fun deleteOrder(order: Order) = viewModelScope.launch { repository.delete(order) }
    fun deleteOrderById(id: Int) = viewModelScope.launch { repository.deleteById(id) }

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
            purity = "22K (916)",
            approxWeight = 42.5,
            agreedRate = 72.0,
            makingCharges = 350.0,
            otherCharges = 120.0,
            advancePaid = 1500.0,
            totalAmount = 42.5 * 72.0 + 350.0 + 120.0,
            orderDate = now - (3 * 24 * 3600 * 1000L),
            expectedDeliveryDate = now + (10 * 24 * 3600 * 1000L),
            notes = "Engagement necklace. Traditional paisley design with central ruby stone.",
            status = "In Progress"
        )

        val order2 = Order(
            customerName = "Michael Chen",
            customerPhone = "+1 555-0149",
            jewelleryType = "Diamond Wedding Ring",
            metalType = "Platinum",
            purity = "Pt950",
            approxWeight = 6.2,
            agreedRate = 95.0,
            makingCharges = 280.0,
            otherCharges = 850.0,
            advancePaid = 500.0,
            totalAmount = 6.2 * 95.0 + 280.0 + 850.0,
            orderDate = now - (1 * 24 * 3600 * 1000L),
            expectedDeliveryDate = now + (5 * 24 * 3600 * 1000L),
            notes = "Inscribe inside band: 'M & S - Eternal'. Solitaire 0.5ct.",
            status = "Pending"
        )

        repository.insert(order1)
        repository.insert(order2)
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
```

---

## 5. Visual Theme - Sophisticated Dark Mode

A luxury dark obsidian backdrop crafted to represent a premium gold boutique database.

### `Color.kt`
Configure this inside `com.example.ui.theme`:
```kotlin
package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Sophisticated Dark Color Palette
val SophisticatedBg = Color(0xFF0F0F0F)
val SophisticatedSurface = Color(0xFF161616)
val SophisticatedSurfaceVariant = Color(0xFF222222)
val SophisticatedGoldPrimary = Color(0xFFD4AF37)
val SophisticatedGoldSecondary = Color(0xFFF9E498)
val SophisticatedMutedGold = Color(0xFFAA8A31)
val SophisticatedWhiteMuted = Color(0xFFD1D5DB)
val SophisticatedBorder = Color(0x14FFFFFF) // white with 8% alpha outline
val SophisticatedError = Color(0xFFCF6679)
```

### `Theme.kt`
Sets dynamic light schemas to dark and disables default Android dynamic background tinting to shield the signature gold-and-charcoal identity:
```kotlin
package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SophisticatedGoldPrimary,
    secondary = SophisticatedGoldSecondary,
    tertiary = SophisticatedMutedGold,
    background = SophisticatedBg,
    surface = SophisticatedSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    onPrimary = Color(0xFF0F0F0F), // Rich contrast charcoal text on gold
    onBackground = SophisticatedWhiteMuted,
    onSurface = SophisticatedWhiteMuted,
    outline = SophisticatedBorder,
    error = SophisticatedError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Lock immersive noir design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography, // Bind your typography
        shapes = Shapes,
        content = content
    )
}
```

---

## 6. The Main Activity Bootstrap

```kotlin
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.OrderRepository
import com.example.ui.DashboardScreen
import com.example.ui.OrderViewModel
import com.example.ui.OrderViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = OrderRepository(database.orderDao())
        val viewModel: OrderViewModel by viewModels { OrderViewModelFactory(repository) }

        setContent {
            MyApplicationTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
```

---

## 7. UI Blueprint & Layout System (`DashboardScreen.kt`)

`DashboardScreen.kt` forms the visual core of this application. When implementing using an AI agent or custom tools, design your UI based on these canonical layouts:

### Layout Core Layers
1. **Interactive Scaffold**:
   - Contains a floating action button (FAB) with a golden background (`MaterialTheme.colorScheme.primary`) to launch the custom **Order Form Dialogue**.
   - Contains a customized bottom bar housing a professional branding seal: `"AURELIA JEWELLERS · SECURE ORDER ENGINE"` alongside a golden carat diamond motif.
2. **Screen Header Stack**:
   - Royal design banner displaying a gold-embossed brand label `"Lumina Atelier"` accompanied by an active user session token: `"Aurelia Gold Vault Active"`.
3. **Analytics Stats Grid** (Fluid Adaptive Cards):
   - **Active Orders Count**: Lists active workflows in creation.
   - **Deposits/Advances Collected**: Tracks sum total of downpayments secure in holding.
   - **Remaining Collection Estimates**: Displays total projected gold balances yet to be settled before handover.
   - **Urgent Delivery Tracker**: Flashes red (`MaterialTheme.colorScheme.error`) if expected dates fall within a 3-day buffer.
4. **Dynamic Context Filtering**:
   - High-contrast segmented pill list: `All Items`, `Pending`, `In Progress`, `Completed`, `Delivered`. Clicking any pill mutates the DB subscription state seamlessly.
   - Fully searchable top inquiry card tracking phone details, customer names, gold weights, or jewelry specifics.
5. **Detailed Information Drawer**:
   - Clicking on any order opens an overlay or bottom sheet summarizing detailed statistics: Metal rates, purity profiles, makingCharges versus standard accessory items, and easy-to-use status progression nodes (e.g. progress order with one click).
6. **Luxury Input Form Dialogue**:
   - A highly-validated, gorgeous modal dialog overlay for **Add** or **Edit** operations.
   - Features custom-styled form fields: Metal, Weight, Rates, Advance Deposit, and expected deliveries.
   - Automated total cost estimation fields updated dynamically on input matching the classic gold formula:
     `Total Estimate = (Weight * Rate) + Making Value + Diamond/Other Extras`

---

## How to Instruct another AI Engine to build this App
Simply import this file into your prompt interface or workspace and send the following instruction:
> *"I want to build an offline-first premium Jewelry Custom Order tracking app in Android using Jetpack Compose and Kotlin. Use the Room database configuration, ViewModel architecture, and luxury 'Sophisticated Dark' theme assets specified in the attached replication blueprint. Reconstruct all components exactly with pristine Material 3 guidelines and generous spacing."*
