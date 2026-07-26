package com.example.gymfitness.presentation.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.gymfitness.domain.models.Meal
import com.example.gymfitness.domain.models.UserProfile
import com.example.gymfitness.domain.models.WeightEntry
import com.example.gymfitness.domain.repository.MealRepository
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.repository.WeightRepository
import com.example.gymfitness.domain.repository.LeaderboardRepository
import com.example.gymfitness.utils.HealthConnectManager
import com.example.gymfitness.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mealRepository: MealRepository
    private lateinit var weightRepository: WeightRepository
    private lateinit var userRepository: UserRepository
    private lateinit var leaderboardRepository: LeaderboardRepository
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var context: Context
    private lateinit var viewModel: HomeViewModel

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
        fatsTarget = 80.0,
        currentStreak = 5,
        highestStreak = 10
    )

    private val testMeals = listOf(
        Meal(id = "1", deviceId = deviceId, type = "Breakfast", foodName = "Oats", calories = 400.0, protein = 15.0, carbs = 60.0, fats = 8.0),
        Meal(id = "2", deviceId = deviceId, type = "Lunch", foodName = "Chicken Rice", calories = 600.0, protein = 40.0, carbs = 70.0, fats = 12.0)
    )

    @Before
    fun setUp() {
        // Mock static Settings.Secure
        mockkStatic(Settings.Secure::class)
        every { Settings.Secure.getString(any(), any()) } returns deviceId

        mealRepository = mockk(relaxed = true)
        weightRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        leaderboardRepository = mockk(relaxed = true)
        healthConnectManager = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Mock repositories and manager returns
        every { userRepository.getProfileFlow(any()) } returns flowOf(testProfile)
        coEvery { userRepository.updateStreak(any()) } returns Unit
        every { mealRepository.getMealsForDay(any(), any()) } returns flowOf(testMeals)
        every { weightRepository.getLatestWeight() } returns flowOf(WeightEntry(weightKg = 80.0f))
        
        every { healthConnectManager.healthConnectSteps } returns MutableStateFlow(5000)
        every { healthConnectManager.sleepDurationMinutes } returns MutableStateFlow(420)

        viewModel = HomeViewModel(
            mealRepository = mealRepository,
            weightRepository = weightRepository,
            userRepository = userRepository,
            leaderboardRepository = leaderboardRepository,
            healthConnectManager = healthConnectManager,
            context = context
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `init loads user profile into state`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Test User", state.userName)
            assertEquals(2500f, state.caloriesTarget)
            assertEquals(5, state.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init loads dashboard data into state`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1000f, state.caloriesEaten)
            assertEquals(55f, state.protein)
            assertEquals(130f, state.carbs)
            assertEquals(20f, state.fat)
            assertEquals(80.0f, state.latestWeight)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init loads health connect data into state`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(5000, state.stepsWalked)
            assertEquals(420, state.sleepMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchHealthConnectSteps triggers updates in health connect manager`() = runTest {
        coEvery { healthConnectManager.fetchDailySteps() } returns Unit
        coEvery { healthConnectManager.fetchDailySleep() } returns Unit

        viewModel.fetchHealthConnectSteps()

        coVerify { healthConnectManager.fetchDailySteps() }
        coVerify { healthConnectManager.fetchDailySleep() }
    }

    @Test
    fun `clearError clears state error message`() = runTest {
        viewModel.clearError()
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(null, state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
