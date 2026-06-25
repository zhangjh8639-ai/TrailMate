package com.trailmate.app.core.gpx

import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.TrackRecordingState
import java.time.Instant

object RecordedTrackGpxExporter {
    fun export(trackRecording: TrackRecordingState): String? {
        if (trackRecording.points.isEmpty()) {
            return null
        }

        val name = trackRecording.routeName?.takeIf { it.isNotBlank() } ?: "TrailMate Track"
        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<gpx version="1.1" creator="TrailMate" xmlns="http://www.topografix.com/GPX/1/1">""")
            appendLine("  <trk>")
            appendLine("    <name>${name.escapeXml()}</name>")
            appendLine("    <trkseg>")
            trackRecording.points.forEach { point ->
                appendPoint(point)
            }
            appendLine("    </trkseg>")
            appendLine("  </trk>")
            appendLine("</gpx>")
        }
    }

    private fun StringBuilder.appendPoint(point: RecordedTrackPoint) {
        appendLine("""      <trkpt lat="${point.latitude}" lon="${point.longitude}">""")
        point.elevationMeters?.let { elevation ->
            appendLine("        <ele>$elevation</ele>")
        }
        appendLine("        <time>${Instant.ofEpochMilli(point.timestampEpochMillis)}</time>")
        appendLine("      </trkpt>")
    }

    private fun String.escapeXml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
