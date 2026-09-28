package com.example.gymfitness.presentation.viewmodel

import android.content.Context
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
import com.example.gymfitness.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val deviceId: String get() = tokenManager.getUserId()

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
                        experienceLevel = profile.experienceLevel,
                        daysPerWeek = profile.daysPerWeekAvailable,
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
                            daysPerWeek = profile.daysPerWeekAvailable,
                            sessionDurationMinutes = profile.sessionDurationMinutes,
                            experienceLevel = profile.experienceLevel,
                            equipment = listOf("barbell", "dumbbell", "cable", "sled machine", "body weight"),
                            goalPriority = profile.goalPriority,
                            focusMuscles = profile.focusMuscles,
                            avoidMuscles = profile.avoidMuscles,
                            physicalLimitations = profile.physicalLimitations,
                            trainingStyle = profile.trainingStyle,
                            intensityPreference = profile.intensityPreference,
                            sleepHours = profile.sleepHours,
                            stressLevel = profile.stressLevel,
                            trainingLocation = profile.trainingLocation,
                            warmupIncluded = profile.warmupIncluded,
                            progressionModel = profile.progressionModel
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

    fun createCustomWorkout(
        name: String,
        targetArea: String = "Chest & Triceps",
        intensity: String = "Moderate",
        duration: String = "45 mins",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val defaultExercises = when {
                targetArea.contains("Chest", ignoreCase = true) || targetArea.contains("Tricep", ignoreCase = true) -> listOf(
                    Exercise("Flat Barbell Bench Press", 4, 10, 60.0, MuscleGroup.CHEST),
                    Exercise("Incline Dumbbell Press", 3, 12, 22.0, MuscleGroup.CHEST),
                    Exercise("Cable Chest Fly", 3, 15, 15.0, MuscleGroup.CHEST),
                    Exercise("Rope Tricep Pushdown", 4, 12, 20.0, MuscleGroup.TRICEPS),
                    Exercise("Overhead Dumbbell Extension", 3, 12, 18.0, MuscleGroup.TRICEPS)
                )
                targetArea.contains("Back", ignoreCase = true) || targetArea.contains("Bicep", ignoreCase = true) -> listOf(
                    Exercise("Barbell Deadlift", 4, 8, 80.0, MuscleGroup.BACK),
                    Exercise("Lat Pulldown", 4, 10, 50.0, MuscleGroup.BACK),
                    Exercise("Seated Cable Row", 3, 12, 45.0, MuscleGroup.BACK),
                    Exercise("Barbell Bicep Curl", 4, 10, 25.0, MuscleGroup.BICEPS),
                    Exercise("Hammer Curls", 3, 12, 14.0, MuscleGroup.BICEPS)
                )
                targetArea.contains("Leg", ignoreCase = true) || targetArea.contains("Quad", ignoreCase = true) -> listOf(
                    Exercise("Barbell Back Squat", 4, 8, 70.0, MuscleGroup.QUADS),
                    Exercise("Romanian Deadlift", 3, 10, 60.0, MuscleGroup.HAMSTRINGS),
                    Exercise("Leg Press Machine", 3, 12, 120.0, MuscleGroup.QUADS),
                    Exercise("Lying Leg Curl", 3, 12, 35.0, MuscleGroup.HAMSTRINGS),
                    Exercise("Standing Calf Raise", 4, 15, 40.0, MuscleGroup.CALVES)
                )
                targetArea.contains("Shoulder", ignoreCase = true) -> listOf(
                    Exercise("Overhead Barbell Press", 4, 8, 40.0, MuscleGroup.SHOULDERS),
                    Exercise("Dumbbell Lateral Raise", 4, 15, 10.0, MuscleGroup.SHOULDERS),
                    Exercise("Rear Delt Reverse Fly", 3, 15, 8.0, MuscleGroup.SHOULDERS),
                    Exercise("Face Pulls", 3, 15, 20.0, MuscleGroup.SHOULDERS)
                )
                else -> listOf(
                    Exercise("Push-ups", 3, 15, 0.0, MuscleGroup.CHEST),
                    Exercise("Pull-ups / Inverted Row", 3, 10, 0.0, MuscleGroup.BACK),
                    Exercise("Bodyweight Squats", 3, 20, 0.0, MuscleGroup.QUADS),
                    Exercise("Dumbbell Shoulder Press", 3, 12, 12.0, MuscleGroup.SHOULDERS),
                    Exercise("Plank Hold", 3, 60, 0.0, MuscleGroup.ABS_CORE)
                )
            }

            val splitType = when {
                name.contains("Push", ignoreCase = true) -> SplitType.PUSH
                name.contains("Pull", ignoreCase = true) -> SplitType.PULL
                name.contains("Leg", ignoreCase = true) -> SplitType.LEGS
                name.contains("Upper", ignoreCase = true) -> SplitType.UPPER
                name.contains("Lower", ignoreCase = true) -> SplitType.LOWER
                name.contains("Cardio", ignoreCase = true) -> SplitType.CARDIO
                else -> SplitType.FULL_BODY
            }

            val newWorkout = Workout(
                id = null,
                deviceId = deviceId,
                name = name.ifBlank { "Custom Plan - $targetArea" },
                exercises = defaultExercises,
                totalVolume = defaultExercises.sumOf { it.sets * it.reps * it.weight },
                date = System.currentTimeMillis().toString(),
                splitType = splitType
            )

            saveWorkout(newWorkout)
            fetchWorkouts(deviceId)
            onSuccess()
        }
    }

    fun createMultiDaySplitProgram(
        programName: String,
        splitTemplate: String,
        durationWeeks: String,
        intensity: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val baseTitle = programName.ifBlank { "$splitTemplate ($durationWeeks)" }

            val dailyPlans: List<Pair<String, List<Exercise>>> = when {
                splitTemplate.contains("Bro", ignoreCase = true) || splitTemplate.contains("5", ignoreCase = true) -> listOf(
                    "Day 1: Chest Power" to listOf(
                        Exercise("Flat Barbell Bench Press", 4, 10, 60.0, MuscleGroup.CHEST),
                        Exercise("Incline Dumbbell Press", 3, 12, 24.0, MuscleGroup.CHEST),
                        Exercise("Cable Chest Fly", 3, 15, 15.0, MuscleGroup.CHEST),
                        Exercise("Dips (Chest Focus)", 3, 12, 0.0, MuscleGroup.CHEST)
                    ),
                    "Day 2: Back Density & Lats" to listOf(
                        Exercise("Barbell Conventional Deadlift", 4, 8, 80.0, MuscleGroup.BACK),
                        Exercise("Lat Pulldown (Wide Grip)", 4, 10, 55.0, MuscleGroup.BACK),
                        Exercise("Seated Cable Row", 3, 12, 50.0, MuscleGroup.BACK),
                        Exercise("Bent Over Barbell Row", 3, 10, 45.0, MuscleGroup.BACK)
                    ),
                    "Day 3: Shoulders & Delts" to listOf(
                        Exercise("Overhead Barbell Military Press", 4, 8, 40.0, MuscleGroup.SHOULDERS),
                        Exercise("Dumbbell Lateral Raise", 4, 15, 10.0, MuscleGroup.SHOULDERS),
                        Exercise("Rear Delt Reverse Fly", 3, 15, 8.0, MuscleGroup.SHOULDERS),
                        Exercise("Cable Face Pulls", 3, 15, 20.0, MuscleGroup.SHOULDERS)
                    ),
                    "Day 4: Arms (Biceps & Triceps)" to listOf(
                        Exercise("Barbell Bicep Curl", 4, 10, 25.0, MuscleGroup.BICEPS),
                        Exercise("Incline Dumbbell Curl", 3, 12, 12.0, MuscleGroup.BICEPS),
                        Exercise("Rope Tricep Pushdown", 4, 12, 22.0, MuscleGroup.TRICEPS),
                        Exercise("Overhead EZ-Bar Skullcrushers", 3, 12, 20.0, MuscleGroup.TRICEPS)
                    ),
                    "Day 5: Legs & Core" to listOf(
                        Exercise("Barbell Back Squat", 4, 8, 75.0, MuscleGroup.QUADS),
                        Exercise("Romanian Deadlift", 3, 10, 65.0, MuscleGroup.HAMSTRINGS),
                        Exercise("Leg Press Machine", 3, 12, 130.0, MuscleGroup.QUADS),
                        Exercise("Standing Calf Raise", 4, 15, 40.0, MuscleGroup.CALVES),
                        Exercise("Hanging Knee / Leg Raise", 3, 15, 0.0, MuscleGroup.ABS_CORE)
                    )
                )
                splitTemplate.contains("Push", ignoreCase = true) || splitTemplate.contains("PPL", ignoreCase = true) -> listOf(
                    "Day 1: Push (Chest, Delts & Triceps)" to listOf(
                        Exercise("Barbell Bench Press", 4, 8, 65.0, MuscleGroup.CHEST),
                        Exercise("Incline Dumbbell Press", 3, 10, 22.0, MuscleGroup.CHEST),
                        Exercise("Standing Overhead Press", 3, 10, 35.0, MuscleGroup.SHOULDERS),
                        Exercise("Dumbbell Lateral Raise", 4, 15, 10.0, MuscleGroup.SHOULDERS),
                        Exercise("Tricep Rope Pushdown", 3, 12, 20.0, MuscleGroup.TRICEPS)
                    ),
                    "Day 2: Pull (Back, Biceps & Rear Delts)" to listOf(
                        Exercise("Barbell Deadlift", 4, 6, 85.0, MuscleGroup.BACK),
                        Exercise("Lat Pulldown", 4, 10, 50.0, MuscleGroup.BACK),
                        Exercise("Chest-Supported Row", 3, 12, 40.0, MuscleGroup.BACK),
                        Exercise("Cable Face Pulls", 3, 15, 20.0, MuscleGroup.SHOULDERS),
                        Exercise("Barbell Bicep Curl", 4, 10, 25.0, MuscleGroup.BICEPS)
                    ),
                    "Day 3: Legs, Calves & Abs" to listOf(
                        Exercise("Barbell Back Squat", 4, 8, 75.0, MuscleGroup.QUADS),
                        Exercise("Romanian Deadlift", 3, 10, 60.0, MuscleGroup.HAMSTRINGS),
                        Exercise("Leg Extensions", 3, 15, 40.0, MuscleGroup.QUADS),
                        Exercise("Lying Leg Curls", 3, 12, 35.0, MuscleGroup.HAMSTRINGS),
                        Exercise("Standing Calf Raise", 4, 15, 45.0, MuscleGroup.CALVES),
                        Exercise("Cable Woodchopper / Plank", 3, 45, 0.0, MuscleGroup.ABS_CORE)
                    )
                )
                splitTemplate.contains("Upper", ignoreCase = true) -> listOf(
                    "Day 1: Upper Body Power" to listOf(
                        Exercise("Barbell Bench Press", 4, 8, 65.0, MuscleGroup.CHEST),
                        Exercise("Bent Over Barbell Row", 4, 8, 55.0, MuscleGroup.BACK),
                        Exercise("Overhead Dumbbell Press", 3, 10, 18.0, MuscleGroup.SHOULDERS),
                        Exercise("Barbell Bicep Curl", 3, 10, 25.0, MuscleGroup.BICEPS),
                        Exercise("Skullcrushers", 3, 10, 20.0, MuscleGroup.TRICEPS)
                    ),
                    "Day 2: Lower Body Power" to listOf(
                        Exercise("Barbell Back Squat", 4, 8, 80.0, MuscleGroup.QUADS),
                        Exercise("Romanian Deadlift", 4, 8, 70.0, MuscleGroup.HAMSTRINGS),
                        Exercise("Leg Press Machine", 3, 12, 140.0, MuscleGroup.QUADS),
                        Exercise("Standing Calf Raise", 4, 15, 45.0, MuscleGroup.CALVES),
                        Exercise("Hanging Leg Raise", 3, 15, 0.0, MuscleGroup.ABS_CORE)
                    ),
                    "Day 3: Upper Body Hypertrophy" to listOf(
                        Exercise("Incline Dumbbell Press", 4, 12, 22.0, MuscleGroup.CHEST),
                        Exercise("Lat Pulldown (Close Grip)", 4, 12, 50.0, MuscleGroup.BACK),
                        Exercise("Cable Lateral Raise", 4, 15, 8.0, MuscleGroup.SHOULDERS),
                        Exercise("Incline Dumbbell Curl", 3, 12, 12.0, MuscleGroup.BICEPS),
                        Exercise("Tricep Pushdown", 3, 12, 20.0, MuscleGroup.TRICEPS)
                    ),
                    "Day 4: Lower Body Hypertrophy & Abs" to listOf(
                        Exercise("Front Squats / Leg Press", 4, 10, 60.0, MuscleGroup.QUADS),
                        Exercise("Lying Leg Curls", 4, 12, 40.0, MuscleGroup.HAMSTRINGS),
                        Exercise("Walking Dumbbell Lunges", 3, 12, 14.0, MuscleGroup.GLUTES),
                        Exercise("Seated Calf Raise", 4, 15, 35.0, MuscleGroup.CALVES),
                        Exercise("Abdominal Crunches", 3, 20, 0.0, MuscleGroup.ABS_CORE)
                    )
                )
                else -> listOf(
                    "Day 1: Full Body A (Push & Quad Focus)" to listOf(
                        Exercise("Barbell Bench Press", 4, 8, 60.0, MuscleGroup.CHEST),
                        Exercise("Barbell Squat", 4, 8, 70.0, MuscleGroup.QUADS),
                        Exercise("Lat Pulldown", 3, 10, 50.0, MuscleGroup.BACK),
                        Exercise("Dumbbell Shoulder Press", 3, 10, 16.0, MuscleGroup.SHOULDERS),
                        Exercise("Plank Hold", 3, 60, 0.0, MuscleGroup.ABS_CORE)
                    ),
                    "Day 2: Full Body B (Pull & Hamstring Focus)" to listOf(
                        Exercise("Barbell Deadlift", 4, 6, 80.0, MuscleGroup.BACK),
                        Exercise("Incline Dumbbell Press", 3, 10, 20.0, MuscleGroup.CHEST),
                        Exercise("Seated Cable Row", 3, 10, 45.0, MuscleGroup.BACK),
                        Exercise("Lying Leg Curl", 3, 12, 35.0, MuscleGroup.HAMSTRINGS),
                        Exercise("Barbell Bicep Curl", 3, 10, 22.0, MuscleGroup.BICEPS)
                    ),
                    "Day 3: Full Body C (Compound Density)" to listOf(
                        Exercise("Leg Press Machine", 4, 10, 120.0, MuscleGroup.QUADS),
                        Exercise("Dumbbell Flat Press", 3, 10, 22.0, MuscleGroup.CHEST),
                        Exercise("Pull-ups / Chin-ups", 3, 8, 0.0, MuscleGroup.BACK),
                        Exercise("Dumbbell Lateral Raise", 3, 15, 8.0, MuscleGroup.SHOULDERS),
                        Exercise("Hanging Knee Raise", 3, 15, 0.0, MuscleGroup.ABS_CORE)
                    )
                )
            }

            for ((dayName, exercises) in dailyPlans) {
                val totalVol = exercises.sumOf { it.sets * it.reps * it.weight }
                val splitType = when {
                    dayName.contains("Push", ignoreCase = true) -> SplitType.PUSH
                    dayName.contains("Pull", ignoreCase = true) -> SplitType.PULL
                    dayName.contains("Leg", ignoreCase = true) -> SplitType.LEGS
                    dayName.contains("Upper", ignoreCase = true) -> SplitType.UPPER
                    dayName.contains("Lower", ignoreCase = true) -> SplitType.LOWER
                    else -> SplitType.FULL_BODY
                }

                val workout = Workout(
                    id = null,
                    deviceId = deviceId,
                    name = "$baseTitle — $dayName",
                    exercises = exercises,
                    totalVolume = totalVol,
                    date = System.currentTimeMillis().toString(),
                    splitType = splitType
                )
                saveWorkout(workout)
            }

            fetchWorkouts(deviceId)
            onSuccess()
        }
    }

    fun deleteWorkout(workoutId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteWorkoutById(workoutId)
            fetchWorkouts(deviceId)
            onSuccess()
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
        sessionDurationMinutes: Int = 60,
        experienceLevel: String = "beginner",
        equipment: List<String> = listOf("barbell", "dumbbell", "cable", "sled machine", "body weight"),
        goalPriority: String = "balanced",
        focusMuscles: List<String> = emptyList(),
        avoidMuscles: List<String> = emptyList(),
        preferredExercises: List<String> = emptyList(),
        dislikedExercises: List<String> = emptyList(),
        physicalLimitations: List<String> = emptyList(),
        trainingStyle: String = "bodybuilding",
        intensityPreference: String = "moderate",
        sleepHours: String = "7_8h",
        stressLevel: String = "moderate",
        trainingLocation: String = "commercial_gym",
        warmupIncluded: Boolean = true,
        progressionModel: String = "progressive_overload"
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
                goalPriority = goalPriority,
                focusMuscles = focusMuscles,
                avoidMuscles = avoidMuscles,
                preferredExercises = preferredExercises,
                dislikedExercises = dislikedExercises,
                physicalLimitations = physicalLimitations,
                trainingStyle = trainingStyle,
                intensityPreference = intensityPreference,
                sleepHours = sleepHours,
                stressLevel = stressLevel,
                trainingLocation = trainingLocation,
                warmupIncluded = warmupIncluded,
                progressionModel = progressionModel
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
                // Update UserProfile activeSplit in local Room DB & Cloud
                try {
                    val currentProfile = userRepository.getProfile(deviceId)
                    if (currentProfile != null) {
                        val updated = currentProfile.copy(
                            activeSplit = plan.title,
                            daysPerWeekAvailable = plan.daysPerWeek,
                            sessionDurationMinutes = plan.sessionDurationMinutes
                        )
                        userRepository.saveProfile(updated)
                    }
                } catch (_: Exception) {}
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
