package com.example.screenshotinbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screenshotinbox.data.local.CategoryEntity
import com.example.screenshotinbox.data.local.ScreenshotEntity
import com.example.screenshotinbox.data.repository.CategoryRepository
import com.example.screenshotinbox.data.repository.ScreenshotRepository
import com.example.screenshotinbox.domain.model.FilterType
import com.example.screenshotinbox.domain.model.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val screenshots: List<ScreenshotEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val filter: FilterType = FilterType.All,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val isLoading: Boolean = true,
    val lastDeleted: ScreenshotEntity? = null,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = !isLoading && screenshots.isEmpty() && searchQuery.isBlank() && filter is FilterType.All
    val isSearchEmptyResult: Boolean get() = !isLoading && screenshots.isEmpty() && (searchQuery.isNotBlank() || filter !is FilterType.All)
}

class HomeViewModel(
    private val screenshotRepository: ScreenshotRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val filter = MutableStateFlow<FilterType>(FilterType.All)
    private val sortOrder = MutableStateFlow(SortOrder.NEWEST)
    private val lastDeleted = MutableStateFlow<ScreenshotEntity?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val rawScreenshots = sortOrder.flatMapLatest { screenshotRepository.observeAll(it) }
    private val categories = categoryRepository.observeAll()

    val uiState: StateFlow<HomeUiState> = combine(
        rawScreenshots, categories, searchQuery, filter, sortOrder, lastDeleted, errorMessage
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val all = values[0] as List<ScreenshotEntity>
        @Suppress("UNCHECKED_CAST")
        val cats = values[1] as List<CategoryEntity>
        val query = values[2] as String
        val activeFilter = values[3] as FilterType
        val sort = values[4] as SortOrder
        val deleted = values[5] as ScreenshotEntity?
        val error = values[6] as String?

        val filtered = all
            .filter { shot ->
                when (activeFilter) {
                    is FilterType.All -> true
                    is FilterType.Favorites -> shot.isFavorite
                    is FilterType.ByCategory -> shot.category == activeFilter.category
                }
            }
            .filter { shot ->
                if (query.isBlank()) true
                else {
                    val q = query.trim()
                    shot.note.contains(q, ignoreCase = true) ||
                        shot.fileName.contains(q, ignoreCase = true) ||
                        shot.category.contains(q, ignoreCase = true)
                }
            }

        HomeUiState(
            screenshots = filtered,
            categories = cats,
            searchQuery = query,
            filter = activeFilter,
            sortOrder = sort,
            isLoading = false,
            lastDeleted = deleted,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onFilterChange(newFilter: FilterType) {
        filter.value = newFilter
    }

    fun onSortOrderChange(order: SortOrder) {
        sortOrder.value = order
    }

    fun importUris(uriToFileName: List<Pair<String, String>>) {
        viewModelScope.launch {
            runCatching { screenshotRepository.importUris(uriToFileName) }
                .onFailure { errorMessage.value = "Some screenshots couldn't be imported" }
        }
    }

    fun toggleFavorite(shot: ScreenshotEntity) {
        viewModelScope.launch {
            runCatching { screenshotRepository.update(shot.copy(isFavorite = !shot.isFavorite)) }
                .onFailure { errorMessage.value = "Couldn't update favorite" }
        }
    }

    fun deleteScreenshot(shot: ScreenshotEntity) {
        viewModelScope.launch {
            runCatching {
                screenshotRepository.delete(shot)
                lastDeleted.value = shot
            }.onFailure { errorMessage.value = "Couldn't delete screenshot" }
        }
    }

    fun undoDelete() {
        val shot = lastDeleted.value ?: return
        viewModelScope.launch {
            runCatching { screenshotRepository.insert(shot.copy(id = 0)) }
            lastDeleted.value = null
        }
    }

    fun clearLastDeleted() {
        lastDeleted.value = null
    }

    fun clearError() {
        errorMessage.value = null
    }
}
