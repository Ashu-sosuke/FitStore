package com.example.gymfitness.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.domain.models.LeaderboardEntry
import com.example.gymfitness.domain.models.LeaderboardPeriod
import com.example.gymfitness.domain.models.LeaderboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.gymfitness.domain.repository.LeaderboardRepository
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.utils.TokenManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    val deviceId: String get() = tokenManager.getUserId()

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(LeaderboardPeriod.WEEKLY)
    val selectedPeriod: StateFlow<LeaderboardPeriod> = _selectedPeriod.asStateFlow()

    // Temporary method to generate friendCode if it's missing (same logic will be shared)
    private fun friendCodeFromUserId(userId: String): String {
        if (userId.length < 6) return userId + "123"
        return userId.substring(0, 6).uppercase()
    }

    val friendCode: StateFlow<String> = userRepository.getProfileFlow(deviceId)
        .map { it?.friendCode ?: friendCodeFromUserId(it?.deviceId ?: deviceId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "------")

    private var observeJob: kotlinx.coroutines.Job? = null

    init { 
        observeLeaderboard()
        refreshLeaderboard()
    }

    private fun observeLeaderboard() {
        viewModelScope.launch {
            _selectedPeriod.collect { period ->
                observeJob?.cancel()
                observeJob = viewModelScope.launch {
                    combine(
                        leaderboardRepository.observeLeaderboard(period),
                        userRepository.getProfileFlow(deviceId)
                    ) { entries, profile ->
                        if (entries.isNotEmpty()) {
                            entries
                        } else {
                            val fallbackUser = LeaderboardEntry(
                                userId = profile?.deviceId ?: "",
                                displayName = profile?.name ?: "You",
                                avatarInitials = (profile?.name ?: "Y").take(1).uppercase(),
                                avatarColor = Color(0xFFD0FD3E),
                                weeklyPoints = 0,
                                workoutsThisWeek = 0,
                                currentStreak = profile?.currentStreak ?: 0,
                                isCurrentUser = true,
                                steps = 0
                            )
                            listOf(fallbackUser)
                        }
                    }.catch { e ->
                        _uiState.value = LeaderboardUiState.Error(e.message ?: "Unknown error")
                    }.collect { finalEntries ->
                        _uiState.value = LeaderboardUiState.Success(
                            entries = finalEntries,
                            period = period,
                            currentUserEntry = finalEntries.find { it.isCurrentUser } ?: finalEntries.first()
                        )
                    }
                }
            }
        }
    }

    fun fetchLeaderboard(period: LeaderboardPeriod) {
        _selectedPeriod.value = period
        refreshLeaderboard()
    }

    private fun refreshLeaderboard() {
        viewModelScope.launch {
            leaderboardRepository.refreshLeaderboard(_selectedPeriod.value)
        }
    }
}
