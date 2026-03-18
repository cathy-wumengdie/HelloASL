package ca.uwaterloo.helloasl.domain.starModel

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlinx.coroutines.runBlocking

class StarModelTest {

    private fun newRepo(): Pair<MockDB, MockStarRepository> {
        val db = MockDB()
        val repo = MockStarRepository(db)
        return db to repo
    }

    @Test
    fun add_star_should_add_row() = runBlocking {
        val (db, repo) = newRepo()
        val userId = "1"

        val before = db.getStarRows(userId).size

        repo.addStar(userId, 100L, 1L)

        val rows = db.getStarRows(userId)

        assertEquals(before + 1, rows.size)
        assertTrue(rows.any { it.signId == 100L })
    }

    @Test
    fun remove_star_should_remove_row() = runBlocking {
        val (db, repo) = newRepo()
        val userId = "1"

        db.addStarRow(userId, StarRow(userId, 100L, 1L))
        val before = db.getStarRows(userId).size

        repo.removeStar(userId, 100L)

        val rows = db.getStarRows(userId)

        assertEquals(before - 1, rows.size)
        assertFalse(rows.any { it.signId == 100L })
    }

    @Test
    fun get_starred_sign_ids_should_return_ids() = runBlocking {
        val (db, repo) = newRepo()
        val userId = "1"

        val before = db.getStarredSignIds(userId).size

        db.addStarRow(userId, StarRow(userId, 100L, 1L))
        db.addStarRow(userId, StarRow(userId, 200L, 1L))

        val ids = repo.getStarredSignIds(userId)

        assertEquals(before + 2, ids.size)
        assertTrue(ids.contains(100L))
        assertTrue(ids.contains(200L))
    }

    @Test
    fun create_tag_should_add_new_tag() = runBlocking {
        val (_, repo) = newRepo()
        val userId = "1"

        val before = repo.getTags(userId).size

        val result = repo.createTag(userId, "NewTag")

        val tags = repo.getTags(userId)

        assertTrue(result)
        assertEquals(before + 1, tags.size)
        assertTrue(tags.any { it.name == "NewTag" })
    }

    @Test
    fun create_tag_should_fail_if_duplicate() = runBlocking {
        val (_, repo) = newRepo()
        val userId = "1"

        repo.createTag(userId, "Favorites") // 已存在
        val result = repo.createTag(userId, "Favorites")

        assertFalse(result)
    }

    @Test
    fun get_star_rows_should_return_rows() = runBlocking {
        val (db, repo) = newRepo()
        val userId = "1"

        val before = db.getStarRows(userId).size

        db.addStarRow(userId, StarRow(userId, 100L, 1L))

        val rows = repo.getStarRows(userId)

        assertEquals(before + 1, rows.size)
        assertTrue(rows.any { it.signId == 100L })
    }
}