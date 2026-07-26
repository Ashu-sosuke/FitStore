package com.example.gymfitness.domain.models

object StandardExerciseCatalog {
    val exercises = listOf(
        // CHEST
        CatalogExercise("chest_1", "Barbell Bench Press", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), listOf(SplitType.FULL_BODY, SplitType.UPPER, SplitType.PUSH), 4, 8),
        CatalogExercise("chest_2", "Incline Dumbbell Press", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), listOf(SplitType.UPPER, SplitType.PUSH), 3, 10),
        CatalogExercise("chest_3", "Bodyweight Push-Ups", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.ABS_CORE), listOf(SplitType.FULL_BODY, SplitType.PUSH), 3, 15),
        CatalogExercise("chest_4", "Chest Dips", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS), listOf(SplitType.UPPER, SplitType.PUSH), 3, 10),
        CatalogExercise("chest_5", "Cable Pec Fly", MuscleGroup.CHEST, emptyList(), listOf(SplitType.PUSH), 3, 12),

        // BACK
        CatalogExercise("back_1", "Pull-Ups / Lat Pulldown", MuscleGroup.BACK, listOf(MuscleGroup.BICEPS), listOf(SplitType.FULL_BODY, SplitType.UPPER, SplitType.PULL), 4, 8),
        CatalogExercise("back_2", "Barbell Bent-Over Row", MuscleGroup.BACK, listOf(MuscleGroup.BICEPS), listOf(SplitType.UPPER, SplitType.PULL), 3, 10),
        CatalogExercise("back_3", "Conventional Deadlift", MuscleGroup.BACK, listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES), listOf(SplitType.FULL_BODY, SplitType.PULL), 3, 5),
        CatalogExercise("back_4", "Seated Cable Row", MuscleGroup.BACK, listOf(MuscleGroup.BICEPS), listOf(SplitType.PULL), 3, 12),
        CatalogExercise("back_5", "Face Pulls", MuscleGroup.BACK, listOf(MuscleGroup.SHOULDERS), listOf(SplitType.PULL), 3, 15),

        // SHOULDERS
        CatalogExercise("sh_1", "Overhead Barbell Press", MuscleGroup.SHOULDERS, listOf(MuscleGroup.TRICEPS), listOf(SplitType.FULL_BODY, SplitType.UPPER, SplitType.PUSH), 4, 8),
        CatalogExercise("sh_2", "Dumbbell Lateral Raises", MuscleGroup.SHOULDERS, emptyList(), listOf(SplitType.UPPER, SplitType.PUSH), 4, 12),
        CatalogExercise("sh_3", "Reverse Cable Fly", MuscleGroup.SHOULDERS, listOf(MuscleGroup.BACK), listOf(SplitType.PULL), 3, 15),

        // BICEPS & TRICEPS
        CatalogExercise("arm_1", "Barbell Bicep Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 10),
        CatalogExercise("arm_2", "Dumbbell Hammer Curls", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 12),
        CatalogExercise("arm_3", "Tricep Rope Pushdown", MuscleGroup.TRICEPS, emptyList(), listOf(SplitType.PUSH), 3, 12),
        CatalogExercise("arm_4", "Skull Crushers", MuscleGroup.TRICEPS, emptyList(), listOf(SplitType.PUSH), 3, 10),

        // QUADS & HAMSTRINGS & GLUTES & CALVES
        CatalogExercise("leg_1", "Barbell Back Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS), listOf(SplitType.FULL_BODY, SplitType.LOWER, SplitType.LEGS), 4, 8),
        CatalogExercise("leg_2", "Leg Press", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), listOf(SplitType.LOWER, SplitType.LEGS), 3, 12),
        CatalogExercise("leg_3", "Romanian Deadlift (RDL)", MuscleGroup.HAMSTRINGS, listOf(MuscleGroup.GLUTES), listOf(SplitType.FULL_BODY, SplitType.LOWER, SplitType.LEGS), 3, 10),
        CatalogExercise("leg_4", "Lying Leg Curl", MuscleGroup.HAMSTRINGS, emptyList(), listOf(SplitType.LEGS), 3, 12),
        CatalogExercise("leg_5", "Barbell Hip Thrust", MuscleGroup.GLUTES, listOf(MuscleGroup.HAMSTRINGS), listOf(SplitType.LOWER, SplitType.LEGS), 4, 10),
        CatalogExercise("leg_6", "Walking Dumbbell Lunges", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), listOf(SplitType.LEGS), 3, 12),
        CatalogExercise("leg_7", "Standing Calf Raise", MuscleGroup.CALVES, emptyList(), listOf(SplitType.LOWER, SplitType.LEGS), 4, 15),

        // ABS & CORE
        CatalogExercise("core_1", "Plank Hold", MuscleGroup.ABS_CORE, emptyList(), listOf(SplitType.FULL_BODY, SplitType.CARDIO, SplitType.MOBILITY_YOGA), 3, 60),
        CatalogExercise("core_2", "Hanging Leg Raises", MuscleGroup.ABS_CORE, emptyList(), listOf(SplitType.FULL_BODY, SplitType.LEGS), 3, 12),
        CatalogExercise("core_3", "Cable Kneeling Crunch", MuscleGroup.ABS_CORE, emptyList(), listOf(SplitType.PUSH), 3, 15)
    )

    fun getExercisesForSplit(splitType: SplitType): List<CatalogExercise> {
        if (splitType == SplitType.ALL) return exercises
        return exercises.filter { it.splitTypes.contains(splitType) }
    }

    fun getExercisesForMuscle(muscleGroup: MuscleGroup): List<CatalogExercise> {
        if (muscleGroup == MuscleGroup.FULL_BODY) return exercises
        return exercises.filter { it.primaryMuscle == muscleGroup || it.secondaryMuscles.contains(muscleGroup) }
    }
}
