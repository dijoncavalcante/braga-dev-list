package com.bragadev.list

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.usecase.AddListItemUseCase
import com.bragadev.list.core.domain.usecase.DeleteListItemUseCase
import com.bragadev.list.core.domain.usecase.GetListItemsUseCase
import com.bragadev.list.core.domain.usecase.GetShoppingListUseCase
import com.bragadev.list.core.domain.usecase.SetItemCheckedUseCase
import com.bragadev.list.core.domain.usecase.UpdateListItemUseCase
import com.bragadev.list.features.listdetail.presentation.viewmodel.ListDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailViewModelEditTest {

    private val item = ShoppingListItem(
        id = 7,
        listId = 1,
        name = "Arroz",
        quantity = 1,
        priceInCents = 1_000,
        dueDay = null,
        isChecked = false,
        createdAt = 0,
    )

    private val getShoppingListUseCase: GetShoppingListUseCase = mockk()
    private val getListItemsUseCase: GetListItemsUseCase = mockk()
    private val updateListItemUseCase: UpdateListItemUseCase = mockk()
    private val deleteListItemUseCase: DeleteListItemUseCase = mockk()

    private lateinit var viewModel: ListDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getShoppingListUseCase(1) } returns flowOf(null)
        every { getListItemsUseCase(1) } returns flowOf(listOf(item))
        viewModel = ListDetailViewModel(
            listId = 1,
            getShoppingListUseCase = getShoppingListUseCase,
            getListItemsUseCase = getListItemsUseCase,
            addListItemUseCase = mockk<AddListItemUseCase>(),
            setItemCheckedUseCase = mockk<SetItemCheckedUseCase>(),
            updateListItemUseCase = updateListItemUseCase,
            deleteListItemUseCase = deleteListItemUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clicking an item opens the edit dialog with that item`() {
        viewModel.onItemClick(item)

        assertEquals(item, viewModel.uiState.value.editingItem)
    }

    @Test
    fun `cancel closes the dialog without saving anything`() {
        viewModel.onItemClick(item)

        viewModel.onDismissEditItemDialog()

        assertNull(viewModel.uiState.value.editingItem)
        coVerify(exactly = 0) { updateListItemUseCase(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `save persists the edited values and closes the dialog`() = runTest {
        coEvery { updateListItemUseCase(any(), any(), any(), any(), any()) } returns AppResult.Success(Unit)
        viewModel.onItemClick(item)

        viewModel.onEditItemConfirm(name = "Arroz integral", quantity = 2, priceInCents = 1_590, dueDay = null)

        coVerify(exactly = 1) { updateListItemUseCase(7, "Arroz integral", 2, 1_590, null) }
        assertNull(viewModel.uiState.value.editingItem)
    }

    @Test
    fun `confirming the deletion removes the open item and closes the dialog`() = runTest {
        coEvery { deleteListItemUseCase(any()) } returns AppResult.Success(Unit)
        viewModel.onItemClick(item)

        viewModel.onDeleteItemConfirm()

        coVerify(exactly = 1) { deleteListItemUseCase(7) }
        assertNull(viewModel.uiState.value.editingItem)
    }

    @Test
    fun `failed deletion keeps the dialog open`() = runTest {
        coEvery { deleteListItemUseCase(any()) } returns AppResult.Error(AppError.Database)
        viewModel.onItemClick(item)

        viewModel.onDeleteItemConfirm()

        assertEquals(item, viewModel.uiState.value.editingItem)
    }

    @Test
    fun `deleting without an open item does nothing`() {
        viewModel.onDeleteItemConfirm()

        coVerify(exactly = 0) { deleteListItemUseCase(any()) }
    }
}
