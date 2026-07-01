package com.trailmate.app.core.routeimport

import com.trailmate.app.core.model.RouteGeometry
import com.trailmate.app.core.model.RouteId
import com.trailmate.app.core.model.RouteSourceType
import com.trailmate.app.core.model.TrailRoute
import java.time.Instant

enum class RouteImportFormat(val sourceType: RouteSourceType) {
    Gpx(RouteSourceType.ImportedGpx),
    Kml(RouteSourceType.ImportedKml),
}

enum class RouteImportStatus {
    Parsed,
    UnsupportedFormat,
    MissingTrackGeometry,
    InvalidXml,
}

enum class RouteImportWarning {
    MissingElevation,
    SparseTrack,
    LargePointGap,
    UnsupportedFormat,
    MissingTrackGeometry,
    InvalidXml,
}

data class RouteImportResult(
    val fileName: String,
    val format: RouteImportFormat?,
    val status: RouteImportStatus,
    val routeName: String,
    val geometry: RouteGeometry?,
    val trackPointCount: Int,
    val waypointCount: Int,
    val hasElevation: Boolean,
    val warnings: Set<RouteImportWarning>,
) {
    val sourceType: RouteSourceType? = format?.sourceType

    fun toImportedRoute(
        id: RouteId,
        region: String,
        importedAt: Instant,
    ): TrailRoute {
        require(status == RouteImportStatus.Parsed && geometry != null && sourceType != null) {
            "Only parsed GPX/KML results can become imported routes."
        }

        return TrailRoute.imported(
            id = id,
            name = routeName,
            region = region,
            geometry = geometry,
            importedAt = importedAt,
            sourceType = sourceType,
        )
    }

    companion object {
        fun rejected(
            fileName: String,
            format: RouteImportFormat?,
            status: RouteImportStatus,
            warning: RouteImportWarning,
        ): RouteImportResult =
            RouteImportResult(
                fileName = fileName,
                format = format,
                status = status,
                routeName = fileName.baseName(),
                geometry = null,
                trackPointCount = 0,
                waypointCount = 0,
                hasElevation = false,
                warnings = setOf(warning),
            )
    }
}

internal fun String.baseName(): String =
    substringAfterLast('\\')
        .substringAfterLast('/')
        .substringBeforeLast('.', this)
        .ifBlank { "导入路线" }
