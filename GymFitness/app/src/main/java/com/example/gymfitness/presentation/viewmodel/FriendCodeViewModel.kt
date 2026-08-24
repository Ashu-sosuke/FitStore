package com.example.gymfitness.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.domain.models.FriendCodeUiState
import com.example.gymfitness.domain.models.FriendEntry
import com.example.gymfitness.ui.theme.SunsetOrange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.gymfitness.domain.repository.LeaderboardRepository
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.domain.models.LeaderboardPeriod
import com.example.gymfitness.utils.TokenManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendCodeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    val deviceId: String get() = tokenManager.getUserId()

    private val _uiState = MutableStateFlow<FriendCodeUiState>(FriendCodeUiState.Loading)
    val uiState: StateFlow<FriendCodeUiState> = _uiState.asStateFlow()

    private val _addCodeInput = MutableStateFlow("")
    val addCodeInput: StateFlow<String> = _addCodeInput.asStateFlow()

    private val _addResult = MutableStateFlow<AddFriendResult>(AddFriendResult.Idle)
    val addResult: StateFlow<AddFriendResult> = _addResult.asStateFlow()

    sealed class AddFriendResult {
        object Idle    : AddFriendResult()
        object Loading : AddFriendResult()
        data class Success(val friend: FriendEntry) : AddFriendResult()
        data class Error(val message: String)       : AddFriendResult()
    }

    fun loadData(currentUserId: String = deviceId) {
        viewModelScope.launch {
            // We combine user profile to get own code, and leaderboard to get friends list
            combine(
                userRepository.getProfileFlow(deviceId),
                leaderboardRepository.observeLeaderboard(LeaderboardPeriod.WEEKLY)
            ) { profile, leaderboardEntries ->
                val myCode = profile?.friendCode ?: ""
                val friends = leaderboardEntries
                    .filter { !it.isCurrentUser }
                    .map { entry ->
                        FriendEntry(
                            userId = entry.userId,
                            displayName = entry.displayName,
                            avatarInitials = entry.avatarInitials,
                            avatarColor = entry.avatarColor,
                            currentStreak = entry.currentStreak,
                            isOnline = true, // We don't have online status in DB, just mock it
                            code = ""
                        )
                    }
                FriendCodeUiState.Success(myCode, friends)
            }
            .catch { _uiState.value = FriendCodeUiState.Error(it.message ?: "Error") }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onCodeInputChange(value: String) {
        _addCodeInput.value = value.uppercase().take(6)
    }

    fun addFriendByCode(code: String, currentUserId: String) {
        viewModelScope.launch {
            _addResult.value = AddFriendResult.Loading
            if (code.length != 6) {
                _addResult.value = AddFriendResult.Error("Code must be 6 characters")
                return@launch
            }
            
            val state = _uiState.value as? FriendCodeUiState.Success ?: return@launch
            
            if (code == state.currentUserCode) {
                _addResult.value = AddFriendResult.Error("That's your own code!")
                return@launch
            }
            if (state.friends.any { it.code == code }) {
                _addResult.value = AddFriendResult.Error("Already in your squad")
                return@launch
            }
            
            val result = leaderboardRepository.addFriend(code)
            result.onSuccess {
                // Success, the flow will automatically emit new friends list
                // We just need to fake the "newFriend" for the UI animation or let it rely on the updated list
                // We don't have the friend details immediately, but the Flow will update.
                // For AddResult.Success we can pass a dummy FriendEntry since we don't have the real one
                _addResult.value = AddFriendResult.Success(FriendEntry("", "", "", SunsetOrange, 0, true, code))
                _addCodeInput.value = ""
            }.onFailure { e ->
                _addResult.value = AddFriendResult.Error(e.message ?: "Failed to add friend")
            }
        }
    }
}
