package ca.uwaterloo.helloasl.data.starRepository

import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.domain.starModel.StarTag
import ca.uwaterloo.helloasl.domain.starModel.StarRow

interface StarRepository {
    fun getStarredItems(): List<StarItem>
    fun removeStar(itemId: String)
    suspend fun getStarredSignIds(userId: String): List<Long>
    suspend fun addStar(userId: String, signId: Long, tagId: Long)
    suspend fun removeStar(userId: String, signId: Long)
    suspend fun getTags(userId: String): List<StarTag>
    suspend fun createTag(userId: String, name: String): Boolean
    suspend fun getStarRows(userId: String): List<StarRow>
}