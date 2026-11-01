package com.maksymdietkov.calma.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {

    @Query("SELECT * FROM checklist_items ORDER BY position")
    fun observeAll(): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items ORDER BY position")
    suspend fun getAll(): List<ChecklistItemEntity>

    @Query("SELECT COALESCE(MAX(position), -1) FROM checklist_items")
    suspend fun maxPosition(): Int

    @Insert
    suspend fun insert(item: ChecklistItemEntity): Long

    @Update
    suspend fun update(item: ChecklistItemEntity)

    @Update
    suspend fun updateAll(items: List<ChecklistItemEntity>)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun delete(id: Long)
}