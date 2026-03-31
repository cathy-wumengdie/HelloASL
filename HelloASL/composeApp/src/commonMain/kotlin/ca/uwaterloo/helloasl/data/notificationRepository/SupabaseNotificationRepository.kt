package ca.uwaterloo.helloasl.data.notificationRepository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class SupabaseNotificationRepository(
    private val supabase: SupabaseClient,
    private val anonKey: String
) : NotificationRepository {

    private val httpClient = HttpClient()

    override suspend fun triggerSendMissedReminder() {
        supabase.auth.refreshCurrentSession()

        val session = supabase.auth.currentSessionOrNull()
        val accessToken = session?.accessToken

        println("Send-missed-reminder: Session exists: ${session != null}")
        println("Send-missed-reminder: Access token exists: ${accessToken != null}")

        if (accessToken == null) {
            println("No access token, skipping send-missed-reminder")
            return
        }
            logJwtInfo(accessToken)

        runCatching {
            val response = httpClient.post(
                "https://dbdwlwyemwjivrrvuzjz.supabase.co/functions/v1/send-missed-reminder"
            ) {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.Authorization, "Bearer $accessToken")
                headers.append("apikey", anonKey)
                setBody(buildJsonObject { }.toString())
            }

            val text = response.bodyAsText()
            println("send-missed-reminder status: ${response.status}")
            println("send-missed-reminder response: $text")
        }.onFailure {
            println("send-missed-reminder failed: ${it.message}")
            it.printStackTrace()
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun logJwtInfo(token: String) {
        try {
            val parts = token.split(".")
            if (parts.size < 2) {
                println("Send-missed-reminder: JWT format invalid")
                return
            }

            val payload = parts[1]
            val normalized = payload
                .replace('-', '+')
                .replace('_', '/')
                .let {
                    when (it.length % 4) {
                        2 -> "$it=="
                        3 -> "$it="
                        else -> it
                    }
                }

            val decoded = Base64.decode(normalized)
            println("Send-missed-reminder: JWT payload: ${decoded.decodeToString()}")
        } catch (e: Exception) {
            println("Send-missed-reminder: Failed to decode JWT: ${e.message}")
        }
    }
}