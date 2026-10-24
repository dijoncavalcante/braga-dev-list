package com.bragadev.fincheck.features.home.presentation.state

import com.bragadev.fincheck.core.domain.model.ShoppingList

/** State of the "Listas" tab and of its options sheet and dialogs. */
data class HomeUiState(
    /** True until the lists are read for the first time. */
    val isLoading: Boolean = true,
    /** Every list, newest first. */
    val lists: List<ShoppingList> = emptyList(),
    /** Message shown when the lists could not be read; null otherwise. */
    val error: String? = null,
    /** List whose options sheet (Renomear, Compartilhar, Copiar, Excluir) is open. */
    val menuList: ShoppingList? = null,
    /** List being renamed in the rename dialog. */
    val renamingList: ShoppingList? = null,
    /** The rename dialog shows that the name is empty or too long. */
    val showRenameError: Boolean = false,
    /** List waiting for the user to confirm its deletion. */
    val deletingList: ShoppingList? = null,
    /** Text ready to be handed to the Android share sheet; consumed by the screen. */
    val pendingShareText: String? = null,
) {
    /** Loaded with no error and no list: the empty state is shown. */
    val isEmpty: Boolean get() = !isLoading && error == null && lists.isEmpty()
}
