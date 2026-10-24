package com.bragadev.fincheck.features.create.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.fincheck.core.common.result.AppError
import com.bragadev.fincheck.core.common.result.AppResult
import com.bragadev.fincheck.core.domain.usecase.CreateShoppingListUseCase
import com.bragadev.fincheck.features.create.presentation.state.CreateListError
import com.bragadev.fincheck.features.create.presentation.state.CreateNewListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel of the "Nova lista" screen: keeps the typed name and creates the list. */
class CreateNewListViewModel(
    private val createShoppingListUseCase: CreateShoppingListUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNewListUiState())
    /** State the screen draws. */
    val uiState: StateFlow<CreateNewListUiState> = _uiState.asStateFlow()

    /** The name field changed; clears any error shown. */
    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    /** Creates the list with the typed name and calls [onSaved] with its id; shows an error instead when it fails. */
    fun onSaveClick(onSaved: (Long) -> Unit) {
        val currentName = _uiState.value.name
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = createShoppingListUseCase(currentName)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSaved(result.data.id)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.error.toCreateListError())
                }
            }
        }
    }

    /** A validation error means the name is empty or too long; anything else is generic. */
    private fun AppError.toCreateListError(): CreateListError = when (this) {
        is AppError.Validation -> CreateListError.EmptyName
        else -> CreateListError.Generic
    }
}
