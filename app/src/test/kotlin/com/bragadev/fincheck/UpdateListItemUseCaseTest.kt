package com.bragadev.fincheck

import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import com.bragadev.fincheck.core.domain.usecase.UpdateListItemUseCase
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
        val result = useCase(itemId = 5, name = " ", quantity = 1, priceInCents = 0, dueDay = null)

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.Validation)
        coVerify(exactly = 0) { repository.updateItem(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `edited values are trimmed, sanitized and delegated to the repository`() = runTest {
        coEvery { repository.updateItem(any(), any(), any(), any(), any()) } returns AppResult.Success(Unit)

        val result = useCase(itemId = 5, name = " Conta de luz ", quantity = 0, priceInCents = -1, dueDay = 31)

        assertEquals(AppResult.Success(Unit), result)
        coVerify(exactly = 1) { repository.updateItem(5, "Conta de luz", 1, 0, 31) }
    }

    @Test
    fun `due day outside 1 to 31 returns validation error`() = runTest {
        listOf(0, 32).forEach { invalidDay ->
            val result = useCase(itemId = 5, name = "Luz", quantity = 1, priceInCents = 0, dueDay = invalidDay)

            assertTrue(result is AppResult.Error)
            assertTrue((result as AppResult.Error).error is AppError.Validation)
        }
        coVerify(exactly = 0) { repository.updateItem(any(), any(), any(), any(), any()) }
    }
}
