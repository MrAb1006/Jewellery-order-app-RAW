package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                        text = "AURELIA JEWELLERS · SECURE ORDER ENGINE",
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
                    Column {
                        Text(
                            text = "AURELIA",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Jewellery Orders",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    // Luxury Badge Icon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF2E2615), Color(0xFF131210))))
                            .clickable {
                                Toast.makeText(context, "Aurelia Gold Vault Active", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Royal Seals",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Dashboard Banner
                MetricsPanel(metrics = metrics)

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search customer name, phone, or items...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC5A059),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Flow Status Chips and Sorter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sorting Dropdown Trigger Card
                    var showSortMenu by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { showSortMenu = true }
                            .padding(vertical = 4.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort orders",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFC5A059)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sort: $sortBy",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Sort dropdown"
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

                    Spacer(modifier = Modifier.width(8.dp))

                    // Quick Filter Count tag
                    Text(
                        text = "Found ${orders.size} item(s)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFC5A059),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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
            onUpdateStatus = { status ->
                viewModel.updateOrderStatus(order.id, status)
                // Refresh local dialog state instantly by finding updated order from database logic
                selectedOrderForDetail = order.copy(status = status)
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
                    text = "$${String.format(Locale.getDefault(), "%,.0f", metrics.totalAdvanceCollected)}",
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
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Pending balance collections",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.0f", metrics.pendingCollection)}",
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.jewelleryType,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spec row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metal properties
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = "Metal info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${order.metalType} · ${order.purity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Weight
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = "Weight info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.2f", order.approxWeight)} g",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
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
                        text = "Balance: $${String.format(Locale.getDefault(), "%,.1f", balance)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (balance > 0 && order.status != "Delivered") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total $${String.format(Locale.getDefault(), "%,.1f", order.totalAmount)}",
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
    onUpdateStatus: (String) -> Unit,
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

                        // Status Adjustment Bar
                        Text(
                            text = "ORDER STATUS TASKFLOW",
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
                                val active = order.status == stateItem
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (active) Color(0xFFC5A059) else MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.4f
                                            )
                                        )
                                        .clickable { onUpdateStatus(stateItem) }
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

                        DetailMetricsGrid(order = order)

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

// Subcomponents for Detail Dialog Grid
@Composable
fun DetailMetricsGrid(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val gridItems = listOf<Triple<String, String, ImageVector>>(
                Triple("Jewellery Type", order.jewelleryType, Icons.Default.Diamond),
                Triple("Metal Type", order.metalType, Icons.Default.FilterVintage),
                Triple("Purity Profile", order.purity, Icons.Default.Verified),
                Triple("Exact Weight", "${String.format(Locale.getDefault(), "%.3f", order.approxWeight)} grams", Icons.Default.Scale),
                Triple("Order Book Date", SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(order.orderDate)), Icons.Default.Note),
                Triple("Due Delivery Date", SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(order.expectedDeliveryDate)), Icons.Default.Event)
            )

            gridItems.forEachIndexed { i, entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = entry.third, contentDescription = entry.first, tint = Color(0xFFC5A059), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(entry.first, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(entry.second, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                if (i < gridItems.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// Ledger ticket style layout for pricing
@Composable
fun BillingTicket(order: Order) {
    val metalValValue = order.approxWeight * order.agreedRate
    val balanceDue = order.totalAmount - order.advancePaid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAF3)),
        border = BorderStroke(0.5.dp, Color(0xFFEADBBE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Precious Metal value (${String.format(Locale.getDefault(), "%.2f", order.approxWeight)}g @ $${order.agreedRate}/g)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5C5243)
                )
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.2f", metalValValue)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C251C)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Artisan / Making charges", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5C5243))
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.2f", order.makingCharges)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C251C)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Stones, Diamonds & others", style = MaterialTheme.typography.bodySmall, color = Color(0xFF5C5243))
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.2f", order.otherCharges)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2C251C)
                )
            }

            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            HorizontalDivider(color = Color(0xFFEADBBE), modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "TOTAL VALUE ESTIMATE", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black), color = Color(0xFF2C251C))
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.2f", order.totalAmount)}",
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
                    text = "- $${String.format(Locale.getDefault(), "%,.2f", order.advancePaid)}",
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
                    color = if (balanceDue > 0) Color(0xFF2C251C) else Color(0xFF137333)
                )
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.2f", balanceDue)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (balanceDue > 0) Color(0xFFC5A059) else Color(0xFF137333)
                    )
                )
            }
        }
    }
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
    var jewelleryType by remember { mutableStateOf(order?.jewelleryType ?: "Ring") }
    var metalType by remember { mutableStateOf(order?.metalType ?: "Gold") }
    var purity by remember { mutableStateOf(order?.purity ?: "22K (916)") }
    var approxWeight by remember { mutableStateOf(order?.approxWeight?.toString() ?: "") }
    var agreedRate by remember { mutableStateOf(order?.agreedRate?.toString() ?: "") }
    var makingCharges by remember { mutableStateOf(order?.makingCharges?.toString() ?: "0") }
    var otherCharges by remember { mutableStateOf(order?.otherCharges?.toString() ?: "0") }
    var advancePaid by remember { mutableStateOf(order?.advancePaid?.toString() ?: "0") }
    var notes by remember { mutableStateOf(order?.notes ?: "") }
    var status by remember { mutableStateOf(order?.status ?: "Pending") }

    val defaultCal = Calendar.getInstance()
    if (order != null) {
        defaultCal.timeInMillis = order.expectedDeliveryDate
    } else {
        defaultCal.add(Calendar.DAY_OF_YEAR, 7) // Default 7 days from now
    }
    var expectedDeliveryDate by remember { mutableStateOf(defaultCal.timeInMillis) }

    // Live Validation State
    var isNameError by remember { mutableStateOf(false) }
    var isWeightError by remember { mutableStateOf(false) }

    // Jewelry Categories dropdown options
    val jewelleryOptions = listOf("Ring", "Necklace", "Earrings", "Bracelet", "Bangle", "Pendant", "Chain", "Anklet", "Custom Design")
    val metalOptions = listOf("Gold", "Silver", "Platinum", "Rose Gold")
    val purityOptionsMap = mapOf(
        "Gold" to listOf("22K (916)", "18K (750)", "14K (585)", "24K (Pure)"),
        "Silver" to listOf("92.5% Sterling", "99.9% Pure"),
        "Platinum" to listOf("Pt950", "Pt900"),
        "Rose Gold" to listOf("18K (750)", "14K (585)")
    )
    val activePurityOptions = purityOptionsMap[metalType] ?: listOf("22K (916)", "18K (750)")

    // Dynamic state synchronizer if purity options change
    LaunchedEffect(metalType) {
        if (!activePurityOptions.contains(purity)) {
            purity = activePurityOptions.firstOrNull() ?: ""
        }
    }

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
                            placeholder = { Text("+1 (555) 123-4567") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )
                    }

                    item {
                        DividerIndicator()
                        Text(
                            text = "JEWEL SPECIFICATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                            color = Color(0xFFC5A059)
                        )
                    }

                    // Layout split: Ornaments Type Dropdown (Ring, Necklace, earring etc)
                    item {
                        var expandedJewel by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedJewel,
                            onExpandedChange = { expandedJewel = !expandedJewel }
                        ) {
                            OutlinedTextField(
                                value = jewelleryType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jewellery Ornament") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedJewel) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedJewel,
                                onDismissRequest = { expandedJewel = false }
                            ) {
                                jewelleryOptions.forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption) },
                                        onClick = {
                                            jewelleryType = typeOption
                                            expandedJewel = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Layout split: Metal and Purity Profile
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Metal Picker
                            var expandedMetal by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedMetal,
                                    onExpandedChange = { expandedMetal = !expandedMetal }
                                ) {
                                    OutlinedTextField(
                                        value = metalType,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Metal") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMetal) },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedMetal,
                                        onDismissRequest = { expandedMetal = false }
                                    ) {
                                        metalOptions.forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt) },
                                                onClick = {
                                                    metalType = opt
                                                    expandedMetal = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Purity level Picker
                            var expandedPurity by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedPurity,
                                    onExpandedChange = { expandedPurity = !expandedPurity }
                                ) {
                                    OutlinedTextField(
                                        value = purity,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Purity / Carat") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPurity) },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedPurity,
                                        onDismissRequest = { expandedPurity = false }
                                    ) {
                                        activePurityOptions.forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt) },
                                                onClick = {
                                                    purity = opt
                                                    expandedPurity = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Approx weight
                    item {
                        OutlinedTextField(
                            value = approxWeight,
                            onValueChange = {
                                approxWeight = it
                                isWeightError = it.toDoubleOrNull() == null || it.toDouble() <= 0.0
                            },
                            label = { Text("Approx Weight * (grams)") },
                            placeholder = { Text("0.000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = isWeightError,
                            supportingText = { if (isWeightError) Text("Please type a valid weight greater than zero") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("weight_input")
                        )
                    }

                    item {
                        DividerIndicator()
                        Text(
                            text = "FINANCIAL ESTIMATES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                            color = Color(0xFFC5A059)
                        )
                    }

                    // Agreed rate and Artisan Making charges
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = agreedRate,
                                onValueChange = { agreedRate = it },
                                label = { Text("Metal Rate ($/g)") },
                                placeholder = { Text("72.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = makingCharges,
                                onValueChange = { makingCharges = it },
                                label = { Text("Making Charges") },
                                placeholder = { Text("250.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Option detail row splits of: Stone diamond additions and deposits
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = otherCharges,
                                onValueChange = { otherCharges = it },
                                label = { Text("Stone/Diamonds ($)") },
                                placeholder = { Text("0.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = advancePaid,
                                onValueChange = { advancePaid = it },
                                label = { Text("Deposit Advance ($)") },
                                placeholder = { Text("500.0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Live auto billing banner estimate
                    item {
                        val wt = approxWeight.toDoubleOrNull() ?: 0.0
                        val rate = agreedRate.toDoubleOrNull() ?: 0.0
                        val mak = makingCharges.toDoubleOrNull() ?: 0.0
                        val stone = otherCharges.toDoubleOrNull() ?: 0.0
                        val netValue = (wt * rate) + mak + stone
                        val deposit = advancePaid.toDoubleOrNull() ?: 0.0
                        val bal = netValue - deposit

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFC5A059).copy(alpha = 0.06f),
                            border = BorderStroke(0.5.dp, Color(0xFFC5A059).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Estimated Order Cost:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$${String.format(Locale.getDefault(), "%,.2f", netValue)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Due on Delivery:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$${String.format(Locale.getDefault(), "%,.2f", bal)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color(0xFFC5A059))
                                }
                            }
                        }
                    }

                    item {
                        DividerIndicator()
                        Text(
                            text = "DELIVERY & ARTISTRY NOTES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                            color = Color(0xFFC5A059)
                        )
                    }

                    // Target Delivery date select
                    item {
                        val simpleFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                        val expectedDateString = simpleFormat.format(Date(expectedDeliveryDate))

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
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { datePickerDialog.show() }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
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

                            // Quick preset dates
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(3, 7, 14, 30).forEach { days ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable {
                                                val quickCal = Calendar.getInstance()
                                                quickCal.add(Calendar.DAY_OF_YEAR, days)
                                                expectedDeliveryDate = quickCal.timeInMillis
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "In $days Days",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Artistry engravings detail text
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Special Artistry & Carving Requirements (engraving size, custom details...)") },
                            placeholder = { Text("Insert special ring sizes, custom floral motif inscriptions, or diamond details...") },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // If editing, status dropdown
                    if (order != null) {
                        item {
                            var expandedStatus by remember { mutableStateOf(false) }
                            Column {
                                DividerIndicator()
                                Text(
                                    text = "ORDER TIMELINE PROGRESS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                                    color = Color(0xFFC5A059)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ExposedDropdownMenuBox(
                                    expanded = expandedStatus,
                                    onExpandedChange = { expandedStatus = !expandedStatus }
                                ) {
                                    OutlinedTextField(
                                        value = status,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Work Status") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFC5A059)),
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                        modifier = Modifier.menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedStatus,
                                        onDismissRequest = { expandedStatus = false }
                                    ) {
                                        listOf("Pending", "In Progress", "Completed", "Delivered").forEach { statusOpt ->
                                            DropdownMenuItem(
                                                text = { Text(statusOpt) },
                                                onClick = {
                                                    status = statusOpt
                                                    expandedStatus = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
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
                            isWeightError = approxWeight.toDoubleOrNull() == null || approxWeight.toDouble() <= 0.0

                            if (!isNameError && !isWeightError) {
                                val wt = approxWeight.toDoubleOrNull() ?: 0.0
                                val rate = agreedRate.toDoubleOrNull() ?: 0.0
                                val mak = makingCharges.toDoubleOrNull() ?: 0.0
                                val stone = otherCharges.toDoubleOrNull() ?: 0.0
                                val deposit = advancePaid.toDoubleOrNull() ?: 0.0
                                val calculatedTotal = (wt * rate) + mak + stone

                                val compiledOrder = Order(
                                    id = order?.id ?: 0,
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    jewelleryType = jewelleryType,
                                    metalType = metalType,
                                    purity = purity,
                                    approxWeight = wt,
                                    agreedRate = rate,
                                    makingCharges = mak,
                                    otherCharges = stone,
                                    advancePaid = deposit,
                                    totalAmount = calculatedTotal,
                                    orderDate = order?.orderDate ?: System.currentTimeMillis(),
                                    expectedDeliveryDate = expectedDeliveryDate,
                                    notes = notes,
                                    status = status
                                )
                                onConfirm(compiledOrder)
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
