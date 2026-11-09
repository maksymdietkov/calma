package com.maksymdietkov.calma

import android.content.Context
import com.maksymdietkov.calma.data.CheckRepository
import com.maksymdietkov.calma.data.ChecklistRepository
import com.maksymdietkov.calma.data.db.CalmaDatabase
import java.time.Clock

/** Ручной DI: единственный экземпляр на всё приложение. */
class AppContainer(context: Context) {

    private val database: CalmaDatabase = CalmaDatabase.build(context)

    val clock: Clock = Clock.systemDefaultZone()

    val checklistRepository = ChecklistRepository(database.checklistItemDao())

    val checkRepository = CheckRepository(database, clock)
}