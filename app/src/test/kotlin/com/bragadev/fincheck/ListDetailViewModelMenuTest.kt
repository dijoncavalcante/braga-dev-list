package com.bragadev.fincheck

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ItemSortOrder
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.model.ShoppingListItem
import com.bragadev.fincheck.core.domain.usecase.DeleteListItemsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.GetIncomeSettingsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListItemsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListShareTextUseCase
import com.bragadev.fincheck.core.domain.usecase.GetShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.RenameShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.SetAllItemsCheckedUseCase
import com.bragadev.fincheck.core.domain.usecase.SetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.SetListPreferencesUseCase
import com.bragadev.fincheck.features.listdetail.presentation.viewmodel.ListDetailViewModel
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailViewModelMenuTest {

    private val listFlow = MutableStateFlow<ShoppingList?>(ShoppingList(id = 1, name = "Mercado", createdAt = 0))
    private val items = listOf(item(1, "Pão"), item(2, "arroz"), item(3, "Café"))

    private val getShoppingListUseCase: GetShoppingListUseCase = mockk()
    private val getListItemsUseCase: GetListItemsUseCase = mockk()
    private val renameShoppingListUseCase: RenameShoppingListUseCase = mockk()
    private val getListShareTextUseCase: GetListShareTextUseCase = mockk()
    private val setListPreferencesUseCase: SetListPreferencesUseCase = mockk()
    private val setAllItemsCheckedUseCase: SetAllItemsCheckedUseCase = mockk()
    private val deleteListItemsUseCase: DeleteListItemsUseCase = mockk()

    private val getIncomeSettingsUseCase: GetIncomeSettingsUseCase = mockk {
        every { this@mockk.invoke() } returns flowOf(null)
    }

    private val amountsHidden = MutableStateFlow(false)
    private val getAmountsHiddenUseCase: GetAmountsHiddenUseCase = mockk { every { this@mockk.invoke() } returns amountsHidden }
    private val setAmountsHiddenUseCase: SetAmountsHiddenUseCase = mockk(relaxed = true)

    private lateinit var viewModel: ListDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getShoppingListUseCase(1) } returns listFlow
        every { getListItemsUseCase(1) } returns flowOf(items)
        viewModel = ListDetailViewModel(
            listId = 1,
            getShoppingListUseCase = getShoppingListUseCase,
            getListItemsUseCase = getListItemsUseCase,
            addListItemUseCase = mockk(),
            setItemCheckedUseCase = mockk(),
            updateListItemUseCase = mockk(),
            deleteListItemUseCase = mockk(),
            renameShoppingListUseCase = renameShoppingListUseCase,
            getListShareTextUseCase = getListShareTextUseCase,
            setListPreferencesUseCase = setListPreferencesUseCase,
            setAllItemsCheckedUseCase = setAllItemsCheckedUseCase,
            deleteListItemsUseCase = deleteListItemsUseCase,
            getIncomeSettingsUseCase = getIncomeSettingsUseCase,
            saveIncomeSettingsUseCase = mockk(),
            getExtraIncomesUseCase = mockk { every { this@mockk.invoke() } returns flowOf(emptyList()) },
            saveExtraIncomeUseCase = mockk(),
            deleteExtraIncomeUseCase = mockk(),
            getAmountsHiddenUseCase = getAmountsHiddenUseCase,
            setAmountsHiddenUseCase = setAmountsHiddenUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `items follow the list alphabetical preference`() {
        assertEquals(listOf("Pão", "arroz", "Café"), viewModel.uiState.value.items.map { it.name })

        listFlow.value = listFlow.value!!.copy(sortOrder = ItemSortOrder.ALPHABETICAL)

        assertEquals(listOf("arroz", "Café", "Pão"), viewModel.uiState.value.items.map { it.name })
    }

    @Test
    fun `sort choice and toggles are saved`() {
        coEvery { setListPreferencesUseCase.setSortOrder(any(), any()) } returns AppResult.Success(Unit)
        coEvery { setListPreferencesUseCase.setShowPrices(any(), any()) } returns AppResult.Success(Unit)
        coEvery { setListPreferencesUseCase.setViewMode(any(), any()) } returns AppResult.Success(Unit)

        viewModel.onSortOrderSelected(ItemSortOrder.DUE_DAY)
        viewModel.onShowPricesToggle()
        viewModel.onViewModeSelected(ItemViewMode.FORTNIGHTS)

        coVerify(exactly = 1) { setListPreferencesUseCase.setSortOrder(1, ItemSortOrder.DUE_DAY) }
        coVerify(exactly = 1) { setListPreferencesUseCase.setShowPrices(1, false) }
        coVerify(exactly = 1) { setListPreferencesUseCase.setViewMode(1, ItemViewMode.FORTNIGHTS) }
    }

    @Test
    fun `eye follows the app-wide preference and saves the opposite of the current state`() {
        assertFalse(viewModel.uiState.value.amountsHidden)

        viewModel.onToggleAmountsVisibility()
        coVerify(exactly = 1) { setAmountsHiddenUseCase(true) }

        amountsHidden.value = true
        assertTrue(viewModel.uiState.value.amountsHidden)
        // "Mostrar valor" of the list is a separate preference and stays as it was.
        assertTrue(viewModel.uiState.value.showPrices)

        viewModel.onToggleAmountsVisibility()
        coVerify(exactly = 1) { setAmountsHiddenUseCase(false) }
    }

    @Test
    fun `check all and uncheck all update every item of the list`() {
        coEvery { setAllItemsCheckedUseCase(any(), any()) } returns AppResult.Success(Unit)

        viewModel.onCheckAllClick()
        viewModel.onUncheckAllClick()

        coVerify(exactly = 1) { setAllItemsCheckedUseCase(1, true) }
        coVerify(exactly = 1) { setAllItemsCheckedUseCase(1, false) }
    }

    @Test
    fun `delete items asks first, then deletes only what was chosen`() {
        coEvery { deleteListItemsUseCase(any(), any()) } returns AppResult.Success(Unit)

        viewModel.onDeleteItemsClick()
        assertTrue(viewModel.uiState.value.isDeleteItemsDialogVisible)
        coVerify(exactly = 0) { deleteListItemsUseCase(any(), any()) }

        viewModel.onDeleteItemsConfirm(onlyChecked = true)

        coVerify(exactly = 1) { deleteListItemsUseCase(1, true) }
        assertFalse(viewModel.uiState.value.isDeleteItemsDialogVisible)
    }

    @Test
    fun `cancelling delete items deletes nothing`() {
        viewModel.onDeleteItemsClick()

        viewModel.onDismissDeleteItems()

        assertFalse(viewModel.uiState.value.isDeleteItemsDialogVisible)
        coVerify(exactly = 0) { deleteListItemsUseCase(any(), any()) }
    }

    @Test
    fun `rename list saves the new name and closes the dialog`() {
        coEvery { renameShoppingListUseCase(1, "Feira") } returns AppResult.Success(Unit)

        viewModel.onRenameListClick()
        viewModel.onRenameConfirm("Feira")

        coVerify(exactly = 1) { renameShoppingListUseCase(1, "Feira") }
        assertFalse(viewModel.uiState.value.isRenameDialogVisible)
    }

    @Test
    fun `share exposes the list text once`() {
        coEvery { getListShareTextUseCase(any()) } returns AppResult.Success("Mercado")

        viewModel.onShareClick()
        assertEquals("Mercado", viewModel.uiState.value.pendingShareText)

        viewModel.onShareHandled()
        assertNull(viewModel.uiState.value.pendingShareText)
    }

    @Test
    fun `selecting the order that is already active does not save again`() {
        viewModel.onSortOrderSelected(ItemSortOrder.ADDED)

        coVerify(exactly = 0) { setListPreferencesUseCase.setSortOrder(any(), any()) }
    }

    private fun item(id: Long, name: String) = ShoppingListItem(
        id = id,
        listId = 1,
        name = name,
        quantity = 1,
        isChecked = false,
        createdAt = id,
    )
}
