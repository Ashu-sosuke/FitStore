package com.example.gymfitness.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.gymfitness.presentation.state.DayStepEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private val _healthConnectSteps = MutableStateFlow(0)
    val healthConnectSteps: StateFlow<Int> = _healthConnectSteps.asStateFlow()

    private val _distanceKm = MutableStateFlow(0f)
    val distanceKm: StateFlow<Float> = _distanceKm.asStateFlow()

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned.asStateFlow()

    private val _sleepDurationMinutes = MutableStateFlow(0)
    val sleepDurationMinutes: StateFlow<Int> = _sleepDurationMinutes.asStateFlow()

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun fetchDailySteps() {
        if (!isAvailable) return

        try {
            val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val now = Instant.now()

            try {
                val aggRequest = AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        DistanceRecord.DISTANCE_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )

                val aggResponse = healthConnectClient.aggregate(aggRequest)
                val aggSteps = aggResponse[StepsRecord.COUNT_TOTAL]
                val aggDist = aggResponse[DistanceRecord.DISTANCE_TOTAL]

                val steps = aggSteps?.toInt() ?: 0
                _healthConnectSteps.value = steps

                if (aggDist != null && aggDist.inMeters > 0) {
                    _distanceKm.value = (aggDist.inMeters / 1000.0).toFloat()
                } else {
                    _distanceKm.value = steps * 0.00075f
                }

                _caloriesBurned.value = (steps * 0.04f).toInt()
            } catch (e: Exception) {
                // Fallback to raw record query if aggregation isn't available
                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
                val response = healthConnectClient.readRecords(request)
                val totalSteps = response.records.sumOf { it.count }.toInt()
                _healthConnectSteps.value = totalSteps
                _distanceKm.value = totalSteps * 0.00075f
                _caloriesBurned.value = (totalSteps * 0.04f).toInt()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchDailySleep() {
        if (!isAvailable) return

        try {
            val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).minusDays(1).withHour(18).withMinute(0).withSecond(0).toInstant()
            val now = Instant.now()

            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
            )

            val response = healthConnectClient.readRecords(request)
            val totalSleepMinutes = response.records.sumOf { 
                java.time.Duration.between(it.startTime, it.endTime).toMinutes() 
            }
            
            _sleepDurationMinutes.value = totalSleepMinutes.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchWeeklySteps(): List<DayStepEntry> {
        if (!isAvailable) return emptyList()

        return try {
            val zoneId = ZoneId.systemDefault()
            val today = LocalDate.now(zoneId)
            val dayEntries = mutableListOf<DayStepEntry>()

            // 7 days rolling window ending today
            for (i in 6 downTo 0) {
                val date = today.minusDays(i.toLong())
                val startOfDay = date.atStartOfDay(zoneId).toInstant()
                val endOfDay = if (i == 0) Instant.now() else date.plusDays(1).atStartOfDay(zoneId).toInstant()
                val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                try {
                    val aggRequest = AggregateRequest(
                        metrics = setOf(
                            StepsRecord.COUNT_TOTAL,
                            DistanceRecord.DISTANCE_TOTAL
                        ),
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                    )

                    val aggResponse = healthConnectClient.aggregate(aggRequest)
                    var steps = aggResponse[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
                    if (i == 0 && _healthConnectSteps.value > steps) {
                        steps = _healthConnectSteps.value
                    }

                    val aggDist = aggResponse[DistanceRecord.DISTANCE_TOTAL]
                    val distKm = if (aggDist != null && aggDist.inMeters > 0) {
                        (aggDist.inMeters / 1000.0).toFloat()
                    } else {
                        steps * 0.00075f
                    }

                    dayEntries.add(
                        DayStepEntry(
                            date = date,
                            dayLabel = dayLabel,
                            steps = steps,
                            distanceKm = distKm,
                            caloriesBurned = (steps * 0.04f).toInt()
                        )
                    )
                } catch (e: Exception) {
                    val request = ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                    )
                    val response = healthConnectClient.readRecords(request)
                    var totalSteps = response.records.sumOf { it.count }.toInt()
                    if (i == 0 && _healthConnectSteps.value > totalSteps) {
                        totalSteps = _healthConnectSteps.value
                    }
                    dayEntries.add(
                        DayStepEntry(
                            date = date,
                            dayLabel = dayLabel,
                            steps = totalSteps,
                            distanceKm = totalSteps * 0.00075f,
                            caloriesBurned = (totalSteps * 0.04f).toInt()
                        )
                    )
                }
            }
            dayEntries
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
