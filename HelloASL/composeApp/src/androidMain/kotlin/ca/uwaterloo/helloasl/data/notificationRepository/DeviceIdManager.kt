package ca.uwaterloo.helloasl.data.notificationRepository

import android.content.Context
import java.util.UUID

object DeviceIdManager {
    fun getOrCreateDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val existing = prefs.getString("device_id", null)
        if (existing != null) return existing

        val newId = UUID.randomUUID().toString()
        prefs.edit().putString("device_id", newId).apply()
        return newId
    }
}