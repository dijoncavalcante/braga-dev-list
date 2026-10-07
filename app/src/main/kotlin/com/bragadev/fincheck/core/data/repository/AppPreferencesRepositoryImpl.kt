package com.bragadev.fincheck.core.data.repository

import android.content.SharedPreferences
import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

private const val KEY_AMOUNTS_HIDDEN = "amounts_hidden"

/**
 * App-wide preferences kept in [SharedPreferences]: a couple of flags do not need a database
 * table or a migration. Every change is emitted, so all open screens follow it.
 */
class AppPreferencesRepositoryImpl(
    private val preferences: SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher,
) : AppPreferencesRepository {

    override fun observeAmountsHidden(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == KEY_AMOUNTS_HIDDEN) trySend(prefs.getBoolean(KEY_AMOUNTS_HIDDEN, false))
        }
        trySend(preferences.getBoolean(KEY_AMOUNTS_HIDDEN, false))
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override suspend fun setAmountsHidden(hidden: Boolean) {
        withContext(ioDispatcher) { preferences.edit().putBoolean(KEY_AMOUNTS_HIDDEN, hidden).apply() }
    }
}
