package com.bragadev.fincheck.core.domain.model

/**
 * A bill of the "Meses" tab, stored once as a rule: it shows up in every month from
 * [startMonth] to [endMonth], so no month is ever created or copied ahead of time.
 *
 * - "Só este mês": [endMonth] == [startMonth];
 * - "Todo mês": [endMonth] == null, with no end;
 * - "Por N meses": [endMonth] == [startMonth] + N - 1, e.g. an installment purchase.
 *
 * What changes from one month to another (paid or not) lives in [BillMonthStatus].
 *
 * @param id 0 for a bill not saved yet.
 * @param dueDay day of the month the bill is due (1..31; shorter months fall on their last day);
 * null for expenses without a fixed day, such as groceries ("Outras despesas").
 * @param description free note ("débito automático", "cartão final 1234"); null = none.
 */
data class Bill(
    val id: Long = 0,
    /** Name shown in the month, such as "Internet". */
    val name: String,
    /** Amount of each month, in cents. */
    val priceInCents: Long,
    val dueDay: Int?,
    /** First month the bill shows up in. */
    val startMonth: YearMonth,
    /** Last month it shows up in; null for "Todo mês", with no end. */
    val endMonth: YearMonth?,
    /** Creation time in epoch milliseconds; bills with the same due day keep this order. */
    val createdAt: Long = 0,
    /** Free note such as "débito automático"; null = none. At most [MAX_DESCRIPTION_LENGTH] characters. */
    val description: String? = null,
) {
    /** Shows up in more than one month. */
    val isRecurring: Boolean get() = endMonth != startMonth

    /** Months it lasts; null for "Todo mês". */
    val monthCount: Int? get() = endMonth?.let { startMonth.monthsUntil(it) + 1 }

    /** The "Repete?" choice this bill was saved with, worked out from its start and end months. */
    val repetition: BillRepetition
        get() =
            when (val count = monthCount) {
                null -> BillRepetition.Monthly
                1 -> BillRepetition.Once
                else -> BillRepetition.ForMonths(count)
            }

    /** Whether the bill shows up in [month], ignoring months skipped by [BillMonthStatus]. */
    fun isActiveIn(month: YearMonth): Boolean = month >= startMonth && (endMonth == null || month <= endMonth)

    /** Actual due date in [month], or null without a due day. */
    fun dueDateIn(month: YearMonth): CalendarDate? = dueDay?.let(month::dayClamped)

    /** "3 de 6" for a bill that lasts a fixed number of months; null otherwise. */
    fun installmentIn(month: YearMonth): Installment? {
        val count = monthCount?.takeIf { it > 1 } ?: return null
        return Installment(number = startMonth.monthsUntil(month) + 1, count = count)
    }
}

/** "Repete?" choice of the bill form. */
sealed interface BillRepetition {
    /** Only in the month it starts. */
    data object Once : BillRepetition

    /** Every month, with no end. */
    data object Monthly : BillRepetition

    /** [count] months in a row (2 or more), the first one included. */
    data class ForMonths(
        /** How many months, 2 or more. */
        val count: Int,
    ) : BillRepetition

    /** Last month of a bill that starts in [start]; null when it never ends. */
    fun endMonth(start: YearMonth): YearMonth? =
        when (this) {
            Once -> start
            Monthly -> null
            is ForMonths -> start.plusMonths(count - 1)
        }
}

/** Which month of a fixed-length bill this is: 3 of 6. */
data class Installment(
    /** Position of this month, starting at 1. */
    val number: Int,
    /** How many months the bill lasts. */
    val count: Int,
)

/**
 * What a bill did in one month. Only months the user touched have one: no row means not paid.
 *
 * @param isSkipped the bill does not show up in this month ("Excluir só este mês", or an edit
 * made only for this month, which lives in a one-month bill of its own).
 */
