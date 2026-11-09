package com.maksymdietkov.calma.data.model

import java.time.Instant

data class ChecklistItem(
    val id: Long,
    val title: String,
    val position: Int,
)

data class CheckEntry(
    val id: Long,
    val itemId: Long?,
    val title: String,
    val checkedAt: Instant?,
) {
    val isChecked: Boolean get() = checkedAt != null
}

data class CheckSession(
    val id: Long,
    val startedAt: Instant,
    val completedAt: Instant?,
    val editedAt: Instant?,
    val entries: List<CheckEntry>,
) {
    val isCompleted: Boolean get() = completedAt != null
    val checkedCount: Int get() = entries.count { it.isChecked }
    val totalCount: Int get() = entries.size
}