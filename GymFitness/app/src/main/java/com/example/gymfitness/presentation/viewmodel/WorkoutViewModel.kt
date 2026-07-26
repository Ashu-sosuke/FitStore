package com.example.gymfitness.presentation.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gymfitness.data.sync.LeaderboardSyncWorker
import com.example.gymfitness.domain.models.MuscleGroup
import com.example.gymfitness.domain.models.SplitPlan
import com.example.gymfitness.domain.models.SplitType
import com.example.gymfitness.domain.models.Workout
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.repository.WorkoutRepository
import com.example.gymfitness.domain.usecase.SplitRecommenderUseCase
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
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val splitRecommender: SplitRecommenderUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val deviceId: String by lazy {
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

    init {
        fetchWorkouts(deviceId)
        loadRecommendation()
    }

    private fun loadRecommendation() {
        viewModelScope.launch {
            userRepository.getProfileFlow(deviceId).collect { profile ->
                val userLoggedCount = _workouts.value.size
                val recommendation = splitRecommender.computeRecommendation(
                    experienceLevel = profile?.experienceLevel ?: "BEGINNER",
                    daysPerWeek = profile?.daysPerWeekAvailable ?: 4,
                    userLoggedWorkoutCount = userLoggedCount
                )
                _recommendedSplit.value = recommendation
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
}
