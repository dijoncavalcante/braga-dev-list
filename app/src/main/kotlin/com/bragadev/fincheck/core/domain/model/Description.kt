package com.bragadev.fincheck.core.domain.model

/**
 * Longest description of a list item or a bill. Material Design sets no number, only that a field
 * with a limit shows a character counter; 200 fits in about three lines of the form and keeps the
 * row short.
 */
const val MAX_DESCRIPTION_LENGTH = 200

/** Trimmed description, or null when there is nothing in it. */
fun String?.toDescription(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
