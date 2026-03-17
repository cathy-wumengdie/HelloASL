package ca.uwaterloo.helloasl.data.learningRepository

<<<<<<< HEAD
import ca.uwaterloo.helloasl.data.AppLogger
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import ca.uwaterloo.helloasl.domain.learningModel.QuizChoice
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
=======
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.Lesson
import ca.uwaterloo.helloasl.domain.learningModel.Module
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseLearningRepository(
    private val supabase: SupabaseClient
) : LearningRepository {

    @Serializable
    private data class ModuleRow(
<<<<<<< HEAD
        @SerialName("module_id") val moduleId: Int,
=======
        @SerialName("module_id") val moduleId: Long,
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
        val title: String,
        val category: String? = null
    )

    @Serializable
    private data class LessonRow(
<<<<<<< HEAD
        @SerialName("lesson_id") val lessonId: Int,
        @SerialName("module_id") val moduleId: Int,
=======
        @SerialName("lesson_id") val lessonId: Long,
        @SerialName("module_id") val moduleId: Long,
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
        val title: String
    )

    @Serializable
    private data class SignRow(
<<<<<<< HEAD
        @SerialName("sign_id") val signId: Int,
        @SerialName("lesson_id") val lessonId: Int? = null,
=======
        @SerialName("sign_id") val signId: Long,
        @SerialName("lesson_id") val lessonId: Long? = null,
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
        val gloss: String,
        @SerialName("video_url1") val videoUrl1: String,
        @SerialName("video_url2") val videoUrl2: String? = null
    )

<<<<<<< HEAD
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
=======
    override fun getModules(): List<Module> = runBlocking {
        supabase.from("Module").select().decodeList<ModuleRow>().map {
            Module(
                moduleId = it.moduleId,
                title = it.title,
                category = it.category
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
            )
        }
    }

<<<<<<< HEAD
    override suspend fun getLessons(): List<Lesson> {
        return supabase.from("Lesson").select().decodeList<LessonRow>().map { row ->
            Lesson(
                lessonId = row.lessonId,
                moduleId = row.moduleId,
                title = row.title
=======
    override fun getLessons(): List<Lesson> = runBlocking {
        supabase.from("Lesson").select().decodeList<LessonRow>().map {
            Lesson(
                lessonId = it.lessonId,
                moduleId = it.moduleId,
                title = it.title
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
            )
        }
    }

<<<<<<< HEAD
    override suspend fun getLessonsByModuleId(moduleId: Int): List<Lesson> {
        val rows = supabase
            .from("Lesson")
            .select { filter { eq("module_id", moduleId) } }
            .decodeList<LessonRow>()
        AppLogger.d("SupabaseLearningRepository", "getLessonsByModuleId moduleId=$moduleId rows=${rows.size}")
        return rows.map { row ->
            Lesson(
                lessonId = row.lessonId,
                moduleId = row.moduleId,
                title = row.title
            )
        }
    }

    override suspend fun getModuleById(id: Int): Module {
=======
    override fun getLessonsByModuleId(moduleId: Long): List<Lesson> = runBlocking {
        supabase
            .from("Lesson")
            .select { filter { eq("module_id", moduleId) } }
            .decodeList<LessonRow>()
            .map {
                Lesson(
                    lessonId = it.lessonId,
                    moduleId = it.moduleId,
                    title = it.title
                )
            }
    }

    override fun getModuleById(id: Long): Module = runBlocking {
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
        val moduleRow = supabase
            .from("Module")
            .select { filter { eq("module_id", id) } }
            .decodeSingleOrNull<ModuleRow>()
<<<<<<< HEAD

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

    override suspend fun getLessonById(id: Int): Lesson {
        val lessonRow = supabase
            .from("Lesson")
            .select { filter { eq("lesson_id", id) } }
            .decodeSingleOrNull<LessonRow>() ?: error("Lesson with id $id not found")

        return Lesson(
=======
            ?: error("Module with id $id not found")

        Module(
            moduleId = moduleRow.moduleId,
            title = moduleRow.title,
            category = moduleRow.category
        )
    }

    override fun getLessonById(id: Long): Lesson = runBlocking {
        val lessonRow = supabase
            .from("Lesson")
            .select { filter { eq("lesson_id", id) } }
            .decodeSingleOrNull<LessonRow>()
            ?: error("Lesson with id $id not found")

        Lesson(
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
            lessonId = lessonRow.lessonId,
            moduleId = lessonRow.moduleId,
            title = lessonRow.title
        )
    }

<<<<<<< HEAD
    override suspend fun getSignById(id: Int): ASLSign? {
        return supabase
=======
    override fun getSignById(id: Long): ASLSign? = runBlocking {
        supabase
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
            .from("ASLSign")
            .select { filter { eq("sign_id", id) } }
            .decodeSingleOrNull<SignRow>()
            ?.toDomain()
    }

<<<<<<< HEAD
    override suspend fun getSignsByIds(ids: List<Int>): List<ASLSign> {
        if (ids.isEmpty()) return emptyList()
        val rows = supabase
            .from("ASLSign")
            .select { filter { isIn("sign_id", ids) } }
            .decodeList<SignRow>()

        return rows.map { it.toDomain() }
    }

    override suspend fun getSignsByLessonId(lessonId: Int): List<ASLSign> {
        val rows = supabase
            .from("ASLSign")
            .select { filter { eq("lesson_id", lessonId) } }
            .decodeList<SignRow>()
        AppLogger.d("SupabaseLearningRepository", "getSignsByLessonId lessonId=$lessonId rows=${rows.size}")
        return rows.map { it.toDomain() }
    }

    override suspend fun getQuizChoicesBySignIds(signIds: List<Int>): List<QuizChoice> {
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
=======
    override fun getSignsByIds(ids: List<Long>): List<ASLSign> = runBlocking {
        if (ids.isEmpty()) return@runBlocking emptyList()
        supabase
            .from("ASLSign")
            .select { filter { isIn("sign_id", ids) } }
            .decodeList<SignRow>()
            .map { it.toDomain() }
    }

    override fun getSignsByLessonId(lessonId: Long): List<ASLSign> = runBlocking {
        supabase
            .from("ASLSign")
            .select { filter { eq("lesson_id", lessonId) } }
            .decodeList<SignRow>()
            .map { it.toDomain() }
>>>>>>> 4cc119b (initial work on auth, user learning progress supabase repos)
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
