package com.trailmate.app.core.map

import com.trailmate.app.core.model.RoutePoint
import java.util.Locale

data class AmapOfflineBaseMapTargetHint(
    val regionLabel: String?,
    val fallbackHint: String?
)

object AmapOfflineBaseMapTargetHintEngine {
    fun build(
        targetRegion: AmapTargetRouteRegion?,
        routePoints: List<RoutePoint>
    ): AmapOfflineBaseMapTargetHint {
        val regionLabel = targetRegion?.regionLabel()
        if (regionLabel != null) {
            return AmapOfflineBaseMapTargetHint(
                regionLabel = regionLabel,
                fallbackHint = null
            )
        }

        val samplePoint = AmapTargetRouteRegionSampleEngine.representativePoint(routePoints)
        return AmapOfflineBaseMapTargetHint(
            regionLabel = null,
            fallbackHint = samplePoint?.let { point ->
                "路线中点 ${point.latitude.coordinateString()}, ${point.longitude.coordinateString()}（城市待确认）"
            }
        )
    }

    private fun AmapTargetRouteRegion.regionLabel(): String? =
        cityName?.trim()?.takeIf { it.isNotEmpty() }
            ?: provinceName?.trim()?.takeIf { it.isNotEmpty() }
            ?: adcode?.trim()?.takeIf { it.isNotEmpty() }

    private fun Double.coordinateString(): String =
        String.format(Locale.US, "%.4f", this)
}
