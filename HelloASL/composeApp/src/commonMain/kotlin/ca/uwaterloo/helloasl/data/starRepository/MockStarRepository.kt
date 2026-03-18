package ca.uwaterloo.helloasl.data.starRepository

import ca.uwaterloo.helloasl.domain.starModel.StarTag
import ca.uwaterloo.helloasl.domain.starModel.StarRow
import ca.uwaterloo.helloasl.data.MockDB

class MockStarRepository(
    private val db: MockDB
) : StarRepository {

    private val starRows = mutableMapOf<String, MutableList<StarRow>>()

    private val tags = mutableMapOf<String, MutableList<StarTag>>()

    override suspend fun getStarredSignIds(userId: String): List<Long> {
        return db.getStarredSignIds(userId)
    }

    override suspend fun addStar(userId: String, signId: Long, tagId: Long) {
        db.addStarRow(
            userId,
            StarRow(userId, signId, tagId)
        )
    }

    override suspend fun removeStar(userId: String, signId: Long) {
        db.removeStar(userId, signId)
    }

    override suspend fun getStarRows(userId: String): List<StarRow> {
        return db.getStarRows(userId)
    }

    override suspend fun getTags(userId: String): List<StarTag> {
        return db.getTags(userId)
    }

    override suspend fun createTag(userId: String, name: String): Boolean {
        return db.createTag(userId, name)
    }
}