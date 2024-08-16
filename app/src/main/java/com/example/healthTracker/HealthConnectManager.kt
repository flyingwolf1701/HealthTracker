package com.example.healthTracker

import android.content.Context
import android.util.Log
import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContract
//import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
//import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZonedDateTime

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class),
    )

//    private var permissionsGranted = mutableStateOf(false)

    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

//    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
//        return PermissionController.createRequestPermissionResultContract()
//    }

//    suspend fun readLatestWeight(): Double? = withContext(Dispatchers.IO) {
//        val now = Instant.now()
//        val oneWeekAgo = now.minus(java.time.Duration.ofDays(7))
//
//        val request = ReadRecordsRequest(
//            recordType = WeightRecord::class,
//            timeRangeFilter = TimeRangeFilter.between(oneWeekAgo, now)
//        )
//
//        val response = healthConnectClient.readRecords(request)
//        response.records.maxByOrNull { it.time }?.weight?.inPounds
//    }

    suspend fun readWeightRecords(limit: Int = 10): List<Pair<Instant, Double>> = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val oneYearAgo = now.minus(java.time.Duration.ofDays(365))

        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(oneYearAgo, now)
        )

        val response = healthConnectClient.readRecords(request)
        response.records
            .sortedByDescending { it.time }
            .take(limit)
            .map { Pair(it.time, it.weight.inPounds) }
    }

    suspend fun writeWeightInput(weightInput: Double) {
        val time = ZonedDateTime.now().withNano(0)
        val weightRecord = WeightRecord(
            weight = Mass.pounds(weightInput),
            time = time.toInstant(),
            zoneOffset = time.offset
        )
        val records = listOf(weightRecord)
        try {
            healthConnectClient.insertRecords(records)
            Toast.makeText(context, "Successfully insert records", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            Log.e("com.example.healthTracker.HealthConnectManager", "Error inserting weight record: ${e.message}", e)
            throw e
        }
    }

    suspend fun readSleepSessions(): List<SleepSessionRecord> = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val oneMonthAgo = now.minus(java.time.Duration.ofDays(30))

        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(oneMonthAgo, now)
        )

        val response = healthConnectClient.readRecords(request)
        response.records
    }

//    suspend fun readLatestSleepSession(): SleepSessionRecord? = withContext(Dispatchers.IO) {
//        val now = Instant.now()
//        val oneWeekAgo = now.minus(java.time.Duration.ofDays(7))
//
//        val request = ReadRecordsRequest(
//            recordType = SleepSessionRecord::class,
//            timeRangeFilter = TimeRangeFilter.between(oneWeekAgo, now)
//        )
//
//        val response = healthConnectClient.readRecords(request)
//        response.records.maxByOrNull { it.startTime }
//    }

    fun isHealthConnectAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }
}


