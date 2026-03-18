package ca.uwaterloo.helloasl.data.starRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.domain.starModel.StarTag
import ca.uwaterloo.helloasl.domain.starModel.StarRow

class MockStarRepository(private val db: MockDB) : StarRepository {

    override fun getStarredItems(): List<StarItem> {
//        val userId = db.getUserSession()?.userId ?: return emptyList()
//        return db.getStarredItemsForUser(userId)
        return emptyList()
    }

    override fun removeStar(itemId: String) {
//        val userId = db.getUserSession()?.userId ?: return
//
//        db.removeStarForUser(userId, itemId)
//
//        val progress = db.getUserLearningProgress(userId) ?: return
//        val newCount = db.getStarredItemsForUser(userId).size

//        db.putUserLearningProgress(
//            userId,
//            progress.copy(starredSigns = newCount)
//        )
    }

    override suspend fun getStarredSignIds(userId: String): List<Long> {
        return emptyList()
    }

    override suspend fun addStar(userId: String, signId: Long, tagId: Long) {
        // do nothing
    }

    override suspend fun removeStar(userId: String, signId: Long) {
        // do nothing
    }

    override suspend fun getTags(userId: String): List<StarTag> {
        return emptyList()
    }

    override suspend fun createTag(userId: String, name: String): Boolean {
        return false
    }

    override suspend fun getStarRows(userId: String): List<StarRow> {
        return emptyList()
    }
}