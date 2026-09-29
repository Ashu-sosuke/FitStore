package com.example.gymfitness.presentation.viewmodel

import android.content.Context
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
import com.example.gymfitness.utils.TokenManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    private val generateNutritionPlanUseCase: com.example.gymfitness.domain.usecase.meal.GenerateNutritionPlanUseCase,
    private val db: com.example.gymfitness.data.local.database.AppDatabase,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- Navigation & Step State ---
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    var currentStep by mutableStateOf(0)
        private set

    val totalSteps = 6 // Steps 0 to 5

    // --- Step 0: About You ---
    var name by mutableStateOf("")
    var gender by mutableStateOf("Male")
    var age by mutableStateOf("24")

    // --- Step 1: Body & Health ---
    var weight by mutableStateOf("70.0")
    var height by mutableStateOf("175.0")
    var activityLevel by mutableStateOf("Moderate")
    var dailyStepTarget by mutableStateOf(10000)
    var physicalLimitations by mutableStateOf(listOf<String>()) // shoulder, knee, lower_back, wrist, elbow, ankle, neck, none

    // --- Step 2: Goal & History ---
    var goal by mutableStateOf("Build Muscle") // Build Muscle, Lose Fat, Increase Strength, Maintain, Improve Fitness, Body Recomposition
    var goalPriority by mutableStateOf("Maximum muscle growth") // Maximum muscle growth, Maximum strength, Balanced physique, Fat loss while preserving muscle
    var experienceLevel by mutableStateOf("beginner") // beginner, intermediate, advanced
    var trainingHistory by mutableStateOf("6_12m") // just_starting, under_3m, 3_6m, 6_12m, 1_2y, 2y_plus
    var currentRoutine by mutableStateOf("no_routine") // full_body, upper_lower, ppl, bro_split, other, no_routine

    // --- Step 3: Training Setup ---
    var daysPerWeek by mutableStateOf(4) // 2 to 6
    var sessionDurationMinutes by mutableStateOf(60) // 20-30m, 30-45m, 45-60m, 60-75m, 75-90m, 90m+
    var warmupIncluded by mutableStateOf(true)
    var trainingLocation by mutableStateOf("commercial_gym") // commercial_gym, home_gym, outdoor, studio
    var availableEquipments by mutableStateOf(
        listOf("barbell", "dumbbell", "cable", "sled machine", "body weight")
    )

    // --- Step 4: Personalization & Focus ---
    var focusMuscles by mutableStateOf(listOf<String>("Chest", "Shoulders"))
    var avoidMuscles by mutableStateOf(listOf<String>())
    var preferredExercises by mutableStateOf(listOf<String>())
    var dislikedExercises by mutableStateOf(listOf<String>())
    var trainingStyle by mutableStateOf("bodybuilding") // bodybuilding, high_intensity, circuit, strength, mobility, athletic, ai_decide
    var intensityPreference by mutableStateOf("moderate") // easy, moderate, hard, very_hard
    var planAdaptability by mutableStateOf("hybrid") // adaptive, fixed, hybrid
    var progressionModel by mutableStateOf("progressive_overload") // progressive_overload, reps_first, weight_first, ai_decides

    // --- Step 5: Recovery & Nutrition ---
    var sleepHours by mutableStateOf("7_8h") // under_5h, 5_6h, 6_7h, 7_8h, 8h_plus
    var stressLevel by mutableStateOf("moderate") // low, moderate, high
    var dietPreference by mutableStateOf("non_veg") // vegetarian, vegan, non_veg, eggetarian, custom
    var mealFrequency by mutableStateOf(3) // 2, 3, 4, 5+
    var nutritionPriority by mutableStateOf("high_protein") // high_protein, balanced, low_calorie, flexible
    var showOnLeaderboards by mutableStateOf(true)

    var isSavingUser by mutableStateOf(false)
        private set

    val deviceId: String get() = tokenManager.getUserId()

    init {
        checkUserRegistration()
    }

    private fun checkUserRegistration() {
        viewModelScope.launch {
            // First check local Room DB
            val localProfile = repository.getProfile(deviceId)
            if (localProfile != null) {
                _startDestination.value = Screen.Home.route
                return@launch
            }

            // If not in local DB, try fetching from remote backend with a 2-second timeout (returning user on new install)
            try {
                val syncResult = withTimeoutOrNull(2000L) {
                    repository.syncProfile(deviceId)
                }
                if (syncResult?.isSuccess == true) {
                    _startDestination.value = Screen.Home.route
                    return@launch
                }
            } catch (_: Exception) { /* Remote not available */ }

            // No profile found anywhere — show GetStart screen
            _startDestination.value = Screen.GetStart.route

            // Keep observing for profile creation (e.g. after onboarding completes)
            repository.getProfileFlow(deviceId).collect { user ->
                if (user != null) {
                    _startDestination.value = Screen.Home.route
                }
            }
        }
    }

    // --- Step Navigation & Validation Logic ---
    fun nextStep() {
        if (currentStep < totalSteps - 1 && isCurrentStepValid()) {
            currentStep++
        }
    }

    fun previousStep() {
        if (currentStep > 0) currentStep--
    }

    fun isCurrentStepValid(): Boolean {
        return when (currentStep) {
            0 -> name.trim().length >= 2
            1 -> {
                val w = weight.toDoubleOrNull()
                val h = height.toDoubleOrNull()
                val a = age.toIntOrNull()
                w != null && w in 20.0..300.0 && h != null && h in 100.0..250.0 && a != null && a in 12..100
            }
            else -> true
        }
    }

    // --- Multi-Select Toggle Helpers ---
    fun toggleLimitation(lim: String) {
        physicalLimitations = if (lim == "none") {
            listOf("none")
        } else {
            val list = physicalLimitations.filter { it != "none" }
            if (list.contains(lim)) list - lim else list + lim
        }
    }

    fun toggleFocusMuscle(muscle: String) {
        focusMuscles = if (focusMuscles.contains(muscle)) {
            focusMuscles - muscle
        } else {
            focusMuscles + muscle
        }
    }

    fun toggleAvoidMuscle(muscle: String) {
        avoidMuscles = if (avoidMuscles.contains(muscle)) {
            avoidMuscles - muscle
        } else {
            avoidMuscles + muscle
        }
    }

    fun toggleEquipment(equipmentId: String) {
        availableEquipments = if (availableEquipments.contains(equipmentId)) {
            val remaining = availableEquipments - equipmentId
            if (remaining.isEmpty()) listOf("body weight") else remaining
        } else {
            availableEquipments + equipmentId
        }
    }

    // --- Calculation & Save Logic ---
    fun saveUser(onComplete: () -> Unit) {
        viewModelScope.launch {
            isSavingUser = true
            val w = weight.toDoubleOrNull() ?: 70.0
            val h = height.toDoubleOrNull() ?: 175.0
            val a = age.toIntOrNull() ?: 24

            // 1. Calculate Basal Metabolic Rate (BMR) - Mifflin-St Jeor Formula
            val bmr = if (gender.equals("Male", ignoreCase = true)) {
                (10 * w) + (6.25 * h) - (5 * a) + 5
            } else {
                (10 * w) + (6.25 * h) - (5 * a) - 161
            }

            // 2. Activity Multiplier
            val activityMultiplier = when (activityLevel.lowercase()) {
                "sedentary" -> 1.2
                "light", "lightly_active" -> 1.375
                "moderate", "moderately_active" -> 1.55
                "very", "very_active" -> 1.725
                "extra", "extra_active" -> 1.9
                else -> 1.55
            }
            val tdee = bmr * activityMultiplier

            // 3. Goal Adjustment
            val targetCalories = when {
                goal.contains("Lose", ignoreCase = true) || goal.contains("Fat", ignoreCase = true) -> {
                    if (gender.equals("Male", ignoreCase = true)) tdee - 500 else tdee - 350
                }
                goal.contains("Build", ignoreCase = true) || goal.contains("Muscle", ignoreCase = true) -> {
                    if (gender.equals("Male", ignoreCase = true)) tdee + 450 else tdee + 300
                }
                goal.contains("Strength", ignoreCase = true) -> tdee + 200
                goal.contains("Recomposition", ignoreCase = true) -> tdee
                else -> tdee
            }

            // 4. Macro Ratios based on Nutrition Priority & Goal
            val pTarget: Double
            val cTarget: Double
            val fTarget: Double

            when {
                nutritionPriority == "high_protein" || goal.contains("Muscle", ignoreCase = true) -> {
                    pTarget = (targetCalories * 0.30) / 4.0
                    cTarget = (targetCalories * 0.45) / 4.0
                    fTarget = (targetCalories * 0.25) / 9.0
                }
                nutritionPriority == "low_calorie" || goal.contains("Fat", ignoreCase = true) -> {
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
            val mappedGoal = when {
                goal.contains("Lose", ignoreCase = true) || goal.contains("Fat", ignoreCase = true) -> "weight_loss"
                goal.contains("Build", ignoreCase = true) || goal.contains("Muscle", ignoreCase = true) -> "muscle_gain"
                goal.contains("Strength", ignoreCase = true) -> "strength"
                goal.contains("Recomposition", ignoreCase = true) -> "recomposition"
                else -> "maintenance"
            }

            val mappedActivity = when (activityLevel.lowercase()) {
                "sedentary" -> "sedentary"
                "light" -> "lightly_active"
                "moderate" -> "moderately_active"
                "very" -> "very_active"
                "extra" -> "extra_active"
                else -> "moderately_active"
            }

            val newUser = UserProfile(
                deviceId = deviceId,
                name = name.trim().ifEmpty { "Fitness Champion" },
                age = a,
                gender = gender,
                height = h,
                weight = w,
                fitnessGoal = mappedGoal,
                activityLevel = mappedActivity,
                dailyCalorieTarget = targetCalories.roundToInt().toDouble(),
                proteinTarget = pTarget.roundToInt().toDouble(),
                carbsTarget = cTarget.roundToInt().toDouble(),
                fatsTarget = fTarget.roundToInt().toDouble(),
                showOnLeaderboards = showOnLeaderboards,
                dailyStepTarget = dailyStepTarget,
                experienceLevel = experienceLevel,
                daysPerWeekAvailable = daysPerWeek,
                goalPriority = goalPriority,
                trainingHistory = trainingHistory,
                currentRoutine = currentRoutine,
                trainingLocation = trainingLocation,
                warmupIncluded = warmupIncluded,
                sessionDurationMinutes = sessionDurationMinutes,
                focusMuscles = focusMuscles,
                avoidMuscles = avoidMuscles,
                preferredExercises = preferredExercises,
                dislikedExercises = dislikedExercises,
                physicalLimitations = physicalLimitations,
                trainingStyle = trainingStyle,
                intensityPreference = intensityPreference,
                sleepHours = sleepHours,
                stressLevel = stressLevel,
                dietPreference = dietPreference,
                mealFrequency = mealFrequency,
                nutritionPriority = nutritionPriority,
                planAdaptability = planAdaptability,
                progressionModel = progressionModel
            )

            // Save user profile immediately to Room & Remote MongoDB
            repository.saveProfile(newUser)

            // Generate personalized nutritionist meal plan based on onboarding answers
            try {
                generateNutritionPlanUseCase(newUser)
            } catch (e: Exception) {
                android.util.Log.e("USER_VM", "Failed to generate nutrition plan: ${e.message}")
            }

            // Generate & adopt intelligent multi-factor AI workout plan
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
                    goalPriority = goalPriority,
                    focusMuscles = focusMuscles,
                    avoidMuscles = avoidMuscles,
                    preferredExercises = preferredExercises,
                    dislikedExercises = dislikedExercises,
                    physicalLimitations = physicalLimitations,
                    trainingStyle = trainingStyle,
                    intensityPreference = intensityPreference,
                    sleepHours = sleepHours,
                    stressLevel = stressLevel,
                    trainingLocation = trainingLocation,
                    warmupIncluded = warmupIncluded,
                    progressionModel = progressionModel
                )
                val generated = generateWorkoutPlanUseCase(planPrefs)
                generated.getOrNull()?.let { plan ->
                    workoutRepository.adoptPlan(deviceId, plan)
                }
            } catch (_: Exception) {
                // Background sync queues if offline
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
                        "weight_loss" -> "Lose Fat"
                        "muscle_gain" -> "Build Muscle"
                        "strength" -> "Increase Strength"
                        "recomposition" -> "Body Recomposition"
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
                    dailyStepTarget = if (it.dailyStepTarget > 0) it.dailyStepTarget else 10000
                }
            }
        }
    }

    fun updateStepTarget(target: Int) {
        dailyStepTarget = target
        viewModelScope.launch {
            val current = repository.getProfile(deviceId)
            if (current != null) {
                val updated = current.copy(dailyStepTarget = target)
                repository.saveProfile(updated)
            }
        }
    }

    fun logoutAndClearData(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                db.clearAllTables()
            } catch (_: Exception) { }
            // Sign out of Firebase to clear the persisted auth session
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) { }
            tokenManager.clearUserId()
            tokenManager.clearToken()
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