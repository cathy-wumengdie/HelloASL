package ca.uwaterloo.helloasl.domain.trackingModel

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.data.notificationRepository.NoOpNotificationRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TrackingModelTest {

    private fun makeModel(): Pair<MockDB, Model> {
        val db = MockDB()
        val repos = Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db),
            notification = NoOpNotificationRepository
        )
        val model = Model(
            repos = repos,
            ioDispatcher = StandardTestDispatcher()
        )
        return db to model
    }

    private suspend fun makeLoggedInModel(): Pair<MockDB, Model> {
        val (db, model) = makeModel()
        model.login(email = "yanjin@gmail.com", password = "1234")
        return db to model
    }

    @Test
    fun dailyProgress_isDailyGoalMet_trueWhenMinutesMeetGoal() {
        val progress = DailyProgress(
            minutesLearned = 10,
            lastDailyGoalCompletedDate = null,
            dailyGoalMinutes = 10
        )

        assertTrue(progress.isDailyGoalMet)
    }

    @Test
    fun dailyProgress_isDailyGoalMet_trueWhenMinutesExceedGoal() {
        val progress = DailyProgress(
            minutesLearned = 15,
            lastDailyGoalCompletedDate = null,
            dailyGoalMinutes = 10
        )

        assertTrue(progress.isDailyGoalMet)
    }

    @Test
    fun dailyProgress_isDailyGoalMet_falseWhenMinutesBelowGoal() {
        val progress = DailyProgress(
            minutesLearned = 8,
            lastDailyGoalCompletedDate = null,
            dailyGoalMinutes = 10
        )

        assertFalse(progress.isDailyGoalMet)
    }

    @Test
    fun dailyProgress_isDailyGoalMet_falseWhenGoalIsZero() {
        val progress = DailyProgress(
            minutesLearned = 10,
            lastDailyGoalCompletedDate = null,
            dailyGoalMinutes = 0
        )

        assertFalse(progress.isDailyGoalMet)
    }

    @Test
    fun weeklyProgress_storesValuesCorrectly() {
        val date = LocalDate(2026, 3, 16)
        val progress = WeeklyProgress(
            daysCompleted = 2,
            lastCreditedDate = date,
            weeklyGoalDays = 5
        )

        assertEquals(2, progress.daysCompleted)
        assertEquals(date, progress.lastCreditedDate)
        assertEquals(5, progress.weeklyGoalDays)
    }

    @Test
    fun updateDayStreak_returnsUnchangedWhenGoalNotCompleted() {
        val today = LocalDate(2026, 3, 17)
        val lastDate = LocalDate(2026, 3, 16)

        val result = updateDayStreak(
            currentStreak = 4,
            lastDailyGoalCompletedDate = lastDate,
            today = today,
            isDailyGoalCompleted = false
        )

        assertEquals(4, result.first)
        assertEquals(lastDate, result.second)
    }

    @Test
    fun updateDayStreak_returnsUnchangedWhenAlreadyRecordedToday() {
        val today = LocalDate(2026, 3, 17)

        val result = updateDayStreak(
            currentStreak = 5,
            lastDailyGoalCompletedDate = today,
            today = today,
            isDailyGoalCompleted = true
        )

        assertEquals(5, result.first)
        assertEquals(today, result.second)
    }

    @Test
    fun updateDayStreak_setsStreakToOneWhenFirstCompletion() {
        val today = LocalDate(2026, 3, 17)

        val result = updateDayStreak(
            currentStreak = 0,
            lastDailyGoalCompletedDate = null,
            today = today,
            isDailyGoalCompleted = true
        )

        assertEquals(1, result.first)
        assertEquals(today, result.second)
    }

    @Test
    fun updateDayStreak_incrementsWhenLastCompletionWasYesterday() {
        val today = LocalDate(2026, 3, 17)
        val yesterday = LocalDate(2026, 3, 16)

        val result = updateDayStreak(
            currentStreak = 3,
            lastDailyGoalCompletedDate = yesterday,
            today = today,
            isDailyGoalCompleted = true
        )

        assertEquals(4, result.first)
        assertEquals(today, result.second)
    }

    @Test
    fun updateDayStreak_resetsToOneWhenLastCompletionWasEarlierThanYesterday() {
        val today = LocalDate(2026, 3, 17)
        val olderDate = LocalDate(2026, 3, 14)

        val result = updateDayStreak(
            currentStreak = 6,
            lastDailyGoalCompletedDate = olderDate,
            today = today,
            isDailyGoalCompleted = true
        )

        assertEquals(1, result.first)
        assertEquals(today, result.second)
    }

    @Test
    fun progressSummary_storesAllValuesCorrectly() {
        val date = LocalDate(2026, 3, 17)
        val daily = DailyProgress(
            minutesLearned = 8,
            lastDailyGoalCompletedDate = LocalDate(2026, 3, 16),
            dailyGoalMinutes = 10
        )
        val weekly = WeeklyProgress(
            daysCompleted = 2,
            lastCreditedDate = LocalDate(2026, 3, 16),
            weeklyGoalDays = 4
        )

        val summary = ProgressSummary(
            userId = "user-123",
            date = date,
            dailyProgress = daily,
            weeklyProgress = weekly,
            dayStreak = 3
        )

        assertEquals("user-123", summary.userId)
        assertEquals(date, summary.date)
        assertEquals(daily, summary.dailyProgress)
        assertEquals(weekly, summary.weeklyProgress)
        assertEquals(3, summary.dayStreak)
    }

    @Test
    fun model_getProgressSummary_returnsCurrentSummary() = runTest {
        val (_, model) = makeLoggedInModel()

        val summary = model.getProgressSummary()

        assertNotNull(summary)
        assertTrue(summary.userId.isNotBlank())
        assertTrue(summary.dailyProgress.dailyGoalMinutes >= 0)
        assertTrue(summary.weeklyProgress.weeklyGoalDays >= 0)
        assertTrue(summary.dayStreak >= 0)
    }

    @Test
    fun model_addLearningMinutes_increasesMinutesLearned() = runTest {
        val (_, model) = makeLoggedInModel()

        val before = model.getProgressSummary()
        val after = model.addLearningMinutes(5)

        assertEquals(before.dailyProgress.minutesLearned + 5, after.dailyProgress.minutesLearned)
    }

    @Test
    fun model_setLearningGoals_updatesGoalsInSummary() = runTest {
        val (_, model) = makeLoggedInModel()

        model.setLearningGoals(minutesPerDay = 25, daysPerWeek = 6)
        val summary = model.getProgressSummary()

        assertEquals(25, summary.dailyProgress.dailyGoalMinutes)
        assertEquals(6, summary.weeklyProgress.weeklyGoalDays)
    }

    @Test
    fun model_addLearningMinutes_canMakeDailyGoalMet() = runTest {
        val (_, model) = makeLoggedInModel()

        val initial = model.getProgressSummary()
        val currentMinutes = initial.dailyProgress.minutesLearned
        val weeklyGoalDays = initial.weeklyProgress.weeklyGoalDays

        model.setLearningGoals(
            minutesPerDay = currentMinutes + 10,
            daysPerWeek = weeklyGoalDays
        )

        val before = model.getProgressSummary()
        assertFalse(before.dailyProgress.isDailyGoalMet)

        val after = model.addLearningMinutes(10)

        assertTrue(after.dailyProgress.isDailyGoalMet)
    }

    @Test
    fun model_addLearningMinutes_setsLastDailyGoalCompletedDateWhenGoalReached() = runTest {
        val (_, model) = makeLoggedInModel()

        model.setLearningGoals(minutesPerDay = 10, daysPerWeek = 3)
        val updated = model.addLearningMinutes(10)

        assertNotNull(updated.dailyProgress.lastDailyGoalCompletedDate)
    }

    @Test
    fun model_addLearningMinutes_doesNotDecreaseDayStreakWhenGoalReached() = runTest {
        val (_, model) = makeLoggedInModel()

        model.setLearningGoals(minutesPerDay = 10, daysPerWeek = 3)

        val before = model.getProgressSummary()
        val after = model.addLearningMinutes(10)

        assertTrue(after.dayStreak >= before.dayStreak)
    }

    @Test
    fun model_getUserLearningProgress_returnsProgress() = runTest {
        val (_, model) = makeLoggedInModel()

        val progress = model.getUserLearningProgress()

        assertNotNull(progress.lessonId)
        assertTrue(progress.lessonId > 0)
        assertTrue(progress.wordsLearned >= 0)
    }

    @Test
    fun model_setLearningGoals_persistAcrossReads() = runTest {
        val (_, model) = makeLoggedInModel()

        val returned = model.setLearningGoals(minutesPerDay = 15, daysPerWeek = 4)

        val first = model.getProgressSummary()
        val second = model.getProgressSummary()

        assertEquals(15, returned.dailyProgress.dailyGoalMinutes)
        assertEquals(4, returned.weeklyProgress.weeklyGoalDays)

        assertEquals(15, first.dailyProgress.dailyGoalMinutes)
        assertEquals(4, first.weeklyProgress.weeklyGoalDays)
        assertEquals(15, second.dailyProgress.dailyGoalMinutes)
        assertEquals(4, second.weeklyProgress.weeklyGoalDays)
    }
}