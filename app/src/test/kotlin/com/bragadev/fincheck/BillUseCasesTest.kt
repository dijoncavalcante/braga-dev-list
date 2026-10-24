package com.bragadev.fincheck

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillEditScope
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.BillRepository
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import com.bragadev.fincheck.core.domain.usecase.ConvertListToBillsUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteBillUseCase
import com.bragadev.fincheck.core.domain.usecase.SaveBillUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BillUseCasesTest {
    private val october = YearMonth(2026, 10)
    private val december = YearMonth(2026, 12)

    private val repository: BillRepository =
        mockk(relaxed = true) {
            coEvery { addBill(any()) } returns AppResult.Success(Unit)
            coEvery { addBills(any()) } returns AppResult.Success(Unit)
            coEvery { updateBill(any()) } returns AppResult.Success(Unit)
            coEvery { updateBillEndingEarlier(any()) } returns AppResult.Success(Unit)
            coEvery { splitBill(any(), any()) } returns AppResult.Success(Unit)
            coEvery { replaceBillInMonth(any(), any(), any()) } returns AppResult.Success(Unit)
            coEvery { deleteBill(any()) } returns AppResult.Success(Unit)
            coEvery { skipMonth(any(), any()) } returns AppResult.Success(Unit)
        }
    private val save = SaveBillUseCase(repository)
    private val delete = DeleteBillUseCase(repository)

    /** "Internet" every month since October, created at 5. */
    private val internet =
        Bill(
            id = 1,
            name = "Internet",
            priceInCents = 10_000,
            dueDay = 10,
            startMonth = october,
            endMonth = null,
            createdAt = 5,
        )

    @Test
    fun `adding validates the name, due day and period and trims the name`() =
        runTest {
            assertIs<AppResult.Success<Unit>>(save.add(internet.copy(id = 0, name = "  Internet ")))
            coVerify { repository.addBill(internet.copy(id = 0)) }

            val invalid =
                mapOf(
                    "invalid_bill_name" to internet.copy(name = " "),
                    "invalid_due_day" to internet.copy(dueDay = 32),
                    "invalid_bill_end" to internet.copy(endMonth = october.plusMonths(-1)),
                )
            invalid.forEach { (reason, bill) ->
                assertEquals(AppError.Validation(reason), assertIs<AppResult.Error>(save.add(bill)).error)
            }
        }

    @Test
    fun `a bill of a single month is simply replaced`() =
        runTest {
            val once = internet.copy(endMonth = october)

            save.edit(once, once.copy(priceInCents = 12_000), october, BillEditScope.ONLY_THIS_MONTH)

            coVerify { repository.updateBill(once.copy(priceInCents = 12_000)) }
        }

    @Test
    fun `only this month replaces that month with a one-month bill`() =
        runTest {
            save.edit(internet, internet.copy(priceInCents = 12_000), december, BillEditScope.ONLY_THIS_MONTH)

            coVerify {
                repository.replaceBillInMonth(1, december, internet.copy(priceInCents = 12_000, startMonth = december, endMonth = december))
            }
        }

    @Test
    fun `this and next months keeps the months before as they were`() =
        runTest {
            save.edit(internet, internet.copy(priceInCents = 12_000), december, BillEditScope.THIS_AND_NEXT)

            coVerify {
                repository.splitBill(
                    original = internet.copy(endMonth = YearMonth(2026, 11)),
                    newBill = internet.copy(priceInCents = 12_000, startMonth = december),
                )
            }
        }

    @Test
    fun `this and next months from the first month changes the whole bill`() =
        runTest {
            save.edit(internet, internet.copy(priceInCents = 12_000), october, BillEditScope.THIS_AND_NEXT)
            coVerify { repository.updateBill(internet.copy(priceInCents = 12_000)) }

            save.edit(internet, internet.copy(endMonth = YearMonth(2026, 11)), october, BillEditScope.THIS_AND_NEXT)
            coVerify { repository.updateBillEndingEarlier(internet.copy(endMonth = YearMonth(2026, 11))) }
        }

    @Test
    fun `an end set before the month on screen just ends the bill there`() =
        runTest {
            // Looking at December, the bill is changed to last 2 months: October and November.
            val edited = internet.copy(priceInCents = 99_000, endMonth = BillRepetition.ForMonths(2).endMonth(october))

            save.edit(internet, edited, december, BillEditScope.THIS_AND_NEXT)

            coVerify { repository.updateBillEndingEarlier(internet.copy(endMonth = YearMonth(2026, 11))) }
        }

    @Test
    fun `deleting keeps the months before`() =
        runTest {
            delete(internet, december, BillEditScope.ONLY_THIS_MONTH)
            coVerify { repository.skipMonth(1, december) }

            delete(internet, december, BillEditScope.THIS_AND_NEXT)
            coVerify { repository.updateBillEndingEarlier(internet.copy(endMonth = YearMonth(2026, 11))) }

            delete(internet, october, BillEditScope.THIS_AND_NEXT)
            delete(internet.copy(endMonth = october), october, BillEditScope.ONLY_THIS_MONTH)
            coVerify(exactly = 2) { repository.deleteBill(1) }
        }

    @Test
    fun `a list becomes bills with the total of each item from the chosen month`() =
        runTest {
            val listRepository: ShoppingListRepository =
                mockk {
                    coEvery { getItems(3) } returns
                        AppResult.Success(
                            listOf(
                                ShoppingListItem(
                                    1,
                                    3,
                                    "Luz",
                                    quantity = 1,
                                    priceInCents = 18_000,
                                    dueDay = 10,
                                    isChecked = true,
                                    createdAt = 0,
                                ),
                                ShoppingListItem(
                                    2,
                                    3,
                                    "Gás",
                                    quantity = 2,
                                    priceInCents = 12_000,
                                    dueDay = null,
                                    isChecked = false,
                                    createdAt = 0,
                                ),
                            ),
                        )
                }
            val bills = slot<List<Bill>>()
            coEvery { repository.addBills(capture(bills)) } returns AppResult.Success(Unit)

            val result = ConvertListToBillsUseCase(listRepository, repository)(3, YearMonth(2026, 11), BillRepetition.Monthly)

            assertEquals(2, assertIs<AppResult.Success<Int>>(result).data)
            assertEquals(
                listOf(
                    Bill(name = "Luz", priceInCents = 18_000, dueDay = 10, startMonth = YearMonth(2026, 11), endMonth = null),
                    Bill(name = "Gás", priceInCents = 24_000, dueDay = null, startMonth = YearMonth(2026, 11), endMonth = null),
                ),
                bills.captured,
            )
        }
}
