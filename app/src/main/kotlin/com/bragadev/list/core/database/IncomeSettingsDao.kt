package com.bragadev.list.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeSettingsDao {

    /** Null until the user sets up their income. */
    @Query("SELECT * FROM income_settings WHERE id = ${IncomeSettingsEntity.SINGLE_ROW_ID}")
    fun observe(): Flow<IncomeSettingsEntity?>

    @Upsert
    suspend fun upsert(settings: IncomeSettingsEntity)
}
