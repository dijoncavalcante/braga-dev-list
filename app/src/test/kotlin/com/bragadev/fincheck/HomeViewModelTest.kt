package com.bragadev.fincheck

import app.cash.turbine.test
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.usecase.GetShoppingListsUseCase
import com.bragadev.fincheck.features.home.presentation.viewmodel.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    // Standard (not Unconfined): the ViewModel starts collecting in init, so with an unconfined
    // dispatcher the lists would already be loaded before the test subscribes and the
    // "loading" state could never be observed. Here coroutines run only as the test advances.
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty repository flow results in the empty state`() = runTest {
        val getShoppingListsUseCase: GetShoppingListsUseCase = mockk()
        every { getShoppingListsUseCase() } returns flowOf(emptyList())

        val viewModel = HomeViewModel(
            getShoppingListsUseCase = getShoppingListsUseCase,
            renameShoppingListUseCase = mockk(),
            deleteShoppingListUseCase = mockk(),
            duplicateShoppingListUseCase = mockk(),
            getListShareTextUseCase = mockk(),
        )

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertTrue(loaded.isEmpty)
            assertEquals(0, loaded.lists.size)
        }
    }

    @Test
    fun `non-empty repository flow is exposed as the lists state`() = runTest {
        val lists = listOf(ShoppingList(id = 1, name = "Compras do mês", createdAt = 0, itemCount = 3))
        val getShoppingListsUseCase: GetShoppingListsUseCase = mockk()
        every { getShoppingListsUseCase() } returns flowOf(lists)

        val viewModel = HomeViewModel(
            getShoppingListsUseCase = getShoppingListsUseCase,
            renameShoppingListUseCase = mockk(),
            deleteShoppingListUseCase = mockk(),
            duplicateShoppingListUseCase = mockk(),
            getListShareTextUseCase = mockk(),
        )

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals(lists, loaded.lists)
            assertTrue(!loaded.isEmpty)
        }
    }
}
