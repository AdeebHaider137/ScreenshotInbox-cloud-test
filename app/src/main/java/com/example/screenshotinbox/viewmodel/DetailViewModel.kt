package com.example.screenshotinbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screenshotinbox.data.local.CategoryEntity
import com.example.screenshotinbox.data.local.ScreenshotEntity
import com.example.screenshotinbox.data.repository.CategoryRepository
import com.example.screenshotinbox.data.repository.ScreenshotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val screenshot: ScreenshotEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

class DetailViewModel(
    private val screenshotId: Long,
    private val screenshotRepository: ScreenshotRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val isDeleted = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DetailUiState> = combine(
        screenshotRepository.observeById(screenshotId),
        categoryRepository.observeAll(),
        isDeleted,
        errorMessage
    ) { shot, cats, deleted, error ->
        DetailUiState(shot, cats, deleted, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetailUiState())

    fun updateNote(note: String) {
        val current = uiState.value.screenshot ?: return
        viewModelScope.launch {
            runCatching { screenshotRepository.update(current.copy(note = note)) }
                .onFailure { errorMessage.value = "Couldn't save note" }
        }
    }

    fun updateCategory(category: String) {
        val current = uiState.value.screenshot ?: return
        viewModelScope.launch {
            runCatching { screenshotRepository.update(current.copy(category = category)) }
                .onFailure { errorMessage.value = "Couldn't update category" }
        }
    }

    fun toggleFavorite() {
        val current = uiState.value.screenshot ?: return
        viewModelScope.launch {
            runCatching { screenshotRepository.update(current.copy(isFavorite = !current.isFavorite)) }
                .onFailure { errorMessage.value = "Couldn't update favorite" }
        }
    }

    fun delete() {
        val current = uiState.value.screenshot ?: return
        viewModelScope.launch {
            runCatching {
                screenshotRepository.delete(current)
                isDeleted.value = true
            }.onFailure { errorMessage.value = "Couldn't delete screenshot" }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}
