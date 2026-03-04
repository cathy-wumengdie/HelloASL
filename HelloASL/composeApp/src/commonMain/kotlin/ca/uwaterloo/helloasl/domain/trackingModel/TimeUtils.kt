package ca.uwaterloo.helloasl.domain.trackingModel

import kotlinx.datetime.*

object TimeUtils {
    // Week starts on Monday
    val weekStart: LocalDate
        get() {
            val diff = today().dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber
            return today().minus(DatePeriod(days = diff))
        }

    // Week ends on Sunday
    val weekEnd: LocalDate
        get() = weekStart.plus(DatePeriod(days = 6))

    /** Today */
    fun today(): LocalDate =
        Clock.System.todayIn(TimeZone.currentSystemDefault())

    /** Week start = Monday */
    fun weekStart(date: LocalDate): LocalDate {
        val diff = date.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber
        return date.minus(DatePeriod(days = diff))
    }

    /** Week end = Sunday */
    fun weekEnd(date: LocalDate): LocalDate =
        weekStart(date).plus(DatePeriod(days = 6))

    /** True if two dates are the same */
    fun isSameDate(a: LocalDate, b: LocalDate) = a == b

    /** True if two dates are in the same Monday-start week */
    fun isSameWeek(a: LocalDate, b: LocalDate): Boolean =
        weekStart(a) == weekStart(b)
}