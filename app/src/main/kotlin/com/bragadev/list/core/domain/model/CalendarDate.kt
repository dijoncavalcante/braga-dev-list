@file:Suppress("MagicNumber") // calendar arithmetic: the numbers are the calendar itself

package com.bragadev.list.core.domain.model

import java.util.Calendar

/**
 * A day of the calendar (no time, no time zone), e.g. 25/09/2026.
 *
 * Pure Kotlin on purpose: minSdk is 24 and java.time needs API 26, and keeping it free of
 * [Calendar] lets the financial cycle rules be unit tested on the JVM. Dates are converted
 * to and from an epoch day (days since 01/01/1970), which makes "plus N days" and "days
 * between" exact across month and year boundaries.
 *
 * @param month 1 = January ... 12 = December
 */
data class CalendarDate(val year: Int, val month: Int, val day: Int) : Comparable<CalendarDate> {

    init {
        require(month in 1..12) { "Invalid month: $month" }
        require(day in 1..daysInMonth(year, month)) { "Invalid day: $year-$month-$day" }
    }

    /** Days since 01/01/1970 (civil-from-days algorithm by Howard Hinnant). */
    fun toEpochDay(): Long {
        val y = (if (month <= 2) year - 1 else year).toLong()
        val era = Math.floorDiv(y, 400L)
        val yearOfEra = y - era * 400
        val monthFromMarch = (month + 9) % 12
        val dayOfYear = (153 * monthFromMarch + 2) / 5 + day - 1
        val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
        return era * DAYS_PER_ERA + dayOfEra - EPOCH_SHIFT
    }

    fun plusDays(days: Long): CalendarDate = fromEpochDay(toEpochDay() + days)

    /** Days from this date until [other]; negative when [other] is before. */
    fun daysUntil(other: CalendarDate): Long = other.toEpochDay() - toEpochDay()

    /** First day of the month [months] months away (negative = before). */
    fun firstDayOfMonthPlus(months: Int): CalendarDate {
        val index = year * 12 + (month - 1) + months
        return CalendarDate(Math.floorDiv(index, 12), Math.floorMod(index, 12) + 1, 1)
    }

    override fun compareTo(other: CalendarDate): Int = toEpochDay().compareTo(other.toEpochDay())

    /** "25/09". */
    fun toDayMonth(): String = "%02d/%02d".format(day, month)

    /** "25/09/2026". */
    fun toDayMonthYear(): String = "%02d/%02d/%04d".format(day, month, year)

    companion object {
        private const val DAYS_PER_ERA = 146_097L
        private const val EPOCH_SHIFT = 719_468L

        fun fromEpochDay(epochDay: Long): CalendarDate {
            val shifted = epochDay + EPOCH_SHIFT
            val era = Math.floorDiv(shifted, DAYS_PER_ERA)
            val dayOfEra = shifted - era * DAYS_PER_ERA
            val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
            val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
            val monthFromMarch = (5 * dayOfYear + 2) / 153
            val day = dayOfYear - (153 * monthFromMarch + 2) / 5 + 1
            val month = if (monthFromMarch < 10) monthFromMarch + 3 else monthFromMarch - 9
            val year = yearOfEra + era * 400 + if (month <= 2) 1 else 0
            return CalendarDate(year.toInt(), month.toInt(), day.toInt())
        }

        /**
         * The [dayOfMonth] (1..31) in the given month. Months shorter than it fall on their
         * last day, as banks usually do: day 31 -> 30/04, 28/02 (or 29/02 in leap years).
         */
        fun ofDayClamped(year: Int, month: Int, dayOfMonth: Int): CalendarDate =
            CalendarDate(year, month, dayOfMonth.coerceIn(1, daysInMonth(year, month)))

        /** Today on the device's clock and time zone. */
        fun today(): CalendarDate {
            val calendar = Calendar.getInstance()
            return CalendarDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                day = calendar.get(Calendar.DAY_OF_MONTH),
            )
        }
    }
}

/** Number of days of the month: 28/29 for February, 30 or 31 for the others. */
fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}
