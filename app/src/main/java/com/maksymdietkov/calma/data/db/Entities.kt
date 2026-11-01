package com.maksymdietkov.calma.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val position: Int,
)

@Entity(
    tableName = "check_sessions",
    indices = [Index("completedAt")],
)
data class CheckSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val completedAt: Long? = null,
    val editedAt: Long? = null,
)

@Entity(
    tableName = "check_entries",
    foreignKeys = [
        ForeignKey(
            entity = CheckSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ChecklistItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("sessionId"), Index("itemId")],
)
data class CheckEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val itemId: Long?,
    val titleSnapshot: String,
    val position: Int,
    val checkedAt: Long? = null,
)

data class SessionWithEntries(
    @Embedded val session: CheckSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val entries: List<CheckEntryEntity>,
)