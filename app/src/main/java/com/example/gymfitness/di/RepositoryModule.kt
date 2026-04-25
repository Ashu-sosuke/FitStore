package com.example.gymfitness.di

import com.example.gymfitness.data.repository.MealRepositoryImpl // Make sure this path is correct
import com.example.gymfitness.data.repository.UserRepositoryImpl
import com.example.gymfitness.data.repository.WeightRepositoryImpl
import com.example.gymfitness.domain.repository.MealRepository
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.repository.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindMealRepository(
        mealRepositoryImpl: MealRepositoryImpl
    ): MealRepository

    @Binds
    @Singleton
    abstract fun bindWeightRepository(
        weightRepositoryImpl: WeightRepositoryImpl
    ): WeightRepository
}