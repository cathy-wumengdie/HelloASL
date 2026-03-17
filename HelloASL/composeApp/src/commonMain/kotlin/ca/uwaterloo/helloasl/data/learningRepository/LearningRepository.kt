package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.learningModel.QuizChoice

interface LearningRepository {
    suspend fun getModules(): List<Module>
    suspend fun getLessons(): List<Lesson>
    suspend fun getLessonsByModuleId(moduleId: Int): List<Lesson>
    suspend fun getModuleById(id: Int): Module
    suspend fun getLessonById(id: Int): Lesson
    suspend fun getSignById(id: Int): ASLSign?
    suspend fun getSignsByIds(ids: List<Int>): List<ASLSign>
    suspend fun getSignsByLessonId(lessonId: Int): List<ASLSign>
    suspend fun getQuizChoicesBySignIds(signIds: List<Int>): List<QuizChoice>
}