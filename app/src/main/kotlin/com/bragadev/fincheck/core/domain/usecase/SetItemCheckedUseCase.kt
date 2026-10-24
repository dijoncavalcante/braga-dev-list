package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository

/** Checks or unchecks one item of a list. */
class SetItemCheckedUseCase(
    private val repository: ShoppingListRepository,
) {
    /** Checks (or unchecks, when [isChecked] is false) the item with [itemId]. */
    suspend operator fun invoke(itemId: Long, isChecked: Boolean): AppResult<Unit> =
        repository.setItemChecked(itemId, isChecked)
}
