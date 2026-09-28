package com.example.gymfitness.utils

import android.content.Context

class DeviceIdProvider(context: Context) {
    private val prefs = context.getSharedPreferences("device_id", Context.MODE_PRIVATE)

    fun getDeviceId(): String {
        var id = prefs.getString("device_id", null)

        if (id == null) {
            id = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("device_id", id).apply()
        }
        return id
    }
}