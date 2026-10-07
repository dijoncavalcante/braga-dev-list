package com.bragadev.fincheck.core.domain.model

/** How often an extra income arrives. */
sealed interface ExtraIncomeRecurrence {
    /** Every month on [dayOfMonth] (1..31; shorter months fall on their last day), e.g. a rent received. */
    data class Monthly(val dayOfMonth: Int) : ExtraIncomeRecurrence

    /** Only once, on [date], e.g. a freelance job or a refund. */
    data class Once(val date: CalendarDate) : ExtraIncomeRecurrence
}

/**
 * Money received besides the main income ("Outras entradas"). It adds to the cycle its date falls
 * in but never starts or ends a cycle: the cycles keep following the main pay days.
 *
 * @param id 0 for an extra income not saved yet.
 */
data class ExtraIncome(
    val id: Long = 0,
    val name: String,
    val amountInCents: Long,
    val recurrence: ExtraIncomeRecurrence,
) {
    /** Dates this income arrives between [from] and [to] (both included), oldest first. */
    fun datesBetween(from: CalendarDate, to: CalendarDate): List<CalendarDate> = when (recurrence) {
        is ExtraIncomeRecurrence.Once -> listOf(recurrence.date).filter { it in from..to }
        is ExtraIncomeRecurrence.Monthly ->
            generateSequence(from.firstDayOfMonthPlus(0)) { it.firstDayOfMonthPlus(1) }
                .takeWhile { it <= to }
                .map { CalendarDate.ofDayClamped(it.year, it.month, recurrence.dayOfMonth) }
                .filter { it in from..to }
                .toList()
    }
}

/** One arrival of an [ExtraIncome] inside a financial cycle. */
data class ExtraIncomeEntry(val income: ExtraIncome, val date: CalendarDate) {
    val amountInCents: Long get() = income.amountInCents
}
