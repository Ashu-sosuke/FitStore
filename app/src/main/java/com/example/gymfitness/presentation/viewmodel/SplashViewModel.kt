package com.example.gymfitness.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.State // Added this import
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymfitness.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Added explicit type for the State object
    private val _startDestination = mutableStateOf<String?>(null)
    val startDestination: State<String?> = _startDestination

    init {
        checkUserStatus()
    }

    @SuppressLint("HardwareIds") // Required for accessing ANDROID_ID
    private fun checkUserStatus() {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        viewModelScope.launch {
            // Ensure this method exists in your UserDao
            val user = userDao.getUserById(deviceId)
            if (user != null) {
                _startDestination.value = "home"
            } else {
                _startDestination.value = "onboarding"
            }
        }
    }
}