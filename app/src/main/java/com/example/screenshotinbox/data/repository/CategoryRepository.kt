package com.example.screenshotinbox.data.repository

import com.example.screenshotinbox.data.local.CATEGORY_OTHER
import com.example.screenshotinbox.data.local.CategoryDao
import com.example.screenshotinbox.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

sealed class CategoryOpResult {
    data object Success : CategoryOpResult()
    data class Error(val message: String) : CategoryOpResult()
}

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val screenshotRepository: ScreenshotRepository
) {

    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    suspend fun createCategory(name: String): CategoryOpResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return CategoryOpResult.Error("Category name can't be empty")
        val existing = categoryDao.findByName(trimmed)
        if (existing != null) return CategoryOpResult.Error("A category with that name already exists")
        categoryDao.insert(CategoryEntity(name = trimmed, isBuiltIn = false))
        return CategoryOpResult.Success
    }

    suspend fun renameCategory(category: CategoryEntity, newName: String): CategoryOpResult {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return CategoryOpResult.Error("Category name can't be empty")
        val existing = categoryDao.findByName(trimmed)
        if (existing != null && existing.id != category.id) {
            return CategoryOpResult.Error("A category with that name already exists")
        }
        val oldName = category.name
        categoryDao.update(category.copy(name = trimmed))
        if (oldName != trimmed) {
            screenshotRepository.reassignCategory(oldName, trimmed)
        }
        return CategoryOpResult.Success
    }

    suspend fun deleteCategory(category: CategoryEntity): CategoryOpResult {
        if (category.isBuiltIn) {
            return CategoryOpResult.Error("Built-in categories can't be deleted")
        }
        // Move any screenshots in this category to Other before removing it.
        screenshotRepository.reassignCategory(category.name, CATEGORY_OTHER)
        categoryDao.delete(category)
        return CategoryOpResult.Success
    }
}
