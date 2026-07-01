package com.trailmate.app.core.routeimport

import org.w3c.dom.Document
import org.w3c.dom.Element

internal object KmlRouteImportParser {
    fun parse(
        fileName: String,
        document: Document,
        options: RouteImportOptions,
    ): RouteImportResult {
        val lineString = document.elementsByLocalName("LineString").firstOrNull()
        val routeCoordinates = lineString
            ?.directText("coordinates")
            ?.let(::parseCoordinateSequence)
            ?: emptyList()
        val waypointCandidates = pointPlacemarks(document)
        val buildResult = RouteGeometryBuilder.build(
            coordinates = routeCoordinates,
            waypointCandidates = waypointCandidates,
            options = options,
        )
        val geometry = buildResult.geometry ?: return RouteImportResult.rejected(
            fileName = fileName,
            format = RouteImportFormat.Kml,
            status = RouteImportStatus.MissingTrackGeometry,
            warning = RouteImportWarning.MissingTrackGeometry,
        )

        return RouteImportResult(
            fileName = fileName,
            format = RouteImportFormat.Kml,
            status = RouteImportStatus.Parsed,
            routeName = routeName(document, lineString, fileName),
            geometry = geometry,
            trackPointCount = routeCoordinates.size,
            waypointCount = geometry.waypoints.size,
            hasElevation = geometry.hasElevation,
            warnings = buildResult.warnings,
        )
    }

    private fun pointPlacemarks(document: Document): List<ImportedWaypointCandidate> =
        document.elementsByLocalName("Placemark").mapNotNull { placemark ->
            val point = placemark.directChild("Point") ?: return@mapNotNull null
            val coordinate = point.directText("coordinates")
                ?.let(::parseCoordinateSequence)
                ?.firstOrNull()
                ?: return@mapNotNull null

            ImportedWaypointCandidate(
                title = placemark.directText("name") ?: "航点",
                coordinate = coordinate,
            )
        }

    private fun routeName(
        document: Document,
        lineString: Element?,
        fileName: String,
    ): String {
        val documentName = document.elementsByLocalName("Document")
            .firstOrNull()
            ?.directText("name")
        val linePlacemarkName = document.elementsByLocalName("Placemark")
            .firstOrNull { placemark -> lineString != null && placemark.directChild("LineString") == lineString }
            ?.directText("name")

        return documentName ?: linePlacemarkName ?: fileName.baseName()
    }

    private fun parseCoordinateSequence(text: String): List<com.trailmate.app.core.model.GeoCoordinate> =
        text
            .trim()
            .split(Regex("\\s+"))
            .mapNotNull { tuple ->
                val parts = tuple.split(',')
                coordinateOf(
                    longitudeText = parts.getOrNull(0),
                    latitudeText = parts.getOrNull(1),
                    elevationText = parts.getOrNull(2),
                )
            }
}
