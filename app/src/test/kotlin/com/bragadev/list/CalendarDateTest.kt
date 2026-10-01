package com.bragadev.list

import com.bragadev.list.core.domain.model.CalendarDate
import com.bragadev.list.core.domain.model.daysInMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalendarDateTest {

    @Test
    fun `epoch day matches the calendar`() {
        assertEquals(0, CalendarDate(1970, 1, 1).toEpochDay())
        assertEquals(20_727, CalendarDate(2026, 10, 1).toEpochDay())
        assertEquals(CalendarDate(2028, 2, 29), CalendarDate.fromEpochDay(CalendarDate(2028, 2, 29).toEpochDay()))
    }

    @Test
    fun `every day of several years survives the round trip to epoch day`() {
        var date = CalendarDate(2023, 12, 25)
        repeat(3 * 366) {
            assertEquals(date, CalendarDate.fromEpochDay(date.toEpochDay()))
            date = date.plusDays(1)
        }
    }

    @Test
    fun `adding days crosses months, years and leap days`() {
        assertEquals(CalendarDate(2026, 10, 9), CalendarDate(2026, 9, 25).plusDays(14))
        assertEquals(CalendarDate(2027, 1, 9), CalendarDate(2026, 12, 25).plusDays(15))
        assertEquals(CalendarDate(2028, 2, 29), CalendarDate(2028, 2, 28).plusDays(1))
        assertEquals(CalendarDate(2026, 3, 1), CalendarDate(2026, 2, 28).plusDays(1))
        assertEquals(9, CalendarDate(2026, 10, 1).daysUntil(CalendarDate(2026, 10, 10)))
    }

    @Test
    fun `month lengths`() {
        assertEquals(28, daysInMonth(2026, 2))
        assertEquals(29, daysInMonth(2028, 2))
        assertEquals(28, daysInMonth(2100, 2))
        assertEquals(29, daysInMonth(2000, 2))
        assertEquals(30, daysInMonth(2026, 9))
        assertEquals(31, daysInMonth(2026, 10))
    }

    @Test
    fun `a day that does not exist in the month falls on its last day`() {
        assertEquals(CalendarDate(2026, 2, 28), CalendarDate.ofDayClamped(2026, 2, 31))
        assertEquals(CalendarDate(2026, 4, 30), CalendarDate.ofDayClamped(2026, 4, 31))
        assertEquals(CalendarDate(2028, 2, 29), CalendarDate.ofDayClamped(2028, 2, 30))
        assertFailsWith<IllegalArgumentException> { CalendarDate(2026, 2, 30) }
    }

    @Test
    fun `short formats`() {
        assertEquals("05/10", CalendarDate(2026, 10, 5).toDayMonth())
        assertEquals("05/10/2026", CalendarDate(2026, 10, 5).toDayMonthYear())
    }
}
