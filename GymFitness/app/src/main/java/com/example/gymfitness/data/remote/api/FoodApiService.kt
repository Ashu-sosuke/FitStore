package com.example.gymfitness.data.remote.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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
}

data class ScanFoodResponse(
    val success: Boolean,
    @SerializedName("food_name") val foodName: String,
    val calories: Double,
    val macros: MacrosResponse,
    val confidence: Double,
    @SerializedName("logged_at") val loggedAt: String
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
    // Reduced quality to 50% to shrink file size for faster uploads
    this.compress(Bitmap.CompressFormat.JPEG, 50, stream)
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