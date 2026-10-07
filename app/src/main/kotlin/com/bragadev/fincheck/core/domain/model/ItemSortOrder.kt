package com.bragadev.fincheck.core.domain.model

/**
 * "Ordenar por" choice of the list screen menu. Only one order is active at a time,
 * so there is never a conflict between them.
 *
 * [code] is what is stored in the database; keep it stable when adding new orders.
 */
enum class ItemSortOrder(val code: Int) {
    /** Order the items were added (default). */
    ADDED(0),

    /** A→Z, ignoring case and accents. */
    ALPHABETICAL(1),

    /** Closest due day first; same day → A→Z; items without a due day at the end. */
    DUE_DAY(2),
    ;

    companion object {
        fun fromCode(code: Int): ItemSortOrder = entries.firstOrNull { it.code == code } ?: ADDED
    }
}
