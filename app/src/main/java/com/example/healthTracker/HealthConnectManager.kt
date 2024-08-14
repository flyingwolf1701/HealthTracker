import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(WeightRecord::class)
    )

    suspend fun readLatestWeight(): Double? = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val oneWeekAgo = now.minus(java.time.Duration.ofDays(7))

        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(oneWeekAgo, now)
        )

        val response = healthConnectClient.readRecords(request)
        response.records.maxByOrNull { it.time }?.weight?.inKilograms
    }

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
            .map { Pair(it.time, it.weight.inKilograms) }
    }

    suspend fun isHealthConnectAvailable(): Boolean {
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    }
}
