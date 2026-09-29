package com.bragadev.list.core.data.repository

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.data.mapper.toDomain
import com.bragadev.list.core.database.ShoppingListDao
import com.bragadev.list.core.database.ShoppingListItemDao
import com.bragadev.list.core.database.ShoppingListItemEntity
import com.bragadev.list.core.database.ShoppingListEntity
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Data-layer implementation of [ShoppingListRepository]. Talks to Room directly
 * through the DAOs - a dedicated "local data source" indirection was skipped on
 * purpose here, since the Room DAO interface already is that abstraction; adding
 * another wrapper around it would not buy any real decoupling.
 */
class ShoppingListRepositoryImpl(
    private val listDao: ShoppingListDao,
    private val itemDao: ShoppingListItemDao,
    private val ioDispatcher: CoroutineDispatcher,
) : ShoppingListRepository {

    override fun observeLists(): Flow<List<ShoppingList>> =
        listDao.observeListsWithItemCount().map { lists -> lists.map { it.toDomain() } }

    override fun observeList(listId: Long): Flow<ShoppingList?> =
        listDao.observeById(listId).map { entity -> entity?.toDomain() }

    override fun observeItems(listId: Long): Flow<List<ShoppingListItem>> =
        itemDao.observeByListId(listId).map { items -> items.map { it.toDomain() } }

    override suspend fun createList(name: String): AppResult<ShoppingList> = runCatchingToResult {
        withContext(ioDispatcher) {
            val createdAt = System.currentTimeMillis()
            val id = listDao.insert(ShoppingListEntity(name = name, createdAt = createdAt))
            ShoppingList(id = id, name = name, createdAt = createdAt, itemCount = 0)
        }
    }

    override suspend fun addItem(listId: Long, name: String, quantity: Int): AppResult<ShoppingListItem> =
        runCatchingToResult {
            withContext(ioDispatcher) {
                val entity = ShoppingListItemEntity(
                    listId = listId,
                    name = name,
                    quantity = quantity,
                    isChecked = false,
                    createdAt = System.currentTimeMillis(),
                )
                val id = itemDao.insert(entity)
                entity.copy(id = id).toDomain()
            }
        }

    override suspend fun setItemChecked(itemId: Long, isChecked: Boolean): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) {
            itemDao.setChecked(itemId, isChecked)
        }
    }

    private inline fun <T> runCatchingToResult(block: () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (exception: Exception) {
        AppResult.Error(AppError.Database)
    }
}
