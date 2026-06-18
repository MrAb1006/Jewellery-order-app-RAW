package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeletedOrder
import com.example.data.DeletedKarigarOrder
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UnifiedActivityLogDialog(
    deletedCustomerOrders: List<DeletedOrder>,
    deletedKarigarOrders: List<DeletedKarigarOrder>,
    onDismiss: () -> Unit,
    onRestoreCustomer: (DeletedOrder) -> Unit,
    onDeleteCustomerPermanently: (Int) -> Unit,
    onRestoreKarigar: (DeletedKarigarOrder) -> Unit,
    onDeleteKarigarPermanently: (Int) -> Unit,
    onClearAll: (Boolean, Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Customer", "Karigar")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Color(0xFFC5A059)) } },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activity Log", fontWeight = FontWeight.Bold)
                IconButton(onClick = { onClearAll(selectedTab == 0, selectedTab == 1) }) {
                    Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent, contentColor = Color(0xFFC5A059)) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (selectedTab == 0) {
                    DeletedOrdersList(deletedCustomerOrders, onRestoreCustomer, onDeleteCustomerPermanently)
                } else {
                    DeletedKarigarOrdersList(deletedKarigarOrders, onRestoreKarigar, onDeleteKarigarPermanently)
                }
            }
        }
    )
}

@Composable
fun DeletedOrdersList(orders: List<DeletedOrder>, onRestore: (DeletedOrder) -> Unit, onDelete: (Int) -> Unit) {
    if (orders.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) { Text("No deleted customer orders") }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(orders) { order ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(order.customerName, fontWeight = FontWeight.Bold)
                        Text("${order.jewelleryType} · ${order.approxWeight}g", fontSize = 11.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onRestore(order) }) { Icon(Icons.Default.Restore, null, tint = Color(0xFF388E3C)) }
                            IconButton(onClick = { onDelete(order.id) }) { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeletedKarigarOrdersList(orders: List<DeletedKarigarOrder>, onRestore: (DeletedKarigarOrder) -> Unit, onDelete: (Int) -> Unit) {
    if (orders.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) { Text("No deleted karigar orders") }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(orders) { order ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(order.orderNo, fontWeight = FontWeight.Bold)
                        Text("${order.karigarName} · Ref: ${order.referenceCustomerName}", fontSize = 11.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onRestore(order) }) { Icon(Icons.Default.Restore, null, tint = Color(0xFF388E3C)) }
                            IconButton(onClick = { onDelete(order.id) }) { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectiveBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Boolean) -> Unit
) {
    var includeCustomer by remember { mutableStateOf(true) }
    var includeKarigar by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Backup Database") },
        text = {
            Column {
                Text("Select sections to include in backup:", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeCustomer = !includeCustomer }) {
                    Checkbox(checked = includeCustomer, onCheckedChange = { includeCustomer = it })
                    Text("Customer Section")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeKarigar = !includeKarigar }) {
                    Checkbox(checked = includeKarigar, onCheckedChange = { includeKarigar = it })
                    Text("Karigar Section")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(includeCustomer, includeKarigar) },
                enabled = includeCustomer || includeKarigar
            ) {
                Text("Backup Now")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SelectiveRestoreDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Boolean) -> Unit
) {
    var includeCustomer by remember { mutableStateOf(true) }
    var includeKarigar by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Database") },
        text = {
            Column {
                Text("Select sections to restore from file:", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeCustomer = !includeCustomer }) {
                    Checkbox(checked = includeCustomer, onCheckedChange = { includeCustomer = it })
                    Text("Customer Section")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { includeKarigar = !includeKarigar }) {
                    Checkbox(checked = includeKarigar, onCheckedChange = { includeKarigar = it })
                    Text("Karigar Section")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(includeCustomer, includeKarigar) },
                enabled = includeCustomer || includeKarigar
            ) {
                Text("Select File & Restore")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
