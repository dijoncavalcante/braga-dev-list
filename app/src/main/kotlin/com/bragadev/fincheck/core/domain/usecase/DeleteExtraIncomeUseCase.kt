package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.IncomeRepository

/** Deletes an extra income. */
class DeleteExtraIncomeUseCase(
    private val repository: IncomeRepository,
) {
    /** Deletes the extra income with [id]. */
    suspend operator fun invoke(id: Long): AppResult<Unit> = repository.deleteExtraIncome(id)
}
