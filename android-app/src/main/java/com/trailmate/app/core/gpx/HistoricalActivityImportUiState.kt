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
            value = "正在导入 GPX",
            caption = "$fileCount 个历史文件正在本地解析。"
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
            value = "导入失败",
            caption = message
        )

    companion object {
        const val READY_VALUE = "等待历史 GPX"
const val READY_CAPTION = "选择过往 GPX 文件，让后续路线建议更贴近你的体能。"
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
                value = "没有新 GPX",
                caption = "所选历史活动都已经导入过。"
            )
        }

        val totalSelected = batch.activities.size + batch.failures.size
        val value = when {
            addedCount == 0 && duplicateCount > 0 -> "没有新 GPX"
            addedCount > 0 && (duplicateCount > 0 || batch.failures.isNotEmpty()) -> "已导入 $addedCount / $totalSelected"
            else -> batch.summaryValue()
        }
        val caption = when {
            addedCount > 0 && (duplicateCount > 0 || batch.failures.isNotEmpty()) -> buildString {
                append("新增 $addedCount 条活动")
                if (duplicateCount > 0) {
                    append("；跳过 $duplicateCount 条重复")
                }
                if (batch.failures.isNotEmpty()) {
                    append("；${batch.failures.size} 条失败：${batch.failures.first().fileName}")
                }
                append("。")
            }
            addedCount == 0 && duplicateCount > 0 && batch.failures.isNotEmpty() ->
                "没有新活动；跳过 $duplicateCount 条重复；${batch.failures.size} 条失败：${batch.failures.first().fileName}。"
            else -> batch.summaryCaption()
        }

        return HistoricalActivityImportUiState(
            isImporting = false,
            value = value,
            caption = caption
        )
    }
}
