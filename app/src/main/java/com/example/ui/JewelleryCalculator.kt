package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale

@Composable
fun BidirectionalCalculatorDialog(onDismiss: () -> Unit) {
    var state by remember { mutableStateOf(CalcState()) }

    fun onChange(field: String, value: String) {
        val (newFields, newUserSet) = recalc(field, value, state.fields, state.userSet)
        state = state.copy(fields = newFields, userSet = newUserSet, lastEdited = field)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Jewellery Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC5A059)
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                CalculatorField("Gross Weight (g)", state.fields.grossWt, { onChange("grossWt", it) })
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorField("Purity (%)", state.fields.purity, { onChange("purity", it) })
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorField("Fine (g) [Gross * Purity]", state.fields.fineWt, { onChange("fineWt", it) })
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorField("Rate per Gram (₹)", state.fields.ratePerGram, { onChange("ratePerGram", it) })
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorField("Total Price (₹)", state.fields.totalPrice, { onChange("totalPrice", it) })
                
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = {
                    state = CalcState()
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CalculatorField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.all { char -> char.isDigit() || char == '.' }) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}
