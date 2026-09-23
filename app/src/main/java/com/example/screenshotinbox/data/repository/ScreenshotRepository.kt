package com.example.screenshotinbox.data.repository

import com.example.screenshotinbox.data.local.CATEGORY_OTHER
import com.example.screenshotinbox.data.local.ScreenshotDao
import com.example.screenshotinbox.data.local.ScreenshotEntity
import com.example.screenshotinbox.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

class ScreenshotRepository(private val dao: ScreenshotDao) {

    fun observeAll(sortOrder: SortOrder): Flow<List<ScreenshotEntity>> = when (sortOrder) {
        SortOrder.NEWEST -> dao.getAllNewestFirst()
        SortOrder.OLDEST -> dao.getAllOldestFirst()
        SortOrder.FAVORITES -> dao.getAllFavoritesFirst()
    }

    fun observeById(id: Long): Flow<ScreenshotEntity?> = dao.getById(id)

    /** Imports a batch of picked URIs, skipping any URI already present. Returns count actually inserted. */
    suspend fun importUris(uriToFileName: List<Pair<String, String>>): Int {
        var inserted = 0
        for ((uri, fileName) in uriToFileName) {
            val existing = dao.findByUri(uri)
            if (existing == null) {
                dao.insert(
                    ScreenshotEntity(
                        uri = uri,
                        fileName = fileName,
                        category = CATEGORY_OTHER
                    )
                )
                inserted++
            }
        }
        return inserted
    }

    suspend fun update(entity: ScreenshotEntity) = dao.update(entity)

    suspend fun delete(entity: ScreenshotEntity) = dao.delete(entity)

    suspend fun insert(entity: ScreenshotEntity) = dao.insert(entity)

    suspend fun reassignCategory(oldCategory: String, fallback: String = CATEGORY_OTHER) =
        dao.reassignCategory(oldCategory, fallback)
}
