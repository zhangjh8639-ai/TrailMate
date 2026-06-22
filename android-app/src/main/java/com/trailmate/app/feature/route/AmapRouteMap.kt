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
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CircleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.map.AmapCameraFitDecision
import com.trailmate.app.core.map.AmapCameraFitPolicy
import com.trailmate.app.core.map.AmapCameraFitState
import com.trailmate.app.core.map.AmapCameraRouteKeyFactory
import com.trailmate.app.core.map.AmapSdkInitializer
import com.trailmate.app.core.map.TrailMapCheckpointProjector
import com.trailmate.app.core.map.TrailMapUserLocationConfidence
import com.trailmate.app.core.map.TrailMapUserLocationOverlay
import com.trailmate.app.core.map.TrailMapUserLocationOverlayPolicy
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.TrackRecordingState

@Composable
internal fun AmapRouteMap(
    route: ImportedRoute,
    plan: HikePlanSummary,
    trackRecording: TrackRecordingState,
    showUserLocation: Boolean,
    locationSnapshot: TrailMateLocationSnapshot,
    onMapLoaded: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        AmapSdkInitializer.initialize(context)
        MapView(context).apply { onCreate(Bundle()) }
    }
    var cameraFitState by remember { mutableStateOf(AmapCameraFitState()) }
    val currentOnMapLoaded by rememberUpdatedState(onMapLoaded)

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            val routeKey = AmapCameraRouteKeyFactory.build(route)
            val cameraFitResult = AmapCameraFitPolicy.resolve(
                state = cameraFitState,
                routeKey = routeKey,
                hasRouteGeometry = route.routePoints.size >= 2
            )
            cameraFitState = cameraFitResult.state
            view.map.applyRouteOverlay(
                route = route,
                plan = plan,
                trackRecording = trackRecording,
                showUserLocation = showUserLocation,
                locationSnapshot = locationSnapshot,
                fitRouteBounds = cameraFitResult.decision == AmapCameraFitDecision.FIT_ROUTE_BOUNDS,
                onMapLoaded = currentOnMapLoaded
            )
        }
    )
}

private fun AMap.applyRouteOverlay(
    route: ImportedRoute,
    plan: HikePlanSummary,
    trackRecording: TrackRecordingState,
    showUserLocation: Boolean,
    locationSnapshot: TrailMateLocationSnapshot,
    fitRouteBounds: Boolean,
    onMapLoaded: () -> Unit
) {
    clear()
    uiSettings.isZoomControlsEnabled = false
    uiSettings.isMyLocationButtonEnabled = false
    mapType = AMap.MAP_TYPE_NORMAL
    isMyLocationEnabled = false

    val routeLatLngs = route.routePoints.mapNotNull(RoutePoint::toLatLngOrNull)
    if (routeLatLngs.size < 2) {
        return
    }

    addPolyline(
        PolylineOptions()
            .addAll(routeLatLngs)
            .width(12f)
            .color(AMAP_ROUTE_BLUE)
            .geodesic(true)
    )
    val trackLatLngs = trackRecording.points.mapNotNull(RecordedTrackPoint::toLatLngOrNull)
    if (trackLatLngs.size >= 2) {
        addPolyline(
            PolylineOptions()
                .addAll(trackLatLngs)
                .width(10f)
                .color(AMAP_TRACK_ORANGE)
                .geodesic(true)
        )
    }
    TrailMapCheckpointProjector.project(route = route, plan = plan).forEach { marker ->
        addMarker(
            MarkerOptions()
                .position(LatLng(marker.latitude, marker.longitude))
                .title(marker.title)
                .snippet("${marker.distanceKm}km · ${marker.note}")
        )
    }
    TrailMapUserLocationOverlayPolicy.resolve(
        gpsEnabled = showUserLocation,
        locationSnapshot = locationSnapshot
    )?.let { userLocation ->
        val position = LatLng(userLocation.latitude, userLocation.longitude)
        userLocation.accuracyMeters?.let { accuracyMeters ->
            addCircle(
                CircleOptions()
                    .center(position)
                    .radius(accuracyMeters)
                    .fillColor(AMAP_USER_LOCATION_FILL)
                    .strokeColor(AMAP_USER_LOCATION_STROKE)
                    .strokeWidth(2f)
            )
        }
        addMarker(
            MarkerOptions()
                .position(position)
                .title(userLocation.title)
                .snippet(userLocation.accuracyLabel)
                .icon(BitmapDescriptorFactory.defaultMarker(userLocation.markerHue()))
        )
    }
    if (fitRouteBounds) {
        moveCamera(CameraUpdateFactory.newLatLngZoom(routeLatLngs[routeLatLngs.size / 2], DEFAULT_ROUTE_ZOOM))
    }
    val routeBounds = if (fitRouteBounds) routeLatLngs.toBounds() else null
    setOnMapLoadedListener {
        routeBounds?.let { bounds ->
            moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING_PX))
        }
        onMapLoaded()
    }
}

private fun RoutePoint.toLatLngOrNull(): LatLng? =
    if (latitude.isFinite() && longitude.isFinite()) {
        LatLng(latitude, longitude)
    } else {
        null
    }

private fun RecordedTrackPoint.toLatLngOrNull(): LatLng? =
    if (latitude.isFinite() && longitude.isFinite()) {
        LatLng(latitude, longitude)
    } else {
        null
    }

private fun TrailMapUserLocationOverlay.markerHue(): Float =
    when (confidence) {
        TrailMapUserLocationConfidence.PRECISE -> BitmapDescriptorFactory.HUE_AZURE
        TrailMapUserLocationConfidence.APPROXIMATE -> BitmapDescriptorFactory.HUE_YELLOW
    }

private fun List<LatLng>.toBounds(): LatLngBounds {
    val builder = LatLngBounds.builder()
    forEach(builder::include)
    return builder.build()
}

private const val AMAP_ROUTE_BLUE = 0xFF2D75E8.toInt()
private const val AMAP_TRACK_ORANGE = 0xFFE07A1F.toInt()
private const val AMAP_USER_LOCATION_STROKE = 0xFF0B6B4F.toInt()
private const val AMAP_USER_LOCATION_FILL = 0x330B6B4F
private const val MAP_PADDING_PX = 96
private const val DEFAULT_ROUTE_ZOOM = 13f
