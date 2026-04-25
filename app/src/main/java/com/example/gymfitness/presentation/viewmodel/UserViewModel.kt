package com.example.gymfitness.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.data.local.entity.UserEntity
import com.example.gymfitness.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- Navigation & Step State ---
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    var currentStep by mutableStateOf(0)
        private set

    // --- User Input State ---
    var name by mutableStateOf("")
    var age by mutableStateOf("")
    var weight by mutableStateOf("")
    var height by mutableStateOf("") // Removed 'remember' (only for Composables)
    var gender by mutableStateOf("Male")
    var goal by mutableStateOf("Maintain")

    @SuppressLint("HardwareIds")
    private val deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    init {
        checkUserRegistration()
    }

    private fun checkUserRegistration() {
        viewModelScope.launch {
            val user = userDao.getUserFlow().firstOrNull()
            _startDestination.value = if (user != null) Screen.Home.route else Screen.GetStart.route
        }
    }

    // --- Step Navigation Logic ---
    fun nextStep() {
        if (currentStep < 2) currentStep++
    }

    fun previousStep() {
        if (currentStep > 0) currentStep--
    }

    // --- Calculation & Save Logic ---
    fun saveUser(onComplete: () -> Unit) {
        viewModelScope.launch {
            val w = weight.toDoubleOrNull() ?: 70.0
            val h = height.toDoubleOrNull() ?: 170.0
            val a = age.toIntOrNull() ?: 25

            // 1. Calculate Basal Metabolic Rate (BMR) - Mifflin-St Jeor
            val bmr = if (gender == "Male") {
                (10 * w) + (6.25 * h) - (5 * a) + 5
            } else {
                (10 * w) + (6.25 * h) - (5 * a) - 161
            }

            // 2. Activity Multipliers (Adjusted for Gender)
            // Men typically have higher muscle mass (higher TDEE multiplier)
            val activityMultiplier = if (gender == "Male") 1.55 else 1.375
            val tdee = bmr * activityMultiplier

            // 3. Goal Adjustment (Gender-specific calorie gaps)
            val targetCalories = when (goal) {
                "Lose Weight", "Lean Out" -> if (gender == "Male") tdee - 500 else tdee - 350
                "Gain Muscle", "Bulk Up" -> if (gender == "Male") tdee + 500 else tdee + 300
                else -> tdee
            }.toFloat()

            // 4. Macro Ratios (Optimized for Body Type/Gender)
            val proteinTarget: Float
            val carbsTarget: Float
            val fatsTarget: Float

            when (goal) {
                "Gain Muscle", "Bulk Up" -> {
                    // Surplus: High Carb/High Protein
                    proteinTarget = (targetCalories * 0.30f) / 4f
                    carbsTarget = (targetCalories * 0.50f) / 4f
                    fatsTarget = (targetCalories * 0.20f) / 9f
                }
                "Lose Weight", "Lean Out" -> {
                    // Deficit: Very High Protein (to prevent muscle loss)
                    proteinTarget = (targetCalories * 0.40f) / 4f
                    carbsTarget = (targetCalories * 0.30f) / 4f
                    fatsTarget = (targetCalories * 0.30f) / 9f
                }
                else -> {
                    // Maintenance: Balanced
                    proteinTarget = (targetCalories * 0.25f) / 4f
                    carbsTarget = (targetCalories * 0.50f) / 4f
                    fatsTarget = (targetCalories * 0.25f) / 9f
                }
            }

            val newUser = UserEntity(
                deviceId = deviceId,
                age = a,
                gender = gender,
                heightCm = h,
                weightKg = w,
                activityLevel = "Moderate",
                goal = goal,
                bmr = bmr.toFloat(),
                dailyCaloriesTarget = targetCalories,
                proteinTarget = proteinTarget,
                carbsTarget = carbsTarget,
                fatTarget = fatsTarget
            )

            userDao.insertUser(newUser)
            onComplete()
        }
    }

    fun logoutAndClearData(onComplete: () -> Unit) {
        viewModelScope.launch {
            userDao.clearUserData()
            _startDestination.value = Screen.GetStart.route
            onComplete()
        }
    }
}