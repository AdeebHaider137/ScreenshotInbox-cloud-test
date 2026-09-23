package com.example.screenshotinbox.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ScreenshotEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun screenshotDao(): ScreenshotDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "screenshot_inbox.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default categories exactly once, on first creation only.
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).categoryDao()
                            if (dao.count() == 0) {
                                dao.insertAll(
                                    DEFAULT_CATEGORIES.map { name ->
                                        CategoryEntity(name = name, isBuiltIn = true)
                                    }
                                )
                            }
                        }
                    }
                }).build().also { INSTANCE = it }
            }
        }
    }
}
