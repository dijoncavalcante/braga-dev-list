package com.bragadev.list

import com.bragadev.list.core.domain.model.CalendarDate
import com.bragadev.list.core.domain.model.CycleInsight
import com.bragadev.list.core.domain.model.CycleStatus
import com.bragadev.list.core.domain.model.FinancialOverview
import com.bragadev.list.core.domain.model.IncomeFrequency
import com.bragadev.list.core.domain.model.IncomeSettings
import com.bragadev.list.core.domain.model.PayDay
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.buildFinancialOverview
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
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

        assertEquals(listOf("Aluguel", "Internet", "Energia"), current.bills.map { it.item.name })
        assertEquals(listOf(date(2026, 9, 25), date(2026, 9, 28), date(2026, 10, 5)), current.bills.map { it.dueDate })
        assertEquals(listOf("Mercado"), current.otherExpenses.map { it.item.name })
        assertEquals(130_000, current.billsInCents)
        assertEquals(40_000, current.otherExpensesInCents)
        assertEquals(170_000, current.expensesInCents)
        assertEquals(80_000, current.projectedBalanceInCents)
        assertEquals(68, current.committedPercent)
        assertEquals(CycleStatus.HEALTHY, current.status)

        assertEquals(listOf("Cartão"), overview.next.bills.map { it.item.name })
        assertTrue(overview.next.otherExpenses.isEmpty())
    }

    @Test
    fun `the following cycles keep alternating payments`() {
        val overview = overview(twiceAMonth, items, today = date(2026, 10, 20))

        assertEquals(date(2026, 10, 10), overview.current.startDate)
        assertEquals(date(2026, 10, 24), overview.current.endDate)
        assertEquals(date(2026, 10, 25), overview.next.startDate)
        assertEquals(date(2026, 11, 9), overview.next.endDate)
        assertEquals(listOf("Aluguel", "Internet", "Energia"), overview.next.bills.map { it.item.name })
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
        assertEquals(listOf("Energia", "Cartão", "Aluguel", "Internet"), overview.current.bills.map { it.item.name })
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
        val withChecked = items.map { if (it.name == "Aluguel") it.copy(isChecked = true) else it }

        val insights = overview(twiceAMonth, withChecked, today = date(2026, 10, 1)).insights()

        val received = assertIs<CycleInsight.Received>(insights[0])
        assertEquals(date(2026, 9, 25), received.payment.date)
        val next = assertIs<CycleInsight.NextPayment>(insights[1])
        assertEquals(9, next.daysUntil)
        assertEquals(200_000, next.payment.amountInCents)
        assertIs<CycleInsight.Balance>(insights[2])
        // Internet (28/09) passed unchecked; Energia (05/10) still comes before the next payment.
        val overdue = assertIs<CycleInsight.OverdueBills>(insights[3])
        assertEquals(listOf("Internet"), overdue.bills.map { it.item.name })
        val dueSoon = assertIs<CycleInsight.BillsDueBeforeNextPayment>(insights[4])
        assertEquals(listOf("Energia"), dueSoon.bills.map { it.item.name })
        assertEquals(5, insights.size)
    }

    @Test
    fun `insight warns when the next cycle already spends more than it receives`() {
        val bigBill = item(1, "Financiamento", 210_000, dueDay = 15)

        val insights = overview(twiceAMonth, listOf(bigBill), today = date(2026, 10, 1)).insights()

        assertTrue(insights.any { it is CycleInsight.NextCycleOverBudget })
    }

    private fun overview(settings: IncomeSettings, items: List<ShoppingListItem>, today: CalendarDate): FinancialOverview =
        assertNotNull(buildFinancialOverview(settings, items, today))

    private fun item(id: Long, name: String, priceInCents: Long, dueDay: Int?) = ShoppingListItem(
        id = id,
        listId = 1,
        name = name,
        quantity = 1,
        priceInCents = priceInCents,
        dueDay = dueDay,
        isChecked = false,
        createdAt = id,
    )

    private fun date(year: Int, month: Int, day: Int) = CalendarDate(year, month, day)
}
