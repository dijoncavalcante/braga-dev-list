package com.bragadev.list

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import com.bragadev.list.core.domain.usecase.DuplicateShoppingListUseCase
import com.bragadev.list.core.domain.usecase.GetListShareTextUseCase
import com.bragadev.list.core.domain.usecase.MAX_LIST_NAME_LENGTH
import com.bragadev.list.core.domain.usecase.RenameShoppingListUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListOptionsUseCasesTest {

    private val repository: ShoppingListRepository = mockk()
    private val list = ShoppingList(id = 3, name = "Contas de casa", createdAt = 0, itemCount = 2)

    @Test
    fun `rename trims the name and delegates to the repository`() = runTest {
        coEvery { repository.renameList(3, "Contas 2026") } returns AppResult.Success(Unit)

        val result = RenameShoppingListUseCase(repository)(listId = 3, name = "  Contas 2026 ")

        assertEquals(AppResult.Success(Unit), result)
    }

    @Test
    fun `rename rejects blank and too long names`() = runTest {
        val useCase = RenameShoppingListUseCase(repository)

        listOf("   ", "x".repeat(MAX_LIST_NAME_LENGTH + 1)).forEach { invalid ->
            val result = useCase(listId = 3, name = invalid)
            assertTrue((result as AppResult.Error).error is AppError.Validation)
        }
        coVerify(exactly = 0) { repository.renameList(any(), any()) }
    }

    @Test
    fun `copy is named Copia de and cut to the max name length`() = runTest {
        val name = slot<String>()
        coEvery { repository.duplicateList(3, capture(name)) } answers {
            AppResult.Success(list.copy(id = 4, name = name.captured))
        }
        val useCase = DuplicateShoppingListUseCase(repository)

        useCase(list)
        assertEquals("Cópia de Contas de casa", name.captured)

        useCase(list.copy(name = "x".repeat(MAX_LIST_NAME_LENGTH)))
        assertEquals(MAX_LIST_NAME_LENGTH, name.captured.length)
    }

    @Test
    fun `share text lists every item with price, due day and total`() = runTest {
        coEvery { repository.getItems(3) } returns AppResult.Success(
            listOf(
                item(id = 1, name = "Luz", quantity = 1, priceInCents = 18_990, dueDay = 10),
                item(id = 2, name = "Água", quantity = 2, priceInCents = 2_500, isChecked = true),
                item(id = 3, name = "Pão", quantity = 1),
            ),
        )

        val result = GetListShareTextUseCase(repository)(list)

        val expected = """
            Contas de casa

            ☐ Luz (1) - R$ 189,90 - vence dia 10
            ☑ Água (2) - R$ 25,00
            ☐ Pão (1)

            Total: R$ 239,90
        """.trimIndent()
        assertEquals(AppResult.Success(expected), result)
    }

    @Test
    fun `share text of an empty list is just its name`() = runTest {
        coEvery { repository.getItems(3) } returns AppResult.Success(emptyList())

        val result = GetListShareTextUseCase(repository)(list)

        assertEquals(AppResult.Success("Contas de casa"), result)
    }

    @Test
    fun `share text follows the list order and hides prices when Mostrar valor is off`() = runTest {
        coEvery { repository.getItems(3) } returns AppResult.Success(
            listOf(
                item(id = 1, name = "Luz", quantity = 1, priceInCents = 18_990, dueDay = 10),
                item(id = 2, name = "Água", quantity = 1, priceInCents = 5_000),
            ),
        )

        val result = GetListShareTextUseCase(repository)(
            list.copy(sortOrder = ItemSortOrder.ALPHABETICAL, showPrices = false),
        )

        val expected = """
            Contas de casa

            ☐ Água (1)
            ☐ Luz (1) - vence dia 10
        """.trimIndent()
        assertEquals(AppResult.Success(expected), result)
    }

    private fun item(
        id: Long,
        name: String,
        quantity: Int,
        priceInCents: Long = 0,
        dueDay: Int? = null,
        isChecked: Boolean = false,
    ) = ShoppingListItem(
        id = id,
        listId = 3,
        name = name,
        quantity = quantity,
        priceInCents = priceInCents,
        dueDay = dueDay,
        isChecked = isChecked,
        createdAt = id,
    )
}
