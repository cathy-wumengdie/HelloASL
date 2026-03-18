package ca.uwaterloo.helloasl.data.translateRepository

import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.translateModel.AslRecognitionResult
import ca.uwaterloo.helloasl.domain.translateModel.TranslateHistoryItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseTranslateRepository(
    private val supabase: SupabaseClient
) : TranslateRepository {

    companion object {
        private const val MAX_HISTORY_ROWS = 10  // Max # of history row each user can have
    }

    @Serializable
    private data class SignRow(
        @SerialName("sign_id") val signId: Long,
        @SerialName("lesson_id") val lessonId: Long? = null,
        val gloss: String,
        @SerialName("video_url1") val videoUrl1: String,
        @SerialName("video_url2") val videoUrl2: String? = null
    )

    @Serializable
    private data class TranslateHistoryRow(
        @SerialName("history_id") val historyId: Long,
        @SerialName("user_id") val userId: String,
        val query: String,
        @SerialName("searched_at") val searchedAt: String
    )

    @Serializable
    private data class TranslateHistoryInsertRow(
        val query: String
    )

    override suspend fun searchWord(word: String): ASLSign? {
        val cleaned = word.trim().lowercase()
        if (cleaned.isBlank()) return null

        return supabase
            .from(table = "ASLSign")
            .select {
                filter { eq(column = "gloss", value = cleaned) }
            }
            .decodeSingleOrNull<SignRow>()
            ?.toDomain()
    }

    override suspend fun getSearchHistory(): List<TranslateHistoryItem> {
        val rows = getSortedHistoryRowsForCurrentUser()

        return rows
            .distinctBy { normalizeQuery(it.query) }
            .map { row ->
                TranslateHistoryItem(
                    id = row.historyId.toInt(),
                    query = row.query
                )
            }
    }

    override suspend fun addHistory(word: String) {
        val cleaned = word.trim().lowercase()
        if (cleaned.isBlank()) return

        supabase
            .from("TranslateHistory")
            .insert(TranslateHistoryInsertRow(query = cleaned))

        trimHistoryToLimit()
    }

    override suspend fun clearHistory() {
        val userId = requireUserId()

        supabase
            .from("TranslateHistory")
            .delete {
                filter { eq("user_id", userId) }
            }
    }

    override suspend fun recognizeAsl(): AslRecognitionResult {
        return AslRecognitionResult(
            recognizedText = "Hello",
            confidence = 0.86f
        )
    }

    private suspend fun getSortedHistoryRowsForCurrentUser(): List<TranslateHistoryRow> {
        val userId = requireUserId()

        return supabase
            .from("TranslateHistory")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<TranslateHistoryRow>()
            .sortedWith(
                compareByDescending<TranslateHistoryRow> { it.searchedAt }
                    .thenByDescending { it.historyId }
            )
    }

    private suspend fun trimHistoryToLimit() {
        val rows = getSortedHistoryRowsForCurrentUser()

        val rowsToDelete = rows.drop(MAX_HISTORY_ROWS)
        for (row in rowsToDelete) {
            supabase
                .from("TranslateHistory")
                .delete {
                    filter { eq("history_id", row.historyId) }
                }
        }
    }

    private fun normalizeQuery(query: String): String =
        query.trim().lowercase()

    private fun SignRow.toDomain(): ASLSign {
        return ASLSign(
            signId = signId,
            lessonId = lessonId,
            gloss = gloss,
            videoUrl1 = videoUrl1,
            videoUrl2 = videoUrl2
        )
    }

    private fun requireUserId(): String {
        val session = supabase.auth.currentSessionOrNull()
            ?: error("No logged-in user")

        return session.user?.id ?: error("Logged-in user has no id")
    }
}