package ca.uwaterloo.helloasl.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ca.uwaterloo.helloasl.BuildConfig
import ca.uwaterloo.helloasl.R
import ca.uwaterloo.helloasl.data.SupabaseClientFactory
import ca.uwaterloo.helloasl.data.notificationRepository.AndroidTokenSyncer
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HelloAslFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("HelloASL_FCM", "New token: $token")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val supabase = SupabaseClientFactory.create(
                    BuildConfig.SUPABASE_URL,
                    BuildConfig.SUPABASE_ANON_KEY
                )

                AndroidTokenSyncer.syncProvidedToken(
                    context = applicationContext,
                    supabase = supabase,
                    token = token
                )
            } catch (e: Exception) {
                Log.e("HelloASL_FCM", "Failed to sync refreshed token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("HelloASL_FCM", "Message received: data=${message.data}")
        createNotificationChannel()

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "HelloASL"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Time to practice ASL"

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HelloASL reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Practice reminders and learning updates"
            }

            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "helloasl_reminders"
    }
}