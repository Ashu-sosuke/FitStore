package com.example.gymfitness.domain.models

enum class SplitType(val displayName: String, val categoryIcon: String) {
    ALL("All", "⚡"),
    FULL_BODY("Full Body", "🏋️"),
    UPPER("Upper", "💪"),
    LOWER("Lower", "🦵"),
    PUSH("Push", "🔥"),
    PULL("Pull", "🎯"),
    LEGS("Legs", "🍗"),
    CARDIO("Cardio", "🏃"),
    MOBILITY_YOGA("Yoga & Mobility", "🧘")
}

enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    QUADS("Quads"),
    HAMSTRINGS("Hamstrings"),
    GLUTES("Glutes"),
    CALVES("Calves"),
    ABS_CORE("Abs & Core"),
    FULL_BODY("Full Body")
}

enum class ExperienceLevel(val displayName: String) {
    BEGINNER("Beginner (< 3 months)"),
    INTERMEDIATE("Intermediate (3-12 months)"),
    ADVANCED("Advanced (1+ years)")
}

data class CatalogExercise(
    val id: String,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val splitTypes: List<SplitType> = emptyList(),
    val defaultSets: Int = 3,
    val defaultReps: Int = 10
)

data class SplitPlan(
    val splitType: SplitType,
    val title: String,
    val daysPerWeek: Int,
    val description: String,
    val recommendedExercises: List<CatalogExercise>
)
