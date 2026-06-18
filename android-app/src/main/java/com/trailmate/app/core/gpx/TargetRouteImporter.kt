package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.ImportedRoute

sealed interface TargetRouteImportState {
    data object Empty : TargetRouteImportState

    data class Imported(
        val route: ImportedRoute
    ) : TargetRouteImportState

    data class Failed(
        val fileName: String,
        val message: String
    ) : TargetRouteImportState
}

data class TargetRouteImportQueueSummary(
    val value: String,
    val caption: String
)

data class TargetRouteImportQueueState(
    val lastImportedRoute: ImportedRoute? = null,
    val activeFileName: String? = null,
    val failedFileName: String? = null,
    val failureMessage: String? = null
) {
    val isImporting: Boolean
        get() = activeFileName != null

    val canRetry: Boolean
        get() = failedFileName != null

    fun start(fileName: String): TargetRouteImportQueueState =
        copy(
            activeFileName = fileName,
            failedFileName = null,
            failureMessage = null
        )

    fun complete(state: TargetRouteImportState): TargetRouteImportQueueState =
        when (state) {
            TargetRouteImportState.Empty -> this
            is TargetRouteImportState.Imported -> copy(
                lastImportedRoute = state.route,
                activeFileName = null,
                failedFileName = null,
                failureMessage = null
            )
            is TargetRouteImportState.Failed -> copy(
                activeFileName = null,
                failedFileName = state.fileName,
                failureMessage = state.message
            )
        }

    fun summary(): TargetRouteImportQueueSummary =
        when {
            activeFileName != null -> TargetRouteImportQueueSummary(
                value = "Importing GPX",
                caption = "$activeFileName is being parsed locally."
            )
            failedFileName != null -> TargetRouteImportQueueSummary(
                value = "Retry available",
                caption = buildString {
                    append("$failedFileName: ${failureMessage ?: "Unable to import GPX."}")
                    lastImportedRoute?.let { route ->
                        append(" Keeping ${route.routeName}.")
                    }
                }
            )
            lastImportedRoute != null -> TargetRouteImportQueueSummary(
                value = "Parsed",
                caption = "${lastImportedRoute.fileName} / ${lastImportedRoute.summaryLabel()} / ${lastImportedRoute.pointCount} points"
            )
            else -> TargetRouteImportQueueSummary(
                value = "Ready for GPX",
                caption = "Choose a GPX file or use the sample route."
            )
        }

    companion object {
        fun fromRoute(route: ImportedRoute?): TargetRouteImportQueueState =
            TargetRouteImportQueueState(lastImportedRoute = route)
    }
}

object TargetRouteImporter {
    fun importText(fileName: String, content: String): TargetRouteImportState =
        runCatching {
            TargetRouteGpxParser.parse(fileName = fileName, content = content)
        }.fold(
            onSuccess = { route -> TargetRouteImportState.Imported(route = route) },
            onFailure = { error ->
                TargetRouteImportState.Failed(
                    fileName = fileName,
                    message = error.message ?: "Unable to parse this GPX route."
                )
            }
        )
}
