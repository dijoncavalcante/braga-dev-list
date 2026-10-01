package com.bragadev.list.core.data.repository

import com.bragadev.list.core.common.result.AppError
import com.bragadev.list.core.common.result.AppResult
import com.bragadev.list.core.data.mapper.toDomain
import com.bragadev.list.core.database.ShoppingListDao
import com.bragadev.list.core.database.ShoppingListItemDao
import com.bragadev.list.core.database.ShoppingListItemEntity
import com.bragadev.list.core.database.ShoppingListEntity
import com.bragadev.list.core.domain.model.ItemSortOrder
import com.bragadev.list.core.domain.model.ShoppingList
import com.bragadev.list.core.domain.model.ShoppingListItem
import com.bragadev.list.core.domain.repository.ShoppingListRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    override suspend fun renameList(listId: Long, name: String): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) {
            listDao.rename(listId, name)
        }
    }

    override suspend fun setSortOrder(listId: Long, sortOrder: ItemSortOrder): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) { listDao.setSortOrder(listId, sortOrder.code) }
    }

    override suspend fun setShowPrices(listId: Long, showPrices: Boolean): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) { listDao.setShowPrices(listId, showPrices) }
    }

    override suspend fun setGroupByCycle(listId: Long, groupByCycle: Boolean): AppResult<Unit> =
        runCatchingToResult {
            withContext(ioDispatcher) { listDao.setGroupByCycle(listId, groupByCycle) }
        }

    override suspend fun setAllItemsChecked(listId: Long, isChecked: Boolean): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) { itemDao.setAllChecked(listId, isChecked) }
    }

    override suspend fun deleteItems(listId: Long, onlyChecked: Boolean): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) {
            if (onlyChecked) itemDao.deleteCheckedByListId(listId) else itemDao.deleteAllByListId(listId)
        }
    }

    override suspend fun deleteList(listId: Long): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) {
            listDao.deleteById(listId)
        }
    }

    override suspend fun duplicateList(listId: Long, newName: String): AppResult<ShoppingList> =
        runCatchingToResult {
            withContext(ioDispatcher) {
                val createdAt = System.currentTimeMillis()
                val source = listDao.getById(listId)
                val copy = ShoppingListEntity(
                    name = newName,
                    createdAt = createdAt,
                    sortOrder = source?.sortOrder ?: ItemSortOrder.ADDED.code,
                    showPrices = source?.showPrices ?: true,
                    groupByFortnight = source?.groupByFortnight ?: false,
                )
                val newListId = listDao.duplicate(sourceListId = listId, copy = copy)
                copy.copy(id = newListId).toDomain()
            }
        }

    override suspend fun getItems(listId: Long): AppResult<List<ShoppingListItem>> = runCatchingToResult {
        withContext(ioDispatcher) {
            itemDao.observeByListId(listId).first().map { it.toDomain() }
        }
    }

    override suspend fun addItem(
        listId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
    ): AppResult<ShoppingListItem> =
        runCatchingToResult {
            withContext(ioDispatcher) {
                val entity = ShoppingListItemEntity(
                    listId = listId,
                    name = name,
                    quantity = quantity,
                    priceInCents = priceInCents,
                    dueDay = dueDay,
                    isChecked = false,
                    createdAt = System.currentTimeMillis(),
                )
                val id = itemDao.insert(entity)
                entity.copy(id = id).toDomain()
            }
        }

    override suspend fun updateItem(
        itemId: Long,
        name: String,
        quantity: Int,
        priceInCents: Long,
        dueDay: Int?,
    ): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) {
            itemDao.updateDetails(itemId, name, quantity, priceInCents, dueDay)
        }
    }

    override suspend fun deleteItem(itemId: Long): AppResult<Unit> = runCatchingToResult {
        withContext(ioDispatcher) {
            itemDao.deleteById(itemId)
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
