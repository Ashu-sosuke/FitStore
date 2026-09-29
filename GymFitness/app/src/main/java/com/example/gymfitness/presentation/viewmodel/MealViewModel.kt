package com.example.gymfitness.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.data.local.dao.MealDao
import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.data.remote.api.FoodApiService
import com.example.gymfitness.data.remote.api.toMultipartBody
import com.example.gymfitness.domain.models.PersonalizedMealPlan
import com.example.gymfitness.domain.models.PlannedMealItem
import com.example.gymfitness.domain.models.UserProfile
import com.example.gymfitness.domain.repository.MealRepository
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.usecase.meal.GenerateNutritionPlanUseCase
import com.example.gymfitness.utils.NutritionPlanManager
import com.example.gymfitness.utils.TokenManager
import com.example.gymfitness.data.remote.api.AuthApiService
import com.example.gymfitness.data.remote.api.AuthRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val authApi: AuthApiService,
    private val mealRepository: MealRepository,
    private val userRepository: UserRepository,
    private val nutritionPlanManager: NutritionPlanManager,
    private val generateNutritionPlanUseCase: GenerateNutritionPlanUseCase,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val deviceId: String get() = tokenManager.getUserId()

    val userProfile: StateFlow<UserProfile?> = userRepository.getProfileFlow(tokenManager.getUserId())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val personalizedPlan: StateFlow<PersonalizedMealPlan?> = nutritionPlanManager.currentPlan

    val todayMeals: StateFlow<List<MealEntity>> = dao.getMealsForDay(getStartOfDay(System.currentTimeMillis()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scannedFood = MutableStateFlow<MealEntity?>(null)
    val scannedFood = _scannedFood.asStateFlow()

    private val _scanAlternatives = MutableStateFlow<List<String>>(emptyList())
    val scanAlternatives = _scanAlternatives.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError = _scanError.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.example.gymfitness.data.remote.dto.NutrientDto>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    init {
        // Load cached plan for active device
        nutritionPlanManager.getPlan(deviceId)

        // Observe profile to generate plan automatically if not generated yet
        viewModelScope.launch {
            userRepository.getProfileFlow(deviceId).collect { profile ->
                if (profile != null && nutritionPlanManager.getPlan(deviceId) == null) {
                    try {
                        generateNutritionPlanUseCase(profile)
                    } catch (e: Exception) {
                        Log.e("MEAL_VM", "Plan generation error: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Analyzes image captured via Camera shutter button or Gallery picker.
     */
    fun analyzeCapturedBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _scanError.value = null
            Log.d("SCANNER", "📸 Analyzing captured food image...")

            try {
                // Ensure JWT token exists for backend request authentication
                if (tokenManager.getToken() == null) {
                    try {
                        val authRes = authApi.getAccessToken(AuthRequestDto(deviceId = tokenManager.getUserId()))
                        tokenManager.saveToken(authRes.access_token)
                        Log.d("SCANNER", "Acquired guest JWT token successfully")
                    } catch (authErr: Exception) {
                        Log.w("SCANNER", "Could not fetch auto-guest token, proceeding with guest headers: ${authErr.message}")
                    }
                }

                val body = bitmap.toMultipartBody()
                val response = api.scanFood(body, userId = tokenManager.getUserId())

                Log.d("SCANNER", "✅ Success: Received ${response.foodName}")
                _scanAlternatives.value = response.topAlternatives ?: emptyList()

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
                Log.e("SCANNER", "❌ Error during scan: ${e.localizedMessage}")
                _scanError.value = e.localizedMessage ?: "Could not recognize food item. Please try again."
                _scannedFood.value = null
                _scanAlternatives.value = emptyList()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearScanError() {
        _scanError.value = null
    }

    /**
     * Backward-compatible alias for frame capture
     */
    fun identifyFoodWithFastAPI(bitmap: Bitmap) {
        analyzeCapturedBitmap(bitmap)
    }

    fun selectAlternativeFood(foodName: String) {
        viewModelScope.launch {
            try {
                val results = mealApi.searchFood(foodName)
                val match = results.firstOrNull()
                val current = _scannedFood.value
                val mealType = current?.mealType ?: "lunch"
                if (match != null) {
                    _scannedFood.value = MealEntity(
                        name = match.foodName,
                        calories = match.calories.toFloat(),
                        proteinG = match.proteinG.toFloat(),
                        carbsG = match.carbsG.toFloat(),
                        fatG = match.fatsG.toFloat(),
                        mealType = mealType
                    )
                } else {
                    _scannedFood.value = current?.copy(name = foodName)
                }
            } catch (e: Exception) {
                Log.e("SCANNER", "Failed to switch alternative: ${e.localizedMessage}")
                val current = _scannedFood.value
                _scannedFood.value = current?.copy(name = foodName)
            }
        }
    }

    fun logScannedFood(
        baseMeal: MealEntity,
        quantityGrams: Float,
        mealType: String
    ) {
        val multiplier = (quantityGrams / 100f).coerceAtLeast(0.01f)
        val calculatedCalories = (baseMeal.calories * multiplier).toDouble()
        val calculatedProtein = (baseMeal.proteinG * multiplier).toDouble()
        val calculatedCarbs = (baseMeal.carbsG * multiplier).toDouble()
        val calculatedFats = (baseMeal.fatG * multiplier).toDouble()

        val foodDisplayName = if (quantityGrams != 100f) "${baseMeal.name} (${quantityGrams.toInt()}g)" else baseMeal.name
        val domainMeal = com.example.gymfitness.domain.models.Meal(
            id = null,
            deviceId = deviceId,
            type = mealType.lowercase(),
            foodName = foodDisplayName,
            calories = calculatedCalories,
            protein = calculatedProtein,
            carbs = calculatedCarbs,
            fats = calculatedFats
        )
        viewModelScope.launch {
            mealRepository.addMeal(domainMeal)
            _scannedFood.value = null
        }
    }

    fun logPlannedMeal(item: PlannedMealItem) {
        val domainMeal = com.example.gymfitness.domain.models.Meal(
            id = null,
            deviceId = deviceId,
            type = item.mealType.lowercase(),
            foodName = "${item.title} (${item.foodDescription})",
            calories = item.calories,
            protein = item.proteinG,
            carbs = item.carbsG,
            fats = item.fatsG
        )
        viewModelScope.launch {
            mealRepository.addMeal(domainMeal)
        }
    }

    fun regenerateMealPlan() {
        val profile = userProfile.value
        if (profile != null) {
            generateNutritionPlanUseCase(profile)
        }
    }

    fun saveMealToRoom(meal: MealEntity) {
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
        _scanError.value = null
    }

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

    fun logFoodAsMeal(
        nutrient: com.example.gymfitness.data.remote.dto.NutrientDto,
        mealType: String,
        quantityGrams: Float = 100f
    ) {
        val multiplier = (quantityGrams / 100f).coerceAtLeast(0.01f)
        val meal = com.example.gymfitness.domain.models.Meal(
            id = null,
            deviceId = deviceId,
            type = mealType,
            foodName = if (quantityGrams != 100f) "${nutrient.foodName} (${quantityGrams.toInt()}g)" else nutrient.foodName,
            calories = nutrient.calories * multiplier,
            protein = nutrient.proteinG * multiplier,
            carbs = nutrient.carbsG * multiplier,
            fats = nutrient.fatsG * multiplier
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
                val savedNutrient = mealApi.addCustomFood(customFood)
                logFoodAsMeal(savedNutrient, mealType)
            } catch (e: Exception) {
                Log.e("MEAL_VM", "Failed to add custom food: ${e.localizedMessage}")
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