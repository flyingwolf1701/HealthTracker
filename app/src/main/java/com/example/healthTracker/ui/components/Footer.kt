package com.example.healthTracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Footer.kt
@Composable
fun Footer(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { navController.navigate("sleep") }) {
            Icon(Icons.Filled.Bedtime, contentDescription = "Sleep")
        }
        IconButton(onClick = { navController.navigate("workout") }) {
            Icon(Icons.Filled.FitnessCenter, contentDescription = "Workout")
        }
        IconButton(onClick = { navController.navigate("user") }) {
            Icon(Icons.Filled.Person, contentDescription = "User")
        }
        IconButton(onClick = { navController.navigate("nutrition") }) {
            Icon(Icons.Filled.Restaurant, contentDescription = "Nutrition")
        }
    }
}