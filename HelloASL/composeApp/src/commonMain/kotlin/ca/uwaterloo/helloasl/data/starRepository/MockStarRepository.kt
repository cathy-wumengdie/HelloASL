package ca.uwaterloo.helloasl.data.starRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.starModel.StarItem

class MockStarRepository(private val db: MockDB) : StarRepository {

    override fun getStarredItems(): List<StarItem> {
        val userId = db.getUserSession()?.userId ?: return emptyList()
        return db.getStarredItemsForUser(userId)
    }

    override fun removeStar(itemId: String) {
        val userId = db.getUserSession()?.userId ?: return

        db.removeStarForUser(userId, itemId)

        val progress = db.getUserLearningProgress(userId) ?: return
        val newCount = db.getStarredItemsForUser(userId).size

//        db.putUserLearningProgress(
//            userId,
//            progress.copy(starredSigns = newCount)
//        )
    }
}