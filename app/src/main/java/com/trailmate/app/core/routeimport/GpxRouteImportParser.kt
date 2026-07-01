package com.trailmate.app.core.routeimport

import org.w3c.dom.Document
import org.w3c.dom.Element

internal object GpxRouteImportParser {
    fun parse(
        fileName: String,
        document: Document,
        options: RouteImportOptions,
    ): RouteImportResult {
        val trackPoints = parsePoints(document.elementsByLocalName("trkpt"))
            .takeIf { it.size >= 2 }
            ?: parsePoints(document.elementsByLocalName("rtept"))
        val waypoints = document.elementsByLocalName("wpt").mapNotNull { waypoint ->
            coordinateOf(
                latitudeText = waypoint.getAttribute("lat"),
                longitudeText = waypoint.getAttribute("lon"),
                elevationText = waypoint.directText("ele"),
            )?.let { coordinate ->
                ImportedWaypointCandidate(
                    title = waypoint.directText("name") ?: "航点",
                    coordinate = coordinate,
                )
            }
        }
        val buildResult = RouteGeometryBuilder.build(
            coordinates = trackPoints,
            waypointCandidates = waypoints,
            options = options,
        )
        val geometry = buildResult.geometry ?: return RouteImportResult.rejected(
            fileName = fileName,
            format = RouteImportFormat.Gpx,
            status = RouteImportStatus.MissingTrackGeometry,
            warning = RouteImportWarning.MissingTrackGeometry,
        )

        return RouteImportResult(
            fileName = fileName,
            format = RouteImportFormat.Gpx,
            status = RouteImportStatus.Parsed,
            routeName = routeName(document, fileName),
            geometry = geometry,
            trackPointCount = trackPoints.size,
            waypointCount = geometry.waypoints.size,
            hasElevation = geometry.hasElevation,
            warnings = buildResult.warnings,
        )
    }

    private fun parsePoints(elements: List<Element>) =
        elements.mapNotNull { point ->
            coordinateOf(
                latitudeText = point.getAttribute("lat"),
                longitudeText = point.getAttribute("lon"),
                elevationText = point.directText("ele"),
            )
        }

    private fun routeName(document: Document, fileName: String): String {
        val root = document.documentElement
        val metadataName = root
            ?.directChild("metadata")
            ?.directText("name")
        val trackName = document.elementsByLocalName("trk")
            .firstOrNull()
            ?.directText("name")
        val routeName = document.elementsByLocalName("rte")
            .firstOrNull()
            ?.directText("name")

        return metadataName ?: trackName ?: routeName ?: fileName.baseName()
    }
}
