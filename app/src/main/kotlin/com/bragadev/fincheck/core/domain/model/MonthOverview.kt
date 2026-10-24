package com.bragadev.fincheck.core.domain.model

/** Where a month stands compared with today. */
enum class MonthTiming { PAST, CURRENT, FUTURE }

/**
 * The money of one calendar month, for the "Meses" tab: what comes in during the month, its bills
 * and what should be left.
 *
 * @param payments the user's payments dated in the month; empty until the income is set up.
 * @param extraIncomes extra incomes dated in the month.
 */
data class MonthOverview(
    /** The month shown. */
    val month: YearMonth,
    /** Today's date, used to tell past, current and future months and overdue bills apart. */
    val today: CalendarDate,
    /** Bills of the month, as [BillBook.billsOf] returns them. */
    val bills: List<MonthBill>,
    val payments: List<Payment> = emptyList(),
    val extraIncomes: List<ExtraIncomeEntry> = emptyList(),
) {
    /** Whether the month is before, the same as or after the month of [today]. */
    val timing: MonthTiming
        get() {
            val current = YearMonth.of(today)
            return when {
                month < current -> MonthTiming.PAST
                month > current -> MonthTiming.FUTURE
                else -> MonthTiming.CURRENT
            }
        }

    /** Total, paid and unpaid amounts of the month's bills. */
    val totals: MonthBillsTotals get() = bills.totals()

    /** What comes in during the month: payments plus extra incomes, in cents. */
    val incomeInCents: Long get() = payments.sumOf { it.amountInCents } + extraIncomes.sumOf { it.amountInCents }

    /** Income of the month minus all its bills; negative = missing money. */
    val projectedBalanceInCents: Long get() = incomeInCents - totals.totalInCents

    /**
     * Bills whose due date has passed and are still not paid. A future month never has any: its
     * bills are only a forecast.
     */
    val overdueBills: List<MonthBill>
        get() = if (timing == MonthTiming.FUTURE) emptyList() else bills.filter { it.isOverdue(today) }

    /** Bills of the current month still to pay from today on. */
    val upcomingBills: List<MonthBill>
        get() =
            if (timing != MonthTiming.CURRENT) {
                emptyList()
            } else {
                bills.filter { !it.isPaid && it.dueDate?.let { date -> date >= today } == true }
            }
}

/** Builds the [MonthOverview] of [month]; [settings] is null until the user sets up their income. */
fun buildMonthOverview(
    month: YearMonth,
    today: CalendarDate,
    bills: BillBook,
    settings: IncomeSettings?,
    extraIncomes: List<ExtraIncome> = emptyList(),
): MonthOverview =
    MonthOverview(
        month = month,
        today = today,
        bills = bills.billsOf(month),
        payments = settings?.toSchedule()?.paymentsBetween(month.firstDay(), month.lastDay()).orEmpty(),
        extraIncomes = extraIncomes.entriesBetween(month.firstDay(), month.lastDay()),
    )
