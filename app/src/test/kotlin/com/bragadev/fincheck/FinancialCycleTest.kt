package com.bragadev.fincheck

import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.BillMonthStatus
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.CycleInsight
import com.bragadev.fincheck.core.domain.model.CycleStatus
import com.bragadev.fincheck.core.domain.model.FinancialOverview
import com.bragadev.fincheck.core.domain.model.IncomeFrequency
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.PayDay
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.monthsTo
import com.bragadev.fincheck.core.domain.model.buildFinancialOverview
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FinancialCycleTest {

    /** Receives R$ 2.000 on day 10 and R$ 2.500 on day 25; next payment 10/10/2026. */
    private val twiceAMonth = IncomeSettings(
        frequency = IncomeFrequency.TWICE_A_MONTH,
        payDays = listOf(PayDay(10, 200_000), PayDay(25, 250_000)),
        nextPaymentDate = date(2026, 10, 10),
    )

    private val items = listOf(
        item(1, "Aluguel", 100_000, dueDay = 25),
        item(2, "Internet", 10_000, dueDay = 28),
        item(3, "Energia", 20_000, dueDay = 5),
        item(4, "Cartão", 30_000, dueDay = 15),
        item(5, "Mercado", 40_000, dueDay = null),
    )

    @Test
    fun `current cycle goes from the last payment to the day before the next one, across the month`() {
        val overview = overview(twiceAMonth, items, today = date(2026, 10, 1))

        assertEquals(date(2026, 9, 25), overview.current.startDate)
        assertEquals(date(2026, 10, 9), overview.current.endDate)
        assertEquals(250_000, overview.current.incomeInCents)
        assertEquals(date(2026, 10, 10), overview.next.startDate)
        assertEquals(date(2026, 10, 24), overview.next.endDate)
        assertEquals(200_000, overview.next.incomeInCents)
    }

    @Test
    fun `bills fall in the cycle of their due date and items without due day are other expenses`() {
        val overview = overview(twiceAMonth, items, today = date(2026, 10, 1))
        val current = overview.current

        assertEquals(listOf("Aluguel", "Internet", "Energia"), current.bills.map { it.name })
        assertEquals(listOf(date(2026, 9, 25), date(2026, 9, 28), date(2026, 10, 5)), current.bills.map { it.dueDate })
        assertEquals(listOf("Mercado"), current.otherExpenses.map { it.name })
        assertEquals(130_000, current.billsInCents)
        assertEquals(40_000, current.otherExpensesInCents)
        assertEquals(170_000, current.expensesInCents)
        assertEquals(80_000, current.projectedBalanceInCents)
        assertEquals(68, current.committedPercent)
        assertEquals(CycleStatus.HEALTHY, current.status)

        assertEquals(listOf("Cartão"), overview.next.bills.map { it.name })
        assertTrue(overview.next.otherExpenses.isEmpty())
    }

    @Test
    fun `the following cycles keep alternating payments`() {
        val overview = overview(twiceAMonth, items, today = date(2026, 10, 20))

        assertEquals(date(2026, 10, 10), overview.current.startDate)
        assertEquals(date(2026, 10, 24), overview.current.endDate)
        assertEquals(date(2026, 10, 25), overview.next.startDate)
        assertEquals(date(2026, 11, 9), overview.next.endDate)
        assertEquals(listOf("Aluguel", "Internet", "Energia"), overview.next.bills.map { it.name })
    }

    @Test
    fun `cycle crossing the year`() {
        val overview = overview(twiceAMonth, items, today = date(2026, 12, 31))

        assertEquals(date(2026, 12, 25), overview.current.startDate)
        assertEquals(date(2027, 1, 9), overview.current.endDate)
        assertEquals(
            listOf(date(2026, 12, 25), date(2026, 12, 28), date(2027, 1, 5)),
            overview.current.bills.map { it.dueDate },
        )
    }

    @Test
    fun `monthly income makes one cycle per month starting on the pay day`() {
        val monthly = IncomeSettings(IncomeFrequency.MONTHLY, listOf(PayDay(5, 500_000)), date(2026, 10, 5))

        val overview = overview(monthly, items, today = date(2026, 10, 5))

        assertEquals(date(2026, 10, 5), overview.current.startDate)
        assertEquals(date(2026, 11, 4), overview.current.endDate)
        assertEquals(31, overview.daysUntilNextPayment)
        // Every bill falls once in a month-long cycle; Energia (day 5) falls on the pay day itself.
        assertEquals(listOf("Energia", "Cartão", "Aluguel", "Internet"), overview.current.bills.map { it.name })
        assertEquals(date(2026, 10, 5), overview.current.bills.first().dueDate)
    }

    @Test
    fun `bill due on day 31 falls on the last day of february`() {
        val monthly = IncomeSettings(IncomeFrequency.MONTHLY, listOf(PayDay(10, 500_000)), date(2027, 2, 10))
        val bill = item(1, "Condomínio", 50_000, dueDay = 31)

        val overview = overview(monthly, listOf(bill), today = date(2027, 2, 15))

        assertEquals(listOf(date(2027, 2, 28)), overview.current.bills.map { it.dueDate })
    }

    @Test
    fun `tight and over budget cycles`() {
        val tight = overview(twiceAMonth, listOf(item(1, "Aluguel", 240_000, dueDay = 26)), today = date(2026, 10, 1))
        assertEquals(CycleStatus.TIGHT, tight.current.status)
        assertEquals(96, tight.current.committedPercent)

        val over = overview(twiceAMonth, listOf(item(1, "Aluguel", 260_000, dueDay = 26)), today = date(2026, 10, 1))
        assertEquals(CycleStatus.OVER_BUDGET, over.current.status)
        assertEquals(-10_000, over.current.projectedBalanceInCents)
    }

    @Test
    fun `insights tell what was received, what comes next and what is still to pay`() {
        val insights = overview(twiceAMonth, items, today = date(2026, 10, 1), paid = setOf("Aluguel")).insights()

        val received = assertIs<CycleInsight.Received>(insights[0])
        assertEquals(date(2026, 9, 25), received.payment.date)
        val next = assertIs<CycleInsight.NextPayment>(insights[1])
        assertEquals(9, next.daysUntil)
        assertEquals(200_000, next.payment.amountInCents)
        assertIs<CycleInsight.Balance>(insights[2])
        // Internet (28/09) passed unchecked; Energia (05/10) still comes before the next payment.
        val overdue = assertIs<CycleInsight.OverdueBills>(insights[3])
        assertEquals(listOf("Internet"), overdue.bills.map { it.name })
        val dueSoon = assertIs<CycleInsight.BillsDueBeforeNextPayment>(insights[4])
        assertEquals(listOf("Energia"), dueSoon.bills.map { it.name })
        assertEquals(5, insights.size)
    }

    @Test
    fun `projected balance of the overview adds up both cycles`() {
        val overview = overview(twiceAMonth, items, today = date(2026, 10, 1))

        assertEquals(
            overview.current.projectedBalanceInCents + overview.next.projectedBalanceInCents,
            overview.projectedBalanceInCents,
        )
        assertEquals(overview.next.endDate, overview.periodEndDate)
    }

    @Test
    fun `main alert is the overdue bills, never the facts already shown as numbers`() {
        val alert = overview(twiceAMonth, items, today = date(2026, 10, 1), paid = setOf("Aluguel")).mainAlert

        val overdue = assertIs<CycleInsight.OverdueBills>(alert)
        assertEquals(listOf("Internet"), overdue.bills.map { it.name })
    }

    @Test
    fun `no main alert when nothing is overdue or waiting for an extra income`() {
        val allPaid = items.map { it.name }.toSet()

        assertNull(overview(twiceAMonth, items, today = date(2026, 10, 1), paid = allPaid).mainAlert)
    }

    @Test
    fun `insight warns when the next cycle already spends more than it receives`() {
        val bigBill = item(1, "Financiamento", 210_000, dueDay = 15)

        val insights = overview(twiceAMonth, listOf(bigBill), today = date(2026, 10, 1)).insights()

        assertTrue(insights.any { it is CycleInsight.NextCycleOverBudget })
    }

    @Test
    fun `another month starts on its first pay day and raises no alert`() {
        val overview = overview(twiceAMonth, items, today = date(2026, 10, 1), month = YearMonth(2026, 11))

        assertFalse(overview.isLive)
        assertEquals(date(2026, 11, 10), overview.current.startDate)
        assertEquals(date(2026, 11, 24), overview.current.endDate)
        assertEquals(listOf("Cartão"), overview.current.bills.map { it.name })
        assertEquals(listOf("Mercado"), overview.current.otherExpenses.map { it.name })
        assertTrue(overview.overdueBills.isEmpty())
        assertNull(overview.mainAlert)
    }

    @Test
    fun `a cycle holds the bills of each month it spans, with their own paid state`() {
        // Both paid in October: the cycle 25/10 → 09/11 still has the November energy bill to pay.
        val bills = listOf(item(1, "Internet", 10_000, dueDay = 28), item(2, "Energia", 20_000, dueDay = 5))
        val statuses = listOf(1L, 2L).map { BillMonthStatus(billId = it, month = YearMonth(2026, 10), isPaid = true) }

        val overview = assertNotNull(
            buildFinancialOverview(twiceAMonth, BillBook(bills, statuses), YearMonth(2026, 10), date(2026, 10, 30)),
        )

        assertEquals(listOf(date(2026, 10, 28), date(2026, 11, 5)), overview.current.bills.map { it.dueDate })
        assertEquals(listOf(true, false), overview.current.bills.map { it.isPaid })
    }

    @Test
    fun `a bill that ends or starts later is only in the months it lasts`() {
        val installment = item(1, "Notebook", 50_000, dueDay = 5).copy(startMonth = YearMonth(2026, 11), endMonth = YearMonth(2026, 11))

        val october = overview(twiceAMonth, listOf(installment), today = date(2026, 10, 1))
        val november = overview(twiceAMonth, listOf(installment), today = date(2026, 11, 1))

        assertTrue(october.current.bills.isEmpty())
        assertEquals(listOf(date(2026, 11, 5)), november.current.bills.map { it.dueDate })
    }

    /** [paid] bills are paid in every month around [today]. */
    private fun overview(
        settings: IncomeSettings,
        bills: List<Bill>,
        today: CalendarDate,
        paid: Set<String> = emptySet(),
        month: YearMonth = YearMonth.of(today),
    ): FinancialOverview {
        val months = YearMonth.of(today).plusMonths(-2).monthsTo(YearMonth.of(today).plusMonths(3))
        val statuses = bills.filter { it.name in paid }.flatMap { bill ->
            months.map { BillMonthStatus(billId = bill.id, month = it, isPaid = true) }
        }
        return assertNotNull(buildFinancialOverview(settings, BillBook(bills, statuses), month, today))
    }

    /** A bill of every month since January 2026. */
    private fun item(id: Long, name: String, priceInCents: Long, dueDay: Int?) = Bill(
        id = id,
        name = name,
        priceInCents = priceInCents,
        dueDay = dueDay,
        startMonth = YearMonth(2026, 1),
        endMonth = null,
        createdAt = id,
    )

    private fun date(year: Int, month: Int, day: Int) = CalendarDate(year, month, day)
}
