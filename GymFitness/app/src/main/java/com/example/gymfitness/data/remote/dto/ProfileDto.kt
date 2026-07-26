package com.example.gymfitness.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName("_id") val id: String? = null,
    val deviceId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val fitnessGoal: String,
    val activityLevel: String,
    val dailyCalorieTarget: Double,
    val proteinTarget: Double? = 0.0,
    val carbsTarget: Double? = 0.0,
    val fatsTarget: Double? = 0.0,
    val activeSplit: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val friendCode: String? = null,
    val showOnLeaderboards: Boolean = true
)

data class ProfileCreateDto(
    val deviceId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val fitnessGoal: String,
    val activityLevel: String,
    val dailyCalorieTarget: Double,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatsTarget: Double,
    val activeSplit: String? = null,
    val friendCode: String? = null,
    val showOnLeaderboards: Boolean = true
)

