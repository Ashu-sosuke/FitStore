package com.example.gymfitness.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.data.local.entity.WorkoutEntity
import java.io.File
import java.io.FileWriter

object ExportUtils {
    fun exportToCSV(
        context: Context,
        workouts: List<WorkoutEntity>,
        meals: List<MealEntity>
    ) {
        val fileName = "FitStore_Export_${System.currentTimeMillis()}.csv"
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, fileName)

        try {
            val writer = FileWriter(file)
            writer.append("Type,Name,Calories,Protein(g),Carbs(g),Fat(g),Date\\n")

            // Write Meals
            meals.forEach { meal ->
                writer.append("Meal,${meal.name},${meal.calories},${meal.proteinG},${meal.carbsG},${meal.fatG},${meal.timestampMs}\\n")
            }

            // Write Workouts (simplified for export)
            workouts.forEach { workout ->
                writer.append("Workout,${workout.name},,,,${workout.createdAtMs}\\n")
            }

            writer.flush()
            writer.close()

            // Share the file
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export FitStore Data"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
