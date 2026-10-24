package com.bragadev.fincheck

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillBook
import com.bragadev.fincheck.core.domain.model.BillEditScope
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.usecase.DeleteBillUseCase
import com.bragadev.fincheck.core.domain.usecase.GetBillBookUseCase
import com.bragadev.fincheck.core.domain.usecase.GetExtraIncomesUseCase
import com.bragadev.fincheck.core.domain.usecase.GetFirstMonthUseCase
import com.bragadev.fincheck.core.domain.usecase.GetIncomeSettingsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetMonthViewModeUseCase
import com.bragadev.fincheck.core.domain.usecase.SaveBillUseCase
import com.bragadev.fincheck.core.domain.usecase.SetBillPaidUseCase
import com.bragadev.fincheck.features.months.presentation.state.PendingBillChange
import com.bragadev.fincheck.features.months.presentation.viewmodel.MonthViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MonthViewModelTest {
    private val today = CalendarDate(2026, 10, 24)
    private val october = YearMonth(2026, 10)

    /** "Internet" every month since October; "Notebook" only in November. */
    private val internet = Bill(1, "Internet", 10_000, dueDay = 10, startMonth = october, endMonth = null, createdAt = 1)
    private val notebook =
        Bill(2, "Notebook", 50_000, dueDay = 5, startMonth = YearMonth(2026, 11), endMonth = YearMonth(2026, 11), createdAt = 2)
    private val book = MutableStateFlow(BillBook(bills = listOf(internet, notebook)))

    private val saveBillUseCase: SaveBillUseCase = mockk()
    private val deleteBillUseCase: DeleteBillUseCase = mockk()
    private val setBillPaidUseCase: SetBillPaidUseCase =
        mockk { coEvery { this@mockk(any(), any(), any()) } returns AppResult.Success(Unit) }

    private lateinit var viewModel: MonthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            MonthViewModel(
                getBillBookUseCase = mockk<GetBillBookUseCase> { every { this@mockk() } returns book },
                saveBillUseCase = saveBillUseCase,
                deleteBillUseCase = deleteBillUseCase,
                setBillPaidUseCase = setBillPaidUseCase,
                getIncomeSettingsUseCase = mockk<GetIncomeSettingsUseCase> { every { this@mockk() } returns flowOf(null) },
                saveIncomeSettingsUseCase = mockk(),
                getExtraIncomesUseCase = mockk<GetExtraIncomesUseCase> { every { this@mockk() } returns flowOf(emptyList()) },
                saveExtraIncomeUseCase = mockk(),
                deleteExtraIncomeUseCase = mockk(),
                getMonthViewModeUseCase = mockk<GetMonthViewModeUseCase> { every { this@mockk() } returns flowOf(ItemViewMode.ALL) },
                setMonthViewModeUseCase = mockk(relaxed = true),
                getFirstMonthUseCase = mockk<GetFirstMonthUseCase> { coEvery { this@mockk(any()) } returns october },
                getAmountsHiddenUseCase = mockk { every { this@mockk() } returns flowOf(false) },
                setAmountsHiddenUseCase = mockk(relaxed = true),
                currentDate = { today },
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opens on the current month with its own bills and the next twelve months in the strip`() {
        val state = viewModel.uiState.value

        assertEquals(october, state.selectedMonth)
        assertEquals(listOf("Internet"), state.bills.map { it.name })
        assertEquals(october, state.strip.first().month)
        assertEquals(12, state.strip.size)
        assertEquals(60_000, state.strip[1].billsInCents)
    }

    @Test
    fun `each month shows the bills of its rules and no month before the first one opens`() {
        viewModel.onNextMonthClick()
        assertEquals(
            listOf("Notebook", "Internet"),
            viewModel.uiState.value.bills
                .map { it.name },
        )

        viewModel.onMonthSelected(YearMonth(2027, 1))
        assertEquals(
            listOf("Internet"),
            viewModel.uiState.value.bills
                .map { it.name },
        )

        viewModel.onCurrentMonthClick()
        assertFalse(viewModel.uiState.value.canGoBack)
        viewModel.onPreviousMonthClick()
        assertEquals(october, viewModel.uiState.value.selectedMonth)
    }

    @Test
    fun `a new bill starts in the month on screen`() {
        coEvery { saveBillUseCase.add(any()) } returns AppResult.Success(Unit)
        viewModel.onMonthSelected(YearMonth(2026, 12))
        viewModel.onAddBillClick()

        viewModel.onAddBillConfirm("Curso", 30_000, dueDay = 15, repetition = BillRepetition.ForMonths(6))

        coVerify {
            saveBillUseCase.add(
                Bill(name = "Curso", priceInCents = 30_000, dueDay = 15, startMonth = YearMonth(2026, 12), endMonth = YearMonth(2027, 5)),
            )
        }
        assertFalse(viewModel.uiState.value.isAddBillVisible)
    }

    @Test
    fun `editing a recurring bill asks which months, then saves for the month it was opened in`() {
        coEvery { saveBillUseCase.edit(any(), any(), any(), any()) } returns AppResult.Success(Unit)
        viewModel.onNextMonthClick()
        val novemberInternet =
            viewModel.uiState.value.bills
                .single { it.name == "Internet" }
        viewModel.onBillClick(novemberInternet)

        viewModel.onEditBillConfirm("Internet", 12_000, dueDay = 10, repetition = BillRepetition.Monthly)
        val pending = assertIs<PendingBillChange.Edit>(viewModel.uiState.value.pendingChange)
        assertEquals(12_000, pending.edited.priceInCents)

        viewModel.onScopeSelected(BillEditScope.THIS_AND_NEXT)

        coVerify { saveBillUseCase.edit(internet, internet.copy(priceInCents = 12_000), YearMonth(2026, 11), BillEditScope.THIS_AND_NEXT) }
        assertNull(viewModel.uiState.value.pendingChange)
        assertNull(viewModel.uiState.value.editingBill)
    }

    @Test
    fun `a bill of a single month is deleted without asking`() {
        coEvery { deleteBillUseCase(any(), any(), any()) } returns AppResult.Success(Unit)
        viewModel.onNextMonthClick()
        viewModel.onBillClick(
            viewModel.uiState.value.bills
                .single { it.name == "Notebook" },
        )

        viewModel.onDeleteBillConfirm()

        assertNull(viewModel.uiState.value.pendingChange)
        coVerify { deleteBillUseCase(notebook, YearMonth(2026, 11), any()) }
    }

    @Test
    fun `paying a bill marks only the month it belongs to`() {
        val octoberInternet =
            viewModel.uiState.value.bills
                .single()

        viewModel.onBillPaidChange(octoberInternet, isPaid = true)

        coVerify { setBillPaidUseCase(1, october, true) }
    }
}
