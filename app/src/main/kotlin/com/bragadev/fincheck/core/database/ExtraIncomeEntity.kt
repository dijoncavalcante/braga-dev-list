package com.bragadev.fincheck.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Money received besides the main income. Exactly one of [dayOfMonth] (every month) and
 * [dateEpochDay] (only once) is set.
 */
@Entity(tableName = "extra_incomes")
data class ExtraIncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountInCents: Long,
    /** Day of the month it arrives (1..31) when it repeats every month. */
    val dayOfMonth: Int?,
    /** Date it arrives, as days since 01/01/1970, when it comes only once. */
    val dateEpochDay: Long?,
)
