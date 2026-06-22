package com.trailmate.app.core.map

import android.content.Context
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.trailmate.app.core.model.RoutePoint

object AmapTargetRouteRegionReader {
    fun read(
        context: Context,
        routePoints: List<RoutePoint>
    ): AmapTargetRouteRegion? =
        runCatching {
            val samplePoint = AmapTargetRouteRegionSampleEngine.representativePoint(routePoints)
                ?: return@runCatching null
            val geocodeSearch = GeocodeSearch(context.applicationContext)
            val query = RegeocodeQuery(
                LatLonPoint(samplePoint.latitude, samplePoint.longitude),
                REGEOCODE_RADIUS_METERS,
                GeocodeSearch.AMAP
            ).apply {
                extensions = GeocodeSearch.EXTENSIONS_BASE
            }
            val address = geocodeSearch.getFromLocation(query)
            AmapTargetRouteRegion(
                provinceName = address.province,
                cityName = address.city.ifBlank { address.province },
                cityCode = address.cityCode,
                adcode = address.adCode
            )
        }.getOrNull()

    private const val REGEOCODE_RADIUS_METERS = 1000f
}
