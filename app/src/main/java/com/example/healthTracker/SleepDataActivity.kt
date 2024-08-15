package com.example.healthTracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import kotlin.time.Duration

class SleepDataActivity : ComponentActivity() {
    private lateinit var healthConnectManager: HealthConnectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectManager = HealthConnectManager(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel = remember { SleepViewModel(healthConnectManager) }
                    SleepDataScreen(viewModel, onBackClick = { finish() })
                }
            }
        }
    }
}

class SleepViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
    private val _sleepSessions = mutableStateOf<List<HealthConnectManager.SleepSessionData>>(emptyList())
    val sleepSessions: State<List<HealthConnectManager.SleepSessionData>> = _sleepSessions

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun fetchSleepSessions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _sleepSessions.value = healthConnectManager.readSleepSessions()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getFormattedDate(instant: java.time.Instant): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    fun getFormattedDuration(duration: Duration): String {
        val hours = duration.inWholeHours
        val minutes = duration.inWholeMinutes % 60
        return String.format("%d h %02d min", hours, minutes)
    }
}

@Composable
fun SleepDataScreen(viewModel: SleepViewModel, onBackClick: () -> Unit) {
    LaunchedEffect(Unit) {
        viewModel.fetchSleepSessions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sleep Sessions", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading.value) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(viewModel.sleepSessions.value) { session ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Start: ${viewModel.getFormattedDate(session.startTime)}")
                            Text("End: ${viewModel.getFormattedDate(session.endTime)}")
                            Text("Duration: ${viewModel.getFormattedDuration(session.duration)}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Main")
        }
    }
}





