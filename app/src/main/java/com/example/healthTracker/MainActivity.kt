package com.example.healthTracker

import HealthConnectManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var healthConnectManager: HealthConnectManager

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectManager = HealthConnectManager(this)

        checkAndRequestPermissions()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel = remember { WeightViewModel(healthConnectManager) }
                    BodyWeightScreen(viewModel)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = healthConnectManager.permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

class WeightViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    private val _weight = mutableStateOf<Double?>(null)
    val weight: State<Double?> = _weight

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isMetric = mutableStateOf(true)
    val isMetric: State<Boolean> = _isMetric

    fun fetchLatestWeight() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _weight.value = healthConnectManager.readLatestWeight()
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error fetching weight", e)
                _weight.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleUnit() {
        _isMetric.value = !_isMetric.value
    }

    fun getFormattedWeight(): String {
        val weightValue = weight.value
        return when {
            weightValue == null -> "Not available"
            isMetric.value -> String.format("%.2f kg", weightValue)
            else -> String.format("%.2f lbs", weightValue * 2.20462)
        }
    }

    suspend fun isHealthConnectAvailable(): Boolean {
        return healthConnectManager.isHealthConnectAvailable()
    }
}

@Composable
fun BodyWeightScreen(viewModel: WeightViewModel) {
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (viewModel.isHealthConnectAvailable()) {
                            viewModel.fetchLatestWeight()
                        } else {
                            // Show a message that Health Connect is not available
                        }
                    }
                },
                enabled = !viewModel.isLoading.value
            ) {
                Text("Get Latest Weight")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading.value) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Latest Weight: ${viewModel.getFormattedWeight()}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Footer with settings button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.toggleUnit() },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(if (viewModel.isMetric.value) "Switch to lbs" else "Switch to kg")
            }
        }
    }
}