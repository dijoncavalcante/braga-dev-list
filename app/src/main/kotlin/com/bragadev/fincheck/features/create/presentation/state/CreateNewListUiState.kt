package com.bragadev.fincheck.features.create.presentation.state

/** State of the "Nova lista" screen. */
data class CreateNewListUiState(
    /** Name typed so far. */
    val name: String = "",
    /** True while the list is being saved; the button is disabled meanwhile. */
    val isSaving: Boolean = false,
    /** Why the last save failed; null when there is nothing to show. */
    val error: CreateListError? = null,
)

/**
 * UI-facing error - the ViewModel maps the domain AppError into this before it
 * ever reaches Compose, so no technical exception/message leaks into the screen.
 */
enum class CreateListError {
    EmptyName,
    Generic,
}
