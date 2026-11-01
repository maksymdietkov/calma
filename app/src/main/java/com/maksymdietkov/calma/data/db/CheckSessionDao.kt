package com.maksymdietkov.calma.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckSessionDao {

    @Transaction
    @Query(
        """
        SELECT * FROM check_sessions
        WHERE completedAt IS NULL
        ORDER BY startedAt DESC
        LIMIT 1
        """
    )
    fun observeActive(): Flow<SessionWithEntries?>

    @Transaction
    @Query(
        """
        SELECT * FROM check_sessions
        WHERE completedAt IS NOT NULL
        ORDER BY completedAt DESC
        LIMIT 1
        """
    )
    fun observeLastCompleted(): Flow<SessionWithEntries?>

    @Insert
    suspend fun insertSession(session: CheckSessionEntity): Long

    @Insert
    suspend fun insertEntries(entries: List<CheckEntryEntity>)

    @Query("DELETE FROM check_sessions WHERE completedAt IS NULL")
    suspend fun deleteActive()

    @Query("UPDATE check_entries SET checkedAt = :checkedAt WHERE id = :entryId")
    suspend fun setEntryCheckedAt(entryId: Long, checkedAt: Long?)

    @Query("UPDATE check_sessions SET completedAt = :completedAt WHERE id = :sessionId")
    suspend fun complete(sessionId: Long, completedAt: Long)

    @Query("UPDATE check_sessions SET editedAt = :editedAt WHERE id = :sessionId")
    suspend fun markEdited(sessionId: Long, editedAt: Long)

    /**
     * Удаляет незавершённую проверку (если есть) и создаёт новую
     * с пустыми отметками по текущему списку пунктов.
     */
    @Transaction
    suspend fun startNewSession(startedAt: Long, items: List<ChecklistItemEntity>): Long {
        deleteActive()
        val sessionId = insertSession(CheckSessionEntity(startedAt = startedAt))
        insertEntries(
            items.mapIndexed { index, item ->
                CheckEntryEntity(
                    sessionId = sessionId,
                    itemId = item.id,
                    titleSnapshot = item.title,
                    position = index,
                )
            }
        )
        return sessionId
    }
}