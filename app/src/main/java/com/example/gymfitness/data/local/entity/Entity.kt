package com.example.gymfitness.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: String,
    val age: Int,
    val gender: String,
    val heightCm: Double,
    val weightKg: Double,
    val goal: String,
    val bmr: Float,
    val dailyCaloriesTarget: Float,
    val proteinTarget: Float,
    val carbsTarget: Float,
    val fatTarget: Float,
    val activityLevel: String
)

@Entity(
    tableName = "meals"
)
data class MealEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val calories: Float,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,

    val mealType: String,

    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "workouts")
data class WorkoutEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val notes: String = "",

    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutId")]
)
data class ExerciseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val workoutId: Long,

    val name: String
)


@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class SetEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val exerciseId: Long,

    val reps: Int,

    val weightKg: Float
)

@Entity(tableName = "weight_logs")
data class WeightEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val weightKg: Float,

    val timestampMs: Long = System.currentTimeMillis()
)

data class MacroTotals(
    val protein: Float?,
    val carbs: Float?,
    val fat: Float?
)

data class ExerciseWithSets(

    @Embedded
    val exercise: ExerciseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val sets: List<SetEntity>
)

data class WorkoutWithExercises(

    @Embedded
    val workout: WorkoutEntity,

    @Relation(
        entity = ExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<ExerciseWithSets>
)