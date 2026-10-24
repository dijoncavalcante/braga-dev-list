package com.bragadev.fincheck.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A bill of the "Meses" tab, stored once as a rule (see [com.bragadev.fincheck.core.domain.model.Bill]).
 * Months are stored as [com.bragadev.fincheck.core.domain.model.YearMonth.index].
 */
@Entity(tableName = "bills")
data class BillEntity(
    /** Generated id; 0 before the bill is saved. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Name shown in the month, such as "Internet". */
    val name: String,
    /** In cents (R$ 12,50 -> 1250). */
    val priceInCents: Long,
    /** 1..31; null = no due day. */
    val dueDay: Int?,
    /** First month it shows up in, as a [com.bragadev.fincheck.core.domain.model.YearMonth.index]. */
    val startMonth: Int,
    /** Last month it shows up in; null = every month, with no end. */
    val endMonth: Int?,
    /** Creation time in epoch milliseconds; keeps bills in the order they were added. */
    val createdAt: Long,
    /** Free note; null = none. */
    val description: String? = null,
)

/** What a bill did in one month; only months the user touched have a row. */
@Entity(
    tableName = "bill_month_status",
    primaryKeys = ["billId", "month"],
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("billId")],
)
/** What a bill did in one month; only months the user touched have a row. */
data class BillMonthStatusEntity(
    /** The bill this status belongs to; deleted together with it. */
    val billId: Long,
    /** The month, as a [com.bragadev.fincheck.core.domain.model.YearMonth.index]. */
    val month: Int,
    /** Whether the bill was paid in that month. */
    val isPaid: Boolean,
    /** Whether the bill was left out of that month ("Só este mês" edits and deletes). */
    val isSkipped: Boolean,
)
