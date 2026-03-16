package ca.uwaterloo.helloasl.data.learningRepository

import android.util.Log
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseLearningRepository(
    private val supabase: SupabaseClient
) : LearningRepository {

    @Serializable
    private data class ModuleRow(
        @SerialName("module_id") val moduleId: Int,
        val title: String,
        val category: String? = null
    )

    @Serializable
    private data class LessonRow(
        @SerialName("lesson_id") val lessonId: Int,
        @SerialName("module_id") val moduleId: Int,
        val title: String
    )

    @Serializable
    private data class SignRow(
        @SerialName("sign_id") val signId: Int,
        @SerialName("lesson_id") val lessonId: Int? = null,
        val gloss: String,
        @SerialName("video_url1") val videoUrl1: String,
        @SerialName("video_url2") val videoUrl2: String? = null
    )

    override fun getModules(): List<Module> = runBlocking {
        val rows = supabase.from("Module").select().decodeList<ModuleRow>()
        Log.d("[SupabaseLearningRepository]"," getModules rows=${rows.size}")
        rows.map { row ->
            Module(
                moduleId = row.moduleId,
                title = row.title,
                category = row.category
            )
        }
    }

    override fun getLessons(): List<Lesson> = runBlocking {
        supabase.from("Lesson").select().decodeList<LessonRow>().map { row ->
            Lesson(
                lessonId = row.lessonId,
                moduleId = row.moduleId,
                title = row.title
            )
        }
    }

    override fun getLessonsByModuleId(moduleId: Int): List<Lesson> = runBlocking {
        val rows = supabase
            .from("Lesson")
            .select { filter { eq("module_id", moduleId) } }
            .decodeList<LessonRow>()
        Log.d("[SupabaseLearningRepository]", "getLessonsByModuleId moduleId=$moduleId rows=${rows.size}")
        rows.map { row ->
            Lesson(
                lessonId = row.lessonId,
                moduleId = row.moduleId,
                title = row.title
            )
        }
    }

    override fun getModuleById(id: Int): Module = runBlocking {
        val moduleRow = supabase
            .from("Module")
            .select { filter { eq("module_id", id) } }
            .decodeSingleOrNull<ModuleRow>()

        if (moduleRow != null) {
            return@runBlocking Module(
                moduleId = moduleRow.moduleId,
                title = moduleRow.title,
                category = moduleRow.category
            )
        }

        val fallback = supabase.from("Module").select().decodeList<ModuleRow>().firstOrNull()
        if (fallback != null) {
            return@runBlocking Module(
                moduleId = fallback.moduleId,
                title = fallback.title,
                category = fallback.category
            )
        }

        Module(
            moduleId = id,
            title = "Learning",
            category = null
        )
    }

    override fun getLessonById(id: Int): Lesson = runBlocking {
        val lessonRow = supabase
            .from("Lesson")
            .select { filter { eq("lesson_id", id) } }
            .decodeSingleOrNull<LessonRow>() ?: error("Lesson with id $id not found")

        Lesson(
            lessonId = lessonRow.lessonId,
            moduleId = lessonRow.moduleId,
            title = lessonRow.title
        )
    }

    override fun getSignById(id: Int): ASLSign? = runBlocking {
        supabase
            .from("ASLSign")
            .select { filter { eq("sign_id", id) } }
            .decodeSingleOrNull<SignRow>()
            ?.toDomain()
    }

    override fun getSignsByIds(ids: List<Int>): List<ASLSign> = runBlocking {
        if (ids.isEmpty()) return@runBlocking emptyList()
        val rows = supabase
            .from("ASLSign")
            .select { filter { isIn("sign_id", ids) } }
            .decodeList<SignRow>()

        rows.map { it.toDomain() }
    }

    override fun getSignsByLessonId(lessonId: Int): List<ASLSign> = runBlocking {
        val rows = supabase
            .from("ASLSign")
            .select { filter { eq("lesson_id", lessonId) } }
            .decodeList<SignRow>()
        Log.d("[SupabaseLearningRepository]", "getSignsByLessonId lessonId=$lessonId rows=${rows.size}")
        rows.map { it.toDomain() }
    }

    private fun SignRow.toDomain(): ASLSign {
        return ASLSign(
            signId = signId,
            lessonId = lessonId,
            gloss = gloss,
            videoUrl1 = videoUrl1,
            videoUrl2 = videoUrl2
        )
    }
}
