package com.bragadev.fincheck.core.domain.model

/** From this share of the income on (in %), a cycle is considered tight. */
const val TIGHT_CYCLE_PERCENT = 90

/** Percentages are calculated on a 0..100 scale. */
private const val PERCENT = 100

/** Months before and after today searched for the payments around it. */
private const val MONTHS_SEARCHED_BEFORE = 2
/** Months after today searched for the payments around it. */
private const val MONTHS_SEARCHED_AFTER = 3

/**
 * A bill counted in a financial cycle, in the month it falls due.
 *
 * [dueDate] is the actual date the bill falls due in the cycle; null for bills without a due day
 * ("outras despesas"), which have no date of their own.
 */
data class CycleExpense(val bill: MonthBill) {
    /** Name of the bill. */
    val name: String get() = bill.name

    /** Whether it was paid in its month. */
    val isPaid: Boolean get() = bill.isPaid

    /** Actual due date; null for bills without a due day. */
    val dueDate: CalendarDate? get() = bill.dueDate

    /** Amount of the bill, in cents. */
    val amountInCents: Long get() = bill.amountInCents
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
    /** The payment that opens the cycle. */
    val payment: Payment,
    /** The payment that opens the next cycle; the cycle ends the day before it. */
    val nextPayment: Payment,
    val bills: List<CycleExpense>,
    val otherExpenses: List<CycleExpense> = emptyList(),
    val extraIncomes: List<ExtraIncomeEntry> = emptyList(),
) {
    /** First day of the cycle: the day [payment] arrives. */
    val startDate: CalendarDate get() = payment.date

    /** Last day of the cycle: the day before [nextPayment] arrives. */
    val endDate: CalendarDate get() = nextPayment.date.plusDays(-1)

    /** Sum of the extra incomes of the cycle, in cents. */
    val extraIncomeInCents: Long get() = extraIncomes.sumOf { it.amountInCents }

    /** Everything that comes in during the cycle: the payment that opens it plus the extra incomes. */
    val incomeInCents: Long get() = payment.amountInCents + extraIncomeInCents

    /** Sum of the bills with a due day, in cents. */
    val billsInCents: Long get() = bills.sumOf { it.amountInCents }

    /** Sum of the expenses without a due day, in cents. */
    val otherExpensesInCents: Long get() = otherExpenses.sumOf { it.amountInCents }

    /** Everything the cycle has to pay, in cents. */
    val expensesInCents: Long get() = billsInCents + otherExpensesInCents

    /** What should be left once every expense of the cycle is paid; negative = missing money. */
    val projectedBalanceInCents: Long get() = incomeInCents - expensesInCents

    /** Share of the income already taken by the expenses (may pass 100); null when there is no income. */
    val committedPercent: Int?
        get() = if (incomeInCents > 0) (expensesInCents * PERCENT / incomeInCents).toInt() else null

    /** Healthy, tight or over budget, from how much of the income the expenses take. */
    val status: CycleStatus
        get() = when {
            expensesInCents > incomeInCents -> CycleStatus.OVER_BUDGET
            expensesInCents * PERCENT >= incomeInCents * TIGHT_CYCLE_PERCENT -> CycleStatus.TIGHT
            else -> CycleStatus.HEALTHY
        }

    /** Whether [date] falls between the first and last day of the cycle. */
    operator fun contains(date: CalendarDate): Boolean = date in startDate..endDate
}

/**
 * Where the user is today: the cycle they are in (last payment → day before the next one) and
 * the one that starts with the next payment.
 */
