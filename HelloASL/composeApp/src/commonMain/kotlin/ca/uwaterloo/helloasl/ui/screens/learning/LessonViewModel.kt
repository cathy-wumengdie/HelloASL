package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LessonViewModel(private val model: Model) {
    var state by mutableStateOf(LessonUIState())
        private set

    private var signs: List<ASLSign> = emptyList()
    private var currentIndex: Int = 0
    private var lessonId: Int? = null
    private var onLessonCompleted: ((Int) -> Unit)? = null
    private fun stopAndCommit() {
        if (startMs == null) return
        tickerJob?.cancel()
        tickerJob = null
        commitMinutesIfAny()
        startMs = null
    }

    fun setOnLessonCompleted(listener: (Int) -> Unit) {
        onLessonCompleted = listener
    }

    fun onChoose(option: String) {
        val sign = signs.getOrNull(currentIndex) ?: return
        val correct = sign.word == option
        val isLast = currentIndex == signs.lastIndex
        val completed = correct && isLast

        if (completed) {
            // Save any accumulated learning minutes immediately
            stopAndCommit()
            _elapsedSeconds.value = 0
            committedMinutes = 0
            val completedLessonId = lessonId ?: return
            model.onLessonCompleted(completedLessonId)
            onLessonCompleted?.invoke(completedLessonId)
        }

        state = state.copy(
            selected = option,
            isCorrect = correct,
            showNext = correct && !isLast
        )
    }

    fun onNext() {
        if (signs.isEmpty()) return
        if (currentIndex < signs.lastIndex) {
            currentIndex += 1
            rebuildQuestion()
        }
    }

    fun onStar() {
        val sign = signs.getOrNull(currentIndex) ?: return
        model.toggleStar(sign.id)
    }

    fun loadLesson(lessonId: Int) {
        this.lessonId = lessonId
        this.signs = model.getSignsForLesson(lessonId)
        this.currentIndex = 0

        val title = model.getLesson(lessonId)?.title ?: "Lesson"
        state = state.copy(title = title)

        rebuildQuestion()
    }

    private fun rebuildQuestion() {
        val sign = signs.getOrNull(currentIndex)
        if (sign == null) {
            state = state.copy(
                options = emptyList(),
                videoUrl = null,
                selected = null,
                isCorrect = null,
                showNext = false,
                progress = ""
            )
            return
        }

        val correct = sign.word
        val distractors = signs.map { it.word }.filter { it != correct }.shuffled().take(2)
        val options = (distractors + correct).shuffled()
        val progressText = "${currentIndex + 1}/${signs.size}"

        state = state.copy(
            options = options,
            videoUrl = sign.videoUrls.firstOrNull(),
            selected = null,
            isCorrect = null,
            showNext = false,
            progress = progressText
        )
    }

    // timing the amount of time user learned from entering to exiting the lesson
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var startMs: Long? = null
    private var tickerJob: Job? = null
    private var committedMinutes: Int = 0
    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    fun onEnterLesson() {
        if (startMs != null) return
        startMs = System.currentTimeMillis()
        val start = startMs!!
        committedMinutes = 0
        _elapsedSeconds.value = 0

        tickerJob = scope.launch {
            while (isActive) {
                _elapsedSeconds.value = ((System.currentTimeMillis() - start) / 1000).toInt()
                delay(1000)
            }
        }
    }

    fun onExitLesson() {
        stopAndCommit()
        _elapsedSeconds.value = 0
        committedMinutes = 0
    }

    // call this on exit, and optionally when lesson completes
    fun commitMinutesIfAny() {
        val totalMinutes = _elapsedSeconds.value / 60
        val uncommitted = totalMinutes - committedMinutes
        if (uncommitted > 0) {
            model.addLearningMinutes(uncommitted)
            committedMinutes += uncommitted
        }
    }

    fun onCleared() {
        scope.cancel()
    }
}
