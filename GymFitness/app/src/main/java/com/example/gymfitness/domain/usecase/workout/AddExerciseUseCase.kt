package com.example.gymfitness.domain.usecase.workout

import com.example.gymfitness.domain.repository.WorkoutRepository
import javax.inject.Inject

class AddExerciseUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workoutId: Long, name: String): Long {
        return repository.addExercise(workoutId, name)
    }
}