package com.bragadev.fincheck.core.domain.model

/**
 * "Visualizar" choice of the list screen menu: how the items are grouped on screen.
 * Only one view is active at a time.
 *
 * [code] is what is stored in the database; keep it stable when adding new views.
 * 0 and 1 match the old "group by cycle" flag (false / true), so existing lists keep their view.
 */
enum class ItemViewMode(val code: Int) {
    /** Every item together: "Não marcados" then "Marcados" (default). */
    ALL(0),

    /** By financial cycle: pay day → day before the next pay day. */
    CYCLES(1),

    /** By fortnight of the due day: 1ª quinzena (1–15), 2ª quinzena (16–31) and items without a due day. */
    FORTNIGHTS(2),
    ;

    companion object {
        fun fromCode(code: Int): ItemViewMode = entries.firstOrNull { it.code == code } ?: ALL
    }
}
