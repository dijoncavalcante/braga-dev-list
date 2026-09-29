package com.bragadev.list

import com.bragadev.list.core.util.extensions.currencyInputToCents
import com.bragadev.list.core.util.extensions.toBrlCurrency
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyExtensionsTest {

    @Test
    fun `cents are formatted as Brazilian Real`() {
        assertEquals("R$ 0,00", 0L.toBrlCurrency())
        assertEquals("R$ 12,50", 1_250L.toBrlCurrency())
        assertEquals("R$ 1.234,56", 123_456L.toBrlCurrency())
    }

    @Test
    fun `typing a digit shifts the value like a cash register`() {
        assertEquals(5, "R$ 0,005".currencyInputToCents())
        assertEquals(1_250, "R$ 1,250".currencyInputToCents())
    }

    @Test
    fun `deleting a digit shifts the value back`() {
        assertEquals(125, "R$ 12,5".currencyInputToCents())
        assertEquals(0, "".currencyInputToCents())
    }
}
