package com.example.gymfitness.di

import com.example.gymfitness.data.remote.api.FoodApiService
import com.example.gymfitness.data.remote.api.MealApiService
import com.example.gymfitness.data.remote.api.ProfileApiService
import com.example.gymfitness.data.remote.api.WorkoutApiService
import com.example.gymfitness.data.remote.api.LeaderboardApiService
import com.example.gymfitness.data.remote.api.AuthApiService
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
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val MAIN_API_URL = "https://pulse-backend-6srs.onrender.com/"
    private const val FOOD_ANALYSER_URL = "https://pulse-backend-6srs.onrender.com/"


    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
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

        // Certificate pinning: only pin for production domain with a real hash.
        // Render.com uses dynamic TLS certificates, so pinning is not practical here.
        // For a fixed-infrastructure domain, uncomment and configure:
        // val certificatePinner = CertificatePinner.Builder()
        //     .add("your-production-domain.com", "sha256/YOUR_REAL_CERT_HASH=")
        //     .build()

        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
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

    @Provides
    @Singleton
    fun provideAuthApiService(@javax.inject.Named("MainRetrofit") retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}