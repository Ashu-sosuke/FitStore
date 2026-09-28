package com.example.gymfitness.domain.models

object StandardExerciseCatalog {
    val exercises = listOf(
        // CHEST / PECTORALS
        CatalogExercise("3TZduzM", "Barbell Incline Bench Press", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), listOf(SplitType.FULL_BODY, SplitType.UPPER, SplitType.PUSH), 4, 8),
        CatalogExercise("5v7KYld", "Smith Incline Bench Press", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS), listOf(SplitType.UPPER, SplitType.PUSH), 3, 10),
        CatalogExercise("7saC5zz", "Cable Decline Fly", MuscleGroup.CHEST, emptyList(), listOf(SplitType.PUSH), 3, 12),
        CatalogExercise("8eqjhOl", "Dumbbell Palms-In Incline Press", MuscleGroup.CHEST, listOf(MuscleGroup.TRICEPS), listOf(SplitType.UPPER, SplitType.PUSH), 3, 10),

        // BACK / LATS / UPPER BACK
        CatalogExercise("7F1DVzn", "Lever Front Pulldown", MuscleGroup.BACK, listOf(MuscleGroup.BICEPS), listOf(SplitType.FULL_BODY, SplitType.UPPER, SplitType.PULL), 4, 8),
        CatalogExercise("7I6LNUG", "Lever Seated Row", MuscleGroup.BACK, listOf(MuscleGroup.BICEPS), listOf(SplitType.UPPER, SplitType.PULL), 3, 10),
        CatalogExercise("8urJS9b", "Weighted Hyperextension (Stability Ball)", MuscleGroup.BACK, listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS), listOf(SplitType.FULL_BODY, SplitType.PULL), 3, 12),

        // SHOULDERS / DELTS
        CatalogExercise("3eGE2JC", "Dumbbell Front Raise", MuscleGroup.SHOULDERS, emptyList(), listOf(SplitType.FULL_BODY, SplitType.UPPER, SplitType.PUSH), 3, 12),
        CatalogExercise("6cKQC5E", "Dumbbell One-Arm Upright Row", MuscleGroup.SHOULDERS, listOf(MuscleGroup.BACK), listOf(SplitType.UPPER, SplitType.PULL), 3, 10),

        // BICEPS
        CatalogExercise("3XFdb1Z", "Cable Squatting Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 10),
        CatalogExercise("4dF3maG", "Dumbbell One-Arm Hammer Preacher Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 12),
        CatalogExercise("4dUn2iv", "Barbell Standing Close-Grip Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 10),
        CatalogExercise("6sMAmNv", "Dumbbell Reverse Spider Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 12),
        CatalogExercise("7inpWch", "Dumbbell Standing Concentration Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 12),
        CatalogExercise("8oYqOt9", "Cable Seated Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 12),

        // TRICEPS
        CatalogExercise("05Cf2v8", "Impossible Dips", MuscleGroup.TRICEPS, listOf(MuscleGroup.CHEST), listOf(SplitType.UPPER, SplitType.PUSH), 3, 10),
        CatalogExercise("5uFK1xr", "Barbell Seated Overhead Triceps Extension", MuscleGroup.TRICEPS, emptyList(), listOf(SplitType.PUSH), 3, 12),
        CatalogExercise("6MfS53i", "Dumbbell Lying Single Arm Extension", MuscleGroup.TRICEPS, emptyList(), listOf(SplitType.PUSH), 3, 12),

        // FOREARMS
        CatalogExercise("3tAXPQ6", "Dumbbell Over Bench Reverse Wrist Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 15),
        CatalogExercise("6kSxYnw", "Barbell Wrist Curl", MuscleGroup.BICEPS, emptyList(), listOf(SplitType.PULL), 3, 15),

        // LEGS / QUADS / GLUTES / HAMSTRINGS
        CatalogExercise("2Qh2J1e", "Sled 45° Leg Press", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS), listOf(SplitType.FULL_BODY, SplitType.LOWER, SplitType.LEGS), 4, 10),
        CatalogExercise("5bpPTHv", "Kettlebell Pistol Squat", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), listOf(SplitType.LOWER, SplitType.LEGS), 3, 8),
        CatalogExercise("7zdxRTl", "Smith Machine Leg Press", MuscleGroup.QUADS, listOf(MuscleGroup.GLUTES), listOf(SplitType.LOWER, SplitType.LEGS), 3, 12),
        CatalogExercise("6sYyrRX", "Bent Knee Lying Twist", MuscleGroup.GLUTES, listOf(MuscleGroup.ABS_CORE), listOf(SplitType.LOWER, SplitType.LEGS), 3, 15),

        // CALVES
        CatalogExercise("2ORFMoR", "Hack Machine Calf Raise", MuscleGroup.CALVES, emptyList(), listOf(SplitType.LOWER, SplitType.LEGS), 4, 15),
        CatalogExercise("6HiHHe0", "Barbell Standing Rocking Leg Calf Raise", MuscleGroup.CALVES, emptyList(), listOf(SplitType.LOWER, SplitType.LEGS), 3, 15),
        CatalogExercise("8ozhUIZ", "Barbell Standing Calf Raise", MuscleGroup.CALVES, emptyList(), listOf(SplitType.LOWER, SplitType.LEGS), 4, 15),

        // ABS / CORE
        CatalogExercise("6bOA1Oi", "Weighted Side Bend (Stability Ball)", MuscleGroup.ABS_CORE, emptyList(), listOf(SplitType.FULL_BODY, SplitType.LEGS), 3, 15),
        CatalogExercise("8K0w2yA", "Assisted Hanging Knee Raise", MuscleGroup.ABS_CORE, emptyList(), listOf(SplitType.FULL_BODY, SplitType.LEGS), 3, 12),
        CatalogExercise("8xUv4J7", "Cable Seated Crunch", MuscleGroup.ABS_CORE, emptyList(), listOf(SplitType.FULL_BODY, SplitType.PUSH), 3, 15)
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
