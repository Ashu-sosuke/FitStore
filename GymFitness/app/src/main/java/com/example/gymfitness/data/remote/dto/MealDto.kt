package com.example.gymfitness.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MealDto(
    @SerializedName("_id") val id: String? = null,
    val deviceId: String,
    val mealType: String,
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val createdAt: String? = null
)

data class MealCreateDto(
    val deviceId: String,
    val mealType: String,
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)

data class DailySummaryDto(
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFats: Double,
    val mealCount: Int
)

data class NutrientDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("calories") val calories: Double,
    @SerializedName("protein_g") val proteinG: Double,
    @SerializedName("carbs_g") val carbsG: Double,
    @SerializedName("fats_g") val fatsG: Double,
    @SerializedName("serving_size") val servingSize: String? = "100g"
)
