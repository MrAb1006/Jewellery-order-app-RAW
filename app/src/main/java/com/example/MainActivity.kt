package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.OrderRepository
import com.example.ui.DashboardScreen
import com.example.ui.OrderViewModel
import com.example.ui.OrderViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database layers
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = OrderRepository(database.orderDao())
    
    // Obtain OrderViewModel with Factory
    val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(repository)
    }

    setContent {
      MyApplicationTheme {
        DashboardScreen(viewModel = viewModel)
      }
    }
  }
}

