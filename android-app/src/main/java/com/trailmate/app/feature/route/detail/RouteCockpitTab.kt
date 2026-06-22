package com.trailmate.app.feature.route.detail

import androidx.compose.runtime.Composable
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.map.AmapLaunchDiagnostics
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProofCaptureEngine
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProofCaptureState
import com.trailmate.app.core.map.TrailMapReadiness
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.HikeCheckpointDetail
import com.trailmate.app.core.model.HikeLocationFix
import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.HikeSessionState
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.LiveCheckpointGuidance
import com.trailmate.app.core.model.LocationBackedHikeStatus
import com.trailmate.app.core.model.RouteAssessmentSummary
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.feature.route.RouteCockpitTabContent

@Composable
internal fun RouteCockpitTab(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    liveGuidance: LiveCheckpointGuidance,
    mapReadiness: TrailMapReadiness,
    offlineRoutePackReady: Boolean,
    offlineBaseMapRegionCount: Int? = null,
    offlineBaseMapCoversTargetRoute: Boolean = false,
    offlineBaseMapTilesVerifiedWithoutNetwork: Boolean = false,
    gearRecommendations: List<GearRecommendation>,
    onSessionChange: (HikeSessionState) -> Unit,
    onCheckpointFocused: (HikePlanCheckpoint) -> Unit,
    onOfflineRoutePackToggle: () -> Unit,
    onShowGearTab: () -> Unit,
    gpsEnabled: Boolean,
    locationSnapshot: TrailMateLocationSnapshot,
    locationPresentationNowEpochMillis: Long = System.currentTimeMillis(),
    locationGuidanceStatus: LocationBackedHikeStatus,
    locationGuidanceCaption: String,
    latestLocationFix: HikeLocationFix?,
    wasRecentlyOffRoute: Boolean,
    trackRecording: TrackRecordingState,
    notificationPermissionGranted: Boolean,
    trackActionLabel: String,
    checkpointDetailFor: (HikePlanCheckpoint) -> HikeCheckpointDetail,
    onRequestLocation: () -> Unit,
    onStopLocationUpdates: () -> Unit,
    onShareSafetyText: (String) -> Unit,
    onTrackAction: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenTrackDataRequested: () -> Unit,
    onFinishTrack: () -> Unit,
    onAcknowledgeRouteRejoin: () -> Unit,
    amapLaunchDiagnostics: AmapLaunchDiagnostics? = null,
    onOpenOfflineMap: () -> Unit = {},
    onOpenNetworkSettings: () -> Unit = {},
    onRecordOfflineBaseMapTileProof: () -> Unit = {},
    offlineBaseMapTileProofCaptureState: AmapOfflineBaseMapTileProofCaptureState = AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
        targetRegionKnown = false,
        offlineBaseMapCoversTargetRoute = false,
        networkUnavailable = false,
        amapBaseMapRenderedInCurrentSession = false
    ),
    offlineBaseMapTileProofMessage: String? = null,
    offlineBaseMapManagerReturnMessage: String? = null,
    onAmapBaseMapRenderedChange: (Boolean) -> Unit = {},
    navigationFullscreen: Boolean = false,
    onNavigationFullscreenChange: (Boolean) -> Unit = {}
) {
    RouteCockpitTabContent(
        route = route,
        assessment = assessment,
        plan = plan,
        hikeSession = hikeSession,
        liveGuidance = liveGuidance,
        mapReadiness = mapReadiness,
        offlineRoutePackReady = offlineRoutePackReady,
        offlineBaseMapRegionCount = offlineBaseMapRegionCount,
        offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
        offlineBaseMapTilesVerifiedWithoutNetwork = offlineBaseMapTilesVerifiedWithoutNetwork,
        gearRecommendations = gearRecommendations,
        onSessionChange = onSessionChange,
        onCheckpointFocused = onCheckpointFocused,
        onOfflineRoutePackToggle = onOfflineRoutePackToggle,
        onShowGearTab = onShowGearTab,
        gpsEnabled = gpsEnabled,
        locationSnapshot = locationSnapshot,
        locationPresentationNowEpochMillis = locationPresentationNowEpochMillis,
        locationGuidanceStatus = locationGuidanceStatus,
        locationGuidanceCaption = locationGuidanceCaption,
        latestLocationFix = latestLocationFix,
        wasRecentlyOffRoute = wasRecentlyOffRoute,
        trackRecording = trackRecording,
        notificationPermissionGranted = notificationPermissionGranted,
        trackActionLabel = trackActionLabel,
        checkpointDetailFor = checkpointDetailFor,
        onRequestLocation = onRequestLocation,
        onStopLocationUpdates = onStopLocationUpdates,
        onShareSafetyText = onShareSafetyText,
        onTrackAction = onTrackAction,
        onRequestNotificationPermission = onRequestNotificationPermission,
        onOpenTrackDataRequested = onOpenTrackDataRequested,
        onFinishTrack = onFinishTrack,
        onAcknowledgeRouteRejoin = onAcknowledgeRouteRejoin,
        amapLaunchDiagnostics = amapLaunchDiagnostics,
        onOpenOfflineMap = onOpenOfflineMap,
        onOpenNetworkSettings = onOpenNetworkSettings,
        onRecordOfflineBaseMapTileProof = onRecordOfflineBaseMapTileProof,
        offlineBaseMapTileProofCaptureState = offlineBaseMapTileProofCaptureState,
        offlineBaseMapTileProofMessage = offlineBaseMapTileProofMessage,
        offlineBaseMapManagerReturnMessage = offlineBaseMapManagerReturnMessage,
        onAmapBaseMapRenderedChange = onAmapBaseMapRenderedChange,
        navigationFullscreen = navigationFullscreen,
        onNavigationFullscreenChange = onNavigationFullscreenChange
    )
}