data class FinancialOverview(
    /** Today, or the reference day of the month shown (see [isLive]). */
    val today: CalendarDate,
    /** The cycle [today] is in. */
    val current: FinancialCycle,
    /** The cycle that starts with the next payment. */
    val next: FinancialCycle,
    /**
     * The cycles are those of the month the user is in. False for another month: [today] is then
     * that month's first pay day, the cycles are a forecast (or history) and no alert is raised.
     */
    val isLive: Boolean = true,
) {
    /** The payment that opened the current cycle. */
    val lastPayment: Payment get() = current.payment

    /** The payment that closes the current cycle and opens the next one. */
    val nextPayment: Payment get() = current.nextPayment

    /** Last day covered by the two cycles together. */
    val periodEndDate: CalendarDate get() = next.endDate

    /**
     * What should be left at [periodEndDate] once every expense of both cycles is paid: the two
     * projected balances added up, so the user sees the whole period (about a month when paid twice
     * a month) in one number. Negative = missing money.
     */
    val projectedBalanceInCents: Long get() = current.projectedBalanceInCents + next.projectedBalanceInCents

    /**
     * The one alert worth interrupting the user for, or null when everything is fine: bills overdue
     * first, then bills that fall due before the extra income they depend on. Facts the screen
     * already shows as numbers (received, balance, next payment) are never repeated as alerts.
     */
    val mainAlert: CycleInsight?
        get() = if (!isLive) {
            null
        } else {
            insights().firstOrNull { it is CycleInsight.OverdueBills || it is CycleInsight.BillsDueBeforeExtraIncome }
        }

    /** Always at least 1: on a pay day that payment is already the current cycle's. */
    val daysUntilNextPayment: Long get() = today.daysUntil(nextPayment.date)

    /** Bills of the current cycle not yet paid that still fall due before the next payment. */
    val billsDueBeforeNextPayment: List<CycleExpense>
        get() = current.bills.filter { !it.isPaid && it.dueDate?.let { date -> date >= today } == true }

    /** Bills of the current cycle whose due date has passed and are still not paid. */
    val overdueBills: List<CycleExpense>
        get() = current.bills.filter { !it.isPaid && it.dueDate?.let { date -> date < today } == true }

    /** Extra incomes of the current cycle that have not arrived yet (dated after today). */
    val upcomingExtraIncomes: List<ExtraIncomeEntry>
        get() = current.extraIncomes.filter { it.date > today }

    /**
     * Bills not yet paid that fall due before the first upcoming extra income, when the money
     * received so far does not cover the cycle's expenses: they depend on money that is not in yet.
     */
    val billsDueBeforeExtraIncome: List<CycleExpense>
        get() {
            val firstExtra = upcomingExtraIncomes.firstOrNull() ?: return emptyList()
            val receivedSoFar = current.incomeInCents - upcomingExtraIncomes.sumOf { it.amountInCents }
            if (current.expensesInCents <= receivedSoFar) return emptyList()
            return billsDueBeforeNextPayment.filter { it.dueDate?.let { date -> date < firstExtra.date } == true }
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

    /** Bills whose due date has passed without being paid. */
    data class OverdueBills(val bills: List<CycleExpense>) : CycleInsight

    /** The next cycle already has more expenses than income. */
    data class NextCycleOverBudget(val cycle: FinancialCycle) : CycleInsight
}

/**
 * Builds the current and next financial cycles of [month] from the user's income.
 *
 * In the month the user is in, the cycles are those around [today], with alerts. In any other month
 * they start on that month's first pay day and are a forecast (or history), with no alert: a bill of
 * a future month is never "overdue".
 *
 * Each cycle holds the bills of [bills] dated inside it, whatever month they belong to (a cycle can
 * span two months). Bills of [month] without a due day are expenses of the current cycle.
 * [extraIncomes] add to the cycle their dates fall in, without changing where cycles start or end.
 * Returns null when the schedule produces no payments.
 */
fun buildFinancialOverview(
    settings: IncomeSettings,
    bills: BillBook,
    month: YearMonth,
    today: CalendarDate,
    extraIncomes: List<ExtraIncome> = emptyList(),
): FinancialOverview? {
    val schedule = settings.toSchedule()
    val isLive = month == YearMonth.of(today)
    val anchor = if (isLive) {
        today
    } else {
        schedule.paymentsBetween(month.firstDay(), month.lastDay()).firstOrNull()?.date ?: month.firstDay()
    }
    // Wide enough for a monthly payment adjusted to a later date on both sides of the anchor.
    val payments = schedule.paymentsBetween(
        anchor.firstDayOfMonthPlus(-MONTHS_SEARCHED_BEFORE),
        anchor.firstDayOfMonthPlus(MONTHS_SEARCHED_AFTER + 1).plusDays(-1),
    )
    val last = payments.lastOrNull { it.date <= anchor }
    val next = payments.firstOrNull { it.date > anchor }
    val afterNext = next?.let { nextPayment -> payments.firstOrNull { it.date > nextPayment.date } }
    if (last == null || next == null || afterNext == null) return null

    return FinancialOverview(
        today = anchor,
        isLive = isLive,
        current = FinancialCycle(
            payment = last,
            nextPayment = next,
            bills = bills.billsDueBetween(last.date, next.date.plusDays(-1)).map(::CycleExpense),
            otherExpenses = bills.billsOf(month).filter { it.bill.dueDay == null }.map(::CycleExpense),
            extraIncomes = extraIncomes.entriesBetween(last.date, next.date.plusDays(-1)),
        ),
        next = FinancialCycle(
            payment = next,
            nextPayment = afterNext,
            bills = bills.billsDueBetween(next.date, afterNext.date.plusDays(-1)).map(::CycleExpense),
            extraIncomes = extraIncomes.entriesBetween(next.date, afterNext.date.plusDays(-1)),
        ),
    )
}

/** Every arrival of these extra incomes between [from] and [to], oldest first. */
fun List<ExtraIncome>.entriesBetween(from: CalendarDate, to: CalendarDate): List<ExtraIncomeEntry> =
    flatMap { income -> income.datesBetween(from, to).map { ExtraIncomeEntry(income, it) } }.sortedBy { it.date }
