package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.HistoricalActivityLog

data class HistoricalActivityImportUiState(
    val isImporting: Boolean = false,
    val value: String = READY_VALUE,
    val caption: String = READY_CAPTION
) {
    fun importing(fileCount: Int): HistoricalActivityImportUiState =
        copy(
            isImporting = true,
            value = "Importing GPX",
            caption = "$fileCount history files are being parsed locally."
        )

    fun completed(batch: HistoricalActivityImportBatch): HistoricalActivityImportUiState =
        copy(
            isImporting = false,
            value = batch.summaryValue(),
            caption = batch.summaryCaption()
        )

    fun failed(message: String): HistoricalActivityImportUiState =
        copy(
            isImporting = false,
            value = "Import failed",
            caption = message
        )

    companion object {
        const val READY_VALUE = "Ready for history GPX"
        const val READY_CAPTION = "Choose past GPX files to improve capability confidence."
    }
}

data class HistoricalActivityImportResult(
    val uiState: HistoricalActivityImportUiState,
    val activities: List<HistoricalActivity>
)

object HistoricalActivityImportUiReducer {
    fun applyBatch(
        currentActivities: List<HistoricalActivity>,
        batch: HistoricalActivityImportBatch
    ): HistoricalActivityImportResult {
        val updatedActivities = HistoricalActivityLog(currentActivities).addAll(batch.activities).activities
        val addedCount = updatedActivities.size - currentActivities.size
        val duplicateCount = batch.activities.size - addedCount
        val uiState = summaryForBatch(
            addedCount = addedCount,
            duplicateCount = duplicateCount,
            batch = batch
        )

        return HistoricalActivityImportResult(
            uiState = uiState,
            activities = updatedActivities
        )
    }

    fun applyFailure(
        currentActivities: List<HistoricalActivity>,
        message: String
    ): HistoricalActivityImportResult =
        HistoricalActivityImportResult(
            uiState = HistoricalActivityImportUiState().failed(message),
            activities = currentActivities
        )

    private fun summaryForBatch(
        addedCount: Int,
        duplicateCount: Int,
        batch: HistoricalActivityImportBatch
    ): HistoricalActivityImportUiState {
        if (addedCount == 0 && duplicateCount > 0 && batch.failures.isEmpty()) {
            return HistoricalActivityImportUiState(
                isImporting = false,
                value = "No new GPX",
                caption = "All selected GPX activities were already imported."
            )
        }

        val totalSelected = batch.activities.size + batch.failures.size
        val value = when {
            addedCount == 0 && duplicateCount > 0 -> "No new GPX"
            addedCount > 0 && (duplicateCount > 0 || batch.failures.isNotEmpty()) -> "Imported $addedCount / $totalSelected"
            else -> batch.summaryValue()
        }
        val caption = when {
            addedCount > 0 && (duplicateCount > 0 || batch.failures.isNotEmpty()) -> buildString {
                append("Added $addedCount ${if (addedCount == 1) "activity" else "activities"}")
                if (duplicateCount > 0) {
                    append("; $duplicateCount duplicate ${if (duplicateCount == 1) "skipped" else "skipped"}")
                }
                if (batch.failures.isNotEmpty()) {
                    append("; ${batch.failures.size} failed: ${batch.failures.first().fileName}")
                }
                append(".")
            }
            addedCount == 0 && duplicateCount > 0 && batch.failures.isNotEmpty() ->
                "No new activities; $duplicateCount duplicate ${if (duplicateCount == 1) "skipped" else "skipped"}; ${batch.failures.size} failed: ${batch.failures.first().fileName}."
            else -> batch.summaryCaption()
        }

        return HistoricalActivityImportUiState(
            isImporting = false,
            value = value,
            caption = caption
        )
    }
}
