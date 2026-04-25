package com.example.gymfitness.domain.usecase.workout

import com.example.gymfitness.data.local.entity.SetEntity
import com.example.gymfitness.domain.repository.WorkoutRepository

class AddSetUseCase(
    private val repository: WorkoutRepository
) {

    suspend operator fun invoke(
        exerciseId: Long,
        reps: Int,
        weight: Float
    ) {

        repository.addSet(
            SetEntity(
                exerciseId = exerciseId,
                reps = reps,
                weightKg = weight
            )
        )
    }

}