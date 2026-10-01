package com.bragadev.list.core.domain.model

/** From this share of the income on (in %), a cycle is considered tight. */
const val TIGHT_CYCLE_PERCENT = 90

/** Percentages are calculated on a 0..100 scale. */
private const val PERCENT = 100

/** Months before and after today searched for the payments around it. */
private const val MONTHS_SEARCHED_BEFORE = 2
private const val MONTHS_SEARCHED_AFTER = 3

/**
 * An item counted in a financial cycle.
 *
 * @param dueDate the actual date the bill falls due in the cycle; null for items without a due
 * day ("outras despesas"), which have no date of their own.
 */
data class CycleExpense(val item: ShoppingListItem, val dueDate: CalendarDate?) {
    /** Unit price × quantity, in cents. */
    val amountInCents: Long get() = item.priceInCents * item.quantity
}

/** How much of the cycle's income its expenses take. */
enum class CycleStatus {
    /** Expenses below [TIGHT_CYCLE_PERCENT] of the income. */
    HEALTHY,

    /** Expenses between [TIGHT_CYCLE_PERCENT] and 100% of the income. */
    TIGHT,

    /** Expenses larger than the income. */
    OVER_BUDGET,
}

/**
 * The money of one payment and everything it has to cover until the next payment arrives.
 *
 * It starts on the day [payment] is received and ends the day before [nextPayment], so it can
 * span two months (25/09 → 09/10) and its length follows the user's own pay days, never a fixed
 * 1–15 / 16–31 split.
 *
 * @param bills items with a due day that falls in the cycle, oldest first ("Contas").
 * @param otherExpenses items without a due day, counted in the current cycle only ("Outras despesas").
 * @param extraIncomes extra incomes dated inside the cycle, oldest first ("Outras entradas").
 */
data class FinancialCycle(
    val payment: Payment,
    val nextPayment: Payment,
    val bills: List<CycleExpense>,
    val otherExpenses: List<CycleExpense> = emptyList(),
    val extraIncomes: List<ExtraIncomeEntry> = emptyList(),
) {
    val startDate: CalendarDate get() = payment.date

    val endDate: CalendarDate get() = nextPayment.date.plusDays(-1)

    val extraIncomeInCents: Long get() = extraIncomes.sumOf { it.amountInCents }

    /** Everything that comes in during the cycle: the payment that opens it plus the extra incomes. */
    val incomeInCents: Long get() = payment.amountInCents + extraIncomeInCents

    val billsInCents: Long get() = bills.sumOf { it.amountInCents }

    val otherExpensesInCents: Long get() = otherExpenses.sumOf { it.amountInCents }

    val expensesInCents: Long get() = billsInCents + otherExpensesInCents

    /** What should be left once every expense of the cycle is paid; negative = missing money. */
    val projectedBalanceInCents: Long get() = incomeInCents - expensesInCents

    /** Share of the income already taken by the expenses (may pass 100); null when there is no income. */
    val committedPercent: Int?
        get() = if (incomeInCents > 0) (expensesInCents * PERCENT / incomeInCents).toInt() else null

    val status: CycleStatus
        get() = when {
            expensesInCents > incomeInCents -> CycleStatus.OVER_BUDGET
            expensesInCents * PERCENT >= incomeInCents * TIGHT_CYCLE_PERCENT -> CycleStatus.TIGHT
            else -> CycleStatus.HEALTHY
        }

    operator fun contains(date: CalendarDate): Boolean = date in startDate..endDate
}

/**
 * Where the user is today: the cycle they are in (last payment → day before the next one) and
 * the one that starts with the next payment.
 */
data class FinancialOverview(
    val today: CalendarDate,
    val current: FinancialCycle,
    val next: FinancialCycle,
) {
    val lastPayment: Payment get() = current.payment

    val nextPayment: Payment get() = current.nextPayment

    /** Always at least 1: on a pay day that payment is already the current cycle's. */
    val daysUntilNextPayment: Long get() = today.daysUntil(nextPayment.date)

    /** Bills of the current cycle not yet checked that still fall due before the next payment. */
    val billsDueBeforeNextPayment: List<CycleExpense>
        get() = current.bills.filter { !it.item.isChecked && it.dueDate != null && it.dueDate >= today }

    /** Bills of the current cycle whose due date has passed and are still not checked. */
    val overdueBills: List<CycleExpense>
        get() = current.bills.filter { !it.item.isChecked && it.dueDate != null && it.dueDate < today }

    /** Extra incomes of the current cycle that have not arrived yet (dated after today). */
    val upcomingExtraIncomes: List<ExtraIncomeEntry>
        get() = current.extraIncomes.filter { it.date > today }

    /**
     * Bills not yet checked that fall due before the first upcoming extra income, when the money
     * received so far does not cover the cycle's expenses: they depend on money that is not in yet.
     */
    val billsDueBeforeExtraIncome: List<CycleExpense>
        get() {
            val firstExtra = upcomingExtraIncomes.firstOrNull() ?: return emptyList()
            val receivedSoFar = current.incomeInCents - upcomingExtraIncomes.sumOf { it.amountInCents }
            if (current.expensesInCents <= receivedSoFar) return emptyList()
            return billsDueBeforeNextPayment.filter { it.dueDate != null && it.dueDate < firstExtra.date }
        }

    /** Feedbacks about the current moment, most important first. */
    fun insights(): List<CycleInsight> = buildList {
        add(CycleInsight.Received(lastPayment))
        add(CycleInsight.NextPayment(nextPayment, daysUntilNextPayment))
        upcomingExtraIncomes.takeIf { it.isNotEmpty() }?.let { add(CycleInsight.ExtraIncomeExpected(it)) }
        add(CycleInsight.Balance(current))
        overdueBills.takeIf { it.isNotEmpty() }?.let { add(CycleInsight.OverdueBills(it)) }
        billsDueBeforeNextPayment.takeIf { it.isNotEmpty() }?.let { add(CycleInsight.BillsDueBeforeNextPayment(it)) }
        billsDueBeforeExtraIncome.takeIf { it.isNotEmpty() }?.let {
            add(CycleInsight.BillsDueBeforeExtraIncome(it, upcomingExtraIncomes.first()))
        }
        if (next.status == CycleStatus.OVER_BUDGET) add(CycleInsight.NextCycleOverBudget(next))
    }
}

