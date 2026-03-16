package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module

class MockLearningRepository(private val db: MockDB) : LearningRepository {
    override fun getModules(): List<Module> = db.modules

    override fun getLessons(): List<Lesson> = db.lessons

    override fun getLessonsByModuleId(moduleId: Int): List<Lesson> = db.lessons.filter { it.moduleId == moduleId }

    override fun getModuleById(id: Int): Module =
        db.modules.find { it.moduleId == id } ?: error("Module with id $id not found")

    override fun getLessonById(id: Int): Lesson =
        db.lessons.find { it.lessonId == id } ?: error("Lesson with id $id not found")

    override fun getSignById(id: Int): ASLSign? = db.signs.find { it.signId == id }

    override fun getSignsByIds(ids: List<Int>): List<ASLSign> {
        val signsMap = db.signs.associateBy { it.signId }
        return ids.mapNotNull { signsMap[it] }
    }

    override fun getSignsByLessonId(lessonId: Int): List<ASLSign> =
        db.signs.filter { it.lessonId == lessonId }
}