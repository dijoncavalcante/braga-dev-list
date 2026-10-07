package com.bragadev.fincheck.core.util.extensions

import java.text.NumberFormat
import java.util.Locale

private val brlLocale: Locale = Locale.forLanguageTag("pt-BR")

/** Max digits accepted in a currency input (R$ 9.999.999.999,99). */
private const val MAX_CURRENCY_DIGITS = 12

/**
 * Formats a value in cents as Brazilian Real, e.g. 1250 -> "R$ 12,50".
 */
fun Long.toBrlCurrency(): String {
    val formatter = NumberFormat.getCurrencyInstance(brlLocale)
    return formatter.format(this / 100.0).replace(' ', ' ')
}

/**
 * Turns whatever the user typed in a currency field into cents, keeping only the
 * digits: "R$ 12,50" -> 1250, "R$ 12,505" -> 12505 (typing one more digit shifts
 * the value left, like a cash register).
 */
fun String.currencyInputToCents(): Long =
    filter { it.isDigit() }.take(MAX_CURRENCY_DIGITS).toLongOrNull() ?: 0L
