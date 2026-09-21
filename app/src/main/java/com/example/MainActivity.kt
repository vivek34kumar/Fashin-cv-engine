package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.main.MainScreen
import com.example.ui.theme.FashionEngineTheme
import com.example.ui.viewmodel.FashionEngineViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: FashionEngineViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val userSettings by viewModel.userSettings.collectAsState()
      val darkTheme = when (userSettings?.themeMode?.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
      }

      FashionEngineTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}


