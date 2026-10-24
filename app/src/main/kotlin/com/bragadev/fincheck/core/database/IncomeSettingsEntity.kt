package com.bragadev.fincheck.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How the user receives their income. The app has a single row ([SINGLE_ROW_ID]): the income
 * belongs to the user, not to a list, so every list shows its cycles from the same settings.
 */
@Entity(tableName = "income_settings")
data class IncomeSettingsEntity(
    /** Always [SINGLE_ROW_ID]: there is only one row. */
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    /** [com.bragadev.fincheck.core.domain.model.IncomeFrequency.code]. */
    val frequency: Int,
    /** Day of the month (1..31) of the first, or only, payment. */
    val firstPayDay: Int,
    /** Amount of the first, or only, payment, in cents. */
    val firstAmountInCents: Long,
    /** Second payment of the month; null for monthly income. */
    val secondPayDay: Int?,
    /** Amount of the second payment, in cents; null for monthly income. */
    val secondAmountInCents: Long?,
    /** Date of the next payment as days since 01/01/1970 (no time zone involved). */
    val nextPaymentEpochDay: Long,
) {
    companion object {
        /** Id of the only row of the table. */
        const val SINGLE_ROW_ID = 1
    }
}
