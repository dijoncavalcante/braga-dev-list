package com.bragadev.list.features.create.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.domain.usecase.CreateShoppingListUseCase
import com.bragadev.list.features.create.presentation.state.CreateListError
import com.bragadev.list.features.create.presentation.state.CreateNewListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateNewListViewModel(
    private val createShoppingListUseCase: CreateShoppingListUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateNewListUiState())
    val uiState: StateFlow<CreateNewListUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

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

    private fun AppError.toCreateListError(): CreateListError = when (this) {
        is AppError.Validation -> CreateListError.EmptyName
        else -> CreateListError.Generic
    }
}
