package com.bragadev.list.features.create.presentation.state

data class CreateNewListUiState(
    val name: String = "",
    val isSaving: Boolean = false,
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
