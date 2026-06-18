package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.HistoricalActivity

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
    ): HistoricalActivityImportResult =
        HistoricalActivityImportResult(
            uiState = HistoricalActivityImportUiState().completed(batch),
            activities = currentActivities + batch.activities
        )

    fun applyFailure(
        currentActivities: List<HistoricalActivity>,
        message: String
    ): HistoricalActivityImportResult =
        HistoricalActivityImportResult(
            uiState = HistoricalActivityImportUiState().failed(message),
            activities = currentActivities
        )
}
