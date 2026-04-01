package ca.uwaterloo.helloasl.ui.screens.learning

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.learningModel.ASLSign
import ca.uwaterloo.helloasl.domain.learningModel.QuizChoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LessonViewModel(private val model: Model) {
    var state by mutableStateOf(LessonUIState())
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var signs: List<ASLSign> = emptyList()
    private var quizChoices: Map<Long, List<QuizChoice>> = emptyMap()
    private var viewingIndex: Int = 0
    private var quizIndex: Int = 0
    private var videoIndex: Int = 0
    private var lessonId: Long? = null
    private var onLessonCompleted: ((Long) -> Unit)? = null

    // timing the amount of time user learned from entering to exiting the lesson
    private var startMs: Long? = null
    private var tickerJob: Job? = null
    private var committedMinutes: Int = 0
    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private fun stopAndCommit() {
        if (startMs == null) return
        tickerJob?.cancel()
        tickerJob = null
        commitMinutesIfAny()
        startMs = null
    }

    fun setOnLessonCompleted(listener: (Long) -> Unit) {
        onLessonCompleted = listener
    }

    fun onChoose(option: String) {
        if (state.phase != LessonPhase.QUIZ) return

        val sign = signs.getOrNull(quizIndex) ?: return
        val correctChoice = quizChoices[sign.signId]
            ?.firstOrNull { it.isCorrect }
            ?.choiceText
            ?: sign.gloss

        val correct = correctChoice == option
        val isLast = quizIndex == signs.lastIndex
        val completed = correct && isLast

        if (completed) {
            stopAndCommit()
            _elapsedSeconds.value = 0
            committedMinutes = 0

            val completedLessonId = lessonId ?: return
            scope.launch {
                model.onLessonCompleted(completedLessonId)
                onLessonCompleted?.invoke(completedLessonId)
            }
        }

        state = state.copy(
            selected = option,
            isCorrect = correct,
            showNext = correct && !isLast
        )
    }

    fun onNext() {
        if (state.phase != LessonPhase.QUIZ) return
        if (signs.isEmpty()) return

        if (quizIndex < signs.lastIndex) {
            quizIndex += 1
            rebuildQuiz()
        }
    }

    fun onPrevSign() {
        if (state.phase != LessonPhase.VIEWING) return
        if (viewingIndex > 0) {
            viewingIndex -= 1
            videoIndex = 0
            rebuildViewing()
        }
    }

    fun onNextSign() {
        if (state.phase != LessonPhase.VIEWING) return
        if (viewingIndex < signs.lastIndex) {
            viewingIndex += 1
            videoIndex = 0
            rebuildViewing()
        }
    }

    fun onPrevVideo() {
        if (state.phase != LessonPhase.VIEWING) return
        if (videoIndex > 0) {
            videoIndex -= 1
            rebuildViewing()
        }
    }

    fun onNextVideo() {
        if (state.phase != LessonPhase.VIEWING) return
        val sign = signs.getOrNull(viewingIndex) ?: return
        if (sign.videoUrl2 != null && videoIndex == 0) {
            videoIndex = 1
            rebuildViewing()
        }
    }

    fun onStartQuiz() {
        if (state.phase == LessonPhase.QUIZ) return
        quizIndex = 0
        state = state.copy(phase = LessonPhase.QUIZ)
        rebuildQuiz()
    }

    fun refreshCurrentStarState() {
        val sign = signs.getOrNull(viewingIndex) ?: return
        state = state.copy(isStarred = model.isStarred(sign.signId))
    }

    fun onStar() {
        val sign = signs.getOrNull(viewingIndex) ?: return

        scope.launch {
            if (model.isStarred(sign.signId)) {
                model.toggleStar(sign.signId, 0L)
                refreshCurrentStarState()
            } else {
                showStarPopup(sign.signId)
                model.requestStarWithTag(sign.signId)
            }
        }
    }

    fun showStarPopup(signId: Long) {
        state = state.copy(
            isStarPopupVisible = true,
            pendingStarSignId = signId
        )
    }

    fun dismissStarPopup() {
        state = state.copy(
            isStarPopupVisible = false,
            pendingStarSignId = null
        )
        refreshCurrentStarState()
    }

    fun loadLesson(lessonId: Long) {
        this.lessonId = lessonId
        scope.launch {
            val loadedSigns = model.getSignsForLesson(lessonId)

            model.loadStarredFromRepo()

            val loadedChoices = model.getQuizChoicesForSigns(
                loadedSigns.map { it.signId }
            )

            signs = loadedSigns
            quizChoices = loadedChoices
            viewingIndex = 0
            quizIndex = 0
            videoIndex = 0

            val title = model.getLesson(lessonId).title
            state = state.copy(
                title = title,
                phase = LessonPhase.VIEWING,
                isStarPopupVisible = false,
                pendingStarSignId = null
            )
            rebuildViewing()
        }
    }

    private fun rebuildViewing() {
        val sign = signs.getOrNull(viewingIndex)
        if (sign == null) {
            state = state.copy(
                signIndex = 0,
                signTotal = 0,
                signGloss = "",
                videoUrl = null,
                canPrevSign = false,
                canNextSign = false,
                canPrevVideo = false,
                canNextVideo = false,
                options = emptyList(),
                selected = null,
                isCorrect = null,
                showNext = false,
                showStartQuiz = false,
                progress = ""
            )
            return
        }
        val isStarred = model.isStarred(sign.signId)
        val videoUrl = if (videoIndex == 1) sign.videoUrl2 ?: sign.videoUrl1 else sign.videoUrl1
        val hasAltVideo = sign.videoUrl2 != null
        val progressText = "Sign ${viewingIndex + 1}/${signs.size}"

        state = state.copy(
            phase = LessonPhase.VIEWING,
            signIndex = viewingIndex,
            signTotal = signs.size,
            signGloss = sign.gloss,
            videoUrl = videoUrl,
            canPrevSign = viewingIndex > 0,
            canNextSign = viewingIndex < signs.lastIndex,
            canPrevVideo = hasAltVideo && videoIndex == 1,
            canNextVideo = hasAltVideo && videoIndex == 0,
            options = emptyList(),
            selected = null,
            isCorrect = null,
            showNext = false,
            showStartQuiz = viewingIndex == signs.lastIndex,
            progress = progressText,
            isStarred = isStarred
        )
    }

    private fun rebuildQuiz() {
        val sign = signs.getOrNull(quizIndex)
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

        val choices = quizChoices[sign.signId].orEmpty()
        val optionTexts = if (choices.isNotEmpty()) {
            choices.map { it.choiceText }
        } else {
            val fallback = signs.map { it.gloss }.distinct().toMutableList()
            if (!fallback.contains(sign.gloss)) fallback.add(sign.gloss)
            fallback.take(3)
        }

        val progressText = "Quiz ${quizIndex + 1}/${signs.size}"

        state = state.copy(
            phase = LessonPhase.QUIZ,
            signIndex = quizIndex,
            signTotal = signs.size,
            signGloss = "",
            videoUrl = sign.videoUrl1,
            canPrevSign = false,
            canNextSign = false,
            canPrevVideo = false,
            canNextVideo = false,
            options = optionTexts,
            selected = null,
            isCorrect = null,
            showNext = false,
            showStartQuiz = false,
            progress = progressText
        )
    }

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

    fun commitMinutesIfAny() {
        val totalMinutes = _elapsedSeconds.value / 60
        val uncommitted = totalMinutes - committedMinutes
        if (uncommitted > 0) {
            scope.launch {
                model.addLearningMinutes(uncommitted)
            }
            committedMinutes += uncommitted
        }
    }

    fun onCleared() {
        scope.cancel()
    }
}