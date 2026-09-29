package com.bragadev.list

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import com.bragadev.list.core.domain.usecase.UpdateListItemUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateListItemUseCaseTest {

    private val repository: ShoppingListRepository = mockk()
    private val useCase = UpdateListItemUseCase(repository)

    @Test
    fun `blank name returns validation error without touching the repository`() = runTest {
        val result = useCase(itemId = 5, name = " ", quantity = 1, priceInCents = 0, dueDateMillis = null)

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.Validation)
        coVerify(exactly = 0) { repository.updateItem(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `edited values are trimmed, sanitized and delegated to the repository`() = runTest {
        coEvery { repository.updateItem(any(), any(), any(), any(), any()) } returns AppResult.Success(Unit)

        val result = useCase(itemId = 5, name = " Conta de luz ", quantity = 0, priceInCents = -1, dueDateMillis = 42)

        assertEquals(AppResult.Success(Unit), result)
        coVerify(exactly = 1) { repository.updateItem(5, "Conta de luz", 1, 0, 42) }
    }
}
