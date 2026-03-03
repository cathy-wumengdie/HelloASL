package ca.uwaterloo.helloasl.data.repository

import ca.uwaterloo.helloasl.domain.learning.ASLSign
import ca.uwaterloo.helloasl.domain.learning.Lesson
import ca.uwaterloo.helloasl.domain.learning.Module

interface LearningRepository {
    fun getModules(): List<Module>
    fun getLessons(): List<Lesson>
    fun getLessonById(id: Int): Lesson?
    fun getSignById(id: Int): ASLSign?
    fun getSignsByIds(ids: List<Int>): List<ASLSign>
}