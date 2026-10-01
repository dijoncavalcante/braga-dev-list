package com.bragadev.list

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.data.mapper.toDomain
import com.bragadev.list.core.data.mapper.toEntity
import com.bragadev.list.core.domain.model.CalendarDate
import com.bragadev.list.core.domain.model.CycleInsight
import com.bragadev.list.core.domain.model.CycleStatus
import com.bragadev.list.core.domain.model.ExtraIncome
import com.bragadev.list.core.domain.model.ExtraIncomeRecurrence
import com.bragadev.list.core.domain.model.IncomeFrequency
import com.bragadev.list.core.domain.model.IncomeSettings
import com.bragadev.list.core.domain.model.PayDay
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.model.buildFinancialOverview
import com.bragadev.list.core.domain.repository.IncomeRepository
import com.bragadev.list.core.domain.usecase.SaveExtraIncomeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExtraIncomeTest {

    /** Salary on days 10 (R$ 2.000) and 25 (R$ 2.500); today 01/10/2026 → cycle 25/09 → 09/10. */
    private val salary = IncomeSettings(
        frequency = IncomeFrequency.TWICE_A_MONTH,
        payDays = listOf(PayDay(10, 200_000), PayDay(25, 250_000)),
        nextPaymentDate = date(2026, 10, 10),
    )
    private val rent = ExtraIncome(1, "Aluguel", 120_000, ExtraIncomeRecurrence.Monthly(dayOfMonth = 5))
    private val today = date(2026, 10, 1)

    @Test
    fun `monthly extra income repeats every month and falls on the last day of shorter months`() {
        val onDay31 = rent.copy(recurrence = ExtraIncomeRecurrence.Monthly(31))

        assertEquals(
            listOf(date(2026, 9, 5), date(2026, 10, 5)),
            rent.datesBetween(date(2026, 9, 1), date(2026, 10, 31)),
        )
        assertEquals(listOf(date(2027, 2, 28)), onDay31.datesBetween(date(2027, 2, 1), date(2027, 2, 28)))
    }

    @Test
    fun `one-off extra income arrives only on its date`() {
        val freela = ExtraIncome(2, "Freela", 50_000, ExtraIncomeRecurrence.Once(date(2026, 10, 20)))

        assertEquals(listOf(date(2026, 10, 20)), freela.datesBetween(date(2026, 10, 1), date(2026, 12, 31)))
        assertTrue(freela.datesBetween(date(2026, 11, 1), date(2026, 12, 31)).isEmpty())
    }

    @Test
    fun `extra income adds to the cycle it falls in without changing the cycle dates`() {
        val freela = ExtraIncome(2, "Freela", 50_000, ExtraIncomeRecurrence.Once(date(2026, 10, 20)))

        val overview = overview(listOf(bill("Aluguel casa", 230_000, dueDay = 26)), listOf(rent, freela))

        assertEquals(date(2026, 9, 25), overview.current.startDate)
        assertEquals(date(2026, 10, 9), overview.current.endDate)
        assertEquals(listOf("Aluguel"), overview.current.extraIncomes.map { it.income.name })
        assertEquals(370_000, overview.current.incomeInCents) // 2.500 + 1.200
        assertEquals(140_000, overview.current.projectedBalanceInCents)
        assertEquals(CycleStatus.HEALTHY, overview.current.status) // 62%: healthy only thanks to the rent
        // The one-off freela belongs to the next cycle (10/10 → 24/10), the rent of 05/11 to the one after.
        assertEquals(listOf("Freela"), overview.next.extraIncomes.map { it.income.name })
        assertEquals(250_000, overview.next.incomeInCents)
    }

    @Test
    fun `insights announce the extra income still to come and the bills due before it`() {
        // Salary of 2.500 alone does not cover 3.000 of bills: the rent of 05/10 is needed.
        val items = listOf(bill("Cartão", 280_000, dueDay = 26), bill("Internet", 20_000, dueDay = 3))

        val insights = overview(items, listOf(rent)).insights()

        val expected = assertIs<CycleInsight.ExtraIncomeExpected>(insights.single { it is CycleInsight.ExtraIncomeExpected })
        assertEquals(date(2026, 10, 5), expected.entries.single().date)
        val beforeExtra = insights.filterIsInstance<CycleInsight.BillsDueBeforeExtraIncome>().single()
        assertEquals(listOf("Internet"), beforeExtra.bills.map { it.item.name })
        assertEquals("Aluguel", beforeExtra.extraIncome.income.name)
    }

    @Test
    fun `no warning about bills before the extra income when the salary already covers the cycle`() {
        val items = listOf(bill("Internet", 20_000, dueDay = 3))

        val insights = overview(items, listOf(rent)).insights()

        assertTrue(insights.none { it is CycleInsight.BillsDueBeforeExtraIncome })
        assertTrue(insights.any { it is CycleInsight.ExtraIncomeExpected })
    }

    @Test
    fun `extra income already received is not announced as still to come`() {
        val received = rent.copy(recurrence = ExtraIncomeRecurrence.Monthly(dayOfMonth = 28))

        val insights = overview(emptyList(), listOf(received)).insights()

        assertTrue(insights.none { it is CycleInsight.ExtraIncomeExpected })
    }

    @Test
    fun `saving validates name, amount and day`() = runTest {
        val repository: IncomeRepository = mockk { coEvery { saveExtraIncome(any()) } returns AppResult.Success(Unit) }
        val save = SaveExtraIncomeUseCase(repository)

        assertIs<AppResult.Success<Unit>>(save(rent.copy(name = "  Aluguel  ")))
        coVerify { repository.saveExtraIncome(rent.copy(name = "Aluguel")) }

        val invalid = mapOf(
            "invalid_extra_income_name" to rent.copy(name = " "),
            "invalid_extra_income_amount" to rent.copy(amountInCents = 0),
            "invalid_extra_income_day" to rent.copy(recurrence = ExtraIncomeRecurrence.Monthly(32)),
        )
        invalid.forEach { (reason, income) ->
            assertEquals(AppError.Validation(reason), assertIs<AppResult.Error>(save(income)).error)
        }
    }

    @Test
    fun `both kinds survive the round trip to the database row`() {
        val freela = ExtraIncome(2, "Freela", 50_000, ExtraIncomeRecurrence.Once(date(2026, 10, 20)))

        assertEquals(rent, rent.toEntity().toDomain())
        assertEquals(freela, freela.toEntity().toDomain())
    }

    private fun overview(items: List<ShoppingListItem>, extras: List<ExtraIncome>) =
        assertNotNull(buildFinancialOverview(salary, items, today, extras))

    private fun bill(name: String, priceInCents: Long, dueDay: Int) = ShoppingListItem(
        id = name.hashCode().toLong(),
        listId = 1,
        name = name,
        quantity = 1,
        priceInCents = priceInCents,
        dueDay = dueDay,
        isChecked = false,
        createdAt = 0,
    )

    private fun date(year: Int, month: Int, day: Int) = CalendarDate(year, month, day)
}
