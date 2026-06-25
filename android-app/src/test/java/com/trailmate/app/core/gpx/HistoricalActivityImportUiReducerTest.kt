package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.TrailMateSampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HistoricalActivityImportUiReducerTest {
    @Test
    fun batchResultAppendsActivitiesAndStopsImporting() {
        val importedActivity = TrailMateSampleData.historicalActivities.first()
        val result = HistoricalActivityImportUiReducer.applyBatch(
            currentActivities = emptyList(),
            batch = HistoricalActivityImportBatch(
                activities = listOf(importedActivity),
                failures = emptyList()
            )
        )

        assertFalse(result.uiState.isImporting)
        assertEquals(listOf(importedActivity), result.activities)
        assertEquals("已导入 1 条 GPX", result.uiState.value)
    }

    @Test
    fun batchResultDeduplicatesAlreadyImportedActivities() {
        val duplicate = TrailMateSampleData.historicalActivities.first()
        val result = HistoricalActivityImportUiReducer.applyBatch(
            currentActivities = TrailMateSampleData.historicalActivities,
            batch = HistoricalActivityImportBatch(
                activities = listOf(duplicate),
                failures = emptyList()
            )
        )

        assertFalse(result.uiState.isImporting)
        assertEquals(TrailMateSampleData.historicalActivities, result.activities)
        assertEquals("没有新 GPX", result.uiState.value)
        assertEquals("所选历史活动都已经导入过。", result.uiState.caption)
    }

    @Test
    fun batchResultReportsOnlyNewActivitiesWhenMixedWithDuplicatesAndFailures() {
        val duplicate = TrailMateSampleData.historicalActivities.first()
        val newActivity = TrailMateSampleData.historicalActivities.last().copy(routeName = "Fresh Ridge")
        val result = HistoricalActivityImportUiReducer.applyBatch(
            currentActivities = TrailMateSampleData.historicalActivities,
            batch = HistoricalActivityImportBatch(
                activities = listOf(duplicate, newActivity),
                failures = listOf(
                    HistoricalActivityImportFailure(
                        fileName = "bad-history.gpx",
                        message = "GPX route must contain at least two track or route points."
                    )
                )
            )
        )

        assertEquals(TrailMateSampleData.historicalActivities + newActivity, result.activities)
        assertEquals("已导入 1 / 3", result.uiState.value)
        assertEquals("新增 1 条活动；跳过 1 条重复；1 条失败：bad-history.gpx。", result.uiState.caption)
    }

    @Test
    fun batchResultReportsNoNewActivitiesWhenOnlyDuplicatesAndFailuresRemain() {
        val duplicate = TrailMateSampleData.historicalActivities.first()
        val result = HistoricalActivityImportUiReducer.applyBatch(
            currentActivities = TrailMateSampleData.historicalActivities,
            batch = HistoricalActivityImportBatch(
                activities = listOf(duplicate),
                failures = listOf(
                    HistoricalActivityImportFailure(
                        fileName = "bad-history.gpx",
                        message = "GPX route must contain at least two track or route points."
                    )
                )
            )
        )

        assertEquals(TrailMateSampleData.historicalActivities, result.activities)
        assertEquals("没有新 GPX", result.uiState.value)
        assertEquals("没有新活动；跳过 1 条重复；1 条失败：bad-history.gpx。", result.uiState.caption)
    }

    @Test
    fun failureResultStopsImportingAndKeepsExistingActivities() {
        val existingActivities = TrailMateSampleData.historicalActivities
        val result = HistoricalActivityImportUiReducer.applyFailure(
            currentActivities = existingActivities,
            message = "Content resolver was cancelled."
        )

        assertFalse(result.uiState.isImporting)
        assertEquals(existingActivities, result.activities)
        assertEquals("导入失败", result.uiState.value)
        assertEquals("Content resolver was cancelled.", result.uiState.caption)
    }
}
