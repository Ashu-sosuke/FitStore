package com.example.gymfitness.data.repository

import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.data.local.entity.UserEntity
import com.example.gymfitness.data.remote.api.ProfileApiService
import com.example.gymfitness.data.remote.dto.ProfileDto
import com.example.gymfitness.data.sync.SyncManager
import com.example.gymfitness.domain.models.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var userDao: UserDao
    private lateinit var profileApi: ProfileApiService
    private lateinit var syncManager: SyncManager
    private lateinit var repository: UserRepositoryImpl

    private val deviceId = "test_device_id"
    
    private val testProfile = UserProfile(
        deviceId = deviceId,
        name = "Test User",
        age = 25,
        gender = "male",
        height = 180.0,
        weight = 80.0,
        fitnessGoal = "muscle_gain",
        activityLevel = "active",
        dailyCalorieTarget = 2500.0,
        proteinTarget = 150.0,
        carbsTarget = 250.0,
        fatsTarget = 80.0
    )

    private val testEntity = UserEntity(
        deviceId = deviceId,
        name = "Test User",
        age = 25,
        gender = "male",
        heightCm = 180.0,
        weightKg = 80.0,
        goal = "muscle_gain",
        activityLevel = "active",
        dailyCalorieTarget = 2500f,
        proteinTarget = 150f,
        carbsTarget = 250f,
        fatsTarget = 80f,
        bmr = 0f,
        currentStreak = 0,
        highestStreak = 0,
        isSynced = true
    )

    private val testDto = ProfileDto(
        id = "remote_id",
        deviceId = deviceId,
        name = "Test User Remote",
        age = 25,
        gender = "male",
        height = 180.0,
        weight = 80.0,
        fitnessGoal = "muscle_gain",
        activityLevel = "active",
        dailyCalorieTarget = 2500.0,
        proteinTarget = 150.0,
        carbsTarget = 250.0,
        fatsTarget = 80.0,
        activeSplit = null
    )

    @Before
    fun setUp() {
        userDao = mockk(relaxed = true)
        profileApi = mockk(relaxed = true)
        syncManager = mockk(relaxed = true)

        repository = UserRepositoryImpl(
            userDao = userDao,
            profileApi = profileApi,
            syncManager = syncManager
        )
    }

    @Test
    fun `saveProfile saves locally and triggers sync`() = runTest {
        repository.saveProfile(testProfile)

        coVerify { userDao.insertUser(any()) }
        coVerify { syncManager.scheduleSync() }
    }

    @Test
    fun `getProfile returns mapped profile from local DB`() = runTest {
        coEvery { userDao.getUserById(deviceId) } returns testEntity

        val profile = repository.getProfile(deviceId)

        assertEquals("Test User", profile?.name)
        assertEquals(25, profile?.age)
        assertEquals(2500.0, profile?.dailyCalorieTarget)
    }

    @Test
    fun `syncProfile fetches remote updates local and returns success`() = runTest {
        coEvery { profileApi.getProfile(deviceId) } returns testDto

        val result = repository.syncProfile(deviceId)

        assertTrue(result.isSuccess)
        assertEquals("Test User Remote", result.getOrNull()?.name)
        coVerify { userDao.insertUser(any()) }
    }

    @Test
    fun `syncProfile returns failure on network error`() = runTest {
        coEvery { profileApi.getProfile(deviceId) } throws Exception("Network Error")

        val result = repository.syncProfile(deviceId)

        assertTrue(result.isFailure)
        assertEquals("Network Error", result.exceptionOrNull()?.message)
    }
}
