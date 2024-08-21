package com.example.healthTracker.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.healthTracker.HealthConnectManager
import com.example.healthTracker.ui.screens.HomeScreen

// AppNavigation.kt
@Composable
fun AppNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    healthConnectManager: HealthConnectManager
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("home") { HomeScreen() }
//        composable("sleep") { SleepScreen(healthConnectManager) }
//        composable("workout") { WorkoutScreen() }
//        composable("user") { UserScreen() }
//        composable("nutrition") { NutritionScreen() }
//        composable("settings") { SettingsScreen() }
    }
}