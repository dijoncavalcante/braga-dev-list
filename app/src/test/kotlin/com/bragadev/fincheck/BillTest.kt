package com.bragadev.fincheck

import com.bragadev.fincheck.core.data.mapper.toDomain
import com.bragadev.fincheck.core.data.mapper.toEntity
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.BillMonthStatus
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.IncomeFrequency
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.Installment
import com.bragadev.fincheck.core.domain.model.MonthTiming
import com.bragadev.fincheck.core.domain.model.PayDay
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.model.buildMonthOverview
import com.bragadev.fincheck.core.domain.model.nextMonths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BillTest {
    private val october = YearMonth(2026, 10)

    @Test
    fun `months move across the year and the strip starts on the current month`() {
        assertEquals(YearMonth(2027, 1), YearMonth(2026, 12).plusMonths(1))
        assertEquals(YearMonth(2026, 12), YearMonth(2027, 1).plusMonths(-1))
        assertEquals(5, october.monthsUntil(YearMonth(2027, 3)))
        assertEquals(YearMonth(2027, 3), YearMonth.fromIndex(YearMonth(2027, 3).index))

        val strip = october.nextMonths()
        assertEquals(12, strip.size)
        assertEquals(october, strip.first())
        assertEquals(YearMonth(2027, 9), strip.last())
    }

    @Test
    fun `repetition sets which months a bill is in`() {
        assertEquals(october, BillRepetition.Once.endMonth(october))
        assertNull(BillRepetition.Monthly.endMonth(october))
        // 6 months from October: October to March.
        assertEquals(YearMonth(2027, 3), BillRepetition.ForMonths(6).endMonth(october))

        val sixMonths = bill(endMonth = YearMonth(2027, 3))
        assertFalse(sixMonths.isActiveIn(YearMonth(2026, 9)))
        assertTrue(sixMonths.isActiveIn(october))
        assertTrue(sixMonths.isActiveIn(YearMonth(2027, 3)))
        assertFalse(sixMonths.isActiveIn(YearMonth(2027, 4)))
        assertEquals(BillRepetition.ForMonths(6), sixMonths.repetition)
        assertEquals(BillRepetition.Monthly, bill(endMonth = null).repetition)
        assertEquals(BillRepetition.Once, bill(endMonth = october).repetition)
    }

    @Test
    fun `a bill of a fixed number of months tells which one it is`() {
        val sixMonths = bill(endMonth = YearMonth(2027, 3))

        assertEquals(Installment(1, 6), sixMonths.installmentIn(october))
        assertEquals(Installment(4, 6), sixMonths.installmentIn(YearMonth(2027, 1)))
        assertNull(bill(endMonth = null).installmentIn(october))
        assertNull(bill(endMonth = october).installmentIn(october))
    }

    @Test
    fun `due day falls in the month shown, on its last day when the month is shorter`() {
        val condominio = bill(endMonth = null).copy(dueDay = 31)

        assertEquals(CalendarDate(2026, 10, 31), condominio.dueDateIn(october))
        assertEquals(CalendarDate(2027, 2, 28), condominio.dueDateIn(YearMonth(2027, 2)))
        assertNull(bill(endMonth = null).copy(dueDay = null).dueDateIn(october))
    }

    @Test
    fun `each month has its own paid state and a skipped month leaves the bill out`() {
        val internet = bill(id = 1, endMonth = null)
        val book =
            BillBook(
                bills = listOf(internet),
                statuses =
                    listOf(
                        BillMonthStatus(billId = 1, month = october, isPaid = true),
                        BillMonthStatus(billId = 1, month = YearMonth(2026, 12), isSkipped = true),
                    ),
            )

        assertTrue(book.billsOf(october).single().isPaid)
        assertFalse(book.billsOf(YearMonth(2026, 11)).single().isPaid)
        assertTrue(book.billsOf(YearMonth(2026, 12)).isEmpty())
        assertTrue(book.billsOf(YearMonth(2026, 9)).isEmpty())
    }

    @Test
    fun `bills of a month come by due day, those without one at the end`() {
        val book =
            BillBook(
                bills =
                    listOf(
                        bill(id = 1, name = "Mercado", dueDay = null),
                        bill(id = 2, name = "Aluguel", dueDay = 25),
                        bill(id = 3, name = "Energia", dueDay = 5),
                    ),
            )

        assertEquals(listOf("Energia", "Aluguel", "Mercado"), book.billsOf(october).map { it.name })
    }

    @Test
    fun `month overview adds the payments of the month and only past or current months have overdue bills`() {
        val settings =
            IncomeSettings(IncomeFrequency.TWICE_A_MONTH, listOf(PayDay(10, 200_000), PayDay(25, 250_000)), CalendarDate(2026, 10, 10))
        val book = BillBook(bills = listOf(bill(id = 1, name = "Energia", dueDay = 5, priceInCents = 30_000)))
        val today = CalendarDate(2026, 10, 24)

        val current = buildMonthOverview(october, today, book, settings)
        assertEquals(MonthTiming.CURRENT, current.timing)
        assertEquals(450_000, current.incomeInCents)
        assertEquals(420_000, current.projectedBalanceInCents)
        assertEquals(listOf("Energia"), current.overdueBills.map { it.name })

        val november = buildMonthOverview(YearMonth(2026, 11), today, book, settings)
        assertEquals(MonthTiming.FUTURE, november.timing)
        assertTrue(november.overdueBills.isEmpty())

        val noIncome = buildMonthOverview(october, today, book, settings = null)
        assertEquals(0, noIncome.incomeInCents)
        assertEquals(30_000, noIncome.totals.unpaidInCents)
    }

    @Test
    fun `bill survives the round trip to the database row`() {
        val bill = bill(id = 3, endMonth = YearMonth(2027, 3)).copy(createdAt = 42)

        assertEquals(bill, bill.toEntity().toDomain())
        assertEquals(bill(endMonth = null), bill(endMonth = null).toEntity().toDomain())
    }

    private fun bill(
        id: Long = 1,
        name: String = "Internet",
        dueDay: Int? = 10,
        priceInCents: Long = 10_000,
        endMonth: YearMonth? = null,
    ) = Bill(id = id, name = name, priceInCents = priceInCents, dueDay = dueDay, startMonth = october, endMonth = endMonth)
}
