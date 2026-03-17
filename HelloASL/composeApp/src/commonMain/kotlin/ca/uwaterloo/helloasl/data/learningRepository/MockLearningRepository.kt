package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.learningModel.QuizChoice

class MockLearningRepository(private val db: MockDB) : LearningRepository {
    override suspend fun getModules(): List<Module> = db.modules

    override suspend fun getLessons(): List<Lesson> = db.lessons

    override suspend fun getQuizChoicesBySignIds(signIds: List<Long>): List<QuizChoice> {
        if (signIds.isEmpty()) return emptyList()
        val idSet = signIds.toSet()
        return db.quizChoices.filter { it.signId in idSet }
    }

    override suspend fun getLessonsByModuleId(moduleId: Long): List<Lesson> =
        db.lessons.filter { it.moduleId == moduleId }

    override suspend fun getModuleById(id: Long): Module =
        db.modules.find { it.moduleId == id } ?: error("Module with id $id not found")

    override suspend fun getLessonById(id: Long): Lesson =
        db.lessons.find { it.lessonId == id } ?: error("Lesson with id $id not found")

    override suspend fun getSignById(id: Long): ASLSign? =
        db.signs.find { it.signId == id }

    override suspend fun getSignsByIds(ids: List<Long>): List<ASLSign> {
        val signsMap = db.signs.associateBy { it.signId }
        return ids.mapNotNull { signsMap[it] }
    }

    override suspend fun getSignsByLessonId(lessonId: Long): List<ASLSign> =
        db.signs.filter { it.lessonId == lessonId }
}