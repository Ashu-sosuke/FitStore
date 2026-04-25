package com.example.gymfitness.data.local.dao

import androidx.room.*
import com.example.gymfitness.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Used by SplashViewModel for a quick check
    @Query("SELECT * FROM user_table WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getUserById(deviceId: String): UserEntity?

    // Used by HomeViewModel to keep the UI synced with targets
    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM user_table")
    suspend fun clearUserData()

    @Transaction
    suspend fun nukeDatabase() {
        clearUserData()
    }
}