package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow

/** Whether amounts are hidden by the eye of the toolbar. */
class GetAmountsHiddenUseCase(
    private val repository: AppPreferencesRepository,
) {
    /** Emits true while amounts are hidden. */
    operator fun invoke(): Flow<Boolean> = repository.observeAmountsHidden()
}
