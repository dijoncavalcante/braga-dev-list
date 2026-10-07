package com.bragadev.fincheck.core.domain.model

import kotlin.math.abs

/** Smallest day of the month a payment can be set to. */
const val MIN_PAY_DAY = 1

/** Largest day of the month a payment can be set to; shorter months fall on their last day. */
const val MAX_PAY_DAY = 31

/**
 * How often the user receives their income. Each frequency knows how to build its
 * [IncomeSchedule]; new ones (weekly, every 14 days, custom dates...) are added here
 * without touching how cycles, totals and feedbacks are calculated.
 *
 * [code] is what is stored in the database; keep it stable when adding new frequencies.
 */
enum class IncomeFrequency(val code: Int, val paymentsPerMonth: Int) {
    MONTHLY(code = 0, paymentsPerMonth = 1),

    /** Two payments a month, on any two days the user chooses, each with its own amount. */
    TWICE_A_MONTH(code = 1, paymentsPerMonth = 2),
    ;

    companion object {
        fun fromCode(code: Int): IncomeFrequency = entries.firstOrNull { it.code == code } ?: MONTHLY
    }
}

/** One recurring payment of the month: "dia 10 — R$ 2.000". */
data class PayDay(
    /** 1..31; in months shorter than it the payment falls on the last day of the month. */
    val dayOfMonth: Int,
    val amountInCents: Long,
)

/**
 * What the user told the app about their income.
 *
 * @param payDays one entry for [IncomeFrequency.MONTHLY], two for [IncomeFrequency.TWICE_A_MONTH];
 * amounts are independent, never assumed equal.
 * @param nextPaymentDate the date the user said the next payment arrives. It may differ from the
 * usual day (e.g. day 5 fell on a Sunday and the money arrives on the 6th), so it replaces the
 * scheduled payment closest to it. See [DayOfMonthSchedule].
 */
data class IncomeSettings(
    val frequency: IncomeFrequency,
    val payDays: List<PayDay>,
    val nextPaymentDate: CalendarDate,
) {
    fun toSchedule(): IncomeSchedule = when (frequency) {
        IncomeFrequency.MONTHLY, IncomeFrequency.TWICE_A_MONTH -> DayOfMonthSchedule(payDays, nextPaymentDate)
    }
}

/** Money received on a given date. */
data class Payment(val date: CalendarDate, val amountInCents: Long)

/**
 * Turns the income settings into actual payments on the calendar. This is the only thing the
 * financial cycles need to know about the income, so any frequency can feed them.
 */
interface IncomeSchedule {
    /** Payments dated between [from] and [to] (both included), oldest first, at most one per date. */
    fun paymentsBetween(from: CalendarDate, to: CalendarDate): List<Payment>
}

/**
 * Payments on fixed days of every month (monthly = one day, twice a month = two days).
 *
 * Rules:
 * - A day past the end of a short month falls on its last day (31 -> 30/04, 28/02...).
 * - If two days fall on the same date in a short month (e.g. 30 and 31 in February), they become
 *   one payment with both amounts, so no cycle is ever zero days long.
 * - [adjustedNextPayment] replaces the scheduled payment closest to it (earlier one on a tie),
 *   keeping that payment's amount.
 */
class DayOfMonthSchedule(
    private val payDays: List<PayDay>,
    private val adjustedNextPayment: CalendarDate?,
) : IncomeSchedule {

    /** Scheduled date that [adjustedNextPayment] replaces. */
    private val replacedDate: CalendarDate? = adjustedNextPayment?.let { target ->
        scheduledBetween(target.firstDayOfMonthPlus(-1), target.firstDayOfMonthPlus(2).plusDays(-1))
            .map { (date, _) -> date }
            .minWithOrNull(compareBy<CalendarDate> { abs(it.daysUntil(target)) }.thenBy { it })
    }

    override fun paymentsBetween(from: CalendarDate, to: CalendarDate): List<Payment> {
        if (payDays.isEmpty() || from > to) return emptyList()
        // One month of margin on each side: an adjusted payment may move across a month boundary.
        return scheduledBetween(from.firstDayOfMonthPlus(-1), to.firstDayOfMonthPlus(2).plusDays(-1))
            .map { (date, payDay) ->
                val actualDate = if (date == replacedDate && adjustedNextPayment != null) adjustedNextPayment else date
                Payment(actualDate, payDay.amountInCents)
            }
            .groupBy { it.date }
            .map { (date, payments) -> Payment(date, payments.sumOf { it.amountInCents }) }
            .filter { it.date in from..to }
            .sortedBy { it.date }
    }

    /** Raw scheduled dates (before adjustment and merging) of every month touching [from]..[to]. */
    private fun scheduledBetween(from: CalendarDate, to: CalendarDate): List<Pair<CalendarDate, PayDay>> {
        val result = mutableListOf<Pair<CalendarDate, PayDay>>()
        var month = from.firstDayOfMonthPlus(0)
        while (month <= to) {
            payDays.forEach { payDay ->
                val date = CalendarDate.ofDayClamped(month.year, month.month, payDay.dayOfMonth)
                if (date in from..to) result += date to payDay
            }
            month = month.firstDayOfMonthPlus(1)
        }
        return result
    }
}
