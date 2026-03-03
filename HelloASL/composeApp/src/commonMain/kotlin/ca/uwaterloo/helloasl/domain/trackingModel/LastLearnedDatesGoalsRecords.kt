package ca.uwaterloo.helloasl.domain.trackingModel

import kotlinx.datetime.LocalDate

// This class should be updated at EOD
class LastLearnedDatesGoalsRecords (
    val userId: Int,
    val lastLearnedDate: LocalDate,
    val isDailyGoalCompleted: Boolean,  // if the daily goal is completed on the lastLearnedDate
    val learnedDatesCountInWeek: Int,   // in the week of lastLearnedDate, the number of days completed daily goals
    val isWeeklyGoalCompleted: Boolean, // if the weekly goal is completed in the week of lastLearnedDate
)