package com.example.screenshotinbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screenshotinbox.data.local.CategoryEntity
import com.example.screenshotinbox.data.repository.CategoryOpResult
import com.example.screenshotinbox.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val errorMessage: String? = null
)

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryUiState> = combine(
        categoryRepository.observeAll(), errorMessage
    ) { cats, error -> CategoryUiState(cats, error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryUiState())

    fun createCategory(name: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = categoryRepository.createCategory(name)) {
                is CategoryOpResult.Success -> onDone(true)
                is CategoryOpResult.Error -> {
                    errorMessage.value = result.message
                    onDone(false)
                }
            }
        }
    }

    fun renameCategory(category: CategoryEntity, newName: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            when (val result = categoryRepository.renameCategory(category, newName)) {
                is CategoryOpResult.Success -> onDone(true)
                is CategoryOpResult.Error -> {
                    errorMessage.value = result.message
                    onDone(false)
                }
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            when (val result = categoryRepository.deleteCategory(category)) {
                is CategoryOpResult.Success -> Unit
                is CategoryOpResult.Error -> errorMessage.value = result.message
            }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }
}
