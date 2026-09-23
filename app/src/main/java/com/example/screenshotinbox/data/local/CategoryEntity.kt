package com.example.screenshotinbox.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CATEGORY_OTHER = "Other"

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isBuiltIn: Boolean = false
)

val DEFAULT_CATEGORIES = listOf(
    "Shopping", "Jobs", "Travel", "Study", "Bills", "Games", "Tutorials", "Important", CATEGORY_OTHER
)
