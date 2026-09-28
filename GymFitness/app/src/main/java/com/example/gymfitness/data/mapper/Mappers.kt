package com.example.gymfitness.data.mapper

import com.example.gymfitness.data.local.entity.UserEntity
import com.example.gymfitness.data.remote.dto.ProfileCreateDto
import com.example.gymfitness.data.remote.dto.ProfileDto
import com.example.gymfitness.domain.models.UserProfile

fun UserEntity.toDomain(): UserProfile {
    return UserProfile(
        deviceId = deviceId,
        name = name,
        age = age,
        gender = gender,
        height = heightCm,
        weight = weightKg,
        fitnessGoal = goal,
        activityLevel = activityLevel,
        dailyCalorieTarget = dailyCalorieTarget.toDouble(),
        proteinTarget = proteinTarget.toDouble(),
        carbsTarget = carbsTarget.toDouble(),
        fatsTarget = fatsTarget.toDouble(),
        currentStreak = currentStreak,
        highestStreak = highestStreak,
        friendCode = friendCode,
        showOnLeaderboards = showOnLeaderboards,
        dailyStepTarget = dailyStepTarget,
        goalPriority = goalPriority,
        trainingHistory = trainingHistory,
        currentRoutine = currentRoutine,
        trainingLocation = trainingLocation,
        warmupIncluded = warmupIncluded,
        sessionDurationMinutes = sessionDurationMinutes,
        daysPerWeekAvailable = daysPerWeek,
        trainingStyle = trainingStyle,
        intensityPreference = intensityPreference,
        sleepHours = sleepHours,
        stressLevel = stressLevel,
        dietPreference = dietPreference,
        mealFrequency = mealFrequency,
        nutritionPriority = nutritionPriority,
        planAdaptability = planAdaptability,
        progressionModel = progressionModel
    )
}

fun UserProfile.toEntity(): UserEntity {
    return UserEntity(
        deviceId = deviceId,
        name = name,
        age = age,
        gender = gender,
        heightCm = height,
        weightKg = weight,
        goal = fitnessGoal,
        activityLevel = activityLevel,
        dailyCalorieTarget = dailyCalorieTarget.toFloat(),
        proteinTarget = proteinTarget.toFloat(),
        carbsTarget = carbsTarget.toFloat(),
        fatsTarget = fatsTarget.toFloat(),
        bmr = 0f,
        currentStreak = currentStreak,
        highestStreak = highestStreak,
        friendCode = friendCode,
        showOnLeaderboards = showOnLeaderboards,
        dailyStepTarget = dailyStepTarget,
        goalPriority = goalPriority,
        trainingHistory = trainingHistory,
        currentRoutine = currentRoutine,
        trainingLocation = trainingLocation,
        warmupIncluded = warmupIncluded,
        sessionDurationMinutes = sessionDurationMinutes,
        daysPerWeek = daysPerWeekAvailable,
        trainingStyle = trainingStyle,
        intensityPreference = intensityPreference,
        sleepHours = sleepHours,
        stressLevel = stressLevel,
        dietPreference = dietPreference,
        mealFrequency = mealFrequency,
        nutritionPriority = nutritionPriority,
        planAdaptability = planAdaptability,
        progressionModel = progressionModel
    )
}

fun ProfileDto.toDomain(): UserProfile {
    return UserProfile(
        deviceId = deviceId,
        name = name,
        age = age,
        gender = gender,
        height = height,
        weight = weight,
        fitnessGoal = fitnessGoal,
        activityLevel = activityLevel,
        dailyCalorieTarget = dailyCalorieTarget,
        proteinTarget = proteinTarget ?: 0.0,
        carbsTarget = carbsTarget ?: 0.0,
        fatsTarget = fatsTarget ?: 0.0,
        currentStreak = 0,
        highestStreak = 0,
        friendCode = friendCode,
        showOnLeaderboards = showOnLeaderboards,
        dailyStepTarget = dailyStepTarget ?: 10000,
        goalPriority = goalPriority ?: "balanced",
        trainingHistory = trainingHistory ?: "just_starting",
        currentRoutine = currentRoutine ?: "no_routine",
        trainingLocation = trainingLocation ?: "commercial_gym",
        warmupIncluded = warmupIncluded ?: true,
        sessionDurationMinutes = sessionDurationMinutes ?: 60,
        daysPerWeekAvailable = daysPerWeek ?: 4,
        focusMuscles = focusMuscles ?: emptyList(),
        avoidMuscles = avoidMuscles ?: emptyList(),
        preferredExercises = preferredExercises ?: emptyList(),
        dislikedExercises = dislikedExercises ?: emptyList(),
        physicalLimitations = physicalLimitations ?: emptyList(),
        trainingStyle = trainingStyle ?: "bodybuilding",
        intensityPreference = intensityPreference ?: "moderate",
        sleepHours = sleepHours ?: "7_8h",
        stressLevel = stressLevel ?: "moderate",
        dietPreference = dietPreference ?: "non_veg",
        mealFrequency = mealFrequency ?: 3,
        nutritionPriority = nutritionPriority ?: "high_protein",
        planAdaptability = planAdaptability ?: "hybrid",
        progressionModel = progressionModel ?: "progressive_overload"
    )
}

