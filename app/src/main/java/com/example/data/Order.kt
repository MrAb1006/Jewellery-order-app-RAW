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
