package com.example.gymfitness.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymfitness.data.local.dao.MealDao
import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.data.local.dao.WeightDao
import com.example.gymfitness.data.local.dao.WorkoutDao
import com.example.gymfitness.data.local.dao.LeaderboardDao
import com.example.gymfitness.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_table ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_table ADD COLUMN highestStreak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_table ADD COLUMN lastLaunchDateMs INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_table ADD COLUMN friendCode TEXT")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `leaderboard_cache` (
                        `userId` TEXT NOT NULL, 
                        `friendCode` TEXT NOT NULL, 
                        `displayName` TEXT NOT NULL, 
                        `avatarInitials` TEXT NOT NULL, 
                        `weeklyPoints` INTEGER NOT NULL, 
                        `workoutsThisWeek` INTEGER NOT NULL, 
                        `currentStreak` INTEGER NOT NULL, 
                        `period` TEXT NOT NULL, 
                        PRIMARY KEY(`userId`)
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE leaderboard_cache ADD COLUMN steps INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_table ADD COLUMN showOnLeaderboards INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_table ADD COLUMN goalPriority TEXT NOT NULL DEFAULT 'balanced'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN trainingHistory TEXT NOT NULL DEFAULT 'just_starting'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN currentRoutine TEXT NOT NULL DEFAULT 'no_routine'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN trainingLocation TEXT NOT NULL DEFAULT 'commercial_gym'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN warmupIncluded INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_table ADD COLUMN sessionDurationMinutes INTEGER NOT NULL DEFAULT 60")
                db.execSQL("ALTER TABLE user_table ADD COLUMN daysPerWeek INTEGER NOT NULL DEFAULT 4")
                db.execSQL("ALTER TABLE user_table ADD COLUMN trainingStyle TEXT NOT NULL DEFAULT 'bodybuilding'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN intensityPreference TEXT NOT NULL DEFAULT 'moderate'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN sleepHours TEXT NOT NULL DEFAULT '7_8h'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN stressLevel TEXT NOT NULL DEFAULT 'moderate'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN dietPreference TEXT NOT NULL DEFAULT 'non_veg'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN mealFrequency INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE user_table ADD COLUMN nutritionPriority TEXT NOT NULL DEFAULT 'high_protein'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN planAdaptability TEXT NOT NULL DEFAULT 'hybrid'")
                db.execSQL("ALTER TABLE user_table ADD COLUMN progressionModel TEXT NOT NULL DEFAULT 'progressive_overload'")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_table ADD COLUMN dailyStepTarget INTEGER NOT NULL DEFAULT 10000")
            }
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gym_fitness_db"
        )
            .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
            .fallbackToDestructiveMigration()
            .build()

    }

    @Provides
    fun provideMealDao(db: AppDatabase): MealDao = db.mealDao()

    @Provides
    fun provideWeightDao(db: AppDatabase): WeightDao = db.weightDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideLeaderboardDao(db: AppDatabase): LeaderboardDao = db.leaderboardDao()
}