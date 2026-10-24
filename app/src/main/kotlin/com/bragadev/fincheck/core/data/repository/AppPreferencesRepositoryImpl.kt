package com.bragadev.fincheck.core.data.repository

import android.content.SharedPreferences
import com.bragadev.fincheck.core.domain.model.ItemViewMode
import com.bragadev.fincheck.core.domain.model.YearMonth
import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

/** Key of the "amounts hidden" flag. */
private const val KEY_AMOUNTS_HIDDEN = "amounts_hidden"
/** Key of the "Visualizar" choice of the "Meses" tab, stored as an [ItemViewMode.code]. */
private const val KEY_MONTH_VIEW_MODE = "month_view_mode"
/** Key of the first month of the "Meses" tab, stored as a [YearMonth.index]. */
private const val KEY_FIRST_MONTH = "first_month"

/**
 * App-wide preferences kept in [SharedPreferences]: a couple of flags do not need a database
 * table or a migration. Every change is emitted, so all open screens follow it.
 */
class AppPreferencesRepositoryImpl(
    private val preferences: SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher,
) : AppPreferencesRepository {
    override fun observeAmountsHidden(): Flow<Boolean> = observeKey(KEY_AMOUNTS_HIDDEN) { getBoolean(it, false) }

    override suspend fun setAmountsHidden(hidden: Boolean) {
        withContext(ioDispatcher) { preferences.edit().putBoolean(KEY_AMOUNTS_HIDDEN, hidden).apply() }
    }

    override fun observeMonthViewMode(): Flow<ItemViewMode> =
        observeKey(KEY_MONTH_VIEW_MODE) { ItemViewMode.fromCode(getInt(it, ItemViewMode.ALL.code)) }

    override suspend fun setMonthViewMode(viewMode: ItemViewMode) {
        withContext(ioDispatcher) { preferences.edit().putInt(KEY_MONTH_VIEW_MODE, viewMode.code).apply() }
    }

    override suspend fun getOrInitFirstMonth(currentMonth: YearMonth): YearMonth =
        withContext(ioDispatcher) {
            if (preferences.contains(KEY_FIRST_MONTH)) {
                YearMonth.fromIndex(preferences.getInt(KEY_FIRST_MONTH, currentMonth.index))
            } else {
                preferences.edit().putInt(KEY_FIRST_MONTH, currentMonth.index).apply()
                currentMonth
            }
        }

    /** Current value of [key], then again whenever it changes. */
    private fun <T> observeKey(
        key: String,
        read: SharedPreferences.(String) -> T,
    ): Flow<T> =
        callbackFlow {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
                    if (changedKey == key) trySend(prefs.read(key))
                }
            trySend(preferences.read(key))
            preferences.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged()
}
