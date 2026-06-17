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
