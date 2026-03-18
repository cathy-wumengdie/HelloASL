package ca.uwaterloo.helloasl.data.starRepository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.domain.starModel.StarTag
import ca.uwaterloo.helloasl.domain.starModel.StarRow

class SupabaseStarRepository(
    private val supabase: SupabaseClient
) : StarRepository {
//    @Serializable
//    private data class StarRow(
//        @SerialName("user_id") val userId: String,
//        @SerialName("sign_id") val signId: Long,
//        @SerialName("tag_id") val tagId: Long
//    )

    override fun getStarredItems(): List<StarItem> {
        return emptyList()
    }

    override fun removeStar(itemId: String) {
        // TODO later
    }

    override suspend fun getStarredSignIds(userId: String): List<Long> {
        val rows = supabase
            .from("StarSign")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<StarRow>()

        return rows.map { it.signId }
    }

    override suspend fun addStar(userId: String, signId: Long, tagId: Long) {
        supabase
            .from("StarSign")
            .upsert(
                StarRow(
                    userId = userId,
                    signId = signId,
                    tagId = tagId
                )
            )
    }

    override suspend fun removeStar(userId: String, signId: Long) {
        supabase
            .from("StarSign")
            .delete {
                filter {
                    eq("user_id", userId)
                    eq("sign_id", signId)
                }
            }
    }

    override suspend fun getTags(userId: String): List<StarTag> {
        return supabase.from("Tag")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<StarTag>()
    }

    override suspend fun createTag(userId: String, name: String): Boolean {
        return try {
            supabase.from("Tag").insert(
                listOf(
                    mapOf(
                        "name" to name,
                        "user_id" to userId
                    )
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getStarRows(userId: String): List<StarRow> {
        return supabase
            .from("StarSign")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<StarRow>()
    }
}