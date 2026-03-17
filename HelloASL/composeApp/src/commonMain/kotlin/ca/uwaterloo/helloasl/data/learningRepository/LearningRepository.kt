package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.learningModel.QuizChoice

interface LearningRepository {
    suspend fun getModules(): List<Module>
    suspend fun getLessons(): List<Lesson>
    suspend fun getQuizChoicesBySignIds(signIds: List<Long>): List<QuizChoice>
    suspend fun getLessonsByModuleId(moduleId: Long): List<Lesson>
    suspend fun getModuleById(id: Long): Module
    suspend fun getLessonById(id: Long): Lesson
    suspend fun getSignById(id: Long): ASLSign?
    suspend fun getSignsByIds(ids: List<Long>): List<ASLSign>
    suspend fun getSignsByLessonId(lessonId: Long): List<ASLSign>
}