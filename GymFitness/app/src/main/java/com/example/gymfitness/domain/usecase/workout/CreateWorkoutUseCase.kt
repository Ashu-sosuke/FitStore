package com.example.gymfitness.domain.usecase.workout

import com.example.gymfitness.domain.models.Workout
import com.example.gymfitness.domain.repository.WorkoutRepository
import javax.inject.Inject

class CreateWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout): Result<Workout> {
        return repository.saveWorkout(workout)
    }
}