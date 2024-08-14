package com.example.healthTracker

import HealthConnectManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
//    val weight: State<Double?> = _weight

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isMetric = mutableStateOf(true)
    val isMetric: State<Boolean> = _isMetric

    private val _weightRecords = mutableStateOf<List<Pair<Instant, Double>>>(emptyList())
    val weightRecords: State<List<Pair<Instant, Double>>> = _weightRecords

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

    fun fetchWeightRecords() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _weightRecords.value = healthConnectManager.readWeightRecords()
            } catch (e: Exception) {
                Log.e("WeightViewModel", "Error fetching weight records", e)
                _weightRecords.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleUnit() {
        _isMetric.value = !_isMetric.value
    }

    fun getFormattedWeight(weight: Double): String {
        return if (isMetric.value) {
            String.format("%.2f kg", weight)
        } else {
            String.format("%.2f lbs", weight * 2.20462)
        }
    }

    fun getFormattedDate(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    suspend fun isHealthConnectAvailable(): Boolean {
        return healthConnectManager.isHealthConnectAvailable()
    }
}

@Composable
fun BodyWeightScreen(viewModel: WeightViewModel) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (viewModel.isHealthConnectAvailable()) {
            viewModel.fetchWeightRecords()
        }
    }

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
                LazyColumn {
                    itemsIndexed(viewModel.weightRecords.value) { _, (date, weight) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(viewModel.getFormattedDate(date))
                            Text(viewModel.getFormattedWeight(weight))
                        }
                        Divider()
                    }
                }
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


