package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Order
import com.example.data.OrderItem
import com.example.data.OldOrderItem
import com.example.data.getItems
import com.example.data.getOldItems
import com.example.data.serializeItems
import com.example.data.serializeOldItems
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: OrderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val orders by viewModel.filteredOrders.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }
    var selectedOrderForEdit by remember { mutableStateOf<Order?>(null) }
    
    val deletedOrders by viewModel.deletedOrders.collectAsStateWithLifecycle()
    var showActivityDialog by remember { mutableStateOf(false) }

    // Launcher for creating a backup file (export)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                val json = serializeOrders(orders)
                val os = context.contentResolver.openOutputStream(it)
                if (os != null) {
                    os.write(json.toByteArray())
                    os.flush()
                    os.close()
                    Toast.makeText(context, "Backup exported successfully to file system!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Export failed: cannot write stream", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher for opening a backup file to restore (import)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val isStream = context.contentResolver.openInputStream(it)
                if (isStream != null) {
                    val bytes = isStream.readBytes()
                    isStream.close()
                    val jsonStr = String(bytes)
                    val restoredOrders = deserializeOrders(jsonStr)
                    if (restoredOrders.isNotEmpty()) {
                        restoredOrders.forEach { order ->
                            viewModel.addOrder(order.copy(id = 0)) // Insert as new copy
                        }
                        Toast.makeText(context, "Successfully restored ${restoredOrders.size} orders!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Restore failed: File is empty or in an invalid format.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Restore failed: cannot read stream", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Restore failed: Invalid back-up format. Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Luxury Golden Theme Brushes
    val goldGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFD4AF37), // Metallic Gold
            Color(0xFFF3E5AB), // Vanilla/Soft Gold
            Color(0xFFAA7C11)  // Darker Gold
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Elegant brand footer with safety padding
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Signature",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SUHAS JEWELLERS · SECURE ORDER ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("add_order_fab")
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Custom Order",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Screen Header & Branding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Suhas Jewellers",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Backup and Restore 3-dot dropdown menu
                    var showBackupMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showBackupMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Database backup and restore options",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showBackupMenu,
                            onDismissRequest = { showBackupMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Activity Log") },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFC5A059)) },
                                onClick = {
                                    showBackupMenu = false
                                    showActivityDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Backup Database") },
                                leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null, tint = Color(0xFFC5A059)) },
                                onClick = {
                                    showBackupMenu = false
                                    exportLauncher.launch("suhas_jewellers_backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Restore Database") },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFFC5A059)) },
                                onClick = {
                                    showBackupMenu = false
                                    importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Dashboard Banner
                MetricsPanel(metrics = metrics)

                Spacer(modifier = Modifier.height(14.dp))

                // Search & Sort side-by-side inside a cohesive single-line Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search name, number, item", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("search_input"),
                        singleLine = true
                    )

                    // Sorting Trigger dropdown
                    var showSortMenu by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { showSortMenu = true },
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort orders",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFC5A059)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sortBy,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Sort dropdown",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf(
                                "Newest",
                                "Oldest",
                                "Delivery Soonest",
                                "Delivery Latest",
                                "Value Highest",
                                "Value Lowest"
                            ).forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.setSortBy(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status segment label with items count
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status Filters",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Found ${orders.size} item(s)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC5A059)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Status Filter Chips
                StatusFilterTabs(
                    selectedStatus = statusFilter,
                    onStatusSelected = { viewModel.setStatusFilter(it) }
                )
            }

            // Orders Listing Area
            if (orders.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty orders",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No matching orders found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing search keywords or active status filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("orders_list"),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onClick = { selectedOrderForDetail = order }
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet: Order Details
    selectedOrderForDetail?.let { order ->
        OrderDetailDialog(
            order = order,
            onDismiss = { selectedOrderForDetail = null },
            onUpdateItemStatus = { itemIndex, newStatus ->
                val currentItems = order.getItems().toMutableList()
                if (itemIndex in currentItems.indices) {
                    currentItems[itemIndex] = currentItems[itemIndex].copy(status = newStatus)
                }

                // Compute overall status based on item statuses
                val overallStatus = when {
                    currentItems.all { it.status == "Delivered" } -> "Delivered"
                    currentItems.all { it.status == "Completed" || it.status == "Delivered" } -> "Completed"
                    currentItems.any { it.status == "In Progress" || it.status == "Completed" } -> "In Progress"
                    else -> "Pending"
                }

                val updatedOrder = order.copy(
                    status = overallStatus,
                    itemsJson = serializeItems(currentItems)
                )

                viewModel.updateOrder(updatedOrder)
                // Refresh local dialog state instantly
                selectedOrderForDetail = updatedOrder
            },
            onEdit = {
                selectedOrderForEdit = order
                selectedOrderForDetail = null
            },
            onDelete = {
                viewModel.deleteOrder(order)
                selectedOrderForDetail = null
                Toast.makeText(context, "Order deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Dialog: Add Order
    if (showAddDialog) {
        AddEditOrderDialog(
            order = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newOrder ->
                viewModel.addOrder(newOrder)
                showAddDialog = false
                Toast.makeText(context, "New jewellery order registered!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Dialog: Edit Order
    selectedOrderForEdit?.let { order ->
        AddEditOrderDialog(
            order = order,
            onDismiss = { selectedOrderForEdit = null },
            onConfirm = { updatedOrder ->
                viewModel.updateOrder(updatedOrder)
                selectedOrderForEdit = null
                Toast.makeText(context, "Order details updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showActivityDialog) {
        ActivityLogDialog(
            deletedOrders = deletedOrders,
            onDismiss = { showActivityDialog = false },
            onRestore = { item ->
                viewModel.restoreDeletedOrder(item)
                Toast.makeText(context, "Order restored to system!", Toast.LENGTH_SHORT).show()
            },
            onDeletePermanently = { id ->
                viewModel.permanentlyDeleteDeletedOrder(id)
                Toast.makeText(context, "Permanently purged from system memory.", Toast.LENGTH_SHORT).show()
            },
            onClearAll = {
                viewModel.clearAllDeletedOrders()
                Toast.makeText(context, "Activity Bin successfully purged.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// Stats metrics banner
@Composable
fun MetricsPanel(metrics: OrderMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = "Active orders count",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${metrics.activeOrders}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VerticalDivider(modifier = Modifier.height(36.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = "AdvanceCollected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", metrics.totalAdvanceCollected)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Advance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VerticalDivider(modifier = Modifier.height(36.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CurrencyRupee,
                    contentDescription = "Pending balance collections",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", metrics.pendingCollection)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Receivable",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VerticalDivider(modifier = Modifier.height(36.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Due Soon",
                    tint = if (metrics.dueSoonOrders > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${metrics.dueSoonOrders}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (metrics.dueSoonOrders > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    ),
                )
                Text(
                    text = "Due 3 Days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Scrollable status tabs
@Composable
fun StatusFilterTabs(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("All", "Pending", "In Progress", "Completed", "Delivered")
    ScrollableTabRow(
        selectedTabIndex = statuses.indexOf(selectedStatus).coerceAtLeast(0),
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        statuses.forEach { statusName ->
            val isSelected = statusName == selectedStatus
            val boxColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                label = "FilterChipColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "FilterChipTextColor"
            )

            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(boxColor)
                    .clickable { onStatusSelected(statusName) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
}

// Individual order row item
@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val balance = order.totalAmount - order.advancePaid
    val isDueSoon = order.status != "Delivered" && (order.expectedDeliveryDate - System.currentTimeMillis() < 3 * 24 * 3600 * 1000L)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_item_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = if (isDueSoon) 1.dp else 0.5.dp,
            color = if (isDueSoon) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (order.customerPhone.isNotBlank()) {
                        Text(
                            text = order.customerPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            // Listed Jewellery Items with separate status badges
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                order.getItems().forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.jewelleryType,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val purityDisplay = if (item.purity.toDoubleOrNull() != null) "${item.purity}%" else item.purity
                            Text(
                                text = "${item.metalType} · $purityDisplay · ${String.format(Locale.getDefault(), "%.2f", item.approxWeight)} g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = item.status)
                    }
                }
            }

            val oldItems = order.getOldItems()
            if (oldItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Exchange Trade-In",
                        tint = Color(0xFF6200EE),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Exchange: ${oldItems.size} old item(s) (Fine metal subtracted)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF6200EE)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Balance: ₹${String.format(Locale.getDefault(), "%,.0f", balance)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (balance > 0 && order.status != "Delivered") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total ₹${String.format(Locale.getDefault(), "%,.0f", order.totalAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expected date indicator
                val formattedDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(order.expectedDeliveryDate))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Delivery target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDueSoon) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = "Overdue or coming soon warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDueSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

// Status highlight badge
@Composable
fun StatusBadge(status: String) {
    val (backColor, textColor) = when (status) {
        "Pending" -> Color(0xFFFCE8E6) to Color(0xFFC5221F)
        "In Progress" -> Color(0xFFFEF7E0) to Color(0xFFB06000)
        "Completed" -> Color(0xFFE6F4EA) to Color(0xFF137333)
        "Delivered" -> Color(0xFFF1F3F4) to Color(0xFF5F6368)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor
        )
    }
}

// Order details dialog with full breakdown list, status editing, and actions
@Composable
fun OrderDetailDialog(
    order: Order,
    onDismiss: () -> Unit,
    onUpdateItemStatus: (Int, String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isConfirmDeleteState by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFAF6F0).copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            ) {
                // Details Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close detailed view")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Order Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit details", tint = Color(0xFFC5A059))
                        }
                        IconButton(onClick = { isConfirmDeleteState = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete record", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                if (isConfirmDeleteState) {
                    // Quick confirm delete inline card
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Are you sure you want to delete this order?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "This will permanently remove Samantha's records and estimates from the Aurelia database. This action is irreversible.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { isConfirmDeleteState = false }) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = onDelete,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Confirm Delete", color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Scrollable properties breakdown
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer Details Segment
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = order.customerName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = order.customerPhone,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFC5A059).copy(alpha = 0.15f))
                                        .clickable {
                                            try {
                                                val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:${order.customerPhone}")
                                                }
                                                context.startActivity(phoneIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "No dialer available", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Place dial call",
                                        tint = Color(0xFFC5A059),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Jewelry Metal detail card
                        Text(
                            text = "JEWELLERY SPECIFICATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailMetricsGrid(
                            order = order,
                            onUpdateItemStatus = onUpdateItemStatus
                        )

                        if (order.getOldItems().isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            TradedInJewelleryCard(order = order)
                            Spacer(modifier = Modifier.height(16.dp))
                            MetalBalancingSheet(order = order)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Notes section
                        if (order.notes.isNotEmpty()) {
                            Text(
                                text = "SPECIAL ARTISTRY & CRAFT NOTES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = order.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Pricing calculation sheet
                        Text(
                            text = "VALUATION & BILLING SHEET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        BillingTicket(order = order)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailMetricsGrid(
    order: Order,
    onUpdateItemStatus: (Int, String) -> Unit
) {
    val items = order.getItems()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { index, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Diamond, contentDescription = "Jewellery icon", tint = Color(0xFFC5A059), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Item #${index + 1}: ${item.jewelleryType}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        StatusBadge(status = item.status)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))

                    val formattedPurity = if (item.purity.toDoubleOrNull() != null) "${item.purity}%" else item.purity

                    val gridItems = listOf(
                        Pair("Metal Type", item.metalType),
                        Pair("Purity Profile", formattedPurity),
                        Pair("Weight (g)", "${String.format(Locale.getDefault(), "%.2f", item.approxWeight)} g"),
                        Pair("Agreed Rate", "₹${String.format(Locale.getDefault(), "%,.0f", item.agreedRate)}/g"),
                        Pair("Making Charge", "${String.format(Locale.getDefault(), "%.1f", item.makingCharges)}%"),
                        Pair("Extra / Stones", "₹${String.format(Locale.getDefault(), "%,.0f", item.otherCharges)}")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        gridItems.chunked(2).forEach { rowPairs ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                rowPairs.forEach { pair ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = pair.first, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = pair.second, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ITEM STATUS TASKFLOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val statusesList = listOf("Pending", "In Progress", "Completed", "Delivered")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statusesList.forEach { stateItem ->
                            val active = item.status == stateItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (active) Color(0xFFC5A059) else MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.4f
                                        )
                                    )
                                    .clickable { onUpdateItemStatus(index, stateItem) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stateItem,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillingTicket(order: Order) {
    val items = order.getItems()
    val oldItems = order.getOldItems()
    val balanceDue = order.totalAmount - order.advancePaid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAF3)),
        border = BorderStroke(0.5.dp, Color(0xFFEADBBE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            items.forEachIndexed { index, item ->
                val purityPercent = (item.purity.toDoubleOrNull() ?: 100.0) / 100.0
                val metalValValue = item.approxWeight * item.agreedRate * purityPercent
                val isSilver = item.metalType.equals("Silver", ignoreCase = true)
                val makingChargesAmount = if (isSilver) item.makingCharges else (metalValValue * (item.makingCharges / 100.0))
                val itemTotal = metalValValue + makingChargesAmount + item.otherCharges

                Text(
                    text = "Item #${index + 1}: ${item.jewelleryType}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C251C)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val purityLabel = if (item.purity.toDoubleOrNull() != null) "${item.purity}%" else item.purity
                    Text(
                        text = "  · Metal Value (${String.format(Locale.getDefault(), "%.2f", item.approxWeight)}g @ ₹${String.format(Locale.getDefault(), "%,.0f", item.agreedRate)}, $purityLabel)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF5C5243)
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", metalValValue)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C251C)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelText = if (isSilver) "  · Making Charges (Flat addition)" else "  · Making Charges (${String.format(Locale.getDefault(), "%.1f", item.makingCharges)}%)"
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF5C5243)
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", makingChargesAmount)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C251C)
                    )
                }
                if (item.otherCharges > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "  · Stones, Diamonds & others",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF5C5243)
                        )
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%,.0f", item.otherCharges)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2C251C)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "  Item #${index + 1} Subtotal",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF5C5243)
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", itemTotal)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C251C)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (index < items.lastIndex) {
                    HorizontalDivider(color = Color(0xFFEADBBE).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            HorizontalDivider(color = Color(0xFFEADBBE), modifier = Modifier.padding(vertical = 8.dp))

            // Unreduced Grand Total & Exchange Credits
            val unreducedGrandTotal = items.sumOf { item ->
                val purityPercent = (item.purity.toDoubleOrNull() ?: 100.0) / 100.0
                val metalValValue = item.approxWeight * item.agreedRate * purityPercent
                val isSilver = item.metalType.equals("Silver", ignoreCase = true)
                val makingChargesAmount = if (isSilver) item.makingCharges else (metalValValue * (item.makingCharges / 100.0))
                metalValValue + makingChargesAmount + item.otherCharges
            }

            if (oldItems.isNotEmpty()) {
                val totalExchangeCredit = oldItems.sumOf { calculateOldItemValuation(it, items) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Specs Total Charges:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5C5243))
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", unreducedGrandTotal)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2C251C)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Traded-In Exchanges Credit:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF137333))
                    Text(
                        text = "- ₹${String.format(Locale.getDefault(), "%,.0f", totalExchangeCredit)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                    )
                }
                Spacer(modifier = Modifier.padding(vertical = 1.dp))
                HorizontalDivider(color = Color(0xFFEADBBE).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "TOTAL ESTIMATED CHARGES", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black), color = Color(0xFF2C251C))
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                    color = Color(0xFF2C251C)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Advance Deposit Paid", style = MaterialTheme.typography.bodySmall, color = Color(0xFF856404))
                Text(
                    text = "- ₹${String.format(Locale.getDefault(), "%,.0f", order.advancePaid)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black, color = Color(0xFF856404))
                )
            }

            Spacer(modifier = Modifier.padding(vertical = 2.dp))
            HorizontalDivider(color = Color(0xFFEADBBE), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (balanceDue > 0) "BALANCE PAYABLE TO COLLECT" else "SETTLED / PAID FULL",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                    color = if (balanceDue > 0) Color(0xFF2C251C) else Color(0xFF137333),
                    modifier = Modifier.weight(1f)
                )
                val dynamicPayableFontSize = if (balanceDue > 99999) 15.sp else 18.sp
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", balanceDue)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = dynamicPayableFontSize,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (balanceDue > 0) Color(0xFFC5A059) else Color(0xFF137333)
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// Editable item representation for live edits in text fields
data class EditableItem(
    val id: String = UUID.randomUUID().toString(),
    val jewelleryType: String = "Ring",
    val metalType: String = "Gold",
    val purity: String = "91.6",
    val approxWeight: String = "",
    val agreedRate: String = "",
    val makingCharges: String = "0",
    val otherCharges: String = "0",
    val status: String = "Pending"
)

data class EditableOldItem(
    val id: String = UUID.randomUUID().toString(),
    val itemName: String = "",
    val metalType: String = "Gold",
    val approxWeight: String = "",
    val purity: String = "91.6",
    val agreedRate: String = ""
)

fun calculateOldItemValuation(oldItem: OldOrderItem, items: List<OrderItem>): Double {
    val fineWeight = oldItem.approxWeight * (oldItem.purity / 100.0)
    val rate = if (oldItem.agreedRate > 0.0) {
        oldItem.agreedRate
    } else {
        items.firstOrNull { it.metalType.equals(oldItem.metalType, ignoreCase = true) }?.agreedRate ?: 0.0
    }
    return fineWeight * rate
}

fun calculateOldItemValuationEditable(oldItem: EditableOldItem, itemsList: List<EditableItem>): Double {
    val wt = oldItem.approxWeight.toDoubleOrNull() ?: 0.0
    val pur = oldItem.purity.toDoubleOrNull() ?: 0.0
    val fineWeight = wt * (pur / 100.0)
    val rateInput = oldItem.agreedRate.toDoubleOrNull()
    val rate = if (rateInput != null && rateInput > 0.0) {
        rateInput
    } else {
        itemsList.firstOrNull { it.metalType.equals(oldItem.metalType, ignoreCase = true) }?.agreedRate?.toDoubleOrNull() ?: 0.0
    }
    return fineWeight * rate
}

fun calculateItemTotal(item: EditableItem): Double {
    val wt = item.approxWeight.toDoubleOrNull() ?: 0.0
    val rate = item.agreedRate.toDoubleOrNull() ?: 0.0
    val mak = item.makingCharges.toDoubleOrNull() ?: 0.0
    val other = item.otherCharges.toDoubleOrNull() ?: 0.0
    val purityProfile = item.purity.toDoubleOrNull() ?: 100.0
    val purityPercent = purityProfile / 100.0

    val rawCost = wt * rate * purityPercent
    val isSilver = item.metalType.equals("Silver", ignoreCase = true)
    val makVal = if (isSilver) mak else (rawCost * (mak / 100.0))
    return rawCost + makVal + other
}

fun calculateModelItemTotal(item: OrderItem): Double {
    val wt = item.approxWeight
    val rate = item.agreedRate
    val mak = item.makingCharges
    val other = item.otherCharges
    val purityProfile = item.purity.toDoubleOrNull() ?: 100.0
    val purityPercent = purityProfile / 100.0

    val rawCost = wt * rate * purityPercent
    val isSilver = item.metalType.equals("Silver", ignoreCase = true)
    val makVal = if (isSilver) mak else (rawCost * (mak / 100.0))
    return rawCost + makVal + other
}

// Pop up form Dialog to ADD or EDIT details. Highly validated.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderDialog(
    order: Order?, // null for Add, populated for Edit
    onDismiss: () -> Unit,
    onConfirm: (Order) -> Unit
) {
    val context = LocalContext.current

    var customerName by remember { mutableStateOf(order?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(order?.customerPhone ?: "") }
    
    // Track the list of editable jewellery items!
    var itemsList by remember {
        mutableStateOf(
            order?.getItems()?.map { item ->
                EditableItem(
                    id = item.id,
                    jewelleryType = item.jewelleryType,
                    metalType = item.metalType,
                    purity = item.purity,
                    approxWeight = if (item.approxWeight == 0.0) "" else item.approxWeight.toString(),
                    agreedRate = if (item.agreedRate == 0.0) "" else item.agreedRate.toString(),
                    makingCharges = item.makingCharges.toString(),
                    otherCharges = item.otherCharges.toString(),
                    status = item.status
                )
            } ?: listOf(EditableItem())
        )
    }

    var oldItemsList by remember {
        mutableStateOf<List<EditableOldItem>>(
            order?.getOldItems()?.map { item ->
                EditableOldItem(
                    id = item.id,
                    itemName = item.itemName,
                    metalType = item.metalType,
                    approxWeight = if (item.approxWeight == 0.0) "" else item.approxWeight.toString(),
                    purity = if (item.purity == 0.0) "" else item.purity.toString(),
                    agreedRate = if (item.agreedRate == 0.0) "" else item.agreedRate.toString()
                )
            } ?: emptyList<EditableOldItem>()
        )
    }

    var advancePaid by remember { mutableStateOf(order?.advancePaid?.toString() ?: "0") }
    var notes by remember { mutableStateOf(order?.notes ?: "") }

    val defaultCal = Calendar.getInstance()
    if (order != null) {
        defaultCal.timeInMillis = order.expectedDeliveryDate
    } else {
        defaultCal.add(Calendar.DAY_OF_YEAR, 7) // Default 7 days from now
    }
    var expectedDeliveryDate by remember { mutableStateOf(defaultCal.timeInMillis) }

    // Live Validation State
    var isNameError by remember { mutableStateOf(false) }
    var itemsErrorIndex by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var oldItemsErrorIndex by remember { mutableStateOf<Set<Int>>(emptySet()) }

    fun updateItemSafe(index: Int, block: (EditableItem) -> EditableItem) {
        val newList = itemsList.toMutableList()
        if (index in newList.indices) {
            newList[index] = block(newList[index])
            itemsList = newList
        }
    }

    fun updateOldItemSafe(optIndex: Int, block: (EditableOldItem) -> EditableOldItem) {
        val newList = oldItemsList.toMutableList()
        if (optIndex in newList.indices) {
            newList[optIndex] = block(newList[optIndex])
            oldItemsList = newList
        }
    }

    // Jewelry Categories dropdown options
    val jewelleryOptions = listOf("Ring", "Necklace", "Earrings", "Bracelet", "Bangle", "Pendant", "Chain", "Anklet", "Custom Design")
    val metalOptions = listOf("Gold", "Silver")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Form Header
                Text(
                    text = if (order == null) "New Jewellery Order" else "Edit Order Specifications",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "CUSTOMER IDENTIFICATION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                            color = Color(0xFFC5A059)
                        )
                    }

                    // Customer Name
                    item {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = {
                                customerName = it
                                isNameError = it.isBlank()
                            },
                            label = { Text("Customer Name *") },
                            placeholder = { Text("Samantha Smith") },
                            isError = isNameError,
                            supportingText = { if (isNameError) Text("Name is a required field") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )
                    }

                    // Phone Number
                    item {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Contact Number") },
                            placeholder = { Text("e.g. +91 9876543210") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JEWELLERY ITEMS (${itemsList.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                                color = Color(0xFFC5A059)
                            )
                        }
                    }

                    // Multi-item form fields
                    itemsList.forEachIndexed { index, itemState ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("item_card_$index"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(
                                    width = 0.5.dp,
                                    color = if (itemsErrorIndex.contains(index)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Item #${index + 1}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (itemsList.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    val newList = itemsList.toMutableList()
                                                    newList.removeAt(index)
                                                    itemsList = newList
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove Item",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    // First Row: Jewellery Type & Metal Type
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Jewellery Type TextField (Fully Editable as requested)
                                        OutlinedTextField(
                                            value = itemState.jewelleryType,
                                            onValueChange = { newVal ->
                                                updateItemSafe(index) { it.copy(jewelleryType = newVal) }
                                            },
                                            label = { Text("Jewellery Category") },
                                            placeholder = { Text("e.g. Ring, Necklace") },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Metal Type Dropdown with highly responsive click wrapper
                                        var expandedMetal by remember { mutableStateOf(false) }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { expandedMetal = true }
                                        ) {
                                            OutlinedTextField(
                                                value = itemState.metalType,
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("Metal Archetype") },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                trailingIcon = {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Show metals")
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = expandedMetal,
                                                onDismissRequest = { expandedMetal = false }
                                            ) {
                                                metalOptions.forEach { option ->
                                                    DropdownMenuItem(
                                                        text = { Text(option) },
                                                        onClick = {
                                                            val defaultPurity = when (option) {
                                                                "Gold" -> "91.6"
                                                                "Silver" -> "92.5"
                                                                "Platinum" -> "95.0"
                                                                "Rose Gold" -> "75.0"
                                                                else -> "91.6"
                                                            }
                                                            updateItemSafe(index) {
                                                                it.copy(
                                                                    metalType = option,
                                                                    purity = defaultPurity
                                                                )
                                                            }
                                                            expandedMetal = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Second Row: Purity Profile & Weight
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = itemState.purity,
                                            onValueChange = { value ->
                                                updateItemSafe(index) { it.copy(purity = value) }
                                            },
                                            label = { Text("Purity % / Karat") },
                                            placeholder = { Text("91.6 (22K)") },
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = itemState.approxWeight,
                                            onValueChange = { value ->
                                                updateItemSafe(index) { it.copy(approxWeight = value) }
                                            },
                                            label = { Text("Approx Weight (g) *") },
                                            placeholder = { Text("15.5") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Third Row: Agreed Rate Per Gram & Making Charges (%)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = itemState.agreedRate,
                                            onValueChange = { value ->
                                                updateItemSafe(index) { it.copy(agreedRate = value) }
                                            },
                                            label = { Text("Agreed Rate / g (₹)") },
                                            placeholder = { Text("7100") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = itemState.makingCharges,
                                                onValueChange = { value ->
                                                    updateItemSafe(index) { it.copy(makingCharges = value) }
                                                },
                                                label = { Text("Making Charges %") },
                                                placeholder = { Text("12") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            if (itemState.metalType.equals("Gold", ignoreCase = true)) {
                                                val r = itemState.agreedRate.toDoubleOrNull() ?: 0.0
                                                val m = itemState.makingCharges.toDoubleOrNull() ?: 0.0
                                                val rs = r * (m / 100.0)
                                                if (rs > 0.0) {
                                                     Spacer(modifier = Modifier.height(2.dp))
                                                     Text(
                                                         text = "≈ ₹${String.format(Locale.getDefault(), "%,.2f", rs)} / g",
                                                         style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                         color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                                         modifier = Modifier.padding(start = 4.dp)
                                                     )
                                                }
                                            }
                                        }
                                    }

                                    // Fourth Row: Other Charges & Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = itemState.otherCharges,
                                            onValueChange = { value ->
                                                updateItemSafe(index) { it.copy(otherCharges = value) }
                                            },
                                            label = { Text("Stones/Other ₹") },
                                            placeholder = { Text("500") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Status dropdown with highly responsive click wrapper
                                        var expandedStatus by remember { mutableStateOf(false) }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { expandedStatus = true }
                                        ) {
                                            OutlinedTextField(
                                                value = itemState.status,
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("Delivery Status") },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                trailingIcon = {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Show statuses")
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = expandedStatus,
                                                onDismissRequest = { expandedStatus = false }
                                            ) {
                                                listOf("Pending", "In Progress", "Completed", "Delivered").forEach { statusOpt ->
                                                    DropdownMenuItem(
                                                        text = { Text(statusOpt) },
                                                        onClick = {
                                                            updateItemSafe(index) { it.copy(status = statusOpt) }
                                                            expandedStatus = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Real-time valuation display for this individual item card!
                                    val itemValuation = calculateItemTotal(itemState)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "Item Total: ₹${String.format(Locale.getDefault(), "%,.2f", itemValuation)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                itemsList = itemsList + EditableItem()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC5A059)),
                            border = BorderStroke(1.dp, Color(0xFFC5A059).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add another item", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Another Jewellery Item", fontWeight = FontWeight.Bold)
                        }
                    }

                    // --- OLD JEWELLERY RECEIVED (EXCHANGE) SECTION ---
                    item {
                        DividerIndicator()
                        Text(
                            text = "OLD JEWELLERY INTERACTIVES (EXCHANGE)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                            color = Color(0xFF6200EE),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (oldItemsList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No old jewellery items added. Click below to add an item for exchange.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        oldItemsList.forEachIndexed { optIndex, oldItem ->
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                    ),
                                    border = BorderStroke(
                                        width = 0.5.dp,
                                        color = if (oldItemsErrorIndex.contains(optIndex)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Old Item #${optIndex + 1}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = {
                                                    val newList = oldItemsList.toMutableList()
                                                    newList.removeAt(optIndex)
                                                    oldItemsList = newList
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove old item",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = oldItem.itemName,
                                            onValueChange = { newVal ->
                                                updateOldItemSafe(optIndex) { it.copy(itemName = newVal) }
                                            },
                                            label = { Text("Item Name / Description *") },
                                            placeholder = { Text("Old Gold Necklace") },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6200EE)),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            var expandedOldMetal by remember { mutableStateOf(false) }
                                            Box(modifier = Modifier.weight(1f).clickable { expandedOldMetal = true }) {
                                                OutlinedTextField(
                                                    value = oldItem.metalType,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    enabled = false,
                                                    label = { Text("Metal Type") },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select metal") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                DropdownMenu(
                                                    expanded = expandedOldMetal,
                                                    onDismissRequest = { expandedOldMetal = false }
                                                ) {
                                                    listOf("Gold", "Silver").forEach { opt ->
                                                        DropdownMenuItem(
                                                            text = { Text(opt) },
                                                            onClick = {
                                                                updateOldItemSafe(optIndex) { it.copy(metalType = opt) }
                                                                expandedOldMetal = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = oldItem.approxWeight,
                                                onValueChange = { newVal ->
                                                    updateOldItemSafe(optIndex) { it.copy(approxWeight = newVal) }
                                                },
                                                label = { Text("Weight (g) *") },
                                                placeholder = { Text("10.5") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6200EE)),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = oldItem.purity,
                                                onValueChange = { newVal ->
                                                    updateOldItemSafe(optIndex) { it.copy(purity = newVal) }
                                                },
                                                label = { Text("Purity (%) *") },
                                                placeholder = { Text("91.6") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6200EE)),
                                                modifier = Modifier.weight(1f)
                                            )

                                            val oldWt = oldItem.approxWeight.toDoubleOrNull() ?: 0.0
                                            val oldPur = oldItem.purity.toDoubleOrNull() ?: 0.0
                                            val fineValue = oldWt * (oldPur / 100.0)

                                            OutlinedTextField(
                                                value = "${String.format(Locale.getDefault(), "%.3f", fineValue)} g",
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("Fine Wt (wt*purity)") },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = oldItem.agreedRate,
                                                onValueChange = { newVal ->
                                                    updateOldItemSafe(optIndex) { it.copy(agreedRate = newVal) }
                                                },
                                                label = { Text("Exch. Rate (₹/g)") },
                                                placeholder = {
                                                    val fallback = itemsList.firstOrNull { it.metalType.equals(oldItem.metalType, ignoreCase = true) }?.agreedRate ?: ""
                                                    if (fallback.isEmpty()) "e.g. 7100" else "Auto: $fallback"
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6200EE)),
                                                modifier = Modifier.weight(1f)
                                            )

                                            val oldWt = oldItem.approxWeight.toDoubleOrNull() ?: 0.0
                                            val oldPur = oldItem.purity.toDoubleOrNull() ?: 0.0
                                            val fineValue = oldWt * (oldPur / 100.0)
                                            val rateUsed = oldItem.agreedRate.toDoubleOrNull() ?: itemsList.firstOrNull { it.metalType.equals(oldItem.metalType, ignoreCase = true) }?.agreedRate?.toDoubleOrNull() ?: 0.0
                                            val valuationVal = fineValue * rateUsed

                                            OutlinedTextField(
                                                value = "₹${String.format(Locale.getDefault(), "%,.1f", valuationVal)}",
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = false,
                                                label = { Text("Valuation (₹)") },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    disabledTextColor = if (valuationVal > 0) Color(0xFF137333) else MaterialTheme.colorScheme.onSurface,
                                                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                oldItemsList = oldItemsList + EditableOldItem()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6200EE)),
                            border = BorderStroke(1.dp, Color(0xFF6200EE).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add old jewellery exchange", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Old Jewellery For Exchange", fontWeight = FontWeight.Bold)
                        }
                    }

                    // --- GOLD & SILVER LIVE FINE WEIGHT METAL SHEET ---
                    item {
                        val newGoldFine = itemsList.filter { it.metalType.equals("Gold", ignoreCase = true) }.sumOf {
                            val wt = it.approxWeight.toDoubleOrNull() ?: 0.0
                            val pur = (it.purity.toDoubleOrNull() ?: 100.0) / 100.0
                            wt * pur
                        }
                        val oldGoldFine = oldItemsList.filter { it.metalType.equals("Gold", ignoreCase = true) }.sumOf {
                            val wt = it.approxWeight.toDoubleOrNull() ?: 0.0
                            val pur = (it.purity.toDoubleOrNull() ?: 0.0) / 100.0
                            wt * pur
                        }
                        val netGoldFine = newGoldFine - oldGoldFine

                        val newSilverFine = itemsList.filter { it.metalType.equals("Silver", ignoreCase = true) }.sumOf {
                            val wt = it.approxWeight.toDoubleOrNull() ?: 0.0
                            val pur = (it.purity.toDoubleOrNull() ?: 100.0) / 100.0
                            wt * pur
                        }
                        val oldSilverFine = oldItemsList.filter { it.metalType.equals("Silver", ignoreCase = true) }.sumOf {
                            val wt = it.approxWeight.toDoubleOrNull() ?: 0.0
                            val pur = (it.purity.toDoubleOrNull() ?: 0.0) / 100.0
                            wt * pur
                        }
                        val netSilverFine = newSilverFine - oldSilverFine

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "LIVE FINE METAL BALANCING SHEET",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFFC5A059)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                // Gold
                                Text("Gold Metal Area", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFC5A059))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("New Specs Fine Gold:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format(Locale.getDefault(), "%.3f", newGoldFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Old Exchange Fine Gold:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("- ${String.format(Locale.getDefault(), "%.3f", oldGoldFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF137333)))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Net Required Gold Fine:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                                    Text("${String.format(Locale.getDefault(), "%.3f", netGoldFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (netGoldFine >= 0) Color(0xFFC5A059) else Color(0xFF137333)))
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                                // Silver
                                Text("Silver Metal Area", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF7F8C8D))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("New Specs Fine Silver:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format(Locale.getDefault(), "%.3f", newSilverFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Old Exchange Fine Silver:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("- ${String.format(Locale.getDefault(), "%.3f", oldSilverFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF137333)))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Net Required Silver Fine:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                                    Text("${String.format(Locale.getDefault(), "%.3f", netSilverFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (netSilverFine >= 0) Color(0xFF7F8C8D) else Color(0xFF137333)))
                                }
                            }
                        }
                    }

                    item {
                        DividerIndicator()
                        Text(
                            text = "FINANCIALS & DELIVERY TIMELINE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                            color = Color(0xFFC5A059)
                        )
                    }

                    // Advance Paid Deposit
                    item {
                        OutlinedTextField(
                            value = advancePaid,
                            onValueChange = { advancePaid = it },
                            label = { Text("Advance Deposit Given (₹)") },
                            placeholder = { Text("e.g. 1500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("advance_input")
                        )
                    }

                    // Delivery Date Picker
                    item {
                        val expectedDateString = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(expectedDeliveryDate))
                        Column {
                            Text(text = "Expected Delivery Target Date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Platform DatePickerDialog helper integration
                            val datePickerDialog = android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCal = Calendar.getInstance()
                                    selectedCal.set(year, month, dayOfMonth)
                                    expectedDeliveryDate = selectedCal.timeInMillis
                                },
                                defaultCal.get(Calendar.YEAR),
                                defaultCal.get(Calendar.MONTH),
                                defaultCal.get(Calendar.DAY_OF_MONTH)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable { datePickerDialog.show() }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = expectedDateString,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Trigger delivery calendar",
                                    tint = Color(0xFFC5A059)
                                )
                            }
                        }
                    }

                    // Custom Notes / Remarks
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Special Artistry & Design Notes") },
                            placeholder = { Text("Provide custom request specifics, gems color, inscriptions...") },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notes_input")
                        )
                    }

                    // Active aggregate preview breakdown with exchange subtraction
                    item {
                        val computedOverallTotal = itemsList.sumOf { calculateItemTotal(it) }
                        val totalExchangeCredit = oldItemsList.sumOf { calculateOldItemValuationEditable(it, itemsList) }

                        val finalPayableAmount = maxOf(0.0, computedOverallTotal - totalExchangeCredit)
                        val givenDeposit = advancePaid.toDoubleOrNull() ?: 0.0
                        val netBalance = finalPayableAmount - givenDeposit

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Grand Total Specs:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${String.format(Locale.getDefault(), "%,.2f", computedOverallTotal)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                if (totalExchangeCredit > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Exchange Valuation Credit:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF137333))
                                        Text("- ₹${String.format(Locale.getDefault(), "%,.2f", totalExchangeCredit)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF137333)))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Net Payable Amount:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    Text("₹${String.format(Locale.getDefault(), "%,.2f", finalPayableAmount)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Deposit Advance Paid:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("- ₹${String.format(Locale.getDefault(), "%,.2f", givenDeposit)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Net Balance Due:",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val balanceTextSize = when {
                                        netBalance >= 100000.0 -> 14.sp
                                        netBalance >= 10000.0 -> 16.sp
                                        else -> 18.sp
                                    }
                                    Text(
                                        text = "₹${String.format(Locale.getDefault(), "%,.2f", netBalance)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = balanceTextSize,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = if (netBalance > 0) Color(0xFFBA1A1A) else Color(0xFF388E3C),
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .testTag("net_balance_text"),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Pinned validation warning above the bottom action bar
                if (isNameError || itemsErrorIndex.isNotEmpty() || oldItemsErrorIndex.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .background(Color(0xFFFDF2F2), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFF5C2C2), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Pinned Validation Warning",
                                tint = Color(0xFFC53030),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when {
                                    isNameError -> "Customer Name is required."
                                    itemsErrorIndex.isNotEmpty() -> "Please specify valid weights (g) for all jewellery items."
                                    oldItemsErrorIndex.isNotEmpty() -> "Please enter valid item description, weight & details for all exchange old items."
                                    else -> "Please correct highlighted fields before submitting."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFFC53030)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                // Bottom bar actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            isNameError = customerName.isBlank()
                            
                            // Validate weights of each item
                            val invalidIndices = itemsList.mapIndexedNotNull { index, item ->
                                val wt = item.approxWeight.toDoubleOrNull()
                                if (wt == null || wt <= 0.0) index else null
                            }.toSet()
                            itemsErrorIndex = invalidIndices

                            // Validate weights & description of old items
                            val invalidOldIndices = oldItemsList.mapIndexedNotNull { index, item ->
                                val wt = item.approxWeight.toDoubleOrNull()
                                val name = item.itemName.trim()
                                if (wt == null || wt <= 0.0 || name.isBlank()) index else null
                            }.toSet()
                            oldItemsErrorIndex = invalidOldIndices

                            val toastMsg = when {
                                isNameError -> "Customer Name is required."
                                invalidIndices.isNotEmpty() -> "Please specify valid approx weights for all jewellery items."
                                invalidOldIndices.isNotEmpty() -> "Please enter valid descriptions and weights for all exchange old items."
                                else -> null
                            }
                            if (toastMsg != null) {
                                android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_LONG).show()
                            }

                            if (!isNameError && invalidIndices.isEmpty() && invalidOldIndices.isEmpty()) {
                                val finalItems = itemsList.map { item ->
                                    OrderItem(
                                        id = item.id,
                                        jewelleryType = item.jewelleryType,
                                        metalType = item.metalType,
                                        purity = item.purity,
                                        approxWeight = item.approxWeight.toDoubleOrNull() ?: 0.0,
                                        agreedRate = item.agreedRate.toDoubleOrNull() ?: 0.0,
                                        makingCharges = item.makingCharges.toDoubleOrNull() ?: 0.0,
                                        otherCharges = item.otherCharges.toDoubleOrNull() ?: 0.0,
                                        status = item.status
                                    )
                                }
                                val primaryItem = finalItems.first()

                                val finalOldItems = oldItemsList.map { item ->
                                    OldOrderItem(
                                        id = item.id,
                                        itemName = item.itemName,
                                        metalType = item.metalType,
                                        approxWeight = item.approxWeight.toDoubleOrNull() ?: 0.0,
                                        purity = item.purity.toDoubleOrNull() ?: 0.0,
                                        agreedRate = item.agreedRate.toDoubleOrNull() ?: 0.0
                                    )
                                }

                                // Synthesise primary metrics based on all items and old items
                                val aggregatedTotalSum = finalItems.sumOf { calculateModelItemTotal(it) }
                                val aggregatedWeightSum = finalItems.sumOf { it.approxWeight }

                                val totalExchangeCredit = finalOldItems.sumOf { calculateOldItemValuation(it, finalItems) }
                                val finalPayableAmount = maxOf(0.0, aggregatedTotalSum - totalExchangeCredit)
                                
                                // Overall delivery status: if all items are Delivered -> Delivered; if all are Completed -> Completed; else In Progress / Pending
                                val overallStatus = when {
                                    finalItems.all { it.status == "Delivered" } -> "Delivered"
                                    finalItems.all { it.status == "Completed" || it.status == "Delivered" } -> "Completed"
                                    finalItems.any { it.status == "In Progress" || it.status == "Completed" } -> "In Progress"
                                    else -> "Pending"
                                }

                                val customerOrder = Order(
                                    id = order?.id ?: 0,
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    jewelleryType = if (finalItems.size > 1) {
                                        finalItems.joinToString { it.jewelleryType }
                                    } else {
                                        primaryItem.jewelleryType
                                    },
                                    metalType = primaryItem.metalType,
                                    purity = primaryItem.purity,
                                    approxWeight = aggregatedWeightSum,
                                    agreedRate = primaryItem.agreedRate,
                                    makingCharges = primaryItem.makingCharges,
                                    otherCharges = finalItems.sumOf { it.otherCharges },
                                    advancePaid = advancePaid.toDoubleOrNull() ?: 0.0,
                                    totalAmount = finalPayableAmount,
                                    orderDate = order?.orderDate ?: System.currentTimeMillis(),
                                    expectedDeliveryDate = expectedDeliveryDate,
                                    notes = notes,
                                    status = overallStatus,
                                    itemsJson = serializeItems(finalItems),
                                    oldItemsJson = serializeOldItems(finalOldItems)
                                )
                                onConfirm(customerOrder)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059))
                    ) {
                        Text(
                            text = if (order == null) "Create Order" else "Save Order Specs",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

// Quick layout helpers
@Composable
fun DividerIndicator() {
    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    Spacer(modifier = Modifier.height(10.dp))
}

fun serializeOrders(orders: List<Order>): String {
    val jsonArray = JSONArray()
    orders.forEach { order ->
        val jsonObject = JSONObject().apply {
            put("id", order.id)
            put("customerName", order.customerName)
            put("customerPhone", order.customerPhone)
            put("jewelleryType", order.jewelleryType)
            put("metalType", order.metalType)
            put("purity", order.purity)
            put("approxWeight", order.approxWeight)
            put("agreedRate", order.agreedRate)
            put("makingCharges", order.makingCharges)
            put("otherCharges", order.otherCharges)
            put("advancePaid", order.advancePaid)
            put("totalAmount", order.totalAmount)
            put("orderDate", order.orderDate)
            put("expectedDeliveryDate", order.expectedDeliveryDate)
            put("notes", order.notes)
            put("status", order.status)
            put("itemsJson", order.itemsJson)
            put("oldItemsJson", order.oldItemsJson)
        }
        jsonArray.put(jsonObject)
    }
    return jsonArray.toString(4)
}

fun deserializeOrders(jsonStr: String): List<Order> {
    val list = mutableListOf<Order>()
    try {
        val jsonArray = JSONArray(jsonStr)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val order = Order(
                id = jsonObject.optInt("id", 0),
                customerName = jsonObject.optString("customerName", ""),
                customerPhone = jsonObject.optString("customerPhone", ""),
                jewelleryType = jsonObject.optString("jewelleryType", ""),
                metalType = jsonObject.optString("metalType", ""),
                purity = jsonObject.optString("purity", ""),
                approxWeight = jsonObject.optDouble("approxWeight", 0.0),
                agreedRate = jsonObject.optDouble("agreedRate", 0.0),
                makingCharges = jsonObject.optDouble("makingCharges", 0.0),
                otherCharges = jsonObject.optDouble("otherCharges", 0.0),
                advancePaid = jsonObject.optDouble("advancePaid", 0.0),
                totalAmount = jsonObject.optDouble("totalAmount", 0.0),
                orderDate = jsonObject.optLong("orderDate", System.currentTimeMillis()),
                expectedDeliveryDate = jsonObject.optLong("expectedDeliveryDate", System.currentTimeMillis()),
                notes = jsonObject.optString("notes", ""),
                status = jsonObject.optString("status", "Pending"),
                itemsJson = jsonObject.optString("itemsJson", ""),
                oldItemsJson = jsonObject.optString("oldItemsJson", "")
            )
            list.add(order)
        }
    } catch (e: Exception) {
         e.printStackTrace()
    }
    return list
}

@Composable
fun TradedInJewelleryCard(order: Order) {
    val oldItems = order.getOldItems()
    if (oldItems.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FAF6)),
        border = BorderStroke(0.5.dp, Color(0xFFBEDADB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TRADED-IN ITEMS SPECIFICATIONS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF137333)
            )
            Spacer(modifier = Modifier.height(10.dp))
            oldItems.forEachIndexed { index, item ->
                val purityLabel = "${String.format(Locale.getDefault(), "%.1f", item.purity)}%"
                val fineWeight = item.approxWeight * (item.purity / 100.0)

                Text(
                    text = "Old Item #${index + 1}: ${item.itemName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1C2C25)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "  · Metal & Purity: ${item.metalType} ($purityLabel)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF435C52)
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.2f", item.approxWeight)} g",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1C2C25)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "  · Subtracted Fine Weight (wt*purity):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF435C52)
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.3f", fineWeight)} g",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                    )
                }
                val fallbackRate = order.getItems().firstOrNull { it.metalType.equals(item.metalType, ignoreCase = true) }?.agreedRate ?: 0.0
                val rateUsed = if (item.agreedRate > 0.0) item.agreedRate else fallbackRate
                val itemValuation = fineWeight * rateUsed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "  · Exchange Rate Used:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF435C52)
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", rateUsed)} / g ${if (item.agreedRate <= 0.0) "(Auto)" else ""}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1C2C25)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "  · Valuation: (Fine wt × Rate):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF435C52)
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.2f", itemValuation)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF117243))
                    )
                }
                if (index < oldItems.size - 1) {
                    HorizontalDivider(color = Color(0xFFBEDADB).copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun MetalBalancingSheet(order: Order) {
    val items = order.getItems()
    val oldItems = order.getOldItems()

    // Aggregates for Gold
    val newGoldFine = items.filter { it.metalType.equals("Gold", ignoreCase = true) }.sumOf {
        val purPercent = (it.purity.toDoubleOrNull() ?: 100.0) / 100.0
        it.approxWeight * purPercent
    }
    val oldGoldFine = oldItems.filter { it.metalType.equals("Gold", ignoreCase = true) }.sumOf {
        it.approxWeight * (it.purity / 100.0)
    }
    val netGoldFine = newGoldFine - oldGoldFine

    // Aggregates for Silver
    val newSilverFine = items.filter { it.metalType.equals("Silver", ignoreCase = true) }.sumOf {
        val purPercent = (it.purity.toDoubleOrNull() ?: 100.0) / 100.0
        it.approxWeight * purPercent
    }
    val oldSilverFine = oldItems.filter { it.metalType.equals("Silver", ignoreCase = true) }.sumOf {
        it.approxWeight * (it.purity / 100.0)
    }
    val netSilverFine = newSilverFine - oldSilverFine

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "FINE METAL RECONCILIATION SHEET",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Gold balancing row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "GOLD METAL BALANCING",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFC5A059)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  · New Gold Fine Weight Total:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format(Locale.getDefault(), "%.3f", newGoldFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  · Old Gold Traded-in Fine:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("- ${String.format(Locale.getDefault(), "%.3f", oldGoldFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF137333)))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  · Net Gold Fine Required:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.3f", netGoldFine)} g",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (netGoldFine >= 0) Color(0xFFC5A059) else Color(0xFF137333))
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Silver balancing row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SILVER METAL BALANCING",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF7F8C8D)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  · New Silver Fine Weight Total:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format(Locale.getDefault(), "%.3f", newSilverFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  · Old Silver Traded-in Fine:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("- ${String.format(Locale.getDefault(), "%.3f", oldSilverFine)} g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF137333)))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 2.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("  · Net Silver Fine Required:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.3f", netSilverFine)} g",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (netSilverFine >= 0) Color(0xFF7F8C8D) else Color(0xFF137333))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogDialog(
    deletedOrders: List<com.example.data.DeletedOrder>,
    onDismiss: () -> Unit,
    onRestore: (com.example.data.DeletedOrder) -> Unit,
    onDeletePermanently: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    var showConfirmClearAll by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFC5A059))
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Activity Log",
                        tint = Color(0xFFC5A059),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Activity Log",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                if (deletedOrders.isNotEmpty()) {
                    IconButton(onClick = { showConfirmClearAll = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all activity log",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = "Deleted orders are retained here for up to 60 days of retention before being permanently removed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (deletedOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "Empty Bin",
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bin is completely empty",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(deletedOrders, key = { it.id }) { item ->
                            val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
                            val deletedDateStr = sdf.format(Date(item.deletedAt))
                            val metalSpecs = "${item.approxWeight}g ${item.metalType} (${item.purity})"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.customerName.ifBlank { "Unnamed Customer" },
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${item.jewelleryType} · $metalSpecs",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "₹${String.format(Locale.getDefault(), "%,.0f", item.totalAmount)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = Color(0xFFC5A059)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Deleted: $deletedDateStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilledTonalButton(
                                                onClick = { onRestore(item) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = Color(0xFFE2F0D9),
                                                    contentColor = Color(0xFF388E3C)
                                                ),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Restore,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Restore", style = MaterialTheme.typography.labelMedium)
                                            }
                                            IconButton(
                                                onClick = { onDeletePermanently(item.id) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteForever,
                                                    contentDescription = "Permanently Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
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
            title = { Text("Purge Activity Bin?") },
            text = { Text("Are you sure you want to permanently delete all items from history? This action is absolutely irreversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showConfirmClearAll = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Purge All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearAll = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

