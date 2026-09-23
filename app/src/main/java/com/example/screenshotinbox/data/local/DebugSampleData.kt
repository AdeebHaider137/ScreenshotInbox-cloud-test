package com.example.screenshotinbox.data.local

/**
 * Debug-only helper for inserting sample records without real image files, so screens can be
 * previewed with data. Never called from production code paths — wire it up manually from a
 * debug menu or test if you need it. Uses a placeholder content URI string; broken-image
 * handling in ScreenshotCard/DetailScreen will show a fallback icon for these.
 */
object DebugSampleData {

    fun sampleEntities(): List<ScreenshotEntity> = listOf(
        ScreenshotEntity(
            uri = "content://sample/1",
            fileName = "amazon_product.png",
            category = "Shopping",
            note = "Buy this after salary."
        ),
        ScreenshotEntity(
            uri = "content://sample/2",
            fileName = "aws_lambda_command.png",
            category = "Study",
            note = "Use this when configuring Lambda."
        ),
        ScreenshotEntity(
            uri = "content://sample/3",
            fileName = "flight_ticket.png",
            category = "Travel",
            note = "Departure gate B12",
            isFavorite = true
        )
    )

    suspend fun insertInto(dao: ScreenshotDao) {
        sampleEntities().forEach { dao.insert(it) }
    }
}
