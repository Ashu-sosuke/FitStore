package com.example.gymfitness.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _startDestination = mutableStateOf<String?>(null)
    val startDestination: State<String?> = _startDestination

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        val userId = tokenManager.getUserId()

        viewModelScope.launch {
            val user = userDao.getUserById(userId)
            if (user != null) {
                _startDestination.value = "home"
            } else {
                _startDestination.value = "onboarding"
            }
        }
    }
}