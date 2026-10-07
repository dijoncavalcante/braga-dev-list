package com.bragadev.fincheck.core.domain.repository

import kotlinx.coroutines.flow.Flow

/** User preferences that apply to the whole app, not to a single list. */
interface AppPreferencesRepository {
    /** Amounts hidden by the eye of the toolbar; false until the user hides them. */
    fun observeAmountsHidden(): Flow<Boolean>

    suspend fun setAmountsHidden(hidden: Boolean)
}
