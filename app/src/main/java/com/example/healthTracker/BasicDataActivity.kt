package com.example.healthTracker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.lifecycleScope

class BasicDataActivity : ComponentActivity() {
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var viewModel: BasicDataViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultsMap ->
        if (resultsMap.all { it.value }) {
            // All permissions granted, initialize ViewModel
            viewModel = BasicDataViewModel(healthConnectManager)
        } else {
            // Handle permission denial
            Toast.makeText(this, "Permissions are required to use this feature", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectManager = HealthConnectManager(this)

        lifecycleScope.launch {
            if (healthConnectManager.hasAllPermissions(healthConnectManager.permissions)) {
                // Permissions already granted, initialize ViewModel
                viewModel = BasicDataViewModel(healthConnectManager)
                setContent {
                    MaterialTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            BasicDataScreen(viewModel, onBackClick = { finish() })
                        }
                    }
                }
            } else {
                // Request permissions
                requestPermissionLauncher.launch(healthConnectManager.permissions.toTypedArray())
            }
        }
    }
}

class BasicDataViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    private val _weightRecords = mutableStateOf<List<Pair<Instant, Double>>>(emptyList())
    val weightRecords: State<List<Pair<Instant, Double>>> = _weightRecords

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isHealthConnectAvailable = mutableStateOf(false)
    val isHealthConnectAvailable: State<Boolean> = _isHealthConnectAvailable

    init {
        checkHealthConnectAvailability()
        fetchWeightRecords()
    }

    private fun checkHealthConnectAvailability() {
        viewModelScope.launch {
            _isHealthConnectAvailable.value = healthConnectManager.isHealthConnectAvailable()
        }
    }

    private fun fetchWeightRecords() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _weightRecords.value = healthConnectManager.readWeightRecords()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun writeWeightInput(weightInput: Double) {
        viewModelScope.launch {
            try {
                if (healthConnectManager.hasAllPermissions(healthConnectManager.permissions)) {
                    healthConnectManager.writeWeightInput(weightInput)
                    fetchWeightRecords() // Refresh the list after writing
                } else {
                    // Handle case where permissions are not granted
                    // You might want to trigger the permission request again here
                }
            } catch (e: Exception) {
                Log.e("BasicDataViewModel", "Error writing weight: ${e.message}", e)
            }
        }
    }

    fun getFormattedDate(instant: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
}

@Composable
fun BasicDataScreen(viewModel: BasicDataViewModel, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Basic Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isHealthConnectAvailable.value) {
            // Weight input field
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Enter weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit button
            Button(onClick = {
                weightInput.toDoubleOrNull()?.let { weight ->
                    viewModel.writeWeightInput(weight)
                    weightInput = "" // Clear input field
                    Toast.makeText(context, "Weight submitted", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(context, "Please enter a valid weight", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Submit Weight")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight records list
            if (viewModel.isLoading.value) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(viewModel.weightRecords.value) { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(viewModel.getFormattedDate(record.first))
                            Text("${String.format("%.2f", record.second)} lbs")
                        }
                        HorizontalDivider()
                    }
                }
            }
        } else {
            Text("Health Connect is not available on this device.")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Main")
        }
    }
}