/** One feedback of [FinancialOverview.insights], built only from the user's real data. */
sealed interface CycleInsight {
    /** "Você recebeu R$ 2.500 em 25/09." */
    data class Received(val payment: Payment) : CycleInsight

    /** "Faltam 3 dias para o próximo recebimento de R$ 2.000 (10/10)." */
    data class NextPayment(val payment: Payment, val daysUntil: Long) : CycleInsight

    /** "Você ainda vai receber R$ 1.200 de Aluguel em 15/10, neste ciclo." */
    data class ExtraIncomeExpected(val entries: List<ExtraIncomeEntry>) : CycleInsight

    /** Bills that fall due before [extraIncome] arrives while the cycle still needs that money. */
    data class BillsDueBeforeExtraIncome(val bills: List<CycleExpense>, val extraIncome: ExtraIncomeEntry) : CycleInsight

    /** Income × expenses of the current cycle; the wording follows [FinancialCycle.status]. */
    data class Balance(val cycle: FinancialCycle) : CycleInsight

    /** Bills that still fall due before the next payment. */
    data class BillsDueBeforeNextPayment(val bills: List<CycleExpense>) : CycleInsight

    /** Bills whose due date has passed without being checked. */
    data class OverdueBills(val bills: List<CycleExpense>) : CycleInsight

    /** The next cycle already has more expenses than income. */
    data class NextCycleOverBudget(val cycle: FinancialCycle) : CycleInsight
}

/**
 * Builds the current and next financial cycles of [items] from the user's income, as of [today].
 *
 * Bills repeat every month on their due day (a day past the end of a short month falls on its
 * last day), so each cycle holds the occurrences dated inside it. Items without a due day are
 * expenses of the current cycle. [extraIncomes] add to the cycle their dates fall in, without
 * changing where cycles start or end. Returns null when the schedule produces no payments.
 */
fun buildFinancialOverview(
    settings: IncomeSettings,
    items: List<ShoppingListItem>,
    today: CalendarDate,
    extraIncomes: List<ExtraIncome> = emptyList(),
): FinancialOverview? {
    // Wide enough for a monthly payment adjusted to a later date on both sides of today.
    val payments = settings.toSchedule().paymentsBetween(
        today.firstDayOfMonthPlus(-MONTHS_SEARCHED_BEFORE),
        today.firstDayOfMonthPlus(MONTHS_SEARCHED_AFTER + 1).plusDays(-1),
    )
    val last = payments.lastOrNull { it.date <= today }
    val next = payments.firstOrNull { it.date > today }
    val afterNext = next?.let { nextPayment -> payments.firstOrNull { it.date > nextPayment.date } }
    if (last == null || next == null || afterNext == null) return null

    val (withDueDay, withoutDueDay) = items.partition { it.dueDay != null }
    return FinancialOverview(
        today = today,
        current = FinancialCycle(
            payment = last,
            nextPayment = next,
            bills = withDueDay.billsBetween(last.date, next.date.plusDays(-1)),
            otherExpenses = withoutDueDay.map { CycleExpense(it, dueDate = null) },
            extraIncomes = extraIncomes.entriesBetween(last.date, next.date.plusDays(-1)),
        ),
        next = FinancialCycle(
            payment = next,
            nextPayment = afterNext,
            bills = withDueDay.billsBetween(next.date, afterNext.date.plusDays(-1)),
            extraIncomes = extraIncomes.entriesBetween(next.date, afterNext.date.plusDays(-1)),
        ),
    )
}

private fun List<ExtraIncome>.entriesBetween(from: CalendarDate, to: CalendarDate): List<ExtraIncomeEntry> =
    flatMap { income -> income.datesBetween(from, to).map { ExtraIncomeEntry(income, it) } }.sortedBy { it.date }

/** Every due date of these items between [from] and [to], oldest first (same date keeps the list order). */
private fun List<ShoppingListItem>.billsBetween(from: CalendarDate, to: CalendarDate): List<CycleExpense> {
    val months = generateSequence(from.firstDayOfMonthPlus(0)) { it.firstDayOfMonthPlus(1) }.takeWhile { it <= to }
    return flatMap { item ->
        val dueDay = item.dueDay ?: return@flatMap emptyList()
        months.map { CalendarDate.ofDayClamped(it.year, it.month, dueDay) }
            .filter { it in from..to }
            .map { CycleExpense(item, it) }
            .toList()
    }.sortedBy { it.dueDate }
}
