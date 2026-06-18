package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Karigar
import com.example.data.KarigarOrder
import com.example.data.KarigarOrderMetrics
import com.example.data.KarigarOrderItem
import com.example.data.KarigarTransaction
import com.example.data.DeletedKarigarOrder
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// --- Serialization Helpers ---
fun serializeKarigarItems(list: List<KarigarOrderItem>): String {
    val arr = JSONArray()
    list.forEach { item ->
        val obj = JSONObject().apply {
            put("id", item.id)
            put("itemName", item.itemName)
            put("metalType", item.metalType)
            put("quantity", item.quantity)
            put("grossWeight", item.grossWeight)
            put("stoneWeight", item.stoneWeight)
            put("subtractStoneWeight", item.subtractStoneWeight)
            put("purityPct", item.purityPct)
            put("wastagePct", item.wastagePct)
            put("netFineRequired", item.netFineRequired)
        }
        arr.put(obj)
    }
    return arr.toString()
}

fun deserializeKarigarItems(json: String): List<KarigarOrderItem> {
    if (json.isBlank() || json == "[]") return emptyList()
    val list = mutableListOf<KarigarOrderItem>()
    try {
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(KarigarOrderItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                itemName = obj.optString("itemName", ""),
                metalType = obj.optString("metalType", "gold"),
                quantity = obj.optInt("quantity", 1),
                grossWeight = obj.optDouble("grossWeight", 0.0),
                stoneWeight = obj.optDouble("stoneWeight", 0.0),
                subtractStoneWeight = obj.optBoolean("subtractStoneWeight", false),
                purityPct = obj.optDouble("purityPct", 0.0),
                wastagePct = obj.optDouble("wastagePct", 0.0),
                netFineRequired = obj.optDouble("netFineRequired", 0.0)
            ))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun serializeKarigarTransactions(list: List<KarigarTransaction>): String {
    val arr = JSONArray()
    list.forEach { t ->
        val obj = JSONObject().apply {
            put("id", t.id)
            put("type", t.type)
            put("date", t.date)
            put("weight", t.weight)
            put("rate", t.rate)
            put("amount", t.amount)
            put("note", t.note)
        }
        arr.put(obj)
    }
    return arr.toString()
}

fun deserializeKarigarTransactions(json: String): List<KarigarTransaction> {
    if (json.isBlank() || json == "[]") return emptyList()
    val list = mutableListOf<KarigarTransaction>()
    try {
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(KarigarTransaction(
                id = obj.optString("id", UUID.randomUUID().toString()),
                type = obj.optString("type", "metal"),
                date = obj.optLong("date", System.currentTimeMillis()),
                weight = obj.optDouble("weight", 0.0),
                rate = obj.optDouble("rate", 0.0),
                amount = obj.optDouble("amount", 0.0),
                note = obj.optString("note", "")
            ))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

fun serializeKarigarOrders(orders: List<KarigarOrder>): String {
    val arr = JSONArray()
    orders.forEach { o ->
        val obj = JSONObject().apply {
            put("id", o.id)
            put("orderNo", o.orderNo)
            put("karigarId", o.karigarId)
            put("karigarName", o.karigarName)
            put("referenceCustomerName", o.referenceCustomerName)
            put("phone", o.phone)
            put("orderDate", o.orderDate)
            put("deliveryDate", o.deliveryDate)
            put("urgency", o.urgency)
            put("specialInstructions", o.specialInstructions)
            put("designRef", o.designRef)
            put("status", o.status)
            put("itemsJson", o.itemsJson)
            put("transactionsJson", o.transactionsJson)
            put("totalFineRequired", o.totalFineRequired)
            put("totalFineIssued", o.totalFineIssued)
            put("totalFinePending", o.totalFinePending)
            put("makingType", o.makingType)
            put("makingRate", o.makingRate)
        }
        arr.put(obj)
    }
    return arr.toString(4)
}

fun deserializeKarigarOrders(json: String): List<KarigarOrder> {
    val list = mutableListOf<KarigarOrder>()
    try {
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(KarigarOrder(
                id = obj.optInt("id", 0),
                orderNo = obj.optString("orderNo", ""),
                karigarId = obj.optInt("karigarId", 0),
                karigarName = obj.optString("karigarName", ""),
                referenceCustomerName = obj.optString("referenceCustomerName", ""),
                phone = obj.optString("phone", ""),
                orderDate = obj.optLong("orderDate", System.currentTimeMillis()),
                deliveryDate = if (obj.isNull("deliveryDate")) null else obj.optLong("deliveryDate"),
                urgency = obj.optString("urgency", "normal"),
                specialInstructions = obj.optString("specialInstructions", ""),
                designRef = obj.optString("designRef", ""),
                status = obj.optString("status", "pending"),
                itemsJson = obj.optString("itemsJson", "[]"),
                transactionsJson = obj.optString("transactionsJson", "[]"),
                totalFineRequired = obj.optDouble("totalFineRequired", 0.0),
                totalFineIssued = obj.optDouble("totalFineIssued", 0.0),
                totalFinePending = obj.optDouble("totalFinePending", 0.0),
                makingType = obj.optString("makingType", "per_gram"),
                makingRate = obj.optDouble("makingRate", 0.0)
            ))
        }
    } catch (e: Exception) { e.printStackTrace() }
    return list
}

// Helper for capitalize
fun String.capitalizeWords(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarigarDashboardScreen(
    viewModel: OrderViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by rememberSaveable { mutableStateOf("orders") } // "orders" or "karigars"
    
    val orders by viewModel.filteredKarigarOrders.collectAsStateWithLifecycle()
    val karigars by viewModel.karigars.collectAsStateWithLifecycle()
    val karigarBalances by viewModel.karigarBalances.collectAsStateWithLifecycle()
    
    val searchQuery by viewModel.karigarSearchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.karigarStatusFilter.collectAsStateWithLifecycle()
    val metalFilter by viewModel.karigarMetalFilter.collectAsStateWithLifecycle()
    val karigarFilter by viewModel.selectedKarigarFilter.collectAsStateWithLifecycle()

    var showAddOrderDialog by rememberSaveable { mutableStateOf(false) }
    var showAddKarigarDialog by rememberSaveable { mutableStateOf(false) }
    var selectedOrderForDetail by remember { mutableStateOf<KarigarOrder?>(null) }
    var orderToEdit by remember { mutableStateOf<KarigarOrder?>(null) }

    val deletedKarigarOrders by viewModel.deletedKarigarOrders.collectAsStateWithLifecycle()
    val deletedCustomerOrders by viewModel.deletedOrders.collectAsStateWithLifecycle()
    var showActivityDialog by rememberSaveable { mutableStateOf(false) }
    var showBackupRestoreDialog by rememberSaveable { mutableStateOf(false) }
    var showCalculatorDialog by rememberSaveable { mutableStateOf(false) }

    // Master Export Logic
    var backupCustomer by remember { mutableStateOf(false) }
    var backupKarigar by remember { mutableStateOf(true) }

    val masterExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                val masterObj = JSONObject()
                if (backupCustomer) {
                    masterObj.put("customerOrders", JSONArray(serializeOrders(viewModel.filteredOrders.value)))
                }
                if (backupKarigar) {
                    masterObj.put("karigarOrders", JSONArray(serializeKarigarOrders(orders)))
                }
                val json = masterObj.toString(4)
                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(json.toByteArray())
                    Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val masterImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { isStream ->
                    val jsonStr = String(isStream.readBytes())
                    val masterObj = JSONObject(jsonStr)
                    
                    if (backupCustomer && masterObj.has("customerOrders")) {
                        val restored = deserializeOrders(masterObj.getJSONArray("customerOrders").toString())
                        restored.forEach { o -> viewModel.addOrder(o.copy(id = 0)) }
                    }
                    if (backupKarigar && masterObj.has("karigarOrders")) {
                        val restored = deserializeKarigarOrders(masterObj.getJSONArray("karigarOrders").toString())
                        restored.forEach { o -> viewModel.addKarigarOrder(o.copy(id = 0)) }
                    }
                    Toast.makeText(context, "Database restored successfully!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Restore failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("KARIGAR ORDERS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Work Order Management", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Activity Log") },
                                leadingIcon = { Icon(Icons.Default.History, null, tint = Color(0xFFC5A059)) },
                                onClick = { showMenu = false; showActivityDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("Backup & Restore") },
                                leadingIcon = { Icon(Icons.Default.Backup, null, tint = Color(0xFFC5A059)) },
                                onClick = {
                                    showMenu = false
                                    showBackupRestoreDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Jewellery Calculator") },
                                leadingIcon = { Icon(Icons.Default.Calculate, null, tint = Color(0xFFC5A059)) },
                                onClick = {
                                    showMenu = false
                                    showCalculatorDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            if (activeTab == "orders") {
                FloatingActionButton(
                    onClick = { showAddOrderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape, // User said "just a plus sign", making it circular might look simpler
                    modifier = Modifier.padding(bottom = 16.dp, start = 16.dp).size(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Karigar Order")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(4.dp)
            ) {
                TabButton(
                    label = "📋 Orders",
                    isActive = activeTab == "orders",
                    onClick = { activeTab = "orders" },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    label = "👤 Karigar Balance",
                    isActive = activeTab == "karigars",
                    onClick = { activeTab = "karigars" },
                    modifier = Modifier.weight(1f)
                )
            }

            if (activeTab == "orders") {
                KarigarOrdersTab(
                    viewModel = viewModel,
                    orders = orders,
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    metalFilter = metalFilter,
                    karigarFilter = karigarFilter,
                    karigars = karigars,
                    onOrderClick = { selectedOrderForDetail = it }
                )
            } else {
                KarigarBalanceTab(
                    karigars = karigars,
                    balances = karigarBalances,
                    onAddKarigar = { showAddKarigarDialog = true },
                    onDeleteKarigar = { viewModel.deleteKarigar(it) },
                    onViewOrders = { 
                        viewModel.setSelectedKarigarFilter(it.id)
                        activeTab = "orders"
                    }
                )
            }
        }
    }

    if (showAddOrderDialog) {
        AddEditKarigarOrderDialog(
            order = null,
            karigars = karigars,
            onDismiss = { showAddOrderDialog = false },
            onConfirm = { 
                viewModel.addKarigarOrder(it)
                showAddOrderDialog = false
            },
            onNavigateToAddKarigar = {
                showAddOrderDialog = false
                activeTab = "karigars"
                showAddKarigarDialog = true
            }
        )
    }

    if (orderToEdit != null) {
        AddEditKarigarOrderDialog(
            order = orderToEdit,
            karigars = karigars,
            onDismiss = { orderToEdit = null },
            onConfirm = { 
                viewModel.updateKarigarOrder(it)
                orderToEdit = null
            }
        )
    }

    if (selectedOrderForDetail != null) {
        KarigarOrderDetailDialog(
            order = selectedOrderForDetail!!,
            onDismiss = { selectedOrderForDetail = null },
            onEdit = { 
                orderToEdit = it
                selectedOrderForDetail = null
            },
            onDelete = {
                viewModel.deleteKarigarOrder(it)
                selectedOrderForDetail = null
            },
            onStatusUpdate = { 
                viewModel.updateKarigarOrder(it)
                selectedOrderForDetail = it
            }
        )
    }

    if (showAddKarigarDialog) {
        AddKarigarDialog(
            onDismiss = { showAddKarigarDialog = false },
            onConfirm = { 
                viewModel.addKarigar(it)
                showAddKarigarDialog = false
            }
        )
    }

    if (showActivityDialog) {
        UnifiedActivityLogDialog(
            deletedCustomerOrders = deletedCustomerOrders,
            deletedKarigarOrders = deletedKarigarOrders,
            onDismiss = { showActivityDialog = false },
            onRestoreCustomer = { item ->
                viewModel.restoreDeletedOrder(item)
                Toast.makeText(context, "Order restored!", Toast.LENGTH_SHORT).show()
            },
            onDeleteCustomerPermanently = { id ->
                viewModel.permanentlyDeleteDeletedOrder(id)
                Toast.makeText(context, "Permanently deleted!", Toast.LENGTH_SHORT).show()
            },
            onRestoreKarigar = { item ->
                viewModel.restoreDeletedKarigarOrder(item)
                Toast.makeText(context, "Order restored!", Toast.LENGTH_SHORT).show()
            },
            onDeleteKarigarPermanently = { id ->
                viewModel.permanentlyDeleteDeletedKarigarOrder(id)
                Toast.makeText(context, "Permanently deleted!", Toast.LENGTH_SHORT).show()
            },
            onClearAll = { cust, kari ->
                if (cust) viewModel.clearAllDeletedOrders()
                if (kari) viewModel.clearAllDeletedKarigarOrders()
                Toast.makeText(context, "Log cleared!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showBackupRestoreDialog) {
        MasterBackupRestoreDialog(
            onDismiss = { showBackupRestoreDialog = false },
            onBackup = { cust, kari ->
                backupCustomer = cust
                backupKarigar = kari
                masterExportLauncher.launch("aurelia_backup.json")
                showBackupRestoreDialog = false
            },
            onRestore = { _, _ ->
                masterImportLauncher.launch(arrayOf("application/json"))
                showBackupRestoreDialog = false
            }
        )
    }

    if (showCalculatorDialog) {
        BidirectionalCalculatorDialog(
            onDismiss = { showCalculatorDialog = false }
        )
    }
}

@Composable
fun TabButton(label: String, isActive: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
    val contentColor by animateColorAsState(if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}

@Composable
fun KarigarOrdersTab(
    viewModel: OrderViewModel,
    orders: List<KarigarOrder>,
    searchQuery: String,
    statusFilter: String,
    metalFilter: String,
    karigarFilter: Int?,
    karigars: List<Karigar>,
    onOrderClick: (KarigarOrder) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filters
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setKarigarSearchQuery(it) },
                placeholder = { Text("Search Order No, Karigar, Item...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = { if(searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.setKarigarSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = null) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Status Chips
            ScrollableRow(modifier = Modifier.fillMaxWidth()) {
                val statuses = listOf("all", "pending", "in_progress", "ready", "delivered")
                statuses.forEach { s ->
                    FilterChip(
                        selected = statusFilter == s,
                        onClick = { viewModel.setKarigarStatusFilter(s) },
                        label = { Text(if(s=="all") "All" else s.replace("_", " ").capitalizeWords()) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Metal Chips
            ScrollableRow(modifier = Modifier.fillMaxWidth()) {
                val metals = listOf("all", "gold", "silver")
                metals.forEach { m ->
                    FilterChip(
                        selected = metalFilter == m,
                        onClick = { viewModel.setKarigarMetalFilter(m) },
                        label = { Text(m.capitalizeWords()) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Found ${orders.size} orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                KarigarOrderCard(order = order, onClick = { onOrderClick(order) })
            }
        }
    }
}

@Composable
fun KarigarOrderCard(order: KarigarOrder, onClick: () -> Unit) {
    val statusColor = when(order.status) {
        "pending" -> Color(0xFFE69B00)
        "in_progress" -> Color(0xFF1976D2)
        "ready" -> Color(0xFF388E3C)
        "delivered" -> Color(0xFF757575)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(order.orderNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    if (order.referenceCustomerName.isNotBlank()) {
                        Text("Ref: ${order.referenceCustomerName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(statusColor.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(order.status.replace("_", " ").uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = statusColor)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(order.karigarName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(4.dp))
            
            val items = deserializeKarigarItems(order.itemsJson)
            val itemsSummary = if (items.isNotEmpty()) {
                items.joinToString { "${it.itemName} (${it.quantity})" }
            } else {
                "No items listed"
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(itemsSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(modifier = Modifier.alpha(0.05f))
            Spacer(Modifier.height(10.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TOTAL FINE REQ.", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.3f", order.totalFineRequired)} g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ISSUED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.3f", order.totalFineIssued)} g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Column(horizontalAlignment = Alignment.End) {
                    val transactions = deserializeKarigarTransactions(order.transactionsJson)
                    val totalVal = transactions.sumOf { it.amount }
                    val items = deserializeKarigarItems(order.itemsJson)
                    val totalMaking = when(order.makingType) {
                        "per_gram" -> items.sumOf { it.netFineRequired * it.quantity } * order.makingRate
                        "flat" -> items.sumOf { it.quantity.toDouble() } * order.makingRate
                        else -> 0.0
                    }
                    Text("TOTAL COST (CP)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format("%.0f", totalVal + totalMaking)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC5A059))
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    val items = deserializeKarigarItems(order.itemsJson)
                    val totalMaking = when(order.makingType) {
                        "per_gram" -> items.sumOf { it.netFineRequired * it.quantity } * order.makingRate
                        "flat" -> items.sumOf { it.quantity.toDouble() } * order.makingRate
                        else -> 0.0
                    }
                    Text("LABOUR / MAKING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format("%.0f", totalMaking)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TARGET DATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if(order.deliveryDate != null) SimpleDateFormat("dd MMM").format(Date(order.deliveryDate!!)) else "—", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (order.totalFinePending > 0) {
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Color(0xFFFFEBEE)).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                    Text("⚠ FINE PENDING: ${String.format("%.3f", order.totalFinePending)} g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                }
            }
        }
    }
}

@Composable
fun KarigarBalanceTab(
    karigars: List<Karigar>,
    balances: List<KarigarOrderMetrics>,
    onAddKarigar: () -> Unit,
    onDeleteKarigar: (Karigar) -> Unit,
    onViewOrders: (Karigar) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onAddKarigar() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Karigar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        if (karigars.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No karigars added yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(karigars.indices.toList()) { index ->
                    val karigar = karigars[index]
                    val balance = balances.getOrNull(index) ?: KarigarOrderMetrics()
                    KarigarBalanceCard(
                        karigar = karigar,
                        balance = balance,
                        onDelete = { onDeleteKarigar(karigar) },
                        onViewOrders = { onViewOrders(karigar) }
                    )
                }
            }
        }
    }
}

@Composable
fun KarigarBalanceCard(
    karigar: Karigar,
    balance: KarigarOrderMetrics,
    onDelete: () -> Unit,
    onViewOrders: () -> Unit
) {
    val hasPending = balance.totalPending > 0
    val pct = if (balance.totalRequired > 0) (balance.totalIssued / balance.totalRequired * 100).toInt() else 0
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Text(karigar.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(karigar.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${karigar.phone} · ${balance.activeOrders} active orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                    Row {
                        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) }
                    }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BalanceCell(label = "REQUIRED", value = balance.totalRequired, modifier = Modifier.weight(1f))
                BalanceCell(label = "ISSUED", value = balance.totalIssued, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                BalanceCell(label = if(hasPending) "PENDING" else "SETTLED", value = balance.totalPending, color = if(hasPending) Color(0xFFC62828) else Color(0xFF2E7D32), isWarning = hasPending, modifier = Modifier.weight(1f))
            }
            
            if (balance.totalRequired > 0) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                        color = if(hasPending) Color(0xFFE69B00) else Color(0xFF388E3C),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text("$pct% issued", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun BalanceCell(label: String, value: Double, color: Color = MaterialTheme.colorScheme.onSurface, isWarning: Boolean = false, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isWarning) Color(0xFFFFF5F5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = if(isWarning) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(String.format("%.3f", value), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text("g", fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 1.dp), color = color.copy(alpha = 0.7f))
        }
    }
}

// --- Dialogs ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKarigarDialog(onDismiss: () -> Unit, onConfirm: (Karigar) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Karigar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Karigar Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onConfirm(Karigar(name = name, phone = phone)) }) {
                Text("Add Karigar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditKarigarOrderDialog(
    order: KarigarOrder?,
    karigars: List<Karigar>,
    onDismiss: () -> Unit,
    onConfirm: (KarigarOrder) -> Unit,
    onNavigateToAddKarigar: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var currentSection by remember { mutableStateOf(0) }
    val sections = listOf("Karigar", "Items & Weight", "Balance & Making", "Notes")
    
    // Form State
    var selectedKarigar by remember { mutableStateOf<Karigar?>(karigars.find { it.id == order?.karigarId }) }
    var referenceCustomerName by remember { mutableStateOf(order?.referenceCustomerName ?: "") }
    var phone by remember { mutableStateOf(order?.phone ?: "") }
    var orderDate by remember { mutableStateOf(order?.orderDate ?: System.currentTimeMillis()) }
    var deliveryDate by remember { mutableStateOf(order?.deliveryDate) }
    var urgency by remember { mutableStateOf(order?.urgency ?: "normal") }
    
    // Multi-Item State
    var itemsList by remember { mutableStateOf(deserializeKarigarItems(order?.itemsJson ?: "[]").ifEmpty { listOf(KarigarOrderItem()) }) }
    
    // Balance State
    var transactions by remember { mutableStateOf(deserializeKarigarTransactions(order?.transactionsJson ?: "[]")) }
    
    // Calc Helper State (Metal)
    var balGrossWeight by remember { mutableStateOf("") }
    var balPurityPct by remember { mutableStateOf("") }
    var balNetFine by remember { mutableStateOf("") }

    // Calc Helper State (Cash)
    var cashWeight by remember { mutableStateOf("") }
    var cashRate by remember { mutableStateOf("") }
    var cashAmount by remember { mutableStateOf("") }
    
    // Making State
    var makingType by remember { mutableStateOf(order?.makingType ?: "per_gram") }
    var makingRate by remember { mutableStateOf(order?.makingRate?.toString() ?: "") }
    var specialInstructions by remember { mutableStateOf(order?.specialInstructions ?: "") }

    // Calc Helper State (Metal Issue Rate)
    var balRate by remember { mutableStateOf("") }
    var balAmount by remember { mutableStateOf("") }

    // Aggregate Calculations
    val totalFineRequired = itemsList.sumOf { it.netFineRequired }
    val totalFineIssued = transactions.sumOf { it.weight }
    val totalFinePending = totalFineRequired - totalFineIssued

    val totalValuation = transactions.sumOf { it.amount }
    val totalMaking = when(makingType) {
        "per_gram" -> itemsList.sumOf { it.netFineRequired * it.quantity } * (makingRate.toDoubleOrNull() ?: 0.0)
        "flat" -> itemsList.sumOf { it.quantity.toDouble() } * (makingRate.toDoubleOrNull() ?: 0.0)
        else -> 0.0
    }
    val costPrice = totalValuation + totalMaking

    fun updateItem(index: Int, newItem: KarigarOrderItem) {
        val list = itemsList.toMutableList()
        if (index in list.indices) {
            // Recalculate net fine for this item
            val effectiveGross = if (newItem.subtractStoneWeight) (newItem.grossWeight - newItem.stoneWeight).coerceAtLeast(0.0) else newItem.grossWeight
            val netFine = effectiveGross * (newItem.purityPct + newItem.wastagePct) / 100.0
            list[index] = newItem.copy(netFineRequired = netFine)
            itemsList = list
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f).clip(RoundedCornerShape(24.dp)), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(if(order == null) "New Work Order" else "Edit Work Order", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if(order == null) "Dispatched to Karigar" else order.orderNo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                
                // Tabs
                ScrollableRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    sections.forEachIndexed { index, s ->
                        Tab(
                            selected = currentSection == index,
                            onClick = { currentSection = index },
                            text = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.alpha(0.1f))
                
                // Form Content
                LazyColumn(modifier = Modifier.weight(1f).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        when(currentSection) {
                            0 -> { // Karigar
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("KARIGAR IDENTIFICATION", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    
                                    var expandedKarigar by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedTextField(
                                            value = selectedKarigar?.name ?: "",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Select Karigar") },
                                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                            modifier = Modifier.fillMaxWidth().clickable { expandedKarigar = true },
                                            enabled = false,
                                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        DropdownMenu(expanded = expandedKarigar, onDismissRequest = { expandedKarigar = false }) {
                                            karigars.forEach { k ->
                                                DropdownMenuItem(text = { Text(k.name) }, onClick = { selectedKarigar = k; phone = k.phone; expandedKarigar = false })
                                            }
                                        }
                                    }
                                    
                                    if (karigars.isEmpty() && onNavigateToAddKarigar != null) {
                                        TextButton(onClick = onNavigateToAddKarigar) { Text("+ Add New Karigar") }
                                    }
                                    
                                    OutlinedTextField(value = referenceCustomerName, onValueChange = { referenceCustomerName = it }, label = { Text("Ref. Customer Name") }, placeholder = { Text("e.g. John Doe") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = SimpleDateFormat("dd/MM/yy").format(Date(orderDate)), onValueChange = {}, label = { Text("Order Date") }, modifier = Modifier.weight(1f), readOnly = true)
                                        
                                        val datePickerDialog = android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val cal = Calendar.getInstance()
                                                cal.set(year, month, dayOfMonth)
                                                deliveryDate = cal.timeInMillis
                                            },
                                            Calendar.getInstance().get(Calendar.YEAR),
                                            Calendar.getInstance().get(Calendar.MONTH),
                                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                                        )
                                        OutlinedTextField(
                                            value = if(deliveryDate!=null) SimpleDateFormat("dd/MM/yy").format(Date(deliveryDate!!)) else "", 
                                            onValueChange = {}, 
                                            label = { Text("Delivery Date") }, 
                                            modifier = Modifier.weight(1f).clickable { datePickerDialog.show() }, 
                                            readOnly = true,
                                            enabled = false,
                                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                    
                                    Text("URGENCY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("normal", "urgent", "express").forEach { u ->
                                            FilterChip(selected = urgency == u, onClick = { urgency = u }, label = { Text(u.capitalizeWords()) })
                                        }
                                    }
                                }
                            }
                            1 -> { // Items & Weight Merged
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("ITEMS & WEIGHT SPECIFICATIONS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    
                                    itemsList.forEachIndexed { index, item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Item #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    if (itemsList.size > 1) {
                                                        IconButton(onClick = { itemsList = itemsList.filterIndexed { i, _ -> i != index } }, modifier = Modifier.size(24.dp)) {
                                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                                
                                                OutlinedTextField(value = item.itemName, onValueChange = { updateItem(index, item.copy(itemName = it)) }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                                                
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    var expandedMetal by remember { mutableStateOf(false) }
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        OutlinedTextField(value = item.metalType.capitalizeWords(), onValueChange = {}, readOnly = true, label = { Text("Metal") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.fillMaxWidth().clickable { expandedMetal = true }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = MaterialTheme.colorScheme.outline))
                                                        DropdownMenu(expanded = expandedMetal, onDismissRequest = { expandedMetal = false }) {
                                                            listOf("gold", "silver").forEach { m ->
                                                                DropdownMenuItem(text = { Text(m.capitalizeWords()) }, onClick = { updateItem(index, item.copy(metalType = m)); expandedMetal = false })
                                                            }
                                                        }
                                                    }
                                                    OutlinedTextField(value = item.quantity.toString(), onValueChange = { updateItem(index, item.copy(quantity = it.toIntOrNull() ?: 1)) }, label = { Text("Qty") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                                }

                                                OutlinedTextField(value = if(item.grossWeight==0.0) "" else item.grossWeight.toString(), onValueChange = { updateItem(index, item.copy(grossWeight = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Gross Weight (g)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                                
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(checked = item.subtractStoneWeight, onCheckedChange = { updateItem(index, item.copy(subtractStoneWeight = it)) })
                                                    Text("Subtract Stone Weight", fontSize = 11.sp)
                                                }
                                                
                                                if (item.subtractStoneWeight) {
                                                    OutlinedTextField(value = if(item.stoneWeight==0.0) "" else item.stoneWeight.toString(), onValueChange = { updateItem(index, item.copy(stoneWeight = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Stone Weight (g)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(value = if(item.purityPct==0.0) "" else item.purityPct.toString(), onValueChange = { updateItem(index, item.copy(purityPct = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Purity %") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                                    OutlinedTextField(value = if(item.wastagePct==0.0) "" else item.wastagePct.toString(), onValueChange = { updateItem(index, item.copy(wastagePct = it.toDoubleOrNull() ?: 0.0)) }, label = { Text("Wastage %") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                                }
                                                
                                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)).padding(12.dp), contentAlignment = Alignment.Center) {
                                                    Text("Net Fine Required: ${String.format("%.3f", item.netFineRequired)} g", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                }
                                            }
                                        }
                                    }
                                    
                                    Button(onClick = { itemsList = itemsList + KarigarOrderItem() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                                        Icon(Icons.Default.Add, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Add Another Item")
                                    }
                                }
                            }
                            2 -> { // Balance & Making Merged
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("FINE METAL BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        BalanceDisplay(label = "TOTAL REQUIRED", value = totalFineRequired, color = Color(0xFF1976D2), modifier = Modifier.weight(1f))
                                        BalanceDisplay(label = "TOTAL ISSUED", value = totalFineIssued, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                                        BalanceDisplay(label = "PENDING", value = totalFinePending, color = Color(0xFFC62828), modifier = Modifier.weight(1f), isWarning = totalFinePending > 0)
                                    }
                                    
                                    HorizontalDivider()
                                    Text("ISSUE NEW METAL", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = balGrossWeight, onValueChange = { balGrossWeight = it; val g=it.toDoubleOrNull() ?: 0.0; val p=balPurityPct.toDoubleOrNull() ?: 0.0; balNetFine = String.format("%.3f", g*p/100.0); val r=balRate.toDoubleOrNull() ?: 0.0; balAmount = String.format("%.0f", (g*p/100.0)*r) }, label = { Text("Gross") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = balPurityPct, onValueChange = { balPurityPct = it; val g=balGrossWeight.toDoubleOrNull() ?: 0.0; val p=it.toDoubleOrNull() ?: 0.0; balNetFine = String.format("%.3f", g*p/100.0); val r=balRate.toDoubleOrNull() ?: 0.0; balAmount = String.format("%.0f", (g*p/100.0)*r) }, label = { Text("Purity %") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    }
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = balRate, onValueChange = { balRate = it; val f = balNetFine.toDoubleOrNull() ?: 0.0; val r = it.toDoubleOrNull() ?: 0.0; balAmount = String.format("%.0f", f * r) }, label = { Text("Rate / g") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = balAmount, onValueChange = { balAmount = it }, label = { Text("Amount (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    }

                                    Button(
                                        onClick = { 
                                            val addWt = balNetFine.toDoubleOrNull() ?: 0.0
                                            val rate = balRate.toDoubleOrNull() ?: 0.0
                                            val amount = balAmount.toDoubleOrNull() ?: (addWt * rate)
                                            if (addWt > 0) {
                                                transactions = transactions + KarigarTransaction(type = "metal", weight = addWt, rate = rate, amount = amount)
                                                balGrossWeight = ""; balNetFine = ""; balPurityPct = ""; balRate = ""; balAmount = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Issue ${balNetFine.ifBlank { "0.000" }}g Fine Metal (₹${balAmount.ifBlank { "0" }})")
                                    }

                                    HorizontalDivider()
                                    Text("ISSUE IN CASH (SETTLE FINE)", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = cashWeight, onValueChange = { cashWeight = it; val w=it.toDoubleOrNull() ?: 0.0; val r=cashRate.toDoubleOrNull() ?: 0.0; cashAmount = String.format("%.0f", w*r) }, label = { Text("Weight (g)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = cashRate, onValueChange = { cashRate = it; val w=cashWeight.toDoubleOrNull() ?: 0.0; val r=it.toDoubleOrNull() ?: 0.0; cashAmount = String.format("%.0f", w*r) }, label = { Text("Rate / g") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    }
                                    OutlinedTextField(value = cashAmount, onValueChange = { cashAmount = it }, label = { Text("Total Amount (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    
                                    Button(
                                        onClick = { 
                                            val w = cashWeight.toDoubleOrNull() ?: 0.0
                                            val r = cashRate.toDoubleOrNull() ?: 0.0
                                            val a = cashAmount.toDoubleOrNull() ?: (w*r)
                                            if (w > 0) {
                                                transactions = transactions + KarigarTransaction(type = "cash", weight = w, rate = r, amount = a)
                                                cashWeight = ""; cashRate = ""; cashAmount = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137333)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val displayAmt = cashAmount.ifBlank { String.format("%.0f", (cashWeight.toDoubleOrNull() ?: 0.0) * (cashRate.toDoubleOrNull() ?: 0.0)) }
                                        Text("Issue ₹${displayAmt} Cash for ${cashWeight}g Fine")
                                    }

                                    if (transactions.isNotEmpty()) {
                                        Text("HISTORY", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        transactions.reversed().forEach { t ->
                                            Card(modifier = Modifier.fillMaxWidth()) {
                                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(if(t.type=="metal") "Metal Issue" else "Cash Issue", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text(SimpleDateFormat("dd MMM, hh:mm a").format(Date(t.date)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("${String.format("%.3f", t.weight)} g", fontWeight = FontWeight.Black, color = if(t.type=="metal") Color(0xFF2E7D32) else Color(0xFF1976D2))
                                                        if (t.type == "cash") Text("₹${String.format("%.0f", t.amount)}", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider()
                                    Text("LABOUR / MAKING CHARGES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    
                                    Row {
                                        listOf("per_gram", "flat").forEach { m ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).clickable { makingType = m }) {
                                                RadioButton(selected = makingType == m, onClick = { makingType = m })
                                                Text(m.replace("_", " ").capitalizeWords(), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    
                                    OutlinedTextField(value = makingRate, onValueChange = { makingRate = it }, label = { Text("Making Rate") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                    
                                    val rate = makingRate.toDoubleOrNull() ?: 0.0
                                    val est = when(makingType) {
                                        "per_gram" -> itemsList.sumOf { it.netFineRequired * it.quantity } * rate
                                        "flat" -> itemsList.sumOf { it.quantity.toDouble() } * rate
                                        else -> 0.0
                                    }
                                    
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("ESTIMATED MAKING", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text("₹ ${String.format("%,.2f", est)}", fontSize = 24.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                            3 -> { // Notes
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("ADDITIONAL INSTRUCTIONS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    OutlinedTextField(value = specialInstructions, onValueChange = { specialInstructions = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
                                }
                            }
                        }
                    }
                }
                
                // Footer
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    
                    Row {
                        if (currentSection > 0) {
                            TextButton(onClick = { currentSection-- }) { Text("Back") }
                        }
                        Spacer(Modifier.width(8.dp))
                        if (currentSection < sections.size - 1) {
                            Button(onClick = { currentSection++ }) { Text("Next") }
                        } else {
                            Button(
                                onClick = {
                                    if (selectedKarigar == null) {
                                        Toast.makeText(context, "Please select a karigar", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val kOrder = KarigarOrder(
                                        id = order?.id ?: 0,
                                        orderNo = order?.orderNo ?: "KO-${System.currentTimeMillis().toString().takeLast(6)}",
                                        karigarId = selectedKarigar!!.id,
                                        karigarName = selectedKarigar!!.name,
                                        referenceCustomerName = referenceCustomerName,
                                        phone = phone,
                                        orderDate = orderDate,
                                        deliveryDate = deliveryDate,
                                        urgency = urgency,
                                        specialInstructions = specialInstructions,
                                        designRef = itemsList.firstOrNull()?.itemName ?: "", // Using first item name as design ref if not set
                                        status = order?.status ?: "pending",
                                        itemsJson = serializeKarigarItems(itemsList),
                                        transactionsJson = serializeKarigarTransactions(transactions),
                                        totalFineRequired = totalFineRequired,
                                        totalFineIssued = totalFineIssued,
                                        totalFinePending = totalFinePending,
                                        makingType = makingType,
                                        makingRate = makingRate.toDoubleOrNull() ?: 0.0
                                    )
                                    onConfirm(kOrder)
                                }
                            ) {
                                Text(if(order == null) "Create Order" else "Save Changes")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceInput(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, color: Color) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = color),
            singleLine = true
        )
    }
}

@Composable
fun KarigarOrderDetailDialog(
    order: KarigarOrder,
    onDismiss: () -> Unit,
    onEdit: (KarigarOrder) -> Unit,
    onDelete: (KarigarOrder) -> Unit,
    onStatusUpdate: (KarigarOrder) -> Unit
) {
    val context = LocalContext.current
    var currentOrder by remember(order) { mutableStateOf(order) }
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).clip(RoundedCornerShape(24.dp)), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    Text("Order Details", fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { /* Share Logic */
                            val sdf = SimpleDateFormat("dd MMM, hh:mm a")
                            val text = """
                                Order No: ${currentOrder.orderNo}
                                Karigar: ${currentOrder.karigarName}
                                Total Fine Required: ${String.format("%.3f", currentOrder.totalFineRequired)}g
                                Total Fine Issued: ${String.format("%.3f", currentOrder.totalFineIssued)}g
                                Total Fine Pending: ${String.format("%.3f", currentOrder.totalFinePending)}g
                                Status: ${currentOrder.status.uppercase()}
                            """.trimIndent()
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }) { Icon(Icons.Default.Share, null, tint = Color(0xFFC5A059)) }
                        IconButton(onClick = { onEdit(currentOrder) }) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = { onDelete(currentOrder) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    }
                }
                
                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 24.dp)) {
                    item {
                        DetailHeader(currentOrder)
                        Spacer(Modifier.height(24.dp))
                        
                        DetailSection("WEIGHT SPECIFICATIONS") {
                            WeightGrid(currentOrder)
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        DetailSection("FINE METAL RECONCILIATION") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                BalanceDisplay(label = "REQUIRED", value = currentOrder.totalFineRequired, color = Color(0xFF1976D2), modifier = Modifier.weight(1f))
                                BalanceDisplay(label = "ISSUED", value = currentOrder.totalFineIssued, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                                BalanceDisplay(label = "PENDING", value = currentOrder.totalFinePending, color = Color(0xFFC62828), modifier = Modifier.weight(1f), isWarning = currentOrder.totalFinePending > 0)
                            }
                        }
                        
                        val transactions = deserializeKarigarTransactions(currentOrder.transactionsJson)
                        if (transactions.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            DetailSection("TRANSACTION HISTORY") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    transactions.reversed().forEach { t ->
                                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(if(t.type=="metal") "Fine Metal Issued" else "Fine Settled in Cash", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(SimpleDateFormat("dd MMM, hh:mm a").format(Date(t.date)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("${String.format("%.3f", t.weight)} g", fontWeight = FontWeight.Black, color = if(t.type=="metal") Color(0xFF2E7D32) else Color(0xFF1976D2))
                                                    if (t.type == "cash") Text("₹${String.format("%.0f", t.amount)}", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        DetailSection("STATUS WORKFLOW") {
                            val statuses = listOf("pending", "in_progress", "ready", "delivered")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                statuses.forEach { s ->
                                    val isActive = currentOrder.status == s
                                    Box(
                                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if(isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant).clickable { 
                                            val updated = currentOrder.copy(status = s)
                                            currentOrder = updated
                                            onStatusUpdate(updated) 
                                        }.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(s.replace("_", " ").capitalizeWords(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailHeader(order: KarigarOrder) {
    Column {
        Text(order.orderNo, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary)
        if (order.referenceCustomerName.isNotBlank()) {
            Text("Reference: ${order.referenceCustomerName}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("${order.karigarName} · ${order.phone}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
fun WeightGrid(order: KarigarOrder) {
    val items = deserializeKarigarItems(order.itemsJson)
    
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { index, item ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Item #${index + 1}: ${item.itemName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross: ${item.grossWeight}g", fontSize = 11.sp)
                        Text("Stone: ${item.stoneWeight}g", fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Purity: ${item.purityPct}%", fontSize = 11.sp)
                        Text("Fine: ${String.format("%.3f", item.netFineRequired)}g", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceDisplay(label: String, value: Double, color: Color, modifier: Modifier = Modifier, isWarning: Boolean = false) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(if(isWarning) Color(0xFFFFEBEE) else color.copy(alpha = 0.1f)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = if(isWarning) Color(0xFFC62828) else color)
        Spacer(Modifier.height(4.dp))
        Text("${String.format("%.3f", value)}g", fontSize = 16.sp, fontWeight = FontWeight.Black, color = if(isWarning) Color(0xFFC62828) else color)
    }
}

@Composable
fun ScrollableRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(modifier: Modifier = Modifier, horizontalArrangement: Arrangement.Horizontal = Arrangement.Start, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarigarActivityLogDialog(
    deletedOrders: List<DeletedKarigarOrder>,
    onDismiss: () -> Unit,
    onRestore: (DeletedKarigarOrder) -> Unit,
    onDeletePermanently: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var showConfirmClearAll by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Karigar Activity Log") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (deletedOrders.isEmpty()) {
                    Text("No deleted karigar orders.", modifier = Modifier.padding(16.dp))
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showConfirmClearAll = true }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(deletedOrders) { order ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(order.orderNo, fontWeight = FontWeight.Bold)
                                    Text("${order.karigarName} · Ref: ${order.referenceCustomerName}", fontSize = 11.sp)
                                    Text("Fine: ${String.format("%.3f", order.totalFineRequired)}g", fontSize = 11.sp)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        IconButton(onClick = { onRestore(order) }) { Icon(Icons.Default.Restore, null, tint = Color(0xFF388E3C)) }
                                        IconButton(onClick = { onDeletePermanently(order.id) }) { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )

    if (showConfirmClearAll) {
        AlertDialog(
            onDismissRequest = { showConfirmClearAll = false },
            title = { Text("Clear All Log?") },
            text = { Text("Are you sure you want to permanently delete all karigar activity logs?") },
            confirmButton = {
                Button(onClick = { onClearAll(); showConfirmClearAll = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Clear All")
                }
            },
            dismissButton = { TextButton(onClick = { showConfirmClearAll = false }) { Text("Cancel") } }
        )
    }
}
