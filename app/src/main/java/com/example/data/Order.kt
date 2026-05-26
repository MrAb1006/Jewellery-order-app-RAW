package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.util.UUID

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
    val status: String = "Pending", // Pending, In Progress, Completed, Delivered
    val itemsJson: String = "", // Multiple items tracker JSON serialization
    val oldItemsJson: String = "" // Old jewellery received JSON serialization
) : Serializable

data class OrderItem(
    val id: String = UUID.randomUUID().toString(),
    val jewelleryType: String = "",
    val metalType: String = "Gold",
    val purity: String = "91.6",
    val approxWeight: Double = 0.0,
    val agreedRate: Double = 0.0,
    val makingCharges: Double = 0.0,
    val otherCharges: Double = 0.0,
    val status: String = "Pending"
) : Serializable

data class OldOrderItem(
    val id: String = UUID.randomUUID().toString(),
    val itemName: String = "",
    val metalType: String = "Gold", // Only supporting Gold / Silver
    val approxWeight: Double = 0.0,
    val purity: Double = 0.0, // represented as percentage, e.g. 90.0 meaning 90%
    val agreedRate: Double = 0.0
) : Serializable

fun Order.getOldItems(): List<OldOrderItem> {
    val json = this.oldItemsJson ?: ""
    if (json.isBlank()) {
        return emptyList()
    }
    val list = mutableListOf<OldOrderItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                OldOrderItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    itemName = obj.optString("itemName", ""),
                    metalType = obj.optString("metalType", "Gold"),
                    approxWeight = obj.optDouble("approxWeight", 0.0),
                    purity = obj.optDouble("purity", 0.0),
                    agreedRate = obj.optDouble("agreedRate", 0.0)
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun serializeOldItems(items: List<OldOrderItem>): String {
    val array = JSONArray()
    items.forEach { item ->
        val obj = JSONObject().apply {
            put("id", item.id)
            put("itemName", item.itemName)
            put("metalType", item.metalType)
            put("approxWeight", item.approxWeight)
            put("purity", item.purity)
            put("agreedRate", item.agreedRate)
        }
        array.put(obj)
    }
    return array.toString()
}

fun Order.getItems(): List<OrderItem> {
    val json = this.itemsJson ?: ""
    if (json.isBlank()) {
        return listOf(
            OrderItem(
                id = "primary",
                jewelleryType = this.jewelleryType ?: "",
                metalType = this.metalType ?: "Gold",
                purity = this.purity ?: "91.6",
                approxWeight = this.approxWeight ?: 0.0,
                agreedRate = this.agreedRate ?: 0.0,
                makingCharges = this.makingCharges ?: 0.0,
                otherCharges = this.otherCharges ?: 0.0,
                status = this.status ?: "Pending"
            )
        )
    }
    val list = mutableListOf<OrderItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                OrderItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    jewelleryType = obj.optString("jewelleryType", ""),
                    metalType = obj.optString("metalType", "Gold"),
                    purity = obj.optString("purity", "91.6"),
                    approxWeight = obj.optDouble("approxWeight", 0.0),
                    agreedRate = obj.optDouble("agreedRate", 0.0),
                    makingCharges = obj.optDouble("makingCharges", 0.0),
                    otherCharges = obj.optDouble("otherCharges", 0.0),
                    status = obj.optString("status", "Pending")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun serializeItems(items: List<OrderItem>): String {
    val array = JSONArray()
    items.forEach { item ->
        val obj = JSONObject().apply {
            put("id", item.id)
            put("jewelleryType", item.jewelleryType)
            put("metalType", item.metalType)
            put("purity", item.purity)
            put("approxWeight", item.approxWeight)
            put("agreedRate", item.agreedRate)
            put("makingCharges", item.makingCharges)
            put("otherCharges", item.otherCharges)
            put("status", item.status)
        }
        array.put(obj)
    }
    return array.toString()
}

@Entity(tableName = "deleted_orders")
data class DeletedOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val jewelleryType: String,
    val metalType: String,
    val purity: String,
    val approxWeight: Double,
    val agreedRate: Double,
    val makingCharges: Double,
    val otherCharges: Double,
    val advancePaid: Double,
    val totalAmount: Double,
    val orderDate: Long,
    val expectedDeliveryDate: Long,
    val notes: String = "",
    val status: String,
    val itemsJson: String = "",
    val oldItemsJson: String = "",
    val deletedAt: Long = System.currentTimeMillis()
)

fun Order.toDeletedOrder(): DeletedOrder = DeletedOrder(
    customerName = this.customerName,
    customerPhone = this.customerPhone,
    jewelleryType = this.jewelleryType,
    metalType = this.metalType,
    purity = this.purity,
    approxWeight = this.approxWeight,
    agreedRate = this.agreedRate,
    makingCharges = this.makingCharges,
    otherCharges = this.otherCharges,
    advancePaid = this.advancePaid,
    totalAmount = this.totalAmount,
    orderDate = this.orderDate,
    expectedDeliveryDate = this.expectedDeliveryDate,
    notes = this.notes,
    status = this.status,
    itemsJson = this.itemsJson,
    oldItemsJson = this.oldItemsJson,
    deletedAt = System.currentTimeMillis()
)

fun DeletedOrder.toOrder(): Order = Order(
    customerName = this.customerName,
    customerPhone = this.customerPhone,
    jewelleryType = this.jewelleryType,
    metalType = this.metalType,
    purity = this.purity,
    approxWeight = this.approxWeight,
    agreedRate = this.agreedRate,
    makingCharges = this.makingCharges,
    otherCharges = this.otherCharges,
    advancePaid = this.advancePaid,
    totalAmount = this.totalAmount,
    orderDate = this.orderDate,
    expectedDeliveryDate = this.expectedDeliveryDate,
    notes = this.notes,
    status = this.status,
    itemsJson = this.itemsJson,
    oldItemsJson = this.oldItemsJson
)

