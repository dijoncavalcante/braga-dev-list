package com.bragadev.fincheck.core.domain.usecase

import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository

/** The eye of the toolbar: hides or shows the amounts of every list. */
class SetAmountsHiddenUseCase(
    private val repository: AppPreferencesRepository,
) {
    suspend operator fun invoke(hidden: Boolean) = repository.setAmountsHidden(hidden)
}
