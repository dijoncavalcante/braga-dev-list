package com.bragadev.fincheck.core.data.mapper

import com.bragadev.fincheck.core.database.IncomeSettingsEntity
import com.bragadev.fincheck.core.domain.model.CalendarDate
import com.bragadev.fincheck.core.domain.model.IncomeFrequency
import com.bragadev.fincheck.core.domain.model.IncomeSettings
import com.bragadev.fincheck.core.domain.model.PayDay

fun IncomeSettingsEntity.toDomain(): IncomeSettings {
    val second = if (secondPayDay != null && secondAmountInCents != null) {
        PayDay(dayOfMonth = secondPayDay, amountInCents = secondAmountInCents)
    } else {
        null
    }
    return IncomeSettings(
        frequency = IncomeFrequency.fromCode(frequency),
        payDays = listOfNotNull(PayDay(dayOfMonth = firstPayDay, amountInCents = firstAmountInCents), second),
        nextPaymentDate = CalendarDate.fromEpochDay(nextPaymentEpochDay),
    )
}

fun IncomeSettings.toEntity(): IncomeSettingsEntity {
    val first = payDays.first()
    val second = payDays.getOrNull(1)
    return IncomeSettingsEntity(
        frequency = frequency.code,
        firstPayDay = first.dayOfMonth,
        firstAmountInCents = first.amountInCents,
        secondPayDay = second?.dayOfMonth,
        secondAmountInCents = second?.amountInCents,
        nextPaymentEpochDay = nextPaymentDate.toEpochDay(),
    )
}
