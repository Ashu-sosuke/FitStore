package com.example.gymfitness.data.remote.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream

interface FoodApiService {
    @Multipart
    @POST("scan-food")
    suspend fun scanFood(
        @Part file: MultipartBody.Part,
        @Header("X-User-Id") userId: String = "anonymous"
    ): ScanFoodResponse

    @POST("api/scan/feedback")
    suspend fun sendScanFeedback(
        @Body feedback: ScanFeedbackDto
    ): Map<String, Any>
}

data class ScanFoodResponse(
    val success: Boolean,
    @SerializedName("food_name") val foodName: String,
    val calories: Double,
    val macros: MacrosResponse,
    val confidence: Double,
    @SerializedName("logged_at") val loggedAt: String,
    @SerializedName("is_food") val isFood: Boolean? = true,
    val cuisine: String? = "Indian",
    @SerializedName("estimated_grams") val estimatedGrams: Double? = 100.0,
    val items: List<FoodItemBreakdownDto>? = null,
    @SerializedName("top_alternatives") val topAlternatives: List<String>? = null,
    @SerializedName("scan_id") val scanId: String? = null
)

data class FoodItemBreakdownDto(
    val name: String,
    @SerializedName("matched_name") val matchedName: String,
    val grams: Double,
    val calories: Double,
    @SerializedName("protein_g") val proteinG: Double,
    @SerializedName("carbs_g") val carbsG: Double,
    @SerializedName("fats_g") val fatsG: Double,
    val source: String? = "IFCT_2017"
)

data class ScanFeedbackDto(
    @SerializedName("scan_id") val scanId: String?,
    @SerializedName("predicted_food") val predictedFood: String,
    @SerializedName("corrected_food") val correctedFood: String,
    val rating: Int? = null,
    val comments: String? = null
)

data class MacrosResponse(
    @SerializedName("protein_g") val proteinG: Double,
    @SerializedName("carbs_g") val carbsG: Double,
    @SerializedName("fats_g") val fatsG: Double,
    val calories: Double
)

/**
 * Extension to convert Bitmap to MultipartBody for food analysis.
 */
fun Bitmap.toMultipartBody(): MultipartBody.Part {
    val stream = ByteArrayOutputStream()
    // Compress as JPEG 75% quality
    this.compress(Bitmap.CompressFormat.JPEG, 75, stream)
    val byteArray = stream.toByteArray()
    val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())

    return MultipartBody.Part.createFormData("file", "scan.jpg", requestFile)
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun ImageProxy.toRotatedBitmap(): Bitmap? {
    return try {
        // Use the built-in CameraX ImageProxy.toBitmap() which natively supports YUV_420_888
        val bitmap = this.toBitmap()
        val matrix = Matrix().apply {
            postRotate(this@toRotatedBitmap.imageInfo.rotationDegrees.toFloat())
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (e: Exception) {
        android.util.Log.e("CAMERA_EXT", "Error converting ImageProxy to Bitmap: ${e.message}", e)
        null
    }
}