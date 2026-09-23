package com.example.screenshotinbox.domain.model

enum class SortOrder { NEWEST, OLDEST, FAVORITES }

sealed class FilterType {
    data object All : FilterType()
    data object Favorites : FilterType()
    data class ByCategory(val category: String) : FilterType()
}
