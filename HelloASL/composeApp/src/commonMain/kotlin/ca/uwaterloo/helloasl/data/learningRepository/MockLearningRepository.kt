package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module

class MockLearningRepository(private val db: MockDB) : LearningRepository {
    override fun getModules(): List<Module> = db.modules

    override fun getLessons(): List<Lesson> = db.lessons

    override fun getLessonById(id: Int): Lesson? = db.lessons.find { it.id == id }

    override fun getSignById(id: Int): ASLSign? = db.signs.find { it.id == id }

    override fun getSignsByIds(ids: List<Int>): List<ASLSign> {
        val signsMap = db.signs.associateBy { it.id }
        return ids.mapNotNull { signsMap[it] }
    }
}