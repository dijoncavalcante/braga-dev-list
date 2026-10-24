package com.bragadev.fincheck

import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.BillMonthStatus
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.CycleStatus
import com.bragadev.fincheck.core.domain.model.FortnightOverview
import com.bragadev.fincheck.core.domain.model.IncomeFrequency
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.PayDay
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.buildMonthOverview
import com.bragadev.fincheck.core.domain.model.toFortnights
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FortnightOverviewTest {

    private val october = YearMonth(2026, 10)

    /** R$ 5.000 on day 5 and R$ 2.000 on day 20. */
    private val settings = IncomeSettings(
        frequency = IncomeFrequency.TWICE_A_MONTH,
        payDays = listOf(PayDay(5, 500_000), PayDay(20, 200_000)),
        nextPaymentDate = CalendarDate(2026, 10, 5),
    )

    private val bills = listOf(
        bill(1, "Energia", 100_000, dueDay = 4),
        bill(2, "Aluguel", 220_000, dueDay = 15),
        bill(3, "Cartão", 520_000, dueDay = 26),
        bill(4, "Mercado", 50_000, dueDay = null),
    )

    @Test
    fun `each fortnight has the bills and payments of its own days`() {
        val overview = overview(today = CalendarDate(2026, 10, 10))

        assertEquals(CalendarDate(2026, 10, 1), overview.first.startDate)
        assertEquals(CalendarDate(2026, 10, 15), overview.first.endDate)
        assertEquals(CalendarDate(2026, 10, 16), overview.second.startDate)
        assertEquals(CalendarDate(2026, 10, 31), overview.second.endDate)
        assertEquals(listOf("Energia", "Aluguel"), overview.first.bills.map { it.name })
        assertEquals(listOf("Cartão"), overview.second.bills.map { it.name })
        assertEquals(500_000, overview.first.incomeInCents)
        assertEquals(200_000, overview.second.incomeInCents)
    }

    @Test
    fun `what is left in the 1a quinzena is carried into the 2a`() {
        val overview = overview(today = CalendarDate(2026, 10, 10))

        // 5.000 - 1.000 - 2.200
        assertEquals(180_000, overview.first.balanceInCents)
        assertEquals(64, overview.first.committedPercent)
        assertEquals(CycleStatus.HEALTHY, overview.first.status)
        // 1.800 left + 2.000 received - 5.200: alone it would be -3.200, with the carry-over -1.400.
        assertEquals(180_000, overview.second.carriedOverInCents)
        assertEquals(-140_000, overview.second.balanceInCents)
        assertEquals(136, overview.second.committedPercent)
        assertEquals(CycleStatus.OVER_BUDGET, overview.second.status)
    }

    @Test
    fun `bills without a due day only count in the month balance`() {
        val overview = overview(today = CalendarDate(2026, 10, 10))

        assertEquals(listOf("Mercado"), overview.otherExpenses.map { it.name })
        // 7.000 received - 8.400 of bills - 500 of groceries.
        assertEquals(-190_000, overview.projectedBalanceInCents)
        assertEquals(overview.overview.projectedBalanceInCents, overview.projectedBalanceInCents)
    }

    @Test
    fun `the fortnight today is in is the current one, only in the current month`() {
        val first = overview(today = CalendarDate(2026, 10, 15))
        assertEquals(first.first, first.current)
        val second = overview(today = CalendarDate(2026, 10, 16))
        assertEquals(second.second, second.current)
        assertNull(overview(today = CalendarDate(2026, 9, 30)).current)
    }

    @Test
    fun `paid and to pay are counted per fortnight`() {
        val statuses = listOf(BillMonthStatus(billId = 1, month = october, isPaid = true))
        val overview = buildMonthOverview(october, CalendarDate(2026, 10, 20), BillBook(bills, statuses), settings).toFortnights()

        assertEquals(100_000, overview.first.totals.paidInCents)
        assertEquals(220_000, overview.first.totals.unpaidInCents)
        // Energia was paid; Aluguel (day 15) is overdue.
        assertEquals(listOf("Aluguel"), overview.overdueBills.map { it.name })
    }

    @Test
    fun `day 31 falls in the 2a quinzena of a short month`() {
        val condominio = bill(5, "Condomínio", 40_000, dueDay = 31)
        val november = buildMonthOverview(YearMonth(2026, 11), CalendarDate(2026, 10, 1), BillBook(listOf(condominio)), settings)
            .toFortnights()

        assertEquals(CalendarDate(2026, 11, 30), november.second.endDate)
        assertTrue(november.first.bills.isEmpty())
        assertEquals(listOf(CalendarDate(2026, 11, 30)), november.second.bills.map { it.dueDate })
    }

    private fun overview(today: CalendarDate): FortnightOverview =
        buildMonthOverview(october, today, BillBook(bills), settings).toFortnights()

    private fun bill(id: Long, name: String, priceInCents: Long, dueDay: Int?) =
        Bill(id = id, name = name, priceInCents = priceInCents, dueDay = dueDay, startMonth = october, endMonth = null, createdAt = id)
}
