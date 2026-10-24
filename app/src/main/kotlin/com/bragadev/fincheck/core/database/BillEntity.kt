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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** In cents (R$ 12,50 -> 1250). */
    val priceInCents: Long,
    /** 1..31; null = no due day. */
    val dueDay: Int?,
    val startMonth: Int,
    /** Last month it shows up in; null = every month, with no end. */
    val endMonth: Int?,
    val createdAt: Long,
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
data class BillMonthStatusEntity(
    val billId: Long,
    val month: Int,
    val isPaid: Boolean,
    val isSkipped: Boolean,
)
