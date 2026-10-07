package com.bragadev.fincheck.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How the user receives their income. The app has a single row ([SINGLE_ROW_ID]): the income
 * belongs to the user, not to a list, so every list shows its cycles from the same settings.
 */
@Entity(tableName = "income_settings")
data class IncomeSettingsEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    /** [com.bragadev.fincheck.core.domain.model.IncomeFrequency.code]. */
    val frequency: Int,
    val firstPayDay: Int,
    val firstAmountInCents: Long,
    /** Second payment of the month; null for monthly income. */
    val secondPayDay: Int?,
    val secondAmountInCents: Long?,
    /** Date of the next payment as days since 01/01/1970 (no time zone involved). */
    val nextPaymentEpochDay: Long,
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
