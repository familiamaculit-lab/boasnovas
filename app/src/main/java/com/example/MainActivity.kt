package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AppContent
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Instantiate local SQLite Room database & Repository
    val database = AppDatabase.getDatabase(this)
    val dao = database.appDao
    val repository = AppRepository(dao)

    // Instantiate ViewModel via state factory
    val viewModel: AppViewModel by viewModels {
      AppViewModelFactory(repository)
    }

    setContent {
      MyApplicationTheme {
        AppContent(viewModel = viewModel)
      }
    }
  }
}
