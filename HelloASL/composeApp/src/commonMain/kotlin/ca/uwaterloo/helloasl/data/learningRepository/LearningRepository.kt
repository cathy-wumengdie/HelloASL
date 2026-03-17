package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.learningModel.QuizChoice

interface LearningRepository {
    suspend fun getModules(): List<Module>
    suspend fun getLessons(): List<Lesson>
    suspend fun getQuizChoicesBySignIds(signIds: List<Long>): List<QuizChoice>
    fun getLessonsByModuleId(moduleId: Long): List<Lesson>
    fun getModuleById(id: Long): Module
    fun getLessonById(id: Long): Lesson
    fun getSignById(id: Long): ASLSign?
    fun getSignsByIds(ids: List<Long>): List<ASLSign>
    fun getSignsByLessonId(lessonId: Long): List<ASLSign>
}