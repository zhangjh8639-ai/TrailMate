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
            activities.isNotEmpty() && failures.isEmpty() -> "Imported ${activities.size} GPX"
            activities.isNotEmpty() -> "Imported ${activities.size} / ${activities.size + failures.size}"
            failures.isNotEmpty() -> "Import failed"
            else -> "No GPX selected"
        }

    fun summaryCaption(): String =
        when {
            activities.isNotEmpty() && failures.isEmpty() ->
                "Added ${activities.size} historical activities to local capability evidence."
            activities.isNotEmpty() ->
                "Added ${activities.size} activities; ${failures.size} failed: ${failures.first().fileName}."
            failures.isNotEmpty() ->
                "${failures.first().fileName}: ${failures.first().message}"
            else ->
                "Choose historical GPX files to improve capability confidence."
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
                    message = error.message ?: "Unable to parse this GPX activity."
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
            durationMinutes = estimatedDurationMinutes()
        )

    private fun ImportedRoute.estimatedDurationMinutes(): Int {
        val movingHours = distanceKm / DEFAULT_FLAT_SPEED_KMH + ascentMeters / DEFAULT_ASCENT_METERS_PER_HOUR
        return maxOf(MIN_ACTIVITY_DURATION_MINUTES, ceil(movingHours * 60).roundToInt())
    }

    private const val DEFAULT_FLAT_SPEED_KMH = 4.0
    private const val DEFAULT_ASCENT_METERS_PER_HOUR = 450.0
    private const val MIN_ACTIVITY_DURATION_MINUTES = 15
}
