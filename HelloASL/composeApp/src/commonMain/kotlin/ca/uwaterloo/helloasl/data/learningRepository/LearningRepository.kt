package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module

interface LearningRepository {
    fun getModules(): List<Module>
    fun getLessons(): List<Lesson>
    fun getLessonsByModuleId(moduleId: Int): List<Lesson>
    fun getModuleById(id: Int): Module
    fun getLessonById(id: Int): Lesson
    fun getSignById(id: Int): ASLSign?
    fun getSignsByIds(ids: List<Int>): List<ASLSign>
    fun getSignsByLessonId(lessonId: Int): List<ASLSign>
}