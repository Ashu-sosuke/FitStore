package com.example.gymfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymfitness.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntity)

    @Query("SELECT * FROM weight_logs ORDER BY timestampMs DESC")
    fun getWeights(): Flow<List<WeightEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY timestampMs DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightEntity?>
}