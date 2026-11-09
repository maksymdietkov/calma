package com.maksymdietkov.calma.data

import androidx.room.withTransaction
import com.maksymdietkov.calma.data.db.CalmaDatabase
import com.maksymdietkov.calma.data.model.CheckSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock

/** Проверки перед выходом: создание, отметки, завершение, правка. */
class CheckRepository(
    private val db: CalmaDatabase,
    private val clock: Clock,
) {
    private val sessionDao = db.checkSessionDao()
    private val itemDao = db.checklistItemDao()

    /** Текущая незавершённая проверка или null. */
    fun observeActive(): Flow<CheckSession?> =
        sessionDao.observeActive().map { it?.toModel() }

    /** Последняя завершённая проверка или null. */
    fun observeLastCompleted(): Flow<CheckSession?> =
        sessionDao.observeLastCompleted().map { it?.toModel() }

    /** Новая проверка с пустыми отметками. Незавершённая (если была) удаляется. */
    suspend fun startNewSession(): Long =
        sessionDao.startNewSession(
            startedAt = now(),
            items = itemDao.getAll(),
        )

    /**
     * Переключает отметку. Если проверка уже завершена,
     * дополнительно фиксирует время правки.
     */
    suspend fun toggleEntry(session: CheckSession, entryId: Long) {
        val entry = session.entries.firstOrNull { it.id == entryId } ?: return
        val now = now()
        db.withTransaction {
            sessionDao.setEntryCheckedAt(
                entryId = entryId,
                checkedAt = if (entry.isChecked) null else now,
            )
            if (session.isCompleted) {
                sessionDao.markEdited(session.id, now)
            }
        }
    }

    suspend fun completeSession(sessionId: Long) {
        sessionDao.complete(sessionId, now())
    }

    private fun now(): Long = clock.millis()
}