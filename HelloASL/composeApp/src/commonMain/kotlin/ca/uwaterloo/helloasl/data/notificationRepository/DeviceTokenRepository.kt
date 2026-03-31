package ca.uwaterloo.helloasl.data.notificationRepository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
class DeviceTokenRepository(
    private val supabase: SupabaseClient
) {
    suspend fun upsertToken(
        userId: String,
        token: String,
        deviceId: String
    ) {
        println("syncCurrentToken userId=$userId")
        println("About to upsert token. token=${token.take(20)}... deviceId=$deviceId")

        runCatching {
            supabase.postgrest.rpc(
                function = "register_device_token",
                parameters = mapOf(
                    "p_device_id" to deviceId,
                    "p_token" to token,
                    "p_platform" to "android"
                )
            )
        }.onSuccess {
            println("Upsert token success")
        }.onFailure {
            println("Upsert token failed: ${it.message}")
            it.printStackTrace()
            throw it
        }
    }
}