package ca.uwaterloo.helloasl.data.notificationRepository

import ca.uwaterloo.helloasl.domain.notificationModel.DeviceToken
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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
        supabase.postgrest.rpc(
            function = "register_device_token",
            parameters = mapOf(
                "p_device_id" to deviceId,
                "p_token" to token,
                "p_platform" to "android"
            )
        )
    }

    suspend fun getTokensForUser(userId: String): List<DeviceToken> {
        return supabase
            .from("DeviceTokens")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<DeviceToken>()
    }
}