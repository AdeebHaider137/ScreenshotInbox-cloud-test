package com.example.screenshotinbox

import android.app.Application
import com.example.screenshotinbox.data.local.AppDatabase
import com.example.screenshotinbox.data.local.PreferencesManager
import com.example.screenshotinbox.data.repository.CategoryRepository
import com.example.screenshotinbox.data.repository.ScreenshotRepository

/**
 * Simple manual dependency container. No DI framework needed for an app this size -
 * everything is created lazily, once, and reused for the process lifetime.
 */
class ScreenshotInboxApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val screenshotRepository: ScreenshotRepository by lazy {
        ScreenshotRepository(database.screenshotDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao(), screenshotRepository)
    }

    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }
}
