package com.bragadev.fincheck.core.di

import android.content.Context
import com.bragadev.fincheck.core.data.repository.AppPreferencesRepositoryImpl
import com.bragadev.fincheck.core.data.repository.IncomeRepositoryImpl
import com.bragadev.fincheck.core.data.repository.ShoppingListRepositoryImpl
import com.bragadev.fincheck.core.domain.repository.AppPreferencesRepository
import com.bragadev.fincheck.core.domain.repository.IncomeRepository
import com.bragadev.fincheck.core.domain.repository.ShoppingListRepository
import com.bragadev.fincheck.core.domain.usecase.AddListItemUseCase
import com.bragadev.fincheck.core.domain.usecase.CreateShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteExtraIncomeUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteListItemUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteListItemsUseCase
import com.bragadev.fincheck.core.domain.usecase.DeleteShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.DuplicateShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.GetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.GetExtraIncomesUseCase
import com.bragadev.fincheck.core.domain.usecase.GetIncomeSettingsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListShareTextUseCase
import com.bragadev.fincheck.core.domain.usecase.GetListItemsUseCase
import com.bragadev.fincheck.core.domain.usecase.GetShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.GetShoppingListsUseCase
import com.bragadev.fincheck.core.domain.usecase.RenameShoppingListUseCase
import com.bragadev.fincheck.core.domain.usecase.SaveExtraIncomeUseCase
import com.bragadev.fincheck.core.domain.usecase.SaveIncomeSettingsUseCase
import com.bragadev.fincheck.core.domain.usecase.SetAllItemsCheckedUseCase
import com.bragadev.fincheck.core.domain.usecase.SetAmountsHiddenUseCase
import com.bragadev.fincheck.core.domain.usecase.SetItemCheckedUseCase
import com.bragadev.fincheck.core.domain.usecase.SetListPreferencesUseCase
import com.bragadev.fincheck.core.domain.usecase.UpdateListItemUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val APP_PREFERENCES_NAME = "app_preferences"

/**
 * Cross-cutting Data/Domain bindings: dispatcher, repository implementation and
 * use cases. Room/DAO bindings live in [com.bragadev.fincheck.core.database.databaseModule].
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
    factory { DeleteListItemUseCase(repository = get()) }
    factory { RenameShoppingListUseCase(repository = get()) }
    factory { DeleteShoppingListUseCase(repository = get()) }
    factory { DuplicateShoppingListUseCase(repository = get()) }
    factory { GetListShareTextUseCase(repository = get()) }
    factory { SetListPreferencesUseCase(repository = get()) }
    factory { SetAllItemsCheckedUseCase(repository = get()) }
    factory { DeleteListItemsUseCase(repository = get()) }

    single<IncomeRepository> {
        IncomeRepositoryImpl(dao = get(), extraIncomeDao = get(), ioDispatcher = get(named("io")))
    }
    factory { GetIncomeSettingsUseCase(repository = get()) }
    factory { SaveIncomeSettingsUseCase(repository = get()) }
    factory { GetExtraIncomesUseCase(repository = get()) }
    factory { SaveExtraIncomeUseCase(repository = get()) }
    factory { DeleteExtraIncomeUseCase(repository = get()) }

    single<AppPreferencesRepository> {
        AppPreferencesRepositoryImpl(
            preferences = androidContext().getSharedPreferences(APP_PREFERENCES_NAME, Context.MODE_PRIVATE),
            ioDispatcher = get(named("io")),
        )
    }
    factory { GetAmountsHiddenUseCase(repository = get()) }
    factory { SetAmountsHiddenUseCase(repository = get()) }
}
