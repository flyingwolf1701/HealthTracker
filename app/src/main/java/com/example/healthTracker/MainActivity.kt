package com.example.healthTracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.healthTracker.navigation.AppNavigation
import com.example.healthTracker.ui.components.Footer
import com.example.healthTracker.ui.components.Header
import com.example.healthTracker.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    private lateinit var healthConnectManager: HealthConnectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectManager = HealthConnectManager(this)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                Scaffold(
                    topBar = { Header(navController) },
                    bottomBar = { Footer(navController) }
                ) { innerPadding ->
                    AppNavigation(navController, innerPadding, healthConnectManager)
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        topBar = { Header(navController) },
        bottomBar = { Footer(navController) }
    ) { innerPadding ->
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val navController = rememberNavController()
    MainScreen(navController)
}


