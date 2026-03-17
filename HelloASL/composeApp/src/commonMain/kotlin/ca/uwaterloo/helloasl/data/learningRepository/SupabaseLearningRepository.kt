package ca.uwaterloo.helloasl.data.learningRepository

import ca.uwaterloo.helloasl.data.AppLogger
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.learningModel.QuizChoice
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseLearningRepository(
    private val supabase: SupabaseClient
) : LearningRepository {

    @Serializable
    private data class ModuleRow(
        @SerialName("module_id") val moduleId: Long,
        val title: String,
        val category: String? = null
    )

    @Serializable
    private data class LessonRow(
        @SerialName("lesson_id") val lessonId: Long,
        @SerialName("module_id") val moduleId: Long,
        val title: String
    )

    @Serializable
    private data class SignRow(
        @SerialName("sign_id") val signId: Long,
        @SerialName("lesson_id") val lessonId: Long? = null,
        val gloss: String,
        @SerialName("video_url1") val videoUrl1: String,
        @SerialName("video_url2") val videoUrl2: String? = null
    )

    @Serializable
    private data class QuizChoiceRow(
        @SerialName("choice_id") val choiceId: Long,
        @SerialName("sign_id") val signId: Long,
        @SerialName("choice_text") val choiceText: String,
        @SerialName("is_correct") val isCorrect: Boolean
    )

    override suspend fun getModules(): List<Module> {
        val rows = supabase.from("Module").select().decodeList<ModuleRow>()
        AppLogger.d("SupabaseLearningRepository", "getModules rows=${rows.size}")
        return rows.map { row ->
            Module(
                moduleId = row.moduleId,
                title = row.title,
                category = row.category
            )
        }
    }

    override suspend fun getLessons(): List<Lesson> {
        return supabase.from("Lesson").select().decodeList<LessonRow>().map { row ->
            Lesson(
                lessonId = row.lessonId,
                moduleId = row.moduleId,
                title = row.title
            )
        }
    }

    override suspend fun getLessonsByModuleId(moduleId: Long): List<Lesson> {
        val rows = supabase
            .from("Lesson")
            .select { filter { eq("module_id", moduleId) } }
            .decodeList<LessonRow>()

        AppLogger.d(
            "SupabaseLearningRepository",
            "getLessonsByModuleId moduleId=$moduleId rows=${rows.size}"
        )

        return rows.map { row ->
            Lesson(
                lessonId = row.lessonId,
                moduleId = row.moduleId,
                title = row.title
            )
        }
    }

    override suspend fun getModuleById(id: Long): Module {
        val moduleRow = supabase
            .from("Module")
            .select { filter { eq("module_id", id) } }
            .decodeSingleOrNull<ModuleRow>()

        if (moduleRow != null) {
            return Module(
                moduleId = moduleRow.moduleId,
                title = moduleRow.title,
                category = moduleRow.category
            )
        }

        val fallback = supabase.from("Module").select().decodeList<ModuleRow>().firstOrNull()
        if (fallback != null) {
            return Module(
                moduleId = fallback.moduleId,
                title = fallback.title,
                category = fallback.category
            )
        }

        return Module(
            moduleId = id,
            title = "Learning",
            category = null
        )
    }

    override suspend fun getLessonById(id: Long): Lesson {
        val lessonRow = supabase
            .from("Lesson")
            .select { filter { eq("lesson_id", id) } }
            .decodeSingleOrNull<LessonRow>()
            ?: error("Lesson with id $id not found")

        return Lesson(
            lessonId = lessonRow.lessonId,
            moduleId = lessonRow.moduleId,
            title = lessonRow.title
        )
    }

    override suspend fun getSignById(id: Long): ASLSign? {
        return supabase
            .from("ASLSign")
            .select { filter { eq("sign_id", id) } }
            .decodeSingleOrNull<SignRow>()
            ?.toDomain()
    }

    override suspend fun getSignsByIds(ids: List<Long>): List<ASLSign> {
        if (ids.isEmpty()) return emptyList()
        val rows = supabase
            .from("ASLSign")
            .select { filter { isIn("sign_id", ids) } }
            .decodeList<SignRow>()

        return rows.map { it.toDomain() }
    }

    override suspend fun getSignsByLessonId(lessonId: Long): List<ASLSign> {
        val rows = supabase
            .from("ASLSign")
            .select { filter { eq("lesson_id", lessonId) } }
            .decodeList<SignRow>()

        AppLogger.d(
            "SupabaseLearningRepository",
            "getSignsByLessonId lessonId=$lessonId rows=${rows.size}"
        )

        return rows.map { it.toDomain() }
    }

    override suspend fun getQuizChoicesBySignIds(signIds: List<Long>): List<QuizChoice> {
        if (signIds.isEmpty()) return emptyList()
        val rows = supabase
            .from("QuizChoice")
            .select { filter { isIn("sign_id", signIds) } }
            .decodeList<QuizChoiceRow>()

        return rows.map { row ->
            QuizChoice(
                choiceId = row.choiceId,
                signId = row.signId,
                choiceText = row.choiceText,
                isCorrect = row.isCorrect
            )
        }
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