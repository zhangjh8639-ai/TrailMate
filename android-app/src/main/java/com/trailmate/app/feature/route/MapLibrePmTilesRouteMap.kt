package com.trailmate.app.feature.route

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.trailmate.app.core.map.MapLibreCheckpointOverlay
import com.trailmate.app.core.map.MapLibrePmTilesStyleFactory
import com.trailmate.app.core.map.MapLibreRouteGeoPoint
import com.trailmate.app.core.map.MapLibreRouteOverlay
import com.trailmate.app.core.map.MapLibreRouteOverlayProjector
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.TrackRecordingState
import java.io.File
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
internal fun MapLibrePmTilesRouteMap(
    route: ImportedRoute,
    plan: HikePlanSummary,
    trackRecording: TrackRecordingState,
    pmTilesFile: File,
    onMapLoaded: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val overlay = remember(route, plan, trackRecording) {
        MapLibreRouteOverlayProjector.project(
            route = route,
            plan = plan,
            trackRecording = trackRecording
        )
    }
    val mapView = remember(pmTilesFile.path) {
        MapLibre.getInstance(context)
        MapView(context).apply { onCreate(Bundle()) }
    }
    val currentOverlay by rememberUpdatedState(overlay)
    val currentOnMapLoaded by rememberUpdatedState(onMapLoaded)
    var styleLoaded by remember(pmTilesFile.path) { mutableStateOf(false) }
    var routeBoundsFitted by remember(pmTilesFile.path, route.routeName) { mutableStateOf(false) }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { map ->
                if (styleLoaded) {
                    val shouldFitRouteBounds = !routeBoundsFitted && currentOverlay.hasDrawableRoute
                    map.applyRouteOverlay(
                        overlay = currentOverlay,
                        fitRouteBounds = shouldFitRouteBounds
                    )
                    if (shouldFitRouteBounds) {
                        routeBoundsFitted = true
                    }
                } else {
                    map.setStyle(
                        Style.Builder().fromJson(MapLibrePmTilesStyleFactory.buildStyleJson(pmTilesFile))
                    ) {
                        styleLoaded = true
                        map.applyRouteOverlay(
                            overlay = currentOverlay,
                            fitRouteBounds = currentOverlay.hasDrawableRoute
                        )
                        routeBoundsFitted = currentOverlay.hasDrawableRoute
                        currentOnMapLoaded()
                    }
                }
            }
        }
    )
}

@Suppress("DEPRECATION")
private fun MapLibreMap.applyRouteOverlay(
    overlay: MapLibreRouteOverlay,
    fitRouteBounds: Boolean
) {
    clear()
    if (!overlay.hasDrawableRoute) {
        return
    }

    val routeLatLngs = overlay.routePoints.map(MapLibreRouteGeoPoint::toLatLng)
    addPolyline(
        PolylineOptions()
            .addAll(routeLatLngs)
            .width(MAPLIBRE_ROUTE_WIDTH)
            .color(MAPLIBRE_ROUTE_BLUE)
    )
    val trackLatLngs = overlay.trackPoints.map(MapLibreRouteGeoPoint::toLatLng)
    if (trackLatLngs.size >= 2) {
        addPolyline(
            PolylineOptions()
                .addAll(trackLatLngs)
                .width(MAPLIBRE_TRACK_WIDTH)
                .color(MAPLIBRE_TRACK_ORANGE)
        )
    }
    overlay.checkpoints.forEach { checkpoint ->
        addMarker(
            MarkerOptions()
                .position(checkpoint.toLatLng())
                .title(checkpoint.title)
                .snippet("${checkpoint.distanceKm}km · ${checkpoint.note}")
        )
    }
    if (fitRouteBounds) {
        moveCamera(CameraUpdateFactory.newLatLngBounds(routeLatLngs.toBounds(), MAPLIBRE_MAP_PADDING_PX))
    }
}

private fun MapLibreRouteGeoPoint.toLatLng(): LatLng =
    LatLng(latitude, longitude)

private fun MapLibreCheckpointOverlay.toLatLng(): LatLng =
    LatLng(latitude, longitude)

private fun List<LatLng>.toBounds(): LatLngBounds {
    val north = maxOf { point -> point.latitude }
    val south = minOf { point -> point.latitude }
    val east = maxOf { point -> point.longitude }
    val west = minOf { point -> point.longitude }
    return LatLngBounds.from(north, east, south, west)
}

private const val MAPLIBRE_ROUTE_BLUE = 0xFF2D75E8.toInt()
private const val MAPLIBRE_TRACK_ORANGE = 0xFFE07A1F.toInt()
private const val MAPLIBRE_ROUTE_WIDTH = 6f
private const val MAPLIBRE_TRACK_WIDTH = 5f
private const val MAPLIBRE_MAP_PADDING_PX = 96
