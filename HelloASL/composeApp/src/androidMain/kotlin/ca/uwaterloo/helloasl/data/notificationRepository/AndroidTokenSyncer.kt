package ca.uwaterloo.helloasl.data.notificationRepository

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.tasks.await

object AndroidTokenSyncer {

    suspend fun syncCurrentToken(
        context: Context,
        supabase: SupabaseClient
    ): Boolean {
        val userId = supabase.auth.currentSessionOrNull()?.user?.id
        Log.d("HelloASL_FCM", "syncCurrentToken userId=$userId")

        if (userId == null) {
            Log.d("HelloASL_FCM", "No logged-in user, token not saved")
            return false
        }

        val token = FirebaseMessaging.getInstance().token.await()
        Log.d("HelloASL_FCM", "Fetched token=${token.take(20)}...")

        val deviceId = DeviceIdManager.getOrCreateDeviceId(context)
        Log.d("HelloASL_FCM", "About to upsert token. token=${token.take(20)}... deviceId=$deviceId")

        DeviceTokenRepository(supabase).upsertToken(
            userId = userId,
            token = token,
            deviceId = deviceId
        )

        Log.d("HelloASL_FCM", "Upsert token success")
        return true
    }

    suspend fun syncProvidedToken(
        context: Context,
        supabase: SupabaseClient,
        token: String
    ): Boolean {
        val userId = supabase.auth.currentSessionOrNull()?.user?.id
        if (userId == null) {
            Log.d("HelloASL_FCM", "Token received but no logged-in user yet")
            return false
        }

        val deviceId = DeviceIdManager.getOrCreateDeviceId(context)

        DeviceTokenRepository(supabase).upsertToken(
            userId = userId,
            token = token,
            deviceId = deviceId
        )

        Log.d("HelloASL_FCM", "Synced refreshed token for user=$userId deviceId=$deviceId")
        return true
    }
}