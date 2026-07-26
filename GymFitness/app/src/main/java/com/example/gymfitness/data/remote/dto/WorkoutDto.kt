package com.example.gymfitness.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WorkoutDto(
    @SerializedName("_id") val id: String? = null,
    val deviceId: String,
    val workoutName: String,
    val exercises: List<ExerciseDto>,
    val totalVolume: Double,
    val date: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ExerciseDto(
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weight: Double
)

data class WorkoutCreateDto(
    val deviceId: String,
    val workoutName: String,
    val exercises: List<ExerciseDto>,
    val date: String
)
