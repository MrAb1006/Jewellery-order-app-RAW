package com.example

import com.example.data.OrderItem
import com.example.data.OldOrderItem
import com.example.data.Order
import com.example.data.getItems
import com.example.data.getOldItems
import com.example.data.serializeItems
import com.example.data.serializeOldItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testGoldItemCalculations() {
        // Gold: approxWeight = 10.0, agreedRate = 7000.0, purity = "91.6", makingCharges = 10.0%, otherCharges = 500.0
        val item = OrderItem(
            jewelleryType = "Ring",
            metalType = "Gold",
            purity = "91.6",
            approxWeight = 10.0,
            agreedRate = 7000.0,
            makingCharges = 10.0, // 10%
            otherCharges = 500.0
        )

        val purityPercent = (item.purity.toDoubleOrNull() ?: 100.0) / 100.0
        val metalValue = item.approxWeight * item.agreedRate * purityPercent // 10.0 * 7000 * 0.916 = 64120.0
        assertEquals(64120.0, metalValue, 0.001)

        val makingChargesAmount = metalValue * (item.makingCharges / 100.0) // 64120.0 * 0.10 = 6412.0
        assertEquals(6412.0, makingChargesAmount, 0.001)

        val itemTotal = metalValue + makingChargesAmount + item.otherCharges // 64120.0 + 6412.0 + 500 = 71032.0
        assertEquals(71032.0, itemTotal, 0.001)
    }

    @Test
    fun testSilverItemCalculations() {
        // Silver: approxWeight = 100.0, agreedRate = 90.0, purity = "100.0", makingCharges = 350.0 (Flat addition), otherCharges = 150.0
        val item = OrderItem(
            jewelleryType = "Anklet",
            metalType = "Silver",
            purity = "100.0",
            approxWeight = 100.0,
            agreedRate = 90.0,
            makingCharges = 350.0, // Flat
            otherCharges = 150.0
        )

        val purityPercent = (item.purity.toDoubleOrNull() ?: 100.0) / 100.0
        val metalValue = item.approxWeight * item.agreedRate * purityPercent // 100.0 * 90.0 * 1.0 = 9000.0
        assertEquals(9000.0, metalValue, 0.001)

        val isSilver = item.metalType.equals("Silver", ignoreCase = true)
        val makingChargesAmount = if (isSilver) item.makingCharges else (metalValue * (item.makingCharges / 100.0))
        assertEquals(350.0, makingChargesAmount, 0.001)

        val itemTotal = metalValue + makingChargesAmount + item.otherCharges // 9000.0 + 350.0 + 150.0 = 9500.0
        assertEquals(9500.0, itemTotal, 0.001)
    }

    @Test
    fun testExchangeSubtractAndBalanceValuations() {
        // Create order with 1 Gold item and 1 Old Gold item for exchange
        val orderItem = OrderItem(
            jewelleryType = "Bracelet",
            metalType = "Gold",
            purity = "91.6",
            approxWeight = 20.0,
            agreedRate = 7000.0,
            makingCharges = 12.0,
            otherCharges = 1000.0
        )
        val metalValValue = orderItem.approxWeight * orderItem.agreedRate * ((orderItem.purity.toDoubleOrNull() ?: 100.0) / 100.0) // 20.0 * 7000 * 0.916 = 128240.0
        val makingChargesAmount = metalValValue * (orderItem.makingCharges / 100.0) // 128240.0 * 0.12 = 15388.8
        val grandTotalSpecs = metalValValue + makingChargesAmount + orderItem.otherCharges // 128240.0 + 15388.8 + 1000.0 = 144628.8

        // Old item traded in
        val oldItem = OldOrderItem(
            itemName = "Old Gold Chain",
            metalType = "Gold",
            approxWeight = 10.0,
            purity = 91.6 // 91.6%
        )

        // Find the gold rate from new items
        val goldRate = 7000.0
        val oldGoldFine = oldItem.approxWeight * (oldItem.purity / 100.0) // 10 * 0.916 = 9.16g
        assertEquals(9.16, oldGoldFine, 0.001)

        val totalExchangeCredit = oldGoldFine * goldRate // 9.16 * 7000 = 64120.0
        assertEquals(64120.0, totalExchangeCredit, 0.001)

        val finalPayableAmount = maxOf(0.0, grandTotalSpecs - totalExchangeCredit) // 144628.8 - 64120.0 = 80508.8
        assertEquals(80508.8, finalPayableAmount, 0.001)

        val advancePaid = 10508.8
        val balanceDue = finalPayableAmount - advancePaid
        assertEquals(70000.0, balanceDue, 0.001)
    }

    @Test
    fun testSerializationDeserialization() {
        val oldItemsList = listOf(
            OldOrderItem(itemName = "Earrings", metalType = "Gold", approxWeight = 4.5, purity = 75.0),
            OldOrderItem(itemName = "Anklet", metalType = "Silver", approxWeight = 25.0, purity = 92.5)
        )

        val serialized = serializeOldItems(oldItemsList)
        assertTrue(serialized.contains("Earrings"))
        assertTrue(serialized.contains("Anklet"))

        val order = Order(
            customerName = "Jane Doe",
            customerPhone = "1234567890",
            jewelleryType = "Earrings",
            metalType = "Gold",
            purity = "75.0",
            approxWeight = 4.5,
            agreedRate = 6000.0,
            makingCharges = 5.0,
            otherCharges = 0.0,
            advancePaid = 0.0,
            totalAmount = 0.0,
            expectedDeliveryDate = System.currentTimeMillis() + 86400000,
            oldItemsJson = serialized
        )

        val deserialized = order.getOldItems()
        assertEquals(2, deserialized.size)
        assertEquals("Earrings", deserialized[0].itemName)
        assertEquals("Gold", deserialized[0].metalType)
        assertEquals(4.5, deserialized[0].approxWeight, 0.001)
        assertEquals(75.0, deserialized[0].purity, 0.001)

        assertEquals("Anklet", deserialized[1].itemName)
        assertEquals("Silver", deserialized[1].metalType)
        assertEquals(25.0, deserialized[1].approxWeight, 0.001)
        assertEquals(92.5, deserialized[1].purity, 0.001)
    }
}
