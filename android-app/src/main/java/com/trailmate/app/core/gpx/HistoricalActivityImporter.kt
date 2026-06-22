package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import kotlin.math.ceil
import kotlin.math.roundToInt

data class HistoricalActivityImportFile(
    val fileName: String,
    val content: String
)

data class HistoricalActivityImportFailure(
    val fileName: String,
    val message: String
)

sealed interface HistoricalActivityImportState {
    data class Imported(
        val activity: HistoricalActivity
    ) : HistoricalActivityImportState

    data class Failed(
        val fileName: String,
        val message: String
    ) : HistoricalActivityImportState
}

data class HistoricalActivityImportBatch(
    val activities: List<HistoricalActivity>,
    val failures: List<HistoricalActivityImportFailure>
) {
    fun summaryValue(): String =
        when {
            activities.isNotEmpty() && failures.isEmpty() -> "已导入 ${activities.size} 条 GPX"
            activities.isNotEmpty() -> "已导入 ${activities.size} / ${activities.size + failures.size}"
            failures.isNotEmpty() -> "导入失败"
            else -> "未选择 GPX"
        }

    fun summaryCaption(): String =
        when {
            activities.isNotEmpty() && failures.isEmpty() ->
                "已将 ${activities.size} 条历史活动保存到本地档案。"
            activities.isNotEmpty() ->
                "新增 ${activities.size} 条活动；${failures.size} 条失败：${failures.first().fileName}。"
            failures.isNotEmpty() ->
                "${failures.first().fileName}: ${failures.first().message}"
            else ->
                "选择历史 GPX 文件，让后续路线建议更贴近你的体能。"
        }
}

object HistoricalActivityImporter {
    fun importText(fileName: String, content: String): HistoricalActivityImportState =
        runCatching {
            TargetRouteGpxParser.parse(fileName = fileName, content = content).toHistoricalActivity()
        }.fold(
            onSuccess = { activity -> HistoricalActivityImportState.Imported(activity = activity) },
            onFailure = { error ->
                HistoricalActivityImportState.Failed(
                    fileName = fileName,
                    message = error.message ?: "无法解析这条 GPX 活动。"
                )
            }
        )

    fun importFiles(files: List<HistoricalActivityImportFile>): HistoricalActivityImportBatch {
        val activities = mutableListOf<HistoricalActivity>()
        val failures = mutableListOf<HistoricalActivityImportFailure>()

        files.forEach { file ->
            when (val state = importText(fileName = file.fileName, content = file.content)) {
                is HistoricalActivityImportState.Imported -> activities += state.activity
                is HistoricalActivityImportState.Failed -> failures += HistoricalActivityImportFailure(
                    fileName = state.fileName,
                    message = state.message
                )
            }
        }

        return HistoricalActivityImportBatch(
            activities = activities,
            failures = failures
        )
    }

    private fun ImportedRoute.toHistoricalActivity(): HistoricalActivity =
        HistoricalActivity(
            routeName = routeName,
            distanceKm = distanceKm,
            ascentMeters = ascentMeters,
            durationMinutes = durationMinutes ?: estimatedDurationMinutes()
        )

    private fun ImportedRoute.estimatedDurationMinutes(): Int {
        val movingHours = distanceKm / DEFAULT_FLAT_SPEED_KMH + ascentMeters / DEFAULT_ASCENT_METERS_PER_HOUR
        return maxOf(MIN_ACTIVITY_DURATION_MINUTES, ceil(movingHours * 60).roundToInt())
    }

    private const val DEFAULT_FLAT_SPEED_KMH = 4.0
    private const val DEFAULT_ASCENT_METERS_PER_HOUR = 450.0
    private const val MIN_ACTIVITY_DURATION_MINUTES = 15
}
