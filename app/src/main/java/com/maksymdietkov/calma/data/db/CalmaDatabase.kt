package com.maksymdietkov.calma.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ChecklistItemEntity::class,
        CheckSessionEntity::class,
        CheckEntryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class CalmaDatabase : RoomDatabase() {

    abstract fun checklistItemDao(): ChecklistItemDao
    abstract fun checkSessionDao(): CheckSessionDao

    companion object {
        private val DEFAULT_ITEMS = listOf(
            "Газ выключен",
            "Утюг выключен",
            "Окна закрыты",
            "Свет выключен",
            "Дверь заперта",
        )

        fun build(context: Context): CalmaDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                CalmaDatabase::class.java,
                "calma.db",
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        DEFAULT_ITEMS.forEachIndexed { index, title ->
                            db.execSQL(
                                "INSERT INTO checklist_items (title, position) VALUES (?, ?)",
                                arrayOf<Any>(title, index),
                            )
                        }
                    }
                })
                .build()
    }
}