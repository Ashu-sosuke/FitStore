package com.example.gymfitness.di

import com.example.gymfitness.data.remote.api.FoodApiService
import com.example.gymfitness.data.remote.api.MealApiService
import com.example.gymfitness.data.remote.api.ProfileApiService
import com.example.gymfitness.data.remote.api.WorkoutApiService
import com.example.gymfitness.data.remote.api.LeaderboardApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.example.gymfitness.BuildConfig
import com.example.gymfitness.utils.TokenManager
import okhttp3.CertificatePinner
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val MAIN_API_URL = "https://pulse-backend-6srs.onrender.com/"
    private const val FOOD_ANALYSER_URL = "http://192.168.29.171:8000/"


    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getToken()
            
            val requestBuilder = originalRequest.newBuilder()
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                // Fallback to API Key for initial requests if needed, or just let it fail
                requestBuilder.addHeader("X-API-KEY", BuildConfig.API_KEY)
            }
            
            chain.proceed(requestBuilder.build())
        }

        val certificatePinner = CertificatePinner.Builder()
            .add("192.168.29.171", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // Replace with actual hash
            .build()


        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .certificatePinner(certificatePinner)
            .build()
    }

    @Provides
    @Singleton
    @javax.inject.Named("MainRetrofit")
    fun provideMainRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MAIN_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @javax.inject.Named("FoodRetrofit")
    fun provideFoodRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FOOD_ANALYSER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodApiService(@javax.inject.Named("FoodRetrofit") retrofit: Retrofit): FoodApiService {
        return retrofit.create(FoodApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApiService(@javax.inject.Named("MainRetrofit") retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkoutApiService(@javax.inject.Named("MainRetrofit") retrofit: Retrofit): WorkoutApiService {
        return retrofit.create(WorkoutApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMealApiService(@javax.inject.Named("MainRetrofit") retrofit: Retrofit): MealApiService {
        return retrofit.create(MealApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLeaderboardApiService(@javax.inject.Named("MainRetrofit") retrofit: Retrofit): LeaderboardApiService {
        return retrofit.create(LeaderboardApiService::class.java)
    }
}