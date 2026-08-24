package com.example.gymfitness.presentation.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.data.local.dao.MealDao
import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.data.remote.api.FoodApiService
import com.example.gymfitness.data.remote.api.toMultipartBody
import com.example.gymfitness.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealViewModel @Inject constructor(
    private val api: FoodApiService,
    private val dao: MealDao,
    private val mealApi: com.example.gymfitness.data.remote.api.MealApiService,
    private val mealRepository: com.example.gymfitness.domain.repository.MealRepository,
    private val tokenManager: TokenManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _scannedFood = MutableStateFlow<MealEntity?>(null)
    val scannedFood = _scannedFood.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.example.gymfitness.data.remote.dto.NutrientDto>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun searchFood(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = mealApi.searchFood(query)
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e("MEAL_VM", "Search failed: ${e.localizedMessage}")
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun logFoodAsMeal(nutrient: com.example.gymfitness.data.remote.dto.NutrientDto, mealType: String) {
        val deviceId = tokenManager.getUserId()
        val meal = com.example.gymfitness.domain.models.Meal(
            id = null,
            deviceId = deviceId,
            type = mealType,
            foodName = nutrient.foodName,
            calories = nutrient.calories,
            protein = nutrient.proteinG,
            carbs = nutrient.carbsG,
            fats = nutrient.fatsG
        )
        viewModelScope.launch {
            mealRepository.addMeal(meal)
        }
    }

    fun addCustomFoodAndLog(
        foodName: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fats: Double,
        mealType: String
    ) {
        viewModelScope.launch {
            try {
                val customFood = com.example.gymfitness.data.remote.dto.NutrientDto(
                    foodName = foodName,
                    calories = calories,
                    proteinG = protein,
                    carbsG = carbs,
                    fatsG = fats,
                    servingSize = "100g"
                )
                // Save custom food to backend database so it shows up in future searches
                val savedNutrient = mealApi.addCustomFood(customFood)
                // Log it as a meal
                logFoodAsMeal(savedNutrient, mealType)
            } catch (e: Exception) {
                Log.e("MEAL_VM", "Failed to add custom food: ${e.localizedMessage}")
                // Fallback: log it locally even if backend fails
                val fallbackNutrient = com.example.gymfitness.data.remote.dto.NutrientDto(
                    foodName = foodName,
                    calories = calories,
                    proteinG = protein,
                    carbsG = carbs,
                    fatsG = fats
                )
                logFoodAsMeal(fallbackNutrient, mealType)
            }
        }
    }

    val todayMeals: StateFlow<List<MealEntity>> = dao.getMealsForDay(getStartOfDay(System.currentTimeMillis()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prevents flooding the 172.29.97.7 server with 30fps camera frames
    private var lastAnalysisTime = 0L

    fun identifyFoodWithFastAPI(bitmap: Bitmap) {
        val currentTime = System.currentTimeMillis()

        // Throttling: Only scan if not currently busy AND 2 seconds have passed
        if (_isAnalyzing.value || (currentTime - lastAnalysisTime < 2000)) return

        lastAnalysisTime = currentTime

        viewModelScope.launch {
            _isAnalyzing.value = true
            Log.d("SCANNER", "🚀 Uploading image for analysis...")

            try {
                // FIXED: 'body' is now correctly defined here
                val body = bitmap.toMultipartBody()

                // Triggers the predict_food() flow in your Python backend
                val response = api.scanFood(body)

                Log.d("SCANNER", "✅ Success: Received ${response.foodName}")

                // Determine meal type based on time
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val mappedMealType = when (hour) {
                    in 5..10 -> "breakfast"
                    in 11..15 -> "lunch"
                    in 18..22 -> "dinner"
                    else -> "snack"
                }

                _scannedFood.value = MealEntity(
                    name = response.foodName,
                    calories = response.macros.calories.toFloat(),
                    proteinG = response.macros.proteinG.toFloat(),
                    carbsG = response.macros.carbsG.toFloat(),
                    fatG = response.macros.fatsG.toFloat(),
                    mealType = mappedMealType
                )
            } catch (e: Exception) {
                Log.e("SCANNER", "❌ Error: ${e.localizedMessage}")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun saveMealToRoom(meal: MealEntity) {
        val deviceId = tokenManager.getUserId()
        val domainMeal = com.example.gymfitness.domain.models.Meal(
            id = null,
            deviceId = deviceId,
            type = meal.mealType,
            foodName = meal.name,
            calories = meal.calories.toDouble(),
            protein = meal.proteinG.toDouble(),
            carbs = meal.carbsG.toDouble(),
            fats = meal.fatG.toDouble()
        )
        viewModelScope.launch {
            mealRepository.addMeal(domainMeal)
        }
    }

    fun clearResult() {
        _scannedFood.value = null
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
}