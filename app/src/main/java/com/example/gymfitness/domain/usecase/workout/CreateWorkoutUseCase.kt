package com.example.gymfitness.domain.usecase.workout

import com.example.gymfitness.data.local.entity.WorkoutEntity
import com.example.gymfitness.domain.repository.WorkoutRepository

class CreateWorkoutUseCase(private val repository: WorkoutRepository) {
    suspend operator fun invoke(name: String): Long {
    val workout = WorkoutEntity(name = name)
        return repository.createWorkout(workout)
    }
}