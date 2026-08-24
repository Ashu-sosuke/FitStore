package com.example.gymfitness.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.domain.models.PlanGenerationPreferences
import com.example.gymfitness.domain.models.UserProfile
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.repository.WorkoutRepository
import com.example.gymfitness.domain.usecase.workout.GenerateWorkoutPlanUseCase
import com.example.gymfitness.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val db: com.example.gymfitness.data.local.database.AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- Navigation & Step State ---
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    var currentStep by mutableStateOf(0)
        private set

    // --- User Input State ---
    var name by mutableStateOf("")
    var age by mutableStateOf("23")
    var weight by mutableStateOf("52.0")
    var height by mutableStateOf("173.0")
    var gender by mutableStateOf("Male")
    var goal by mutableStateOf("Gain Muscle")
    var activityLevel by mutableStateOf("Moderate")
    var showOnLeaderboards by mutableStateOf(true)

    // --- Routine Builder & Schedule State ---
    var experienceLevel by mutableStateOf("beginner") // beginner, intermediate, advanced
    var daysPerWeek by mutableStateOf(5) // 2 to 6
    var sessionDurationMinutes by mutableStateOf(60) // 30, 45, 60, 90
    var availableEquipments by mutableStateOf(
        listOf("barbell", "dumbbell", "cable", "sled machine", "body weight")
    )
    var focusMuscles by mutableStateOf("Full Body Balance")

    var isSavingUser by mutableStateOf(false)
        private set

    @SuppressLint("HardwareIds")
    val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "default_device"

    init {
        checkUserRegistration()
    }

    private fun checkUserRegistration() {
        viewModelScope.launch {
            repository.getProfileFlow(deviceId).collect { user ->
                _startDestination.value = if (user != null) Screen.Home.route else Screen.GetStart.route
            }
        }
    }

    // --- Step Navigation Logic ---
    fun nextStep() {
        if (currentStep < 3) currentStep++
    }

    fun previousStep() {
        if (currentStep > 0) currentStep--
    }

    fun toggleEquipment(equipmentId: String) {
        availableEquipments = if (availableEquipments.contains(equipmentId)) {
            availableEquipments - equipmentId
        } else {
            availableEquipments + equipmentId
        }
    }

    // --- Calculation & Save Logic ---
    fun saveUser(onComplete: () -> Unit) {
        viewModelScope.launch {
            isSavingUser = true
            val w = weight.toDoubleOrNull() ?: 52.0
            val h = height.toDoubleOrNull() ?: 173.0
            val a = age.toIntOrNull() ?: 23

            // 1. Calculate Basal Metabolic Rate (BMR) - Mifflin-St Jeor
            val bmr = if (gender.equals("Male", ignoreCase = true)) {
                (10 * w) + (6.25 * h) - (5 * a) + 5
            } else {
                (10 * w) + (6.25 * h) - (5 * a) - 161
            }

            // 2. Activity Multipliers
            val activityMultiplier = when (activityLevel) {
                "Sedentary" -> 1.2
                "Light" -> 1.375
                "Moderate" -> 1.55
                "Very" -> 1.725
                "Extra" -> 1.9
                else -> 1.55
            }
            val tdee = bmr * activityMultiplier

            // 3. Goal Adjustment
            val targetCalories = when (goal) {
                "Lose Weight", "Lean Out", "weight_loss", "cut_down" -> if (gender.equals("Male", ignoreCase = true)) tdee - 500 else tdee - 350
                "Gain Muscle", "Bulk Up", "muscle_gain", "bulk_up" -> if (gender.equals("Male", ignoreCase = true)) tdee + 500 else tdee + 300
                else -> tdee
            }

            // 4. Macro Ratios
            val pTarget: Double
            val cTarget: Double
            val fTarget: Double

            when (goal) {
                "Gain Muscle", "Bulk Up", "muscle_gain", "bulk_up" -> {
                    pTarget = (targetCalories * 0.30) / 4.0
                    cTarget = (targetCalories * 0.50) / 4.0
                    fTarget = (targetCalories * 0.20) / 9.0
                }
                "Lose Weight", "Lean Out", "weight_loss", "cut_down" -> {
                    pTarget = (targetCalories * 0.40) / 4.0
                    cTarget = (targetCalories * 0.30) / 4.0
                    fTarget = (targetCalories * 0.30) / 9.0
                }
                else -> {
                    pTarget = (targetCalories * 0.25) / 4.0
                    cTarget = (targetCalories * 0.50) / 4.0
                    fTarget = (targetCalories * 0.25) / 9.0
                }
            }

            // 5. Map UI Strings to Backend Enums
            val mappedGoal = when (goal) {
                "Lose Weight", "Lean Out", "weight_loss", "cut_down" -> "weight_loss"
                "Gain Muscle", "Bulk Up", "muscle_gain", "bulk_up" -> "muscle_gain"
                "Maintain", "maintenance" -> "maintenance"
                "Strength", "strength" -> "strength"
                "Endurance", "endurance" -> "endurance"
                else -> "muscle_gain"
            }

            val mappedActivity = when (activityLevel) {
                "Sedentary", "sedentary" -> "sedentary"
                "Light", "lightly_active" -> "lightly_active"
                "Moderate", "moderately_active" -> "moderately_active"
                "Very", "very_active" -> "very_active"
                "Extra", "extra_active" -> "extra_active"
                else -> "moderately_active"
            }

            val newUser = UserProfile(
                deviceId = deviceId,
                name = name.ifEmpty { "Fitness Champion" },
                age = a,
                gender = gender,
                height = h,
                weight = w,
                fitnessGoal = mappedGoal,
                activityLevel = mappedActivity,
                dailyCalorieTarget = targetCalories,
                proteinTarget = pTarget,
                carbsTarget = cTarget,
                fatsTarget = fTarget,
                showOnLeaderboards = showOnLeaderboards
            )

            // Save user profile
            repository.saveProfile(newUser)

            // Auto-generate & adopt AI personalized workout split
            try {
                val planPrefs = PlanGenerationPreferences(
                    deviceId = deviceId,
                    weightKg = w.toFloat(),
                    heightCm = h.toFloat(),
                    age = a,
                    gender = gender.lowercase(),
                    fitnessGoal = if (mappedGoal == "muscle_gain") "bulk_up" else if (mappedGoal == "weight_loss") "cut_down" else mappedGoal,
                    daysPerWeek = daysPerWeek,
                    sessionDurationMinutes = sessionDurationMinutes,
                    experienceLevel = experienceLevel,
                    availableEquipment = availableEquipments,
                    focusMuscles = listOf(focusMuscles)
                )
                val generated = generateWorkoutPlanUseCase(planPrefs)
                generated.getOrNull()?.let { plan ->
                    workoutRepository.adoptPlan(deviceId, plan)
                }
            } catch (_: Exception) {
                // Keep moving smoothly even if backend sync queues for offline sync
            }

            isSavingUser = false
            onComplete()
        }
    }

    fun fetchUserDetail() {
        viewModelScope.launch {
            repository.getProfileFlow(deviceId).collect { user ->
                user?.let {
                    name = it.name
                    age = it.age.toString()
                    gender = it.gender
                    weight = it.weight.toString()
                    height = it.height.toString()
                    goal = when (it.fitnessGoal) {
                        "weight_loss" -> "Lose Weight"
                        "muscle_gain" -> "Gain Muscle"
                        else -> "Maintain"
                    }
                    activityLevel = when (it.activityLevel) {
                        "sedentary" -> "Sedentary"
                        "lightly_active" -> "Light"
                        "moderately_active" -> "Moderate"
                        "very_active" -> "Very"
                        "extra_active" -> "Extra"
                        else -> "Moderate"
                    }
                    showOnLeaderboards = it.showOnLeaderboards
                }
            }
        }
    }

    fun logoutAndClearData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                db.clearAllTables()
            } catch (e: Exception) {
                // fallback
            }
            _startDestination.value = Screen.GetStart.route
            onComplete()
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            val meals = db.mealDao().getAllMealsList()
            val workouts = db.workoutDao().getAllWorkoutsList()
            com.example.gymfitness.utils.ExportUtils.exportToCSV(context, workouts, meals)
        }
    }
}