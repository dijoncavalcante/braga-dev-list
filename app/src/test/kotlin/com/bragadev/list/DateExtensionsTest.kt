package com.bragadev.list

import com.bragadev.list.core.util.extensions.toBrDateString
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class DateExtensionsTest {

    @Test
    fun `UTC midnight millis are formatted as dd-MM-yyyy`() {
        assertEquals("10/10/2026", 1_791_590_400_000L.toBrDateString())
    }

    @Test
    fun `picked day is kept even when the device is behind UTC`() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Manaus"))
            assertEquals("10/10/2026", 1_791_590_400_000L.toBrDateString())
        } finally {
            TimeZone.setDefault(original)
        }
    }
}
