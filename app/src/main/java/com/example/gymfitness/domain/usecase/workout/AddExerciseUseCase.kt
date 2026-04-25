package com.example.gymfitness.domain.usecase.workout

import com.example.gymfitness.data.local.entity.ExerciseEntity
import com.example.gymfitness.domain.repository.WorkoutRepository

class AddExerciseUseCase(
    private val repository: WorkoutRepository
) {

    suspend operator fun invoke(workoutId: Long, name: String) {

        repository.addExercise(
            ExerciseEntity(
                workoutId = workoutId,
                name = name
            )
        )
    }

}