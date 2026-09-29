package com.bragadev.list.core.di

import com.bragadev.list.features.create.presentation.viewmodel.CreateNewListViewModel
import com.bragadev.list.features.home.presentation.viewmodel.HomeViewModel
import com.bragadev.list.features.listdetail.presentation.viewmodel.ListDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(getShoppingListsUseCase = get()) }
    viewModel { CreateNewListViewModel(createShoppingListUseCase = get()) }
    viewModel { (listId: Long) ->
        ListDetailViewModel(
            listId = listId,
            getShoppingListUseCase = get(),
            getListItemsUseCase = get(),
            addListItemUseCase = get(),
            setItemCheckedUseCase = get(),
            updateListItemUseCase = get(),
        )
    }
}
