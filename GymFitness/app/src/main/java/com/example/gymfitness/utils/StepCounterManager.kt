package com.example.gymfitness.utils

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepCounterManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)

    private val _stepsToday = MutableStateFlow(0)
    val stepsToday: StateFlow<Int> = _stepsToday.asStateFlow()

    private var isListening = false

    init {
        // Load initial values from SharedPreferences
        _stepsToday.value = getSavedStepsToday()
    }

    fun hasPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun startListening() {
        if (!hasPermission()) {
            Log.w("StepCounterManager", "Cannot start listening: ACTIVITY_RECOGNITION permission not granted")
            return
        }
        if (isListening || stepCounterSensor == null) {
            Log.d("StepCounterManager", "Already listening or step counter sensor is null")
            return
        }
        isListening = sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        Log.d("StepCounterManager", "Started listening to step counter sensor: $isListening")
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager.unregisterListener(this)
        isListening = false
        Log.d("StepCounterManager", "Stopped listening to step counter sensor")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return

        val sensorSteps = event.values[0].toInt()
        Log.d("StepCounterManager", "Sensor steps event: $sensorSteps")
        updateSteps(sensorSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    @Synchronized
    private fun updateSteps(sensorSteps: Int) {
        val todayStr = getCurrentDateString()
        val savedDate = sharedPrefs.getString(KEY_LAST_DATE, "") ?: ""
        var stepsAtStart = sharedPrefs.getInt(KEY_STEPS_AT_START, -1)
        var accumulatedSteps = sharedPrefs.getInt(KEY_ACCUMULATED_STEPS, 0)
        var lastSensorSteps = sharedPrefs.getInt(KEY_LAST_SENSOR_STEPS, -1)

        if (savedDate != todayStr) {
            // New day: reset start steps and accumulated steps
            stepsAtStart = sensorSteps
            accumulatedSteps = 0
            lastSensorSteps = sensorSteps

            sharedPrefs.edit()
                .putString(KEY_LAST_DATE, todayStr)
                .putInt(KEY_STEPS_AT_START, stepsAtStart)
                .putInt(KEY_ACCUMULATED_STEPS, accumulatedSteps)
                .putInt(KEY_LAST_SENSOR_STEPS, lastSensorSteps)
                .apply()
        } else {
            // Same day: check if device rebooted
            // If current sensorSteps is less than the last sensor steps we saw (or less than start), a reboot happened
            if (sensorSteps < lastSensorSteps || (stepsAtStart != -1 && sensorSteps < stepsAtStart)) {
                // Reboot detected
                // Calculate steps walked today before reboot and add to accumulated
                val walkedBeforeReboot = if (stepsAtStart != -1 && lastSensorSteps >= stepsAtStart) {
                    lastSensorSteps - stepsAtStart
                } else {
                    0
                }
                accumulatedSteps += walkedBeforeReboot
                stepsAtStart = sensorSteps // sensor steps resets to 0 (or small number) on boot
            }
            
            lastSensorSteps = sensorSteps

            sharedPrefs.edit()
                .putInt(KEY_STEPS_AT_START, stepsAtStart)
                .putInt(KEY_ACCUMULATED_STEPS, accumulatedSteps)
                .putInt(KEY_LAST_SENSOR_STEPS, lastSensorSteps)
                .apply()
        }

        val calculatedStepsToday = if (stepsAtStart != -1) {
            accumulatedSteps + (sensorSteps - stepsAtStart)
        } else {
            accumulatedSteps
        }

        _stepsToday.value = calculatedStepsToday
        
        // Save current calculated steps for recovery if app restarts
        sharedPrefs.edit()
            .putInt(KEY_STEPS_TODAY, calculatedStepsToday)
            .apply()
    }

    private fun getSavedStepsToday(): Int {
        val todayStr = getCurrentDateString()
        val savedDate = sharedPrefs.getString(KEY_LAST_DATE, "") ?: ""
        return if (savedDate == todayStr) {
            sharedPrefs.getInt(KEY_STEPS_TODAY, 0)
        } else {
            0
        }
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    companion object {
        private const val KEY_LAST_DATE = "last_date"
        private const val KEY_STEPS_AT_START = "steps_at_start"
        private const val KEY_ACCUMULATED_STEPS = "accumulated_steps"
        private const val KEY_LAST_SENSOR_STEPS = "last_sensor_steps"
        private const val KEY_STEPS_TODAY = "steps_today"
    }
}
