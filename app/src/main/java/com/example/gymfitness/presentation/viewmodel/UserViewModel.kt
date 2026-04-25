package com.example.gymfitness.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
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


    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

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
            // Use your Repository/Dao to check if any user exists
            val user = userDao.getUserFlow().firstOrNull()
            if (user != null) {
                _startDestination.value = Screen.Home.route
            } else {
                _startDestination.value = Screen.GetStart.route
            }
        }
    }
    fun saveUser(
        age: String,
        weight: String,
        height: String,
        gender: String,
        goal: String
    ) {
        viewModelScope.launch {
            val w = weight.toDoubleOrNull() ?: 70.0
            val h = height.toDoubleOrNull() ?: 170.0
            val a = age.toIntOrNull() ?: 25

            // 1. Calculate Basal Metabolic Rate (BMR)
            val bmr = if (gender.equals("Male", ignoreCase = true)) {
                (10 * w) + (6.25 * h) - (5 * a) + 5
            } else {
                (10 * w) + (6.25 * h) - (5 * a) - 161
            }

            // 2. Maintenance Calories (TDEE) - Assuming 'Moderate' activity (1.55)
            val tdee = bmr * 1.55

            // 3. Adjust Calories based on Goal
            val targetCalories = when (goal) {
                "Lose Weight" -> tdee - 500 // Caloric Deficit
                "Gain Muscle" -> tdee + 400 // Caloric Surplus
                else -> tdee               // Maintenance
            }.toFloat()

            // 4. Macronutrient Distribution (Percentage of total calories)
            // Protein: 4 kcal/g, Carbs: 4 kcal/g, Fats: 9 kcal/g
            val proteinTarget: Float
            val carbsTarget: Float
            val fatsTarget: Float

            when (goal) {
                "Gain Muscle" -> {
                    // High Protein, High Carbs
                    proteinTarget = (targetCalories * 0.30f) / 4f
                    carbsTarget = (targetCalories * 0.50f) / 4f
                    fatsTarget = (targetCalories * 0.20f) / 9f
                }
                "Lose Weight" -> {
                    // High Protein, Low Carb (to preserve muscle)
                    proteinTarget = (targetCalories * 0.40f) / 4f
                    carbsTarget = (targetCalories * 0.30f) / 4f
                    fatsTarget = (targetCalories * 0.30f) / 9f
                }
                else -> {
                    // Balanced
                    proteinTarget = (targetCalories * 0.25f) / 4f
                    carbsTarget = (targetCalories * 0.50f) / 4f
                    fatsTarget = (targetCalories * 0.25f) / 9f
                }
            }

            val newUser = UserEntity(
                deviceId = deviceId, // From Settings.Secure
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
        }
    }

    fun logoutAndClearData(onComplete: () -> Unit) {
        viewModelScope.launch {
            userDao.clearUserData()
            _startDestination.value = Screen.GetStart.route // Reset state
            onComplete()
        }
    }
}