package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

object TargetRouteGpxParser {
    fun parse(fileName: String, content: String): ImportedRoute {
        val document = parseXml(content)
        val source = readRouteSource(document)
        val points = source.points
        require(points.size >= MIN_ROUTE_POINTS) { "GPX route must contain at least two track or route points." }

        val distanceMeters = points.zipWithNext().sumOf { (from, to) ->
            haversineMeters(from.latitude, from.longitude, to.latitude, to.longitude)
        }
        val ascentMeters = points.zipWithNext().sumOf { (from, to) ->
            val fromElevation = from.elevationMeters
            val toElevation = to.elevationMeters
            if (fromElevation != null && toElevation != null) {
                (toElevation - fromElevation).takeIf { it > ELEVATION_GAIN_THRESHOLD_METERS } ?: 0.0
            } else {
                0.0
            }
        }.roundToInt()

        return ImportedRoute(
            routeName = source.name ?: fileName.substringBeforeLast('.').ifBlank { "Imported route" },
            fileName = fileName,
            distanceKm = (distanceMeters / 100.0).roundToInt() / 10.0,
            ascentMeters = ascentMeters,
            status = RouteImportStatus.PARSED,
            pointCount = points.size,
            durationMinutes = durationMinutes(points)
        )
    }

    private fun parseXml(content: String): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        factory.isExpandEntityReferences = false
        factory.isXIncludeAware = false
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        runCatching { factory.setAttribute(ACCESS_EXTERNAL_DTD, "") }
        runCatching { factory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "") }

        return factory.newDocumentBuilder()
            .parse(ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8)))
            .also { it.documentElement.normalize() }
    }

    private fun readRouteSource(document: Document): RouteSource {
        val trackElements = elementsByLocalName(document, "trk")
        val routeElements = elementsByLocalName(document, "rte")

        return routeSourceFromContainers(trackElements, "trkpt")
            ?: routeSourceFromContainers(routeElements, "rtept")
            ?: RouteSource(
                name = elementsByLocalName(document, "metadata").firstNotNullOfOrNull { metadata ->
                    childText(metadata, "name")?.takeIf { it.isNotBlank() }
                },
                points = emptyList()
            )
    }

    private fun routeSourceFromContainers(containers: List<Element>, pointLocalName: String): RouteSource? {
        containers.forEach { container ->
            val points = descendantElementsByLocalName(container, pointLocalName).mapNotNull(::readPoint)
            if (points.isNotEmpty()) {
                return RouteSource(
                    name = childText(container, "name")?.takeIf { it.isNotBlank() },
                    points = points
                )
            }
        }

        return null
    }

    private fun readPoint(element: Element): GpxPoint? {
        val latitude = element.getAttribute("lat").toDoubleOrNull()
        val longitude = element.getAttribute("lon").toDoubleOrNull()
        return if (latitude == null || longitude == null) {
            null
        } else {
            GpxPoint(
                latitude = latitude,
                longitude = longitude,
                elevationMeters = childText(element, "ele")?.toDoubleOrNull(),
                time = childText(element, "time")?.let(::parseInstantOrNull)
            )
        }
    }

    private fun durationMinutes(points: List<GpxPoint>): Int? {
        val times = points.mapNotNull { it.time }
        val start = times.firstOrNull() ?: return null
        val end = times.lastOrNull() ?: return null
        val minutes = Duration.between(start, end).toMinutes()

        return minutes.takeIf { it > 0 }?.coerceAtMost(Int.MAX_VALUE.toLong())?.toInt()
    }

    private fun parseInstantOrNull(value: String): Instant? =
        runCatching { Instant.parse(value) }.getOrNull()

    private fun elementsByLocalName(document: Document, localName: String): List<Element> {
        val namespaced = document.getElementsByTagNameNS("*", localName).asElements()
        return if (namespaced.isNotEmpty()) {
            namespaced
        } else {
            document.getElementsByTagName(localName).asElements()
        }
    }

    private fun descendantElementsByLocalName(element: Element, localName: String): List<Element> {
        val namespaced = element.getElementsByTagNameNS("*", localName).asElements()
        return if (namespaced.isNotEmpty()) {
            namespaced
        } else {
            element.getElementsByTagName(localName).asElements()
        }
    }

    private fun childText(element: Element, localName: String): String? {
        for (index in 0 until element.childNodes.length) {
            val node = element.childNodes.item(index)
            if (node is Element && node.matchesLocalName(localName)) {
                return node.textContent.trim()
            }
        }

        return null
    }

    private fun Node.matchesLocalName(localName: String): Boolean =
        this.localName == localName || this.nodeName == localName

    private fun org.w3c.dom.NodeList.asElements(): List<Element> =
        (0 until length).mapNotNull { index -> item(index) as? Element }

    private fun haversineMeters(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double
    ): Double {
        val fromLatRad = Math.toRadians(fromLatitude)
        val toLatRad = Math.toRadians(toLatitude)
        val deltaLat = Math.toRadians(toLatitude - fromLatitude)
        val deltaLon = Math.toRadians(toLongitude - fromLongitude)
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(fromLatRad) * cos(toLatRad) * sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    private data class GpxPoint(
        val latitude: Double,
        val longitude: Double,
        val elevationMeters: Double?,
        val time: Instant?
    )

    private data class RouteSource(
        val name: String?,
        val points: List<GpxPoint>
    )

    private const val EARTH_RADIUS_METERS = 6_371_000.0
    private const val ELEVATION_GAIN_THRESHOLD_METERS = 3.0
    private const val MIN_ROUTE_POINTS = 2
    private const val ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD"
    private const val ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema"
}
