package com.example.gymfitness.data.remote.api

import com.example.gymfitness.data.remote.dto.DailySummaryDto
import com.example.gymfitness.data.remote.dto.MealCreateDto
import com.example.gymfitness.data.remote.dto.MealDto
import com.example.gymfitness.data.remote.dto.NutrientDto
import retrofit2.http.*

interface MealApiService {
    @POST("api/meals/")
    suspend fun addMeal(@Body meal: MealCreateDto): MealDto

    @GET("api/meals/{deviceId}")
    suspend fun listMeals(
        @Path("deviceId") deviceId: String,
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): List<MealDto>

    @GET("api/meals/summary/{deviceId}")
    suspend fun getDailySummary(@Path("deviceId") deviceId: String): DailySummaryDto

    @GET("api/meals/search-food")
    suspend fun searchFood(@Query("query") query: String): List<NutrientDto>

    @POST("api/meals/add-food")
    suspend fun addCustomFood(@Body food: NutrientDto): NutrientDto
}
