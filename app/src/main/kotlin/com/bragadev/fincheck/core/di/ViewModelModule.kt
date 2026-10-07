package com.bragadev.fincheck.core.di

import com.bragadev.fincheck.features.create.presentation.viewmodel.CreateNewListViewModel
import com.bragadev.fincheck.features.home.presentation.viewmodel.HomeViewModel
import com.bragadev.fincheck.features.listdetail.presentation.viewmodel.ListDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        HomeViewModel(
            getShoppingListsUseCase = get(),
            renameShoppingListUseCase = get(),
            deleteShoppingListUseCase = get(),
            duplicateShoppingListUseCase = get(),
            getListShareTextUseCase = get(),
        )
    }
    viewModel { CreateNewListViewModel(createShoppingListUseCase = get()) }
    viewModel { (listId: Long) ->
        ListDetailViewModel(
            listId = listId,
            getShoppingListUseCase = get(),
            getListItemsUseCase = get(),
            addListItemUseCase = get(),
            setItemCheckedUseCase = get(),
            updateListItemUseCase = get(),
            deleteListItemUseCase = get(),
            renameShoppingListUseCase = get(),
            getListShareTextUseCase = get(),
            setListPreferencesUseCase = get(),
            setAllItemsCheckedUseCase = get(),
            deleteListItemsUseCase = get(),
            getIncomeSettingsUseCase = get(),
            saveIncomeSettingsUseCase = get(),
            getExtraIncomesUseCase = get(),
            saveExtraIncomeUseCase = get(),
            deleteExtraIncomeUseCase = get(),
            getAmountsHiddenUseCase = get(),
            setAmountsHiddenUseCase = get(),
        )
    }
}
