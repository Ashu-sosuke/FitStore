package com.example.gymfitness.domain.usecase.workout

import com.example.gymfitness.domain.repository.WorkoutRepository
import javax.inject.Inject

class AddSetUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(exerciseId: Long, reps: Int, weightKg: Float) {
        repository.addSet(exerciseId, reps, weightKg)
    }
}