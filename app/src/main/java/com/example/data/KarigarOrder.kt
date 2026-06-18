package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "karigar_orders")
data class KarigarOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNo: String,
    val karigarId: Int,
    val karigarName: String,
    val referenceCustomerName: String = "",
    val phone: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long? = null,
    val urgency: String = "normal", // normal, urgent, express
    val specialInstructions: String = "",
    val designRef: String = "",
    val status: String = "pending", // pending, in_progress, ready, delivered
    val itemsJson: String = "[]", // Serialized KarigarOrderItem list
    val transactionsJson: String = "[]", // Serialized KarigarTransaction list
    // Summary fields for easy access/sorting
    val totalFineRequired: Double = 0.0,
    val totalFineIssued: Double = 0.0,
    val totalFinePending: Double = 0.0,
    val makingType: String = "per_gram", // per_gram, flat
    val makingRate: Double = 0.0
) : Serializable

data class KarigarOrderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val itemName: String = "",
    val metalType: String = "gold",
    val quantity: Int = 1,
    val grossWeight: Double = 0.0,
    val stoneWeight: Double = 0.0,
    val subtractStoneWeight: Boolean = false,
    val purityPct: Double = 0.0,
    val wastagePct: Double = 0.0,
    val netFineRequired: Double = 0.0
) : Serializable

data class KarigarTransaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String, // "metal" or "cash"
    val date: Long = System.currentTimeMillis(),
    val weight: Double = 0.0, // Fine metal weight settled
    val rate: Double = 0.0, // For cash issue
    val amount: Double = 0.0, // For cash issue
    val note: String = ""
) : Serializable

@Entity(tableName = "deleted_karigar_orders")
data class DeletedKarigarOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNo: String,
    val karigarId: Int,
    val karigarName: String,
    val referenceCustomerName: String = "",
    val phone: String = "",
    val orderDate: Long,
    val deliveryDate: Long? = null,
    val urgency: String,
    val specialInstructions: String = "",
    val designRef: String = "",
    val status: String,
    val itemsJson: String,
    val transactionsJson: String,
    val totalFineRequired: Double,
    val totalFineIssued: Double,
    val totalFinePending: Double,
    val makingType: String,
    val makingRate: Double,
    val deletedAt: Long = System.currentTimeMillis()
) : Serializable

fun KarigarOrder.toDeleted(): DeletedKarigarOrder = DeletedKarigarOrder(
    orderNo = orderNo,
    karigarId = karigarId,
    karigarName = karigarName,
    referenceCustomerName = referenceCustomerName,
    phone = phone,
    orderDate = orderDate,
    deliveryDate = deliveryDate,
    urgency = urgency,
    specialInstructions = specialInstructions,
    designRef = designRef,
    status = status,
    itemsJson = itemsJson,
    transactionsJson = transactionsJson,
    totalFineRequired = totalFineRequired,
    totalFineIssued = totalFineIssued,
    totalFinePending = totalFinePending,
    makingType = makingType,
    makingRate = makingRate
)

fun DeletedKarigarOrder.toOrder(): KarigarOrder = KarigarOrder(
    orderNo = orderNo,
    karigarId = karigarId,
    karigarName = karigarName,
    referenceCustomerName = referenceCustomerName,
    phone = phone,
    orderDate = orderDate,
    deliveryDate = deliveryDate,
    urgency = urgency,
    specialInstructions = specialInstructions,
    designRef = designRef,
    status = status,
    itemsJson = itemsJson,
    transactionsJson = transactionsJson,
    totalFineRequired = totalFineRequired,
    totalFineIssued = totalFineIssued,
    totalFinePending = totalFinePending,
    makingType = makingType,
    makingRate = makingRate
)

data class KarigarOrderMetrics(
    val totalRequired: Double = 0.0,
    val totalIssued: Double = 0.0,
    val totalPending: Double = 0.0,
    val activeOrders: Int = 0
)
