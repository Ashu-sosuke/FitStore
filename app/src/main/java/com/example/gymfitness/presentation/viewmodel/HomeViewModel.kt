package com.example.gymfitness.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.domain.repository.MealRepository
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.repository.WeightRepository
import com.example.gymfitness.presentation.state.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val weightRepository: WeightRepository,
    private val userRepository: UserRepository
): ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadDashBoard()
    }


    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.getUserFlow().collect { user ->
                user?.let {
                    _state.update { currentState ->
                        currentState.copy(
                            caloriesTarget = it.dailyCaloriesTarget,
                            proteinTarget = it.proteinTarget,
                            carbsTarget = it.carbsTarget,
                            fatTarget = it.fatTarget,
                            // If you added a name field to UserEntity:
                            // userName = it.name
                        )
                    }
                }
            }
        }
    }


    private fun loadDashBoard(){
        val today = System.currentTimeMillis()

        viewModelScope.launch {
            mealRepository.getMealsForDay(today)
                .collect { meals ->
                    _state.update {
                        it.copy(
                            caloriesEaten = meals.sumOf { m -> m.calories.toDouble() }.toFloat(),
                            protein = meals.sumOf { m -> m.proteinG.toDouble() }.toFloat(),
                            carbs = meals.sumOf { m -> m.carbsG.toDouble() }.toFloat(),
                            fat = meals.sumOf { m -> m.fatG.toDouble() }.toFloat()
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
}