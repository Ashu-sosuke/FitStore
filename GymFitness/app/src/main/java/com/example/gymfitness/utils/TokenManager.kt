package com.example.gymfitness.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun clearToken() {
        prefs.edit().remove("jwt_token").apply()
    }

    /** Persist the active user ID (Firebase UID after Google Sign-In). */
    fun saveUserId(uid: String) {
        prefs.edit().putString("active_user_id", uid).apply()
    }

    /**
     * Returns the active user ID.
     * Priority: saved Firebase UID > ANDROID_ID fallback (guest mode).
     */
    @SuppressLint("HardwareIds")
    fun getUserId(): String {
        return prefs.getString("active_user_id", null)
            ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "default_device"
    }

    fun clearUserId() {
        prefs.edit().remove("active_user_id").apply()
    }
}
