package com.example.gymfitness.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.SleepSessionRecord
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

    private val _sleepDurationMinutes = MutableStateFlow(0)
    val sleepDurationMinutes: StateFlow<Int> = _sleepDurationMinutes.asStateFlow()

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    suspend fun fetchDailySteps() {
        if (!isAvailable) return

        try {
            // Define start and end of current day
            val startOfDay = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val now = Instant.now()

            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
            )

            val response = healthConnectClient.readRecords(request)
            val totalSteps = response.records.sumOf { it.count }
            
            _healthConnectSteps.value = totalSteps.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchDailySleep() {
        if (!isAvailable) return

        try {
            // Define start and end of current day (looking back to previous night: yesterday 18:00 to now)
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

                val request = ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )

                val response = healthConnectClient.readRecords(request)
                val totalSteps = response.records.sumOf { it.count }.toInt()
                val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                dayEntries.add(DayStepEntry(date = date, dayLabel = dayLabel, steps = totalSteps))
            }
            dayEntries
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
