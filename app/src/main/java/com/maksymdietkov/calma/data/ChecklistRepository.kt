package com.maksymdietkov.calma.data

import com.maksymdietkov.calma.data.db.ChecklistItemDao
import com.maksymdietkov.calma.data.db.ChecklistItemEntity
import com.maksymdietkov.calma.data.model.ChecklistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Настройка списка пунктов. Не влияет на уже созданные проверки. */
class ChecklistRepository(
    private val dao: ChecklistItemDao,
) {

    fun observeItems(): Flow<List<ChecklistItem>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun addItem(title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        dao.insert(
            ChecklistItemEntity(
                title = clean,
                position = dao.maxPosition() + 1,
            )
        )
    }

    suspend fun renameItem(id: Long, title: String) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        val item = dao.getAll().firstOrNull { it.id == id } ?: return
        dao.update(item.copy(title = clean))
    }

    suspend fun deleteItem(id: Long) {
        dao.delete(id)
    }

    /** orderedIds — id пунктов в новом порядке сверху вниз. */
    suspend fun reorder(orderedIds: List<Long>) {
        val byId = dao.getAll().associateBy { it.id }
        val updated = orderedIds.mapIndexedNotNull { index, id ->
            byId[id]?.copy(position = index)
        }
        dao.updateAll(updated)
    }
}