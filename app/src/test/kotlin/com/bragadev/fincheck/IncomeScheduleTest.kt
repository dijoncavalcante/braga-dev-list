package com.bragadev.fincheck

import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.IncomeFrequency
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.PayDay
import com.bragadev.fincheck.core.domain.model.Payment
import kotlin.test.Test
import kotlin.test.assertEquals

class IncomeScheduleTest {

    @Test
    fun `twice a month uses the user's own days and independent amounts`() {
        val schedule = settings(IncomeFrequency.TWICE_A_MONTH, PayDay(10, 200_000), PayDay(25, 250_000)).toSchedule()

        val payments = schedule.paymentsBetween(date(2026, 9, 1), date(2026, 10, 31))

        assertEquals(
            listOf(
                Payment(date(2026, 9, 10), 200_000),
                Payment(date(2026, 9, 25), 250_000),
                Payment(date(2026, 10, 10), 200_000),
                Payment(date(2026, 10, 25), 250_000),
            ),
            payments,
        )
    }

    @Test
    fun `pay day 31 falls on the last day of shorter months`() {
        val schedule = settings(IncomeFrequency.MONTHLY, PayDay(31, 500_000)).toSchedule()

        val dates = schedule.paymentsBetween(date(2026, 1, 1), date(2026, 4, 30)).map { it.date }

        assertEquals(listOf(date(2026, 1, 31), date(2026, 2, 28), date(2026, 3, 31), date(2026, 4, 30)), dates)
        assertEquals(
            listOf(date(2028, 2, 29)),
            schedule.paymentsBetween(date(2028, 2, 1), date(2028, 2, 29)).map { it.date },
        )
    }

    @Test
    fun `two pay days on the same date of a short month become one payment with both amounts`() {
        val schedule = settings(IncomeFrequency.TWICE_A_MONTH, PayDay(30, 100_000), PayDay(31, 50_000)).toSchedule()

        val payments = schedule.paymentsBetween(date(2026, 2, 1), date(2026, 2, 28))

        assertEquals(listOf(Payment(date(2026, 2, 28), 150_000)), payments)
    }

    @Test
    fun `next payment date given by the user replaces the closest scheduled payment`() {
        // Usual day 5, but this month the money arrives on the 6th.
        val schedule = IncomeSettings(
            frequency = IncomeFrequency.MONTHLY,
            payDays = listOf(PayDay(5, 500_000)),
            nextPaymentDate = date(2026, 10, 6),
        ).toSchedule()

        val dates = schedule.paymentsBetween(date(2026, 9, 1), date(2026, 11, 30)).map { it.date }

        assertEquals(listOf(date(2026, 9, 5), date(2026, 10, 6), date(2026, 11, 5)), dates)
    }

    @Test
    fun `adjusted payment moving across the range edge is still found`() {
        val schedule = IncomeSettings(
            frequency = IncomeFrequency.MONTHLY,
            payDays = listOf(PayDay(31, 500_000)),
            nextPaymentDate = date(2026, 11, 2), // 31/10 is a Saturday; paid on Monday
        ).toSchedule()

        assertEquals(
            listOf(date(2026, 11, 2)),
            schedule.paymentsBetween(date(2026, 11, 1), date(2026, 11, 29)).map { it.date },
        )
        assertEquals(emptyList(), schedule.paymentsBetween(date(2026, 10, 1), date(2026, 10, 31)).map { it.date })
    }

    private fun settings(frequency: IncomeFrequency, vararg payDays: PayDay) = IncomeSettings(
        frequency = frequency,
        payDays = payDays.toList(),
        // Matches a scheduled date, so nothing is moved.
        nextPaymentDate = date(2026, 10, payDays.first().dayOfMonth.coerceAtMost(31)),
    )

    private fun date(year: Int, month: Int, day: Int) = CalendarDate(year, month, day)
}
