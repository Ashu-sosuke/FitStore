package com.example.gymfitness.presentation.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymfitness.data.sync.LeaderboardSyncWorker
import com.example.gymfitness.domain.models.*
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.repository.WorkoutRepository
import com.example.gymfitness.domain.usecase.SplitRecommenderUseCase
import com.example.gymfitness.domain.usecase.workout.GenerateWorkoutPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WeekdayTabItem(
    val dayIndex: Int, // 0 = Mon, ..., 6 = Sun
    val shortName: String, // "Mon", "Tue"...
    val fullName: String, // "Monday", "Tuesday"...
    val dayNumberInMonth: Int,
    val isToday: Boolean
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val splitRecommender: SplitRecommenderUseCase,
    private val generateWorkoutPlanUseCase: GenerateWorkoutPlanUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "default_device"
    }

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _selectedSplit = MutableStateFlow(SplitType.ALL)
    val selectedSplit: StateFlow<SplitType> = _selectedSplit.asStateFlow()

    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroup?>(null)
    val selectedMuscleGroup: StateFlow<MuscleGroup?> = _selectedMuscleGroup.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _currentWorkout = MutableStateFlow<Workout?>(null)
    val currentWorkout: StateFlow<Workout?> = _currentWorkout.asStateFlow()

    private val _recommendedSplit = MutableStateFlow<SplitPlan?>(null)
    val recommendedSplit: StateFlow<SplitPlan?> = _recommendedSplit.asStateFlow()

    // --- AI Workout Plan State ---
    private val _generatedPlan = MutableStateFlow<GeneratedWorkoutPlan?>(null)
    val generatedPlan: StateFlow<GeneratedWorkoutPlan?> = _generatedPlan.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _isAdoptingPlan = MutableStateFlow(false)
    val isAdoptingPlan: StateFlow<Boolean> = _isAdoptingPlan.asStateFlow()

    private val _planError = MutableStateFlow<String?>(null)
    val planError: StateFlow<String?> = _planError.asStateFlow()

    // --- Ascending Weekday Schedule State ---
    private val todayIndex: Int = (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
    private val _selectedWeekday = MutableStateFlow(todayIndex)
    val selectedWeekday: StateFlow<Int> = _selectedWeekday.asStateFlow()

    private val _weekdays = MutableStateFlow<List<WeekdayTabItem>>(emptyList())
    val weekdays: StateFlow<List<WeekdayTabItem>> = _weekdays.asStateFlow()

    init {
        initializeWeekdays()
        fetchWorkouts(deviceId)
        loadUserPlanAndRecommendation()
    }

    private fun initializeWeekdays() {
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val monday = today.minusDays((currentDayOfWeek - 1).toLong())

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val fullNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        val list = (0..6).map { idx ->
            val date = monday.plusDays(idx.toLong())
            WeekdayTabItem(
                dayIndex = idx,
                shortName = dayNames[idx],
                fullName = fullNames[idx],
                dayNumberInMonth = date.dayOfMonth,
                isToday = idx == (currentDayOfWeek - 1)
            )
        }
        _weekdays.value = list
    }

    fun selectWeekday(index: Int) {
        _selectedWeekday.value = index.coerceIn(0, 6)
    }

    private fun loadUserPlanAndRecommendation() {
        viewModelScope.launch {
            userRepository.getProfileFlow(deviceId).collect { profile ->
                if (profile != null) {
                    val userLoggedCount = _workouts.value.size
                    val recommendation = splitRecommender.computeRecommendation(
                        experienceLevel = profile.experienceLevel ?: "BEGINNER",
                        daysPerWeek = profile.daysPerWeekAvailable ?: 5,
                        userLoggedWorkoutCount = userLoggedCount
                    )
                    _recommendedSplit.value = recommendation

                    // Auto-load plan if not yet loaded
                    if (_generatedPlan.value == null) {
                        val isBulking = profile.fitnessGoal.contains("muscle", ignoreCase = true) || profile.fitnessGoal.contains("bulk", ignoreCase = true)
                        val planGoal = if (isBulking) "bulk_up" else if (profile.fitnessGoal.contains("loss", ignoreCase = true)) "cut_down" else profile.fitnessGoal
                        
                        generateAIPlan(
                            weightKg = profile.weight.toFloat(),
                            heightCm = profile.height.toFloat(),
                            age = profile.age,
                            gender = profile.gender,
                            goal = planGoal,
                            daysPerWeek = profile.daysPerWeekAvailable ?: 5,
                            sessionDurationMinutes = 60,
                            experienceLevel = profile.experienceLevel ?: "beginner",
                            equipment = listOf("barbell", "dumbbell", "cable", "sled machine", "body weight")
                        )
                    }
                }
            }
        }
    }

    fun onSplitSelected(splitType: SplitType) {
        _selectedSplit.value = splitType
    }

    fun onMuscleGroupSelected(muscleGroup: MuscleGroup?) {
        _selectedMuscleGroup.value = muscleGroup
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class)
    val filteredWorkouts: StateFlow<List<Workout>> = combine(
        _workouts,
        _selectedSplit,
        _selectedMuscleGroup,
        _searchQuery.debounce(250L)
    ) { allWorkouts, split, muscle, query ->
        allWorkouts.filter { workout ->
            val matchesSplit = (split == SplitType.ALL || workout.splitType == split || workout.name.contains(split.displayName, ignoreCase = true))
            val matchesMuscle = (muscle == null || muscle == MuscleGroup.FULL_BODY || workout.exercises.any { it.primaryMuscle == muscle })
            val matchesQuery = query.isBlank() || workout.name.contains(query, ignoreCase = true) || workout.exercises.any { it.name.contains(query, ignoreCase = true) }
            matchesSplit && matchesMuscle && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun fetchWorkoutDetails(deviceIdParam: String = deviceId, workoutId: Long) {
        viewModelScope.launch {
            repository.getWorkout(deviceIdParam, workoutId).collect {
                _currentWorkout.value = it
            }
        }
    }

    fun fetchWorkouts(deviceIdParam: String = deviceId) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getWorkouts(deviceIdParam).collect { list ->
                _workouts.value = list
                _isLoading.value = false
            }
        }
    }

    fun saveWorkout(workout: Workout) {
        viewModelScope.launch {
            val totalVol = workout.exercises.sumOf { ex -> ex.sets * ex.reps * ex.weight }
            val workoutToSave = workout.copy(totalVolume = if (totalVol > 0) totalVol else workout.totalVolume)
            repository.saveWorkout(workoutToSave)
            triggerLeaderboardSync(100)
        }
    }

    private fun triggerLeaderboardSync(points: Int) {
        val data = Data.Builder().putInt("POINTS", points).build()
        val request = OneTimeWorkRequestBuilder<LeaderboardSyncWorker>()
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun syncWithBackend(deviceIdParam: String = deviceId) {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.syncWorkouts(deviceIdParam)
            _isSyncing.value = false
        }
    }

    fun addExerciseToWorkout(workoutId: Long, name: String) {
        viewModelScope.launch {
            repository.addExercise(workoutId, name)
        }
    }

    fun logSet(exerciseId: Long, reps: Int, weight: Float) {
        viewModelScope.launch {
            repository.addSet(exerciseId, reps, weight)
        }
    }

    // --- AI Plan Generator Functions ---
    fun generateAIPlan(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        gender: String,
        goal: String,
        daysPerWeek: Int,
        sessionDurationMinutes: Int,
        experienceLevel: String,
        equipment: List<String>,
        focusMuscles: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            _planError.value = null
            
            val prefs = PlanGenerationPreferences(
                deviceId = deviceId,
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                gender = gender,
                fitnessGoal = goal,
                daysPerWeek = daysPerWeek,
                sessionDurationMinutes = sessionDurationMinutes,
                experienceLevel = experienceLevel,
                availableEquipment = equipment,
                focusMuscles = focusMuscles
            )

            val result = generateWorkoutPlanUseCase(prefs)
            result.onSuccess { plan ->
                _generatedPlan.value = plan
                _isGeneratingPlan.value = false
            }.onFailure { error ->
                _planError.value = error.localizedMessage ?: "Failed to generate workout plan"
                _isGeneratingPlan.value = false
            }
        }
    }

    fun adoptGeneratedPlan(onSuccess: () -> Unit) {
        val plan = _generatedPlan.value ?: return
        viewModelScope.launch {
            _isAdoptingPlan.value = true
            val result = repository.adoptPlan(deviceId, plan)
            _isAdoptingPlan.value = false
            if (result.isSuccess) {
                fetchWorkouts(deviceId)
                onSuccess()
            } else {
                _planError.value = "Failed to adopt plan: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun clearGeneratedPlan() {
        _generatedPlan.value = null
        _planError.value = null
    }
}
