package com.bragadev.list

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import com.bragadev.list.core.domain.usecase.DeleteListItemUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteListItemUseCaseTest {

    private val repository: ShoppingListRepository = mockk()
    private val useCase = DeleteListItemUseCase(repository)

    @Test
    fun `deletes the given item through the repository`() = runTest {
        coEvery { repository.deleteItem(7) } returns AppResult.Success(Unit)

        val result = useCase(itemId = 7)

        assertEquals(AppResult.Success(Unit), result)
        coVerify(exactly = 1) { repository.deleteItem(7) }
    }
}
