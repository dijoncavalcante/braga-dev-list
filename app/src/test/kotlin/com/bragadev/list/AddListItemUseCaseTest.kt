package com.bragadev.list

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import com.bragadev.list.core.domain.usecase.AddListItemUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddListItemUseCaseTest {

    private val repository: ShoppingListRepository = mockk()
    private val useCase = AddListItemUseCase(repository)

    @Test
    fun `blank name returns validation error without touching the repository`() = runTest {
        val result = useCase(listId = 1, name = "  ", quantity = 1, priceInCents = 500)

        assertTrue(result is AppResult.Error)
        assertTrue((result as AppResult.Error).error is AppError.Validation)
        coVerify(exactly = 0) { repository.addItem(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `price and due date are saved together with name and quantity`() = runTest {
        val saved = ShoppingListItem(
            id = 10,
            listId = 1,
            name = "Arroz",
            quantity = 2,
            priceInCents = 1_250,
            dueDateMillis = DUE_DATE,
            isChecked = false,
            createdAt = 0,
        )
        coEvery { repository.addItem(1, "Arroz", 2, 1_250, DUE_DATE) } returns AppResult.Success(saved)

        val result = useCase(listId = 1, name = " Arroz ", quantity = 2, priceInCents = 1_250, dueDateMillis = DUE_DATE)

        assertEquals(AppResult.Success(saved), result)
        coVerify(exactly = 1) { repository.addItem(1, "Arroz", 2, 1_250, DUE_DATE) }
    }

    @Test
    fun `negative price is coerced to zero`() = runTest {
        coEvery { repository.addItem(any(), any(), any(), any(), any()) } returns AppResult.Error(AppError.Database)

        useCase(listId = 1, name = "Feijão", quantity = 1, priceInCents = -100)

        coVerify(exactly = 1) { repository.addItem(1, "Feijão", 1, 0, null) }
    }

    private companion object {
        /** 10/10/2026 00:00 UTC */
        const val DUE_DATE = 1_791_590_400_000L
    }
}
