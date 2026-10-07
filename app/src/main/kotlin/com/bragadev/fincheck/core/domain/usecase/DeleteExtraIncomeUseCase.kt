package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.repository.IncomeRepository

class DeleteExtraIncomeUseCase(
    private val repository: IncomeRepository,
) {
    suspend operator fun invoke(id: Long): AppResult<Unit> = repository.deleteExtraIncome(id)
}
