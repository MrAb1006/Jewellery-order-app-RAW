@Composable
fun BidirectionalCalculatorDialog(onDismiss: () -> Unit) {
    var grossWeight by remember { mutableStateOf("") }
    var purity by remember { mutableStateOf("") }
    var ratePerGram by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }

    fun calculate(changedField: String) {
        val gw = grossWeight.toDoubleOrNull()
        val pr = purity.toDoubleOrNull()
        val rpg = ratePerGram.toDoubleOrNull()
        val tp = totalPrice.toDoubleOrNull()

        when (changedField) {
            "gw" -> {
                if (pr != null && rpg != null) {
                    totalPrice = String.format(Locale.getDefault(), "%.2f", gw * (pr / 100.0) * rpg)
                }
            }
            "pr" -> {
                if (gw != null && rpg != null) {
                    totalPrice = String.format(Locale.getDefault(), "%.2f", gw * (pr / 100.0) * rpg)
                } else if (gw != null && tp != null) {
                    purity = String.format(Locale.getDefault(), "%.2f", (tp / (gw * (rpg ?: 1.0))) * 100.0)
                }
            }
            "rpg" -> {
                if (gw != null && pr != null) {
                    totalPrice = String.format(Locale.getDefault(), "%.2f", gw * (pr / 100.0) * rpg)
                } else if (gw != null && tp != null && pr != null) {
                    ratePerGram = String.format(Locale.getDefault(), "%.2f", tp / (gw * (pr / 100.0)))
                }
            }
            "tp" -> {
                if (gw != null && pr != null && rpg != null) {
                    // If all others are present, we just let the user set TP, but typically TP is the result.
                    // If the user changes TP and we want to calculate one of the others:
                    // Let's assume if TP is changed, we try to update RPG if GW and PR are present.
                    ratePerGram = String.format(Locale.getDefault(), "%.2f", tp / (gw * (pr / 100.0)))
                }
            }
        }
    }

    // Improved logic: identify which 3 are present and calculate the 4th
    fun autoCompute() {
        val gw = grossWeight.toDoubleOrNull()
        val pr = purity.toDoubleOrNull()
        val rpg = ratePerGram.toDoubleOrNull()
        val tp = totalPrice.toDoubleOrNull()

        val present = listOfNotNull(
            if (gw != null) "gw" else null,
            if (pr != null) "pr" else null,
            if (rpg != null) "rpg" else null,
            if (tp != null) "tp" else null
        )

        if (present.size == 3) {
            val missing = listOf("gw", "pr", "rpg", "tp").first { it !in present }
            when (missing) {
                "gw" -> {
                    if (pr != null && rpg != null && tp != null) {
                        grossWeight = String.format(Locale.getDefault(), "%.3f", tp / ((pr / 100.0) * rpg))
                    }
                }
                "pr" -> {
                    if (gw != null && rpg != null && tp != null) {
                        purity = String.format(Locale.getDefault(), "%.2f", (tp / (gw * rpg)) * 100.0)
                    }
                }
                "rpg" -> {
                    if (gw != null && pr != null && tp != null) {
                        ratePerGram = String.format(Locale.getDefault(), "%.2f", tp / (gw * (pr / 100.0)))
                    }
                }
                "tp" -> {
                    if (gw != null && pr != null && rpg != null) {
                        totalPrice = String.format(Locale.getDefault(), "%.2f", gw * (pr / 100.0) * rpg)
                    }
                }
            }
        }
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
                
                CalculatorField("Gross Weight (g)", grossWeight, { grossWeight = it }, { autoCompute() })
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorField("Purity (%)", purity, { purity = it }, { autoCompute() })
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorField("Rate per Gram (₹)", ratePerGram, { ratePerGram = it }, { autoCompute() })
                Spacer(modifier = Modifier.height(12.dp))
                CalculatorField("Total Price (₹)", totalPrice, { totalPrice = it }, { autoCompute() })
                
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = {
                    grossWeight = ""; purity = ""; ratePerGram = ""; totalPrice = ""
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
fun CalculatorField(label: String, value: String, onValueChange: (String) -> Unit, onValueFinished: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.all { char -> char.isDigit() || char == '.' }) {
                onValueChange(it)
                onValueFinished()
            }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}
