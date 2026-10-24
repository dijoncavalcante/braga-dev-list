@file:Suppress("MagicNumber") // calendar arithmetic: the numbers are the calendar itself

package com.bragadev.fincheck.core.domain.model

/** Months shown ahead in the months strip, the current one included. */
const val MONTHS_AHEAD_SHOWN = 12

/**
 * A month of a year, e.g. outubro de 2026: what the "Meses" tab shows and what a bill belongs to.
 *
 * Pure Kotlin for the same reason as [CalendarDate]. It is stored as [index] (months since year 0),
 * which keeps "plus N months" and "months between" exact across years.
 *
 * @param month 1 = January ... 12 = December
 */
data class YearMonth(
    val year: Int,
    val month: Int,
) : Comparable<YearMonth> {
    init {
        require(month in 1..12) { "Invalid month: $month" }
    }

    /** Months since January of year 0; what the database stores. */
    val index: Int get() = year * 12 + (month - 1)

    fun plusMonths(months: Int): YearMonth = fromIndex(index + months)

    /** Months from this one until [other]; negative when [other] is before. */
    fun monthsUntil(other: YearMonth): Int = other.index - index

    fun firstDay(): CalendarDate = CalendarDate(year, month, 1)

    fun lastDay(): CalendarDate = CalendarDate(year, month, daysInMonth(year, month))

    operator fun contains(date: CalendarDate): Boolean = date.year == year && date.month == month

    /** [dayOfMonth] (1..31) in this month; shorter months fall on their last day. */
    fun dayClamped(dayOfMonth: Int): CalendarDate = CalendarDate.ofDayClamped(year, month, dayOfMonth)

    override fun compareTo(other: YearMonth): Int = index.compareTo(other.index)

    companion object {
        fun fromIndex(index: Int): YearMonth = YearMonth(Math.floorDiv(index, 12), Math.floorMod(index, 12) + 1)

        fun of(date: CalendarDate): YearMonth = YearMonth(date.year, date.month)
    }
}

/** Every month from this one to [last] (both included), in order. */
fun YearMonth.monthsTo(last: YearMonth): List<YearMonth> = (index..last.index).map(YearMonth::fromIndex)

/** The [count] months that start at this one: out/2026, nov/2026, dez/2026, jan/2027... */
fun YearMonth.nextMonths(count: Int = MONTHS_AHEAD_SHOWN): List<YearMonth> = (0 until count).map(::plusMonths)
