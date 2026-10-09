package com.bragadev.fincheck.features.listdetail.presentation.component

import androidx.compose.runtime.saveable.Saver
import com.bragadev.fincheck.core.domain.model.CalendarDate

/** Stands for "no date" in [OptionalCalendarDateSaver] (epoch days of real dates are never this low). */
private const val NO_DATE = Long.MIN_VALUE

/**
 * Keeps an optional date typed in a form across rotation and other configuration changes
 * (rememberSaveable), stored as its epoch day.
 */
internal val OptionalCalendarDateSaver = Saver<CalendarDate?, Long>(
    save = { date -> date?.toEpochDay() ?: NO_DATE },
    restore = { epochDay -> if (epochDay == NO_DATE) null else CalendarDate.fromEpochDay(epochDay) },
)
