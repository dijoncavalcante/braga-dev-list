package com.bragadev.fincheck.core.util.extensions

import java.util.Calendar

/** Smallest due day a bill can have. */
const val MIN_DUE_DAY = 1

/** Largest due day a bill can have. */
const val MAX_DUE_DAY = 31

/**
 * Resolves this due day (1..31) to the actual day the bill falls due in the given month.
 *
 * Months shorter than the due day fall due on their last day, as banks usually do:
 * day 31 -> 30/04, 28/02 (or 29/02 in leap years); day 30 -> 28/02.
 *
 * Uses [Calendar] on purpose instead of java.time: minSdk is 24 and java.time needs API 26.
 *
 * @param year e.g. 2026
 * @param month 1 = January ... 12 = December
 */
fun Int.dueDayIn(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, 1)
    }
    return coerceIn(MIN_DUE_DAY, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
}
