package com.example.screenshotinbox.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {

    @Query("SELECT * FROM screenshots ORDER BY createdAt DESC")
    fun getAllNewestFirst(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots ORDER BY createdAt ASC")
    fun getAllOldestFirst(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllFavoritesFirst(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE id = :id")
    fun getById(id: Long): Flow<ScreenshotEntity?>

    @Query("SELECT * FROM screenshots WHERE uri = :uri LIMIT 1")
    suspend fun findByUri(uri: String): ScreenshotEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ScreenshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<ScreenshotEntity>): List<Long>

    @Update
    suspend fun update(entity: ScreenshotEntity)

    @Delete
    suspend fun delete(entity: ScreenshotEntity)

    @Query("UPDATE screenshots SET category = :fallback WHERE category = :oldCategory")
    suspend fun reassignCategory(oldCategory: String, fallback: String)
}
