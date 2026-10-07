package com.bragadev.fincheck.features.home.presentation.state

import com.bragadev.fincheck.core.domain.model.ShoppingList

data class HomeUiState(
    val isLoading: Boolean = true,
    val lists: List<ShoppingList> = emptyList(),
    val error: String? = null,
    /** List whose options sheet (Renomear, Compartilhar, Copiar, Excluir) is open. */
    val menuList: ShoppingList? = null,
    /** List being renamed in the rename dialog. */
    val renamingList: ShoppingList? = null,
    val showRenameError: Boolean = false,
    /** List waiting for the user to confirm its deletion. */
    val deletingList: ShoppingList? = null,
    /** Text ready to be handed to the Android share sheet; consumed by the screen. */
    val pendingShareText: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && lists.isEmpty()
}
