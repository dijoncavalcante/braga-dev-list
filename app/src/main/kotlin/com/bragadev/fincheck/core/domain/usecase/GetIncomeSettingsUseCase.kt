package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow

/** How the user receives their income. */
class GetIncomeSettingsUseCase(
    private val repository: IncomeRepository,
) {
    /** Emits the income settings; null until the user sets them up. */
    operator fun invoke(): Flow<IncomeSettings?> = repository.observeSettings()
}