data class BillMonthStatus(
    /** The bill this status belongs to. */
    val billId: Long,
    /** The month it applies to. */
    val month: YearMonth,
    /** The bill was paid in this month. */
    val isPaid: Boolean = false,
    val isSkipped: Boolean = false,
)

/** One bill as it shows up in one month. */
data class MonthBill(
    /** The bill rule. */
    val bill: Bill,
    /** The month it is shown in. */
    val month: YearMonth,
    /** Whether it was paid in [month]. */
    val isPaid: Boolean,
) {
    /** Name of the bill. */
    val name: String get() = bill.name

    /** Amount of the bill in this month, in cents. */
    val amountInCents: Long get() = bill.priceInCents

    /** Actual due date in [month]; null for bills without a due day. */
    val dueDate: CalendarDate? get() = bill.dueDateIn(month)

    /** "3 de 6" for a bill that lasts a fixed number of months; null otherwise. */
    val installment: Installment? get() = bill.installmentIn(month)

    /** Its due date has passed and it is still not paid. */
    fun isOverdue(today: CalendarDate): Boolean = !isPaid && dueDate?.let { it < today } == true
}

/** Every bill and every month status, as read from the database. */
data class BillBook(
    /** Every bill rule, in the order they were added. */
    val bills: List<Bill> = emptyList(),
    /** Every stored month status. */
    val statuses: List<BillMonthStatus> = emptyList(),
) {
    /** Statuses indexed by bill id and month index, built once on first use. */
    private val statusByKey: Map<Pair<Long, Int>, BillMonthStatus> by lazy {
        statuses.associateBy { it.billId to it.month.index }
    }

    /** What bill [billId] did in [month]; null when the user never touched that month. */
    fun statusOf(
        billId: Long,
        month: YearMonth,
    ): BillMonthStatus? = statusByKey[billId to month.index]

    /**
     * Bills of [month]: due day first (those without one at the end), then in the order they were
     * added. Skipped months are left out.
     */
    fun billsOf(month: YearMonth): List<MonthBill> =
        bills
            .filter { it.isActiveIn(month) && statusOf(it.id, month)?.isSkipped != true }
            .map { MonthBill(bill = it, month = month, isPaid = statusOf(it.id, month)?.isPaid == true) }
            .sortedWith(compareBy<MonthBill> { it.bill.dueDay == null }.thenBy { it.bill.dueDay }.thenBy { it.bill.createdAt })

    /** Bills with a due date between [from] and [to] (both included), oldest first. */
    fun billsDueBetween(
        from: CalendarDate,
        to: CalendarDate,
    ): List<MonthBill> =
        YearMonth
            .of(from)
            .monthsTo(YearMonth.of(to))
            .flatMap { month -> billsOf(month).filter { bill -> bill.dueDate?.let { it in from..to } == true } }
            .sortedBy { it.dueDate }
}

/** Totals of the bills of a month. */
data class MonthBillsTotals(
    /** Sum of every bill, in cents. */
    val totalInCents: Long,
    /** Sum of the paid bills, in cents. */
    val paidInCents: Long,
    /** How many bills. */
    val count: Int,
    /** How many of them are paid. */
    val paidCount: Int,
) {
    /** What is still to pay, in cents. */
    val unpaidInCents: Long get() = totalInCents - paidInCents
}

/** Totals of these bills: everything, what was paid and how many of each. */
fun List<MonthBill>.totals(): MonthBillsTotals =
    MonthBillsTotals(
        totalInCents = sumOf { it.amountInCents },
        paidInCents = filter { it.isPaid }.sumOf { it.amountInCents },
        count = size,
        paidCount = count { it.isPaid },
    )

/** Which months a change to a recurring bill applies to, asked when it is saved or deleted. */
enum class BillEditScope {
    /** "Só este mês": the other months stay as they were. */
    ONLY_THIS_MONTH,

    /** "Este e os próximos": the months before stay as they were. */
    THIS_AND_NEXT,
}
