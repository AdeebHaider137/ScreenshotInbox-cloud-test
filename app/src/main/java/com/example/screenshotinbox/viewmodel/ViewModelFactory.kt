package com.example.screenshotinbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.screenshotinbox.data.local.PreferencesManager
import com.example.screenshotinbox.data.repository.CategoryRepository
import com.example.screenshotinbox.data.repository.ScreenshotRepository

class ViewModelFactory(
    private val screenshotRepository: ScreenshotRepository,
    private val categoryRepository: CategoryRepository,
    private val preferencesManager: PreferencesManager,
    private val screenshotId: Long = -1L
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(screenshotRepository, categoryRepository) as T

            modelClass.isAssignableFrom(DetailViewModel::class.java) ->
                DetailViewModel(screenshotId, screenshotRepository, categoryRepository) as T

            modelClass.isAssignableFrom(CategoryViewModel::class.java) ->
                CategoryViewModel(categoryRepository) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(preferencesManager) as T

            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(preferencesManager) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
