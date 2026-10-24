package com.bragadev.fincheck.core.data.mapper

import com.bragadev.fincheck.core.database.BillEntity
import com.bragadev.fincheck.core.database.BillMonthStatusEntity
import com.bragadev.fincheck.core.domain.model.Bill
import com.bragadev.fincheck.core.domain.model.BillMonthStatus
import com.bragadev.fincheck.core.domain.model.YearMonth

/** Converts a stored bill row into its domain [Bill], turning month indexes back into [YearMonth]s. */
fun BillEntity.toDomain(): Bill =
    Bill(
        id = id,
        name = name,
        priceInCents = priceInCents,
        dueDay = dueDay,
        startMonth = YearMonth.fromIndex(startMonth),
        endMonth = endMonth?.let(YearMonth::fromIndex),
        createdAt = createdAt,
        description = description,
    )

/** Converts a domain [Bill] into the row stored in the `bills` table. */
fun Bill.toEntity(): BillEntity =
    BillEntity(
        id = id,
        name = name,
        priceInCents = priceInCents,
        dueDay = dueDay,
        startMonth = startMonth.index,
        endMonth = endMonth?.index,
        createdAt = createdAt,
        description = description,
    )

/** Converts a stored month status row into its domain [BillMonthStatus]. */
fun BillMonthStatusEntity.toDomain(): BillMonthStatus =
    BillMonthStatus(
        billId = billId,
        month = YearMonth.fromIndex(month),
        isPaid = isPaid,
        isSkipped = isSkipped,
    )
