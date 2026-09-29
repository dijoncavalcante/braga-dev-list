package com.bragadev.list.core.di

import com.bragadev.list.core.data.repository.ShoppingListRepositoryImpl
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import com.bragadev.list.core.domain.usecase.AddListItemUseCase
import com.bragadev.list.core.domain.usecase.CreateShoppingListUseCase
import com.bragadev.list.core.domain.usecase.GetListItemsUseCase
import com.bragadev.list.core.domain.usecase.GetShoppingListUseCase
import com.bragadev.list.core.domain.usecase.GetShoppingListsUseCase
import com.bragadev.list.core.domain.usecase.SetItemCheckedUseCase
import com.bragadev.list.core.domain.usecase.UpdateListItemUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Cross-cutting Data/Domain bindings: dispatcher, repository implementation and
 * use cases. Room/DAO bindings live in [com.bragadev.list.core.database.databaseModule].
 */
val appModule = module {
    single(named("io")) { Dispatchers.IO }

    single<ShoppingListRepository> {
        ShoppingListRepositoryImpl(
            listDao = get(),
            itemDao = get(),
            ioDispatcher = get(named("io")),
        )
    }

    factory { GetShoppingListsUseCase(repository = get()) }
    factory { GetShoppingListUseCase(repository = get()) }
    factory { GetListItemsUseCase(repository = get()) }
    factory { CreateShoppingListUseCase(repository = get()) }
    factory { AddListItemUseCase(repository = get()) }
    factory { SetItemCheckedUseCase(repository = get()) }
    factory { UpdateListItemUseCase(repository = get()) }
}
