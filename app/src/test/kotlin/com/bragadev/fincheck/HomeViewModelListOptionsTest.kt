package com.bragadev.fincheck

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.usecase.DeleteShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.DuplicateShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListShareTextUseCase
import com.bragadev.fincheck.core.domain.usecase.GetShoppingListsUseCase
import com.bragadev.fincheck.core.domain.usecase.RenameShoppingListUseCase
import com.bragadev.fincheck.features.home.presentation.viewmodel.HomeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelListOptionsTest {

    private val list = ShoppingList(id = 3, name = "Contas de casa", createdAt = 0, itemCount = 2)

    private val getShoppingListsUseCase: GetShoppingListsUseCase = mockk()
    private val renameShoppingListUseCase: RenameShoppingListUseCase = mockk()
    private val deleteShoppingListUseCase: DeleteShoppingListUseCase = mockk()
    private val duplicateShoppingListUseCase: DuplicateShoppingListUseCase = mockk()
    private val getListShareTextUseCase: GetListShareTextUseCase = mockk()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { getShoppingListsUseCase() } returns flowOf(listOf(list))
        viewModel = HomeViewModel(
            getShoppingListsUseCase = getShoppingListsUseCase,
            renameShoppingListUseCase = renameShoppingListUseCase,
            deleteShoppingListUseCase = deleteShoppingListUseCase,
            duplicateShoppingListUseCase = duplicateShoppingListUseCase,
            getListShareTextUseCase = getListShareTextUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `three dots open the options sheet for that list`() {
        viewModel.onListMenuClick(list)

        assertEquals(list, viewModel.uiState.value.menuList)
    }

    @Test
    fun `rename saves the new name and closes the dialog`() {
        coEvery { renameShoppingListUseCase(3, "Contas 2026") } returns AppResult.Success(Unit)
        viewModel.onListMenuClick(list)

        viewModel.onRenameClick()
        assertNull(viewModel.uiState.value.menuList)
        assertEquals(list, viewModel.uiState.value.renamingList)

        viewModel.onRenameConfirm("Contas 2026")

        coVerify(exactly = 1) { renameShoppingListUseCase(3, "Contas 2026") }
        assertNull(viewModel.uiState.value.renamingList)
    }

    @Test
    fun `rename with invalid name keeps the dialog open and shows the error`() {
        coEvery { renameShoppingListUseCase(any(), any()) } returns
            AppResult.Error(AppError.Validation(reason = "invalid_list_name"))
        viewModel.onListMenuClick(list)
        viewModel.onRenameClick()

        viewModel.onRenameConfirm(" ")

        assertEquals(list, viewModel.uiState.value.renamingList)
        assertTrue(viewModel.uiState.value.showRenameError)
    }

    @Test
    fun `cancelling rename does not save anything`() {
        viewModel.onListMenuClick(list)
        viewModel.onRenameClick()

        viewModel.onDismissRename()

        assertNull(viewModel.uiState.value.renamingList)
        coVerify(exactly = 0) { renameShoppingListUseCase(any(), any()) }
    }

    @Test
    fun `share builds the text and exposes it once`() {
        coEvery { getListShareTextUseCase(list) } returns AppResult.Success("Contas de casa")
        viewModel.onListMenuClick(list)

        viewModel.onShareClick()

        assertNull(viewModel.uiState.value.menuList)
        assertEquals("Contas de casa", viewModel.uiState.value.pendingShareText)

        viewModel.onShareHandled()

        assertNull(viewModel.uiState.value.pendingShareText)
    }

    @Test
    fun `copy duplicates the list`() {
        coEvery { duplicateShoppingListUseCase(list) } returns
            AppResult.Success(list.copy(id = 4, name = "Cópia de Contas de casa"))
        viewModel.onListMenuClick(list)

        viewModel.onCopyClick()

        coVerify(exactly = 1) { duplicateShoppingListUseCase(list) }
        assertNull(viewModel.uiState.value.menuList)
    }

    @Test
    fun `delete asks for confirmation before deleting`() {
        coEvery { deleteShoppingListUseCase(3) } returns AppResult.Success(Unit)
        viewModel.onListMenuClick(list)

        viewModel.onDeleteClick()

        assertEquals(list, viewModel.uiState.value.deletingList)
        coVerify(exactly = 0) { deleteShoppingListUseCase(any()) }

        viewModel.onDeleteConfirm()

        coVerify(exactly = 1) { deleteShoppingListUseCase(3) }
        assertNull(viewModel.uiState.value.deletingList)
    }

    @Test
    fun `cancelling delete keeps the list`() {
        viewModel.onListMenuClick(list)
        viewModel.onDeleteClick()

        viewModel.onDismissDelete()

        assertNull(viewModel.uiState.value.deletingList)
        coVerify(exactly = 0) { deleteShoppingListUseCase(any()) }
    }
}
