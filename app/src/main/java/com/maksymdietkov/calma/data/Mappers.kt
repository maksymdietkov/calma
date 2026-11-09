package com.maksymdietkov.calma.data

import com.maksymdietkov.calma.data.db.ChecklistItemEntity
import com.maksymdietkov.calma.data.db.SessionWithEntries
import com.maksymdietkov.calma.data.model.CheckEntry
import com.maksymdietkov.calma.data.model.CheckSession
import com.maksymdietkov.calma.data.model.ChecklistItem
import java.time.Instant

internal fun ChecklistItemEntity.toModel(): ChecklistItem =
    ChecklistItem(id = id, title = title, position = position)

internal fun SessionWithEntries.toModel(): CheckSession =
    CheckSession(
        id = session.id,
        startedAt = Instant.ofEpochMilli(session.startedAt),
        completedAt = session.completedAt?.let(Instant::ofEpochMilli),
        editedAt = session.editedAt?.let(Instant::ofEpochMilli),
        entries = entries
            .sortedBy { it.position }
            .map { entry ->
                CheckEntry(
                    id = entry.id,
                    itemId = entry.itemId,
                    title = entry.titleSnapshot,
                    checkedAt = entry.checkedAt?.let(Instant::ofEpochMilli),
                )
            },
    )