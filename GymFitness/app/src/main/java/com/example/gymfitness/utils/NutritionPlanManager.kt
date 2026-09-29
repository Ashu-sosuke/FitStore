package com.example.gymfitness.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.gymfitness.domain.models.PersonalizedMealPlan
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionPlanManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nutrition_plan_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _currentPlan = MutableStateFlow<PersonalizedMealPlan?>(null)
    val currentPlan: StateFlow<PersonalizedMealPlan?> = _currentPlan.asStateFlow()

    fun getPlan(deviceId: String): PersonalizedMealPlan? {
        val cached = _currentPlan.value
        if (cached != null) return cached

        val json = prefs.getString("plan_$deviceId", null) ?: return null
        return try {
            val plan = gson.fromJson(json, PersonalizedMealPlan::class.java)
            _currentPlan.value = plan
            plan
        } catch (e: Exception) {
            null
        }
    }

    fun savePlan(deviceId: String, plan: PersonalizedMealPlan) {
        val json = gson.toJson(plan)
        prefs.edit().putString("plan_$deviceId", json).apply()
        _currentPlan.value = plan
    }

    fun clearPlan(deviceId: String) {
        prefs.edit().remove("plan_$deviceId").apply()
        _currentPlan.value = null
    }
}
