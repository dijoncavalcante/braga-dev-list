package com.bragadev.fincheck

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.ShoppingList
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import com.bragadev.fincheck.core.domain.usecase.CreateShoppingListUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateShoppingListUseCaseTest {

    private val repository: ShoppingListRepository = mockk()
    private val useCase = CreateShoppingListUseCase(repository)

    @Test
    fun `blank name returns validation error without touching the repository`() = runTest {
        val result = useCase(name = "   ")

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.Validation)
        coVerify(exactly = 0) { repository.createList(any()) }
    }

    @Test
    fun `valid name is trimmed and delegated to the repository`() = runTest {
        val created = ShoppingList(id = 1, name = "Compras do mês", createdAt = 0)
        coEvery { repository.createList("Compras do mês") } returns AppResult.Success(created)

        val result = useCase(name = "  Compras do mês  ")

        assertEquals(AppResult.Success(created), result)
        coVerify(exactly = 1) { repository.createList("Compras do mês") }
    }
}
