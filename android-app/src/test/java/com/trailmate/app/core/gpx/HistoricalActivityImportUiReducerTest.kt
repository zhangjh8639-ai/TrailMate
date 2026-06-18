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
        assertEquals("Imported 1 GPX", result.uiState.value)
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
        assertEquals("Import failed", result.uiState.value)
        assertEquals("Content resolver was cancelled.", result.uiState.caption)
    }
}
