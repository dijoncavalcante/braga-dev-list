package com.bragadev.fincheck.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Room access to the single row of `income_settings`. */
@Dao
interface IncomeSettingsDao {

    /** Null until the user sets up their income. */
    @Query("SELECT * FROM income_settings WHERE id = ${IncomeSettingsEntity.SINGLE_ROW_ID}")
    fun observe(): Flow<IncomeSettingsEntity?>

    /** Creates or replaces the income settings row. */
    @Upsert
    suspend fun upsert(settings: IncomeSettingsEntity)
}
