package com.example.gymfitness.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.domain.repository.MealRepository
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.repository.WeightRepository
import com.example.gymfitness.domain.repository.LeaderboardRepository
import com.example.gymfitness.presentation.state.HomeState
import com.example.gymfitness.utils.HealthConnectManager
import com.example.gymfitness.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val weightRepository: WeightRepository,
    private val userRepository: UserRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val healthConnectManager: HealthConnectManager,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    val deviceId: String get() = tokenManager.getUserId()

    private fun friendCodeFromUserId(userId: String): String {
        if (userId.length < 6) return userId + "123"
        return userId.substring(0, 6).uppercase()
    }

    val friendCode: StateFlow<String> = userRepository.getProfileFlow(deviceId)
        .map { it?.friendCode ?: friendCodeFromUserId(it?.deviceId ?: deviceId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "------")

    init {
        observeUserData()
        loadDashboardData()
        observeHealthConnectSteps()
        observeHealthConnectSleep()
        checkAndUpdateStreak()
        startPeriodicHealthRefresh()
    }

    private fun checkAndUpdateStreak() {
        viewModelScope.launch {
            try {
                userRepository.updateStreak(deviceId)
            } catch (e: Exception) {
                // Non-blocking local failure handling
            }
        }
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.getProfileFlow(deviceId).collect { user ->
                user?.let {
                    _state.update { currentState ->
                        currentState.copy(
                            caloriesTarget = it.dailyCalorieTarget.toFloat(),
                            proteinTarget = it.proteinTarget.toFloat(),
                            carbsTarget = it.carbsTarget.toFloat(),
                            fatsTarget = it.fatsTarget.toFloat(),
                            userName = it.name,
                            currentStreak = it.currentStreak
                        )
                    }
                }
            }
        }
    }

    private fun loadDashboardData() {
        val todayStart = getStartOfDay(System.currentTimeMillis())

        viewModelScope.launch {
            mealRepository.getMealsForDay(deviceId, todayStart)
                .collect { meals ->
                    _state.update { currentState ->
                        currentState.copy(
                            caloriesEaten = meals.sumOf { it.calories }.toFloat(),
                            protein = meals.sumOf { it.protein }.toFloat(),
                            carbs = meals.sumOf { it.carbs }.toFloat(),
                            fat = meals.sumOf { it.fats }.toFloat()
                        )
                    }
                }
        }

        viewModelScope.launch {
            weightRepository.getLatestWeight().collect { weight ->
                _state.update { it.copy(latestWeight = weight?.weightKg) }
            }
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun observeHealthConnectSteps() {
        viewModelScope.launch {
            healthConnectManager.healthConnectSteps.collect { steps ->
                _state.update { it.copy(stepsWalked = steps) }
                try {
                    leaderboardRepository.syncPoints(0)
                } catch (e: Exception) {
                    // Non-blocking sync
                }
            }
        }
        viewModelScope.launch {
            healthConnectManager.distanceKm.collect { dist ->
                _state.update { it.copy(distanceKm = dist) }
            }
        }
        viewModelScope.launch {
            healthConnectManager.caloriesBurned.collect { cal ->
                _state.update { it.copy(caloriesBurned = cal) }
            }
        }
    }

    private fun observeHealthConnectSleep() {
        viewModelScope.launch {
            healthConnectManager.sleepDurationMinutes.collect { mins ->
                _state.update { it.copy(sleepMinutes = mins) }
            }
        }
    }

    fun fetchHealthConnectSteps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                healthConnectManager.fetchDailySteps()
                healthConnectManager.fetchDailySleep()
                val weekly = healthConnectManager.fetchWeeklySteps()
                
                _state.update { currentState ->
                    currentState.copy(
                        weeklySteps = weekly,
                        stepsWalked = healthConnectManager.healthConnectSteps.value,
                        distanceKm = healthConnectManager.distanceKm.value,
                        caloriesBurned = healthConnectManager.caloriesBurned.value,
                        isHealthConnectGranted = healthConnectManager.isAvailable && (weekly.isNotEmpty() || currentState.stepsWalked > 0),
                        isLoading = false
                    )
                }
                try {
                    leaderboardRepository.syncPoints(0)
                } catch (e: Exception) {
                    // Non-blocking leaderboard update
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to load Health Connect data") }
            }
        }
    }

    private fun startPeriodicHealthRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(60_000) // Refresh every 60s for foreground live counters
                if (_state.value.isHealthConnectGranted) {
                    healthConnectManager.fetchDailySteps()
                    healthConnectManager.fetchDailySleep()
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}