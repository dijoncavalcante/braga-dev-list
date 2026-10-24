package com.bragadev.fincheck.core.domain.model

/** Percentages are calculated on a 0..100 scale. */
private const val PERCENT = 100

/**
 * One half of a month in the "Quinzenas" view: days 1–15 or 16 to the end of the month.
 *
 * Its balance carries what was left (or missing) from the fortnight before it, so a 2ª quinzena
 * that pays a big bill with money received on the 5th is not shown as negative while the month
 * is fine.
 *
 * @param bills bills of the month whose due day falls in the fortnight.
 * @param payments payments dated in the fortnight.
 * @param extraIncomes extra incomes dated in the fortnight.
 * @param carriedOverInCents balance of the fortnight before it (0 for the 1ª quinzena); negative when
 * money was missing.
 */
data class Fortnight(
    /** First day of the fortnight. */
    val startDate: CalendarDate,
    /** Last day of the fortnight. */
    val endDate: CalendarDate,
    val bills: List<MonthBill>,
    val payments: List<Payment> = emptyList(),
    val extraIncomes: List<ExtraIncomeEntry> = emptyList(),
    val carriedOverInCents: Long = 0,
) {
    /** What comes in during the fortnight: its payments plus its extra incomes, in cents. */
    val incomeInCents: Long get() = payments.sumOf { it.amountInCents } + extraIncomes.sumOf { it.amountInCents }

    /** Total, paid and unpaid amounts of its bills. */
    val totals: MonthBillsTotals get() = bills.totals()

    /** What is left at the end of the fortnight once its bills are paid; negative = missing money. */
    val balanceInCents: Long get() = carriedOverInCents + incomeInCents - totals.totalInCents

    /** Money the fortnight can count on: what it receives plus what was left from the one before. */
    private val availableInCents: Long get() = incomeInCents + carriedOverInCents.coerceAtLeast(0)

    /** Share of the available money taken by the bills (may pass 100); null when there is none. */
    val committedPercent: Int?
        get() = if (availableInCents > 0) (totals.totalInCents * PERCENT / availableInCents).toInt() else null

    /** Over budget when the balance is negative, tight from [TIGHT_CYCLE_PERCENT] of the available money. */
    val status: CycleStatus
        get() = when {
            balanceInCents < 0 -> CycleStatus.OVER_BUDGET
            totals.totalInCents * PERCENT >= availableInCents * TIGHT_CYCLE_PERCENT -> CycleStatus.TIGHT
            else -> CycleStatus.HEALTHY
        }

    /** Whether [date] falls in the fortnight. */
    operator fun contains(date: CalendarDate): Boolean = date in startDate..endDate
}

/**
 * The month split in its two fortnights, for the top of the "Quinzenas" view.
 *
 * @param otherExpenses bills of the month without a due day: they belong to no fortnight, so they
 * only count in the month's balance.
 */
data class FortnightOverview(
    /** The month being split. */
    val overview: MonthOverview,
    /** Days 1–15. */
    val first: Fortnight,
    /** Day 16 to the end of the month, carrying the balance of [first]. */
    val second: Fortnight,
    val otherExpenses: List<MonthBill>,
) {
    /** Sum of [otherExpenses], in cents. */
    val otherExpensesInCents: Long get() = otherExpenses.sumOf { it.amountInCents }

    /** What should be left at the end of the month: the 2ª quinzena already carries the 1ª. */
    val projectedBalanceInCents: Long get() = second.balanceInCents - otherExpensesInCents

    /** The fortnight today is in; null outside the current month. */
    val current: Fortnight?
        get() = if (overview.timing != MonthTiming.CURRENT) null else listOf(first, second).first { overview.today in it }

    /** Bills of the month already due and not paid; never in a future month. */
    val overdueBills: List<MonthBill> get() = overview.overdueBills
}

/** Splits this month in its two fortnights, carrying the balance of the 1ª into the 2ª. */
fun MonthOverview.toFortnights(): FortnightOverview {
    val firstEnd = month.dayClamped(FIRST_FORTNIGHT_LAST_DAY)
    val secondStart = firstEnd.plusDays(1)
    val (withDueDate, withoutDueDate) = bills.partition { it.dueDate != null }
    val first = Fortnight(
        startDate = month.firstDay(),
        endDate = firstEnd,
        bills = withDueDate.filter { it.dueDate!! <= firstEnd },
        payments = payments.filter { it.date <= firstEnd },
        extraIncomes = extraIncomes.filter { it.date <= firstEnd },
    )
    val second = Fortnight(
        startDate = secondStart,
        endDate = month.lastDay(),
        bills = withDueDate.filter { it.dueDate!! > firstEnd },
        payments = payments.filter { it.date > firstEnd },
        extraIncomes = extraIncomes.filter { it.date > firstEnd },
        carriedOverInCents = first.balanceInCents,
    )
    return FortnightOverview(overview = this, first = first, second = second, otherExpenses = withoutDueDate)
}
