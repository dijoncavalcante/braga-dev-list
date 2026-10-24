package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillRepetition
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.BillRepository
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/**
 * "Transformar em contas do mês": every item of a list becomes a bill of the "Meses" tab from
 * [startMonth] on, keeping its name, total (unit price × quantity) and due day. The list itself is
 * not changed, so nothing is lost if the user keeps using it.
 *
 * Returns how many bills were created.
 */
class ConvertListToBillsUseCase(
    private val listRepository: ShoppingListRepository,
    private val billRepository: BillRepository,
) {
    /**
     * Creates one bill per item of list [listId], starting in [startMonth] and repeating as
     * [repetition] says.
     *
     * @return how many bills were created; 0 for an empty list.
     */
    suspend operator fun invoke(
        listId: Long,
        startMonth: YearMonth,
        repetition: BillRepetition,
    ): AppResult<Int> {
        val items =
            when (val result = listRepository.getItems(listId)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return result
            }
        if (items.isEmpty()) return AppResult.Success(0)
        val bills =
            items.map { item ->
                Bill(
                    name = item.name,
                    priceInCents = item.priceInCents * item.quantity,
                    dueDay = item.dueDay,
                    startMonth = startMonth,
                    endMonth = repetition.endMonth(startMonth),
                    description = item.description,
                )
            }
        return when (val result = billRepository.addBills(bills)) {
            is AppResult.Success -> AppResult.Success(bills.size)
            is AppResult.Error -> result
        }
    }
}
