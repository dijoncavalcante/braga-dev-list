package com.bragadev.fincheck.core.data.mapper

import com.bragadev.fincheck.core.database.ExtraIncomeEntity
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.ExtraIncome
import com.bragadev.fincheck.core.domain.model.ExtraIncomeRecurrence

/** Null for a corrupted row (neither a day nor a date), so it is skipped instead of crashing. */
fun ExtraIncomeEntity.toDomain(): ExtraIncome? {
    val recurrence = when {
        dayOfMonth != null -> ExtraIncomeRecurrence.Monthly(dayOfMonth)
        dateEpochDay != null -> ExtraIncomeRecurrence.Once(CalendarDate.fromEpochDay(dateEpochDay))
        else -> return null
    }
    return ExtraIncome(id = id, name = name, amountInCents = amountInCents, recurrence = recurrence)
}

fun ExtraIncome.toEntity(): ExtraIncomeEntity = ExtraIncomeEntity(
    id = id,
    name = name,
    amountInCents = amountInCents,
    dayOfMonth = (recurrence as? ExtraIncomeRecurrence.Monthly)?.dayOfMonth,
    dateEpochDay = (recurrence as? ExtraIncomeRecurrence.Once)?.date?.toEpochDay(),
)
