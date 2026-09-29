package com.bragadev.list

import com.bragadev.list.core.util.extensions.dueDayIn
import kotlin.test.Test
import kotlin.test.assertEquals

class DateExtensionsTest {

    @Test
    fun `due day that exists in the month is kept`() {
        assertEquals(10, 10.dueDayIn(year = 2026, month = 2))
        assertEquals(31, 31.dueDayIn(year = 2026, month = 12))
    }

    @Test
    fun `due day past the end of a short month falls on its last day`() {
        assertEquals(30, 31.dueDayIn(year = 2026, month = 4))
        assertEquals(28, 31.dueDayIn(year = 2026, month = 2))
        assertEquals(28, 30.dueDayIn(year = 2026, month = 2))
    }

    @Test
    fun `leap year february ends on day 29`() {
        assertEquals(29, 31.dueDayIn(year = 2028, month = 2))
    }
}
