//package com.example.healthTracker
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.runtime.*
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.viewModelScope
//import com.google.gson.GsonBuilder
//import com.google.gson.TypeAdapter
//import com.google.gson.stream.JsonReader
//import com.google.gson.stream.JsonWriter
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import java.time.Duration
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//import java.time.ZoneId
//import java.time.ZoneOffset
//
//class SleepDataActivity : ComponentActivity() {
//    private lateinit var healthConnectManager: HealthConnectManager
//    private lateinit var viewModel: SleepViewModel
//
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { resultsMap ->
//        if (resultsMap.all { it.value }) {
//            // All permissions granted, initialize ViewModel
//            viewModel = SleepViewModel(healthConnectManager)
//        } else {
//            // Handle permission denial
//            Toast.makeText(this, "Permissions are required to use this feature", Toast.LENGTH_LONG).show()
//            finish()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        healthConnectManager = HealthConnectManager(this)
//
//        lifecycleScope.launch {
//            if (healthConnectManager.hasAllPermissions(healthConnectManager.permissions)) {
//                // Permissions already granted, initialize ViewModel
//                viewModel = SleepViewModel(healthConnectManager)
//                setContent {
//                    MaterialTheme {
//                        Surface(
//                            modifier = Modifier.fillMaxSize(),
//                            color = MaterialTheme.colorScheme.background
//                        ) {
//                            SleepDataScreen(viewModel, onBackClick = { finish() })
//                        }
//                    }
//                }
//            } else {
//                // Request permissions
//                requestPermissionLauncher.launch(healthConnectManager.permissions.toTypedArray())
//            }
//        }
//    }
//}
//
//class SleepViewModel(private val healthConnectManager: HealthConnectManager) : ViewModel() {
//    private val _sleepSessions = MutableStateFlow<SleepSessionsTypeAlias>(emptyList())
//    val sleepSessions: StateFlow<SleepSessionsTypeAlias> = _sleepSessions.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    private val _expandedSessionId = MutableStateFlow<String?>(null)
//    val expandedSessionId: StateFlow<String?> = _expandedSessionId.asStateFlow()
//
//    fun fetchSleepSessions() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                _sleepSessions.value = healthConnectManager.readSleepSessions()
//            } catch (e: Exception) {
//                // Handle error
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun toggleExpanded(sessionId: String) {
//        _expandedSessionId.value = if (_expandedSessionId.value == sessionId) null else sessionId
//    }
//
//    fun getTotalSleepDuration(session: SleepSessionRecord): Duration {
//        return Duration.between(session.startTime, session.endTime)
//    }
//
//    fun getActualSleepTime(session: SleepSessionRecord): Duration {
//        return session.stages.filter {
//            it.stage != SleepSessionRecord.STAGE_TYPE_AWAKE &&
//                    it.stage != SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED &&
//                    it.stage != SleepSessionRecord.STAGE_TYPE_OUT_OF_BED
//        }.sumOf { stage ->
//            Duration.between(stage.startTime, stage.endTime).toMillis()
//        }.let { Duration.ofMillis(it) }
//    }
//
//    fun getSleepStages(session: SleepSessionRecord): Map<Int, Duration> {
//        return session.stages.groupBy { it.stage }
//            .mapValues { (_, stages) ->
//                stages.sumOf { stage ->
//                    Duration.between(stage.startTime, stage.endTime).toMillis()
//                }.let { Duration.ofMillis(it) }
//            }
//    }
//
//    fun getJsonString(session: SleepSessionRecord): String {
//        val gson = GsonBuilder()
//            .registerTypeAdapter(Instant::class.java, object : TypeAdapter<Instant>() {
//                override fun write(out: JsonWriter, value: Instant?) {
//                    out.value(value?.toEpochMilli())
//                }
//
//                override fun read(`in`: JsonReader): Instant {
//                    return Instant.ofEpochMilli(`in`.nextLong())
//                }
//            })
//            .setPrettyPrinting()
//            .create()
//        return gson.toJson(session)
//    }
//
//    fun getFormattedDate(instant: Instant): String {
//        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
//            .withZone(ZoneId.systemDefault())
//        return formatter.format(instant)
//    }
//
//    fun getFormattedDuration(duration: Duration): String {
//        val hours = duration.toHours()
//        val minutes = duration.minusHours(hours).toMinutes()
//        return "${hours}h ${minutes}m"
//    }
//}
//
//@Composable
//fun SleepDataScreen(viewModel: SleepViewModel, onBackClick: () -> Unit) {
//    val sleepSessions by viewModel.sleepSessions.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val expandedSessionId by viewModel.expandedSessionId.collectAsState()
//
//    LaunchedEffect(Unit) {
//        viewModel.fetchSleepSessions()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Sleep Sessions",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        if (isLoading) {
//            CircularProgressIndicator(
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
//        } else {
//            LazyColumn(
//                modifier = Modifier.weight(1f)
//            ) {
//                items(sleepSessions.reversed()) { session ->
//                    SleepSessionCard(
//                        session = session,
//                        viewModel = viewModel,
//                        isExpanded = expandedSessionId == session.metadata.id,
//                        onExpandToggle = { viewModel.toggleExpanded(session.metadata.id) }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        BackToMainButton(onBackClick)
//    }
//}
//
//@Composable
//fun BackToMainButton(onBackClick: () -> Unit) {
//    Button(
//        onClick = onBackClick,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Text("Back to Main")
//    }
//}
//
//@Composable
//fun SleepSessionCard(
//    session: SleepSessionRecord,
//    viewModel: SleepViewModel,
//    isExpanded: Boolean,
//    onExpandToggle: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "Sleep Session: ${viewModel.getFormattedDate(session.startTime)}",
//                style = MaterialTheme.typography.titleMedium
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Total Duration: ${viewModel.getFormattedDuration(viewModel.getTotalSleepDuration(session))}",
//                style = MaterialTheme.typography.bodyMedium
//            )
//            Text(
//                text = "Actual Sleep Time: ${viewModel.getFormattedDuration(viewModel.getActualSleepTime(session))}",
//                style = MaterialTheme.typography.bodyMedium
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Sleep Stages:",
//                style = MaterialTheme.typography.bodyMedium
//            )
//            viewModel.getSleepStages(session).forEach { (stage, duration) ->
//                Text(
//                    text = "${getSleepStageString(stage)}: ${viewModel.getFormattedDuration(duration)}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            ExpandButton(isExpanded, onExpandToggle)
//            if (isExpanded) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Raw Data: \n${viewModel.getJsonString(session)}",
//                    style = MaterialTheme.typography.bodySmall,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 8.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun ExpandButton(isExpanded: Boolean, onExpandToggle: () -> Unit) {
//    Button(onClick = onExpandToggle) {
//        Text(text = if (isExpanded) "Hide Sleep Data" else "Show Sleep Data")
//    }
//}
//
//fun getSleepStageString(stage: Int): String {
//    return when (stage) {
//        SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake" // 1
//        SleepSessionRecord.STAGE_TYPE_SLEEPING -> "Sleeping" // 2
//        SleepSessionRecord.STAGE_TYPE_OUT_OF_BED -> "Awake out of Bed" // 3
//        SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light Sleep" // 4
//        SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep Sleep" // 5
//        SleepSessionRecord.STAGE_TYPE_REM -> "REM Sleep" // 6
//        SleepSessionRecord.STAGE_TYPE_AWAKE_IN_BED -> "Awake in Bed" // 7
//        else -> "Unknown" // 0
//    }
//}
//
//
//
//
//