fun UserProfile.toDto(): ProfileCreateDto {
    return ProfileCreateDto(
        deviceId = deviceId,
        name = name,
        age = age,
        gender = gender,
        height = height,
        weight = weight,
        fitnessGoal = fitnessGoal,
        activityLevel = activityLevel,
        dailyCalorieTarget = dailyCalorieTarget,
        proteinTarget = proteinTarget,
        carbsTarget = carbsTarget,
        fatsTarget = fatsTarget,
        friendCode = friendCode,
        showOnLeaderboards = showOnLeaderboards,
        dailyStepTarget = dailyStepTarget,
        goalPriority = goalPriority,
        trainingHistory = trainingHistory,
        currentRoutine = currentRoutine,
        trainingLocation = trainingLocation,
        warmupIncluded = warmupIncluded,
        sessionDurationMinutes = sessionDurationMinutes,
        daysPerWeek = daysPerWeekAvailable,
        focusMuscles = focusMuscles,
        avoidMuscles = avoidMuscles,
        preferredExercises = preferredExercises,
        dislikedExercises = dislikedExercises,
        physicalLimitations = physicalLimitations,
        trainingStyle = trainingStyle,
        intensityPreference = intensityPreference,
        sleepHours = sleepHours,
        stressLevel = stressLevel,
        dietPreference = dietPreference,
        mealFrequency = mealFrequency,
        nutritionPriority = nutritionPriority,
        planAdaptability = planAdaptability,
        progressionModel = progressionModel
    )
}


// Meal Mappers
fun com.example.gymfitness.data.local.entity.MealEntity.toDomain(deviceId: String): com.example.gymfitness.domain.models.Meal {
    return com.example.gymfitness.domain.models.Meal(
        id = id.toString(),
        deviceId = deviceId,
        type = mealType,
        foodName = name,
        calories = calories.toDouble(),
        protein = proteinG.toDouble(),
        carbs = carbsG.toDouble(),
        fats = fatG.toDouble()
    )
}

fun com.example.gymfitness.data.remote.dto.MealDto.toDomain(): com.example.gymfitness.domain.models.Meal {
    return com.example.gymfitness.domain.models.Meal(
        id = id,
        deviceId = deviceId,
        type = mealType,
        foodName = foodName,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats
    )
}

fun com.example.gymfitness.domain.models.Meal.toEntity(): com.example.gymfitness.data.local.entity.MealEntity {
    return com.example.gymfitness.data.local.entity.MealEntity(
        name = foodName,
        calories = calories.toFloat(),
        proteinG = protein.toFloat(),
        carbsG = carbs.toFloat(),
        fatG = fats.toFloat(),
        mealType = type
    )
}

fun com.example.gymfitness.domain.models.Meal.toDto(): com.example.gymfitness.data.remote.dto.MealCreateDto {
    return com.example.gymfitness.data.remote.dto.MealCreateDto(
        deviceId = deviceId,
        mealType = type,
        foodName = foodName,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fats = fats
    )
}

// Workout Mappers
fun com.example.gymfitness.data.local.entity.WorkoutWithExercises.toDomain(deviceId: String): com.example.gymfitness.domain.models.Workout {
    return com.example.gymfitness.domain.models.Workout(
        id = workout.id.toString(),
        deviceId = deviceId,
        name = workout.name,
        date = workout.createdAtMs.toString(), 
        totalVolume = 0.0, 
        exercises = exercises.map { exerciseWithSets ->
            com.example.gymfitness.domain.models.Exercise(
                name = exerciseWithSets.exercise.name,
                sets = exerciseWithSets.sets.size,
                reps = exerciseWithSets.sets.firstOrNull()?.reps ?: 0,
                weight = exerciseWithSets.sets.firstOrNull()?.weightKg?.toDouble() ?: 0.0
            )
        }
    )
}

fun com.example.gymfitness.data.remote.dto.WorkoutDto.toDomain(): com.example.gymfitness.domain.models.Workout {
    return com.example.gymfitness.domain.models.Workout(
        id = id,
        deviceId = deviceId,
        name = workoutName,
        date = date,
        totalVolume = totalVolume,
        exercises = exercises.map { 
            com.example.gymfitness.domain.models.Exercise(
                name = it.exerciseName,
                sets = it.sets,
                reps = it.reps,
                weight = it.weight
            )
        }
    )
}

fun com.example.gymfitness.domain.models.Workout.toDto(): com.example.gymfitness.data.remote.dto.WorkoutCreateDto {
    return com.example.gymfitness.data.remote.dto.WorkoutCreateDto(
        deviceId = deviceId,
        workoutName = name,
        date = date,
        exercises = exercises.map { 
            com.example.gymfitness.data.remote.dto.ExerciseDto(
                exerciseName = it.name,
                sets = it.sets,
                reps = it.reps,
                weight = it.weight
            )
        }
    )
}

// Weight Mappers
fun com.example.gymfitness.data.local.entity.WeightEntity.toDomain(): com.example.gymfitness.domain.models.WeightEntry {
    return com.example.gymfitness.domain.models.WeightEntry(
        id = id,
        weightKg = weightKg,
        timestampMs = timestampMs
    )
}

fun com.example.gymfitness.domain.models.WeightEntry.toEntity(): com.example.gymfitness.data.local.entity.WeightEntity {
    return com.example.gymfitness.data.local.entity.WeightEntity(
        weightKg = weightKg,
        timestampMs = timestampMs
    )
}
