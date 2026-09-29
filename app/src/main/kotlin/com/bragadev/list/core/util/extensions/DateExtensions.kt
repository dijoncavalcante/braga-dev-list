package com.bragadev.list.core.util.extensions

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Formats a date stored as UTC midnight epoch millis (the Material DatePicker
 * format) as "dd/MM/yyyy". Formatting in UTC keeps the day the user picked,
 * whatever the device time zone is (e.g. America/Manaus, UTC-4).
 *
 * SimpleDateFormat is used on purpose instead of java.time: minSdk is 24 and
 * java.time needs API 26 (or core library desugaring).
 */
fun Long.toBrDateString(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(this)
}
