package com.bragadev.list.core.domain.usecase

import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.repository.IncomeRepository

class DeleteExtraIncomeUseCase(
    private val repository: IncomeRepository,
) {
    suspend operator fun invoke(id: Long): AppResult<Unit> = repository.deleteExtraIncome(id)
}
