package com.example.gymfitness.presentation.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.gymfitness.data.local.database.AppDatabase
import com.example.gymfitness.domain.models.UserProfile
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: UserRepository
    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private lateinit var viewModel: UserViewModel

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

    @Before
    fun setUp() {
        mockkStatic(Settings.Secure::class)
        every { Settings.Secure.getString(any(), any()) } returns deviceId

        repository = mockk(relaxed = true)
        db = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { repository.getProfileFlow(any()) } returns flowOf(testProfile)

        viewModel = UserViewModel(
            repository = repository,
            db = db,
            context = context
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `init determines startDestination is Home when profile exists`() = runTest {
        viewModel.startDestination.test {
            assertEquals(Screen.Home.route, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init determines startDestination is GetStart when profile is null`() = runTest {
        every { repository.getProfileFlow(any()) } returns flowOf(null)
        val emptyVm = UserViewModel(repository, db, context)
        emptyVm.startDestination.test {
            assertEquals(Screen.GetStart.route, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nextStep increments and previousStep decrements currentStep within bounds`() {
        assertEquals(0, viewModel.currentStep)

        viewModel.nextStep()
        assertEquals(1, viewModel.currentStep)

        viewModel.nextStep()
        assertEquals(2, viewModel.currentStep)

        viewModel.nextStep() // bound check (max 2)
        assertEquals(2, viewModel.currentStep)

        viewModel.previousStep()
        assertEquals(1, viewModel.currentStep)

        viewModel.previousStep()
        assertEquals(0, viewModel.currentStep)

        viewModel.previousStep() // bound check (min 0)
        assertEquals(0, viewModel.currentStep)
    }

    @Test
    fun `fetchUserDetail populates compose state fields`() = runTest {
        viewModel.fetchUserDetail()

        // Wait for coroutine to collect flow and update fields
        testScheduler.advanceUntilIdle()

        assertEquals("Test User", viewModel.name)
        assertEquals("25", viewModel.age)
        assertEquals("80.0", viewModel.weight)
        assertEquals("180.0", viewModel.height)
    }

    @Test
    fun `saveUser saves mapped profile data`() = runTest {
        viewModel.name = "Jane"
        viewModel.age = "30"
        viewModel.weight = "60.0"
        viewModel.height = "165.0"
        viewModel.gender = "Female"
        viewModel.goal = "Lose Weight"
        viewModel.activityLevel = "Light"

        var callbackCalled = false
        coEvery { repository.saveProfile(any()) } returns Unit

        viewModel.saveUser {
            callbackCalled = true
        }

        testScheduler.advanceUntilIdle()

        coVerify { 
            repository.saveProfile(match { 
                it.name == "Jane" && 
                it.age == 30 && 
                it.weight == 60.0 && 
                it.height == 165.0 && 
                it.gender == "Female" && 
                it.fitnessGoal == "weight_loss" && 
                it.activityLevel == "lightly_active"
            }) 
        }
        assertEquals(true, callbackCalled)
    }
}
