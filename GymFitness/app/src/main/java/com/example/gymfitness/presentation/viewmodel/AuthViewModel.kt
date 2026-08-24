package com.example.gymfitness.presentation.viewmodel

import android.util.Log
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.gymfitness.data.remote.api.LeaderboardApiService
import com.example.gymfitness.data.remote.api.UserProfileRegistration
import com.example.gymfitness.domain.repository.UserRepository
import com.example.gymfitness.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Success(
        val userId: String,
        val friendCode: String,
        val isNewUser: Boolean,
        val displayName: String = "",
        val email: String = ""
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val leaderboardApi: LeaderboardApiService,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Full Google Sign-In flow:
     * 1. Credential Manager shows the Google account picker (bottom sheet / popup)
     * 2. User selects their Google account
     * 3. We receive the Google ID Token
     * 4. Firebase Auth verifies the token and signs the user in
     * 5. We persist the Firebase UID as the active user ID
     * 6. We check if they already have a profile in local Room DB OR remote backend
     * 7. Route to Home (existing user) or Onboarding (new user)
     */
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                // Step 1: Create Credential Manager and show Google account picker
                val credentialManager = CredentialManager.create(activityContext)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("500152435261-3jh26rjta849q5l9jr6fkmg8l3kbhoal.apps.googleusercontent.com")
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // This triggers the Google account selection popup/bottom sheet
                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext,
                )

                // Step 2: Extract Google credentials
                val credential = result.credential
                val googleIdTokenCredential = if (credential is CustomCredential && 
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    GoogleIdTokenCredential.createFrom(credential.data)
                } else if (credential is GoogleIdTokenCredential) {
                    credential
                } else {
                    _uiState.value = AuthUiState.Error("Invalid credential type received: ${credential.type}")
                    return@launch
                }

                val idToken = googleIdTokenCredential.idToken
                val displayName = googleIdTokenCredential.displayName ?: ""
                val email = googleIdTokenCredential.id  // Google email

                // Step 3: Authenticate with Firebase using the Google ID token
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    _uiState.value = AuthUiState.Error("Firebase authentication failed.")
                    return@launch
                }

                // Step 4: Use Firebase UID as the unique user identifier (per-account, not per-device)
                val userId = firebaseUser.uid
                tokenManager.saveUserId(userId)

                Log.d("AUTH", "Firebase Auth successful: uid=$userId, email=${firebaseUser.email}")

                // Step 5: Check if this user already has a profile
                // First check local Room DB
                var existingProfile = userRepository.getProfile(userId)

                // If not in local DB, try fetching from remote backend
                if (existingProfile == null) {
                    try {
                        val syncResult = userRepository.syncProfile(userId)
                        existingProfile = syncResult.getOrNull()
                    } catch (e: Exception) {
                        Log.w("AUTH", "Remote profile fetch failed: ${e.localizedMessage}")
                    }
                }

                val isNewUser = existingProfile == null

                val friendCode = existingProfile?.friendCode
                    ?: (userId.take(2).uppercase() + (1000..9999).random())

                // Step 6: Register on leaderboard API for new users
                if (isNewUser) {
                    try {
                        leaderboardApi.registerUser(UserProfileRegistration(
                            userId = userId,
                            friendCode = friendCode,
                            displayName = displayName.ifEmpty { firebaseUser.displayName ?: "User" },
                            avatarInitials = (displayName.ifEmpty { firebaseUser.displayName ?: "U" })
                                .take(1).uppercase()
                        ))
                    } catch (e: Exception) {
                        Log.w("AUTH", "Leaderboard registration failed: ${e.localizedMessage}")
                    }
                }

                // Step 7: Emit success — UI will route based on isNewUser
                _uiState.value = AuthUiState.Success(
                    userId = userId,
                    friendCode = friendCode,
                    isNewUser = isNewUser,
                    displayName = displayName.ifEmpty { firebaseUser.displayName ?: "" },
                    email = email.ifEmpty { firebaseUser.email ?: "" }
                )

            } catch (e: Exception) {
                Log.e("AUTH", "Google Sign-In failed: ${e.localizedMessage}", e)
                _uiState.value = AuthUiState.Error(
                    e.localizedMessage ?: "Sign-in failed. Please try again."
                )
            }
        }
    }
}
