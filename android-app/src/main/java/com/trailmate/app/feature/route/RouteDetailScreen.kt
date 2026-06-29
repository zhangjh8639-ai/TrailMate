package com.trailmate.app.feature.route

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.BatteryManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.trailmate.app.core.location.AndroidLocationTracker
import com.trailmate.app.core.location.TrailMateLocationActiveUsePolicy
import com.trailmate.app.core.location.TrailMateLocationActivationAction
import com.trailmate.app.core.location.TrailMateLocationActivationEngine
import com.trailmate.app.core.location.TrailMateLocationAppSettingsReturnEffect
import com.trailmate.app.core.location.TrailMateLocationAppSettingsReturnEffectEngine
import com.trailmate.app.core.location.TrailMateLocationFixReliability
import com.trailmate.app.core.location.TrailMateLocationPermissionResult
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import com.trailmate.app.core.location.TrailMateLocationSessionPolicy
import com.trailmate.app.core.location.TrailMateLocationSettingsLaunchOutcome
import com.trailmate.app.core.location.TrailMateLocationSettingsLaunchPolicy
import com.trailmate.app.core.location.TrailMateLocationSettingsReturnEffect
import com.trailmate.app.core.location.TrailMateLocationSettingsReturnEffectEngine
import com.trailmate.app.core.location.TrackRecordingForegroundService
import com.trailmate.app.core.location.RouteDeviationAlertAndroidDelivery
import com.trailmate.app.core.location.LocationReliabilityDetail
import com.trailmate.app.core.location.LocationReliabilityLevel
import com.trailmate.app.core.location.LocationReliabilityPresentation
import com.trailmate.app.core.location.TrailMateLocationReliabilityEngine
import com.trailmate.app.core.map.AmapLaunchDiagnosticItem
import com.trailmate.app.core.map.AmapLaunchDiagnosticStatus
import com.trailmate.app.core.map.AmapLaunchDiagnostics
import com.trailmate.app.core.map.AmapLaunchDiagnosticsEngine
import com.trailmate.app.core.map.AmapDownloadNetworkStatusRefreshAction
import com.trailmate.app.core.map.AmapDownloadNetworkStatusRefreshPolicy
import com.trailmate.app.core.map.AmapNetworkSettingsReturnAction
import com.trailmate.app.core.map.AmapNetworkSettingsReturnRefreshPolicy
import com.trailmate.app.core.map.AndroidPackageSignatureSha1Reader
import com.trailmate.app.core.map.AmapOfflineBaseMapCoverageEngine
import com.trailmate.app.core.map.AmapOfflineBaseMapStatus
import com.trailmate.app.core.map.AmapOfflineBaseMapStatusReader
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProofCaptureEngine
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProofCaptureState
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProofEngine
import com.trailmate.app.core.map.AmapOfflineBaseMapTargetHintEngine
import com.trailmate.app.core.map.AmapOfflineMapLauncher
import com.trailmate.app.core.map.AmapOfflineMapReturnAction
import com.trailmate.app.core.map.AmapOfflineMapReturnRefreshPolicy
import com.trailmate.app.core.map.AmapOfflineMapReturnStatusAction
import com.trailmate.app.core.map.AmapOfflineMapReturnStatusPolicy
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.map.AmapSdkAvailability
import com.trailmate.app.core.map.AmapTargetRouteRegion
import com.trailmate.app.core.map.AmapTargetRouteRegionReader
import com.trailmate.app.core.map.MapScreenPoint
import com.trailmate.app.core.map.MapLibrePmTilesBundledStyleAssetManifestResolver
import com.trailmate.app.core.map.MapLibrePmTilesStyleAssetReadiness
import com.trailmate.app.core.map.MapLibrePmTilesStyleAssetReadinessEngine
import com.trailmate.app.core.map.MapLibreSdkAvailability
import com.trailmate.app.core.map.PmTilesArchiveHeaderParser
import com.trailmate.app.core.map.PmTilesLatLngBounds
import com.trailmate.app.core.map.PmTilesOfflineBasemapImportCandidate
import com.trailmate.app.core.map.PmTilesOfflineBasemapImportPolicy
import com.trailmate.app.core.map.PmTilesOfflineBasemapManifestReader
import com.trailmate.app.core.map.PmTilesOfflineBasemapStatusEngine
import com.trailmate.app.core.map.TrailMapCheckpointMarker
import com.trailmate.app.core.map.TrailMapCheckpointProjector
import com.trailmate.app.core.map.TrailMapLayerLegend
import com.trailmate.app.core.map.TrailMapLayerLegendEngine
import com.trailmate.app.core.map.TrailMapLayerLegendItem
import com.trailmate.app.core.map.TrailMapLayerLegendItemStatus
import com.trailmate.app.core.map.TrailMapLoadingPresentation
import com.trailmate.app.core.map.TrailMapLoadingPresentationEngine
import com.trailmate.app.core.map.TrailMapProjection
import com.trailmate.app.core.map.TrailMapProvider
import com.trailmate.app.core.map.TrailMapReadiness
import com.trailmate.app.core.map.TrailMapReadinessEngine
import com.trailmate.app.core.map.TrailMapReadinessStep
import com.trailmate.app.core.map.TrailMapReadinessStepStatus
import com.trailmate.app.core.map.TrailMapSetupHint
import com.trailmate.app.core.map.TrailMapSurfaceMode
import com.trailmate.app.core.map.TrailMapSurfaceSelector
import com.trailmate.app.core.map.TrailMateDeviceDiagnosticsReportAction
import com.trailmate.app.core.map.TrailMateDeviceDiagnosticsReportActionPresenter
import com.trailmate.app.core.map.TrailMateDeviceIdentity
import com.trailmate.app.core.map.TrailMateDeviceDiagnosticsReportFormatter
import com.trailmate.app.core.network.TrailMateHttpPmTilesBasemapFileDownloader
import com.trailmate.app.core.network.TrailMateOfflineBasemapCatalogApi
import com.trailmate.app.core.network.TrailMatePmTilesBasemapRemoteImportCoordinator
import com.trailmate.app.core.network.TrailMatePmTilesRemoteImportAction
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.AiGearAdvisorContract
import com.trailmate.app.core.model.AiGearAdvisorPresentation
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BacktrackBreadcrumbGuidanceDetail
import com.trailmate.app.core.model.BacktrackBreadcrumbGuidanceEngine
import com.trailmate.app.core.model.BacktrackBreadcrumbGuidancePresentation
import com.trailmate.app.core.model.BacktrackBreadcrumbGuidanceTone
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.DaylightReturnWatchDetail
import com.trailmate.app.core.model.DaylightReturnWatchEngine
import com.trailmate.app.core.model.DaylightReturnWatchPresentation
import com.trailmate.app.core.model.DaylightReturnWatchTone
import com.trailmate.app.core.model.DepartureBriefPlan
import com.trailmate.app.core.model.DepartureBriefShareDetail
import com.trailmate.app.core.model.DepartureBriefShareActionEngine
import com.trailmate.app.core.model.DepartureBriefShareEngine
import com.trailmate.app.core.model.DepartureBriefSharePresentation
import com.trailmate.app.core.model.DepartureReadinessPrimaryAction
import com.trailmate.app.core.model.DepartureReadinessPrimaryActionEngine
import com.trailmate.app.core.model.DepartureReadinessPrimaryActionKind
import com.trailmate.app.core.model.DepartureReadinessEngine
import com.trailmate.app.core.model.DepartureReadinessStep
import com.trailmate.app.core.model.DepartureReadinessSummary
import com.trailmate.app.core.model.GearCatalogItem
import com.trailmate.app.core.model.GearCatalogSelectionEngine
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearDepartureQaOverridePolicy
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.GpsSignalLossWatchDetail
import com.trailmate.app.core.model.GpsSignalLossWatchEngine
import com.trailmate.app.core.model.GpsSignalLossWatchPresentation
import com.trailmate.app.core.model.GpsSignalLossWatchTone
import com.trailmate.app.core.model.HikeLocationFix
import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanCheckpointType
import com.trailmate.app.core.model.HikeCheckpointDetail
import com.trailmate.app.core.model.HikeCheckpointDetailAdvisor
import com.trailmate.app.core.model.HikePlanEngine
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.HikeSessionEngine
import com.trailmate.app.core.model.HikeSessionState
import com.trailmate.app.core.model.HikeSessionStatus
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.LiveCheckpointGuidance
import com.trailmate.app.core.model.LiveCheckpointGuidanceEngine
import com.trailmate.app.core.model.LocationBackedHikeSessionEngine
import com.trailmate.app.core.model.LocationBackedHikeStatus
import com.trailmate.app.core.model.LocationGuidancePresentation
import com.trailmate.app.core.model.LocationGuidancePresentationEngine
import com.trailmate.app.core.model.LocationGuidanceTone
import com.trailmate.app.core.model.LowPowerGuidanceAction
import com.trailmate.app.core.model.LowPowerGuidanceEngine
import com.trailmate.app.core.model.LowPowerGuidancePresentation
import com.trailmate.app.core.model.LowPowerGuidanceTone
import com.trailmate.app.core.model.OfflineBaseMapDepartureQaOverridePolicy
import com.trailmate.app.core.model.OfflineBaseMapDepartureState
import com.trailmate.app.core.model.OfflineBaseMapRequirementPolicy
import com.trailmate.app.core.model.OfflineEmergencyInfoActionEngine
import com.trailmate.app.core.model.OfflineEmergencyInfoDetail
import com.trailmate.app.core.model.OfflineEmergencyInfoEngine
import com.trailmate.app.core.model.OfflineEmergencyInfoPresentation
import com.trailmate.app.core.model.OfflineEmergencyLocation
import com.trailmate.app.core.model.OfflineEmergencyProgress
import com.trailmate.app.core.model.OfflineEmergencyRouteSummary
import com.trailmate.app.core.model.MatchLevel
import com.trailmate.app.core.model.ProgressSafetyWatchDetail
import com.trailmate.app.core.model.ProgressSafetyWatchEngine
import com.trailmate.app.core.model.ProgressSafetyWatchPresentation
import com.trailmate.app.core.model.ProgressSafetyWatchTone
import com.trailmate.app.core.model.RouteGeometryEngine
import com.trailmate.app.core.model.RouteAssessmentEngine
import com.trailmate.app.core.model.RouteAssessmentSummary
import com.trailmate.app.core.model.RouteCockpitPresentation
import com.trailmate.app.core.model.RouteCockpitPresentationEngine
import com.trailmate.app.core.model.RouteCockpitPrimaryActionKind
import com.trailmate.app.core.model.RouteCockpitReadinessActionKind
import com.trailmate.app.core.model.RouteCockpitReadinessItem
import com.trailmate.app.core.model.RouteCockpitReadinessTone
import com.trailmate.app.core.model.RouteBatteryStatus
import com.trailmate.app.core.model.RouteFieldStatusEngine
import com.trailmate.app.core.model.RouteFieldStatusItem
import com.trailmate.app.core.model.RouteFieldStatusSummary
import com.trailmate.app.core.model.RouteDeviationAlertDecision
import com.trailmate.app.core.model.RouteDeviationAlertDeliveryOwnerPolicy
import com.trailmate.app.core.model.RouteDeviationAlertPolicy
import com.trailmate.app.core.model.RouteDeviationAlertPresentation
import com.trailmate.app.core.model.RouteDeviationAlertPresentationEngine
import com.trailmate.app.core.model.RouteDeviationAlertState
import com.trailmate.app.core.model.RouteDeviationAlertTone
import com.trailmate.app.core.model.RouteDeviationRecoveryAction
import com.trailmate.app.core.model.RouteDeviationRecoveryActionKind
import com.trailmate.app.core.model.RouteDeviationRecoveryDetail
import com.trailmate.app.core.model.RouteDeviationRecoveryEngine
import com.trailmate.app.core.model.RouteDeviationRecoveryPresentation
import com.trailmate.app.core.model.RouteDeviationRecoveryStep
import com.trailmate.app.core.model.RouteDeviationRecoveryTone
import com.trailmate.app.core.model.RouteDirectionWatchDetail
import com.trailmate.app.core.model.RouteDirectionWatchEngine
import com.trailmate.app.core.model.RouteDirectionWatchPresentation
import com.trailmate.app.core.model.RouteDirectionWatchTone
import com.trailmate.app.core.model.RouteExitGuidanceEngine
import com.trailmate.app.core.model.RouteExitGuidanceOption
import com.trailmate.app.core.model.RouteExitGuidancePresentation
import com.trailmate.app.core.model.RouteExitGuidanceTone
import com.trailmate.app.core.model.RouteGearAdvisorEngine
import com.trailmate.app.core.model.ReturnEtaPlan
import com.trailmate.app.core.model.ReturnEtaWatchDetail
import com.trailmate.app.core.model.ReturnEtaWatchEngine
import com.trailmate.app.core.model.ReturnEtaWatchPresentation
import com.trailmate.app.core.model.ReturnEtaWatchTone
import com.trailmate.app.core.model.SafetyShareDetail
import com.trailmate.app.core.model.SafetyShareActionEngine
import com.trailmate.app.core.model.SafetyShareEngine
import com.trailmate.app.core.model.SafetyShareLocation
import com.trailmate.app.core.model.SafetySharePresentation
import com.trailmate.app.core.model.SafetyShareRoutePlan
import com.trailmate.app.core.model.TrailMateGearCatalogPreviewData
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TrackRecordingActionGateEngine
import com.trailmate.app.core.model.TrackRecordingActionGateStep
import com.trailmate.app.core.model.TrackRecordingDepartureGateActionKind
import com.trailmate.app.core.model.TrackRecordingDepartureGateEngine
import com.trailmate.app.core.model.TrackRecordingForegroundRecoveryPolicy
import com.trailmate.app.core.model.TrackRecordingReviewEngine
import com.trailmate.app.core.model.TrackRecordingReviewPresentation
import com.trailmate.app.core.model.TrackRecordingRouteIdentityPolicy
import com.trailmate.app.core.model.TrackRecordingServiceCommand
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus
import com.trailmate.app.core.model.TrackRecordingUiActionEngine
import com.trailmate.app.core.model.offlineRoutePackKey
import com.trailmate.app.core.share.TrailMateWechatTextShareLauncher
import com.trailmate.app.core.share.TrailMateWechatTextShareSendStatus
import com.trailmate.app.BuildConfig
import com.trailmate.app.feature.route.detail.RouteAssessmentTab
import com.trailmate.app.feature.route.detail.RouteGearTab
import com.trailmate.app.feature.route.detail.RoutePlanTab
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RouteDetailScreen(
    route: ImportedRoute = TrailMateSampleData.importedTargetRoute,
    profile: BaselineProfile = TrailMateSampleData.baselineProfile,
    catalogItems: List<GearCatalogItem> = TrailMateGearCatalogPreviewData.items,
    catalogStatusLabel: String = "品牌库 · 本地缓存",
    offlineBasemapCatalogApi: TrailMateOfflineBasemapCatalogApi? = null,
    routeAssessment: RouteAssessmentSummary? = null,
    gearRecommendations: List<GearRecommendation>? = null,
    aiGearAdvisorResponse: AiGearAdvisorResponse? = null,
    initialTrackRecording: TrackRecordingState = TrackRecordingState(),
    initialLocationSnapshot: TrailMateLocationSnapshot = TrailMateLocationSnapshot.disabled(),
    initialLocationGuidanceStatus: LocationBackedHikeStatus = LocationBackedHikeStatus.WAITING,
    initialLocationGuidanceCaption: String = "授权定位后，可用当前位置辅助检查点推进。",
    initialLocationFix: HikeLocationFix? = null,
    initialWasRecentlyOffRoute: Boolean = false,
    initialOfflineRoutePackReady: Boolean = false,
    initialOfflineBaseMapTileProofs: List<AmapOfflineBaseMapTileProof> = emptyList(),
    initiallyShowRouteCockpit: Boolean = false,
    initiallyExpandRouteDiagnostics: Boolean = false,
    amapPrivacyConsent: AmapPrivacyConsent = AmapPrivacyConsent(),
    amapApiKeyConfigured: Boolean = BuildConfig.TRAILMATE_AMAP_API_KEY.isNotBlank(),
    amapSdkAvailable: Boolean = AmapSdkAvailability.isLinked,
    notificationPermissionGranted: Boolean? = null,
    routeNavigationFullscreen: Boolean = false,
    onTrackRecordingChanged: (TrackRecordingState) -> Unit = {},
    onOfflineRoutePackReadyChanged: (Boolean) -> Unit = {},
    onOfflineBaseMapTileProofsChanged: (List<AmapOfflineBaseMapTileProof>) -> Unit = {},
    onRouteNavigationFullscreenChanged: (Boolean) -> Unit = {},
    onOpenTrackDataRequested: () -> Unit = {},
    onViewGearMatchesRequested: (String) -> Unit = {},
    onBackToRouteWorkspace: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val packageSha1 = remember(context) { AndroidPackageSignatureSha1Reader.read(context) }
    val locationTracker = remember(context) { AndroidLocationTracker(context) }
    val assessment = routeAssessment ?: RouteAssessmentEngine.assess(profile = profile, route = route)
    val plan = HikePlanEngine.build(route = route, assessment = assessment)
    val fallbackGearRecommendations = gearRecommendations
        ?: RouteGearAdvisorEngine.recommend(route = route, assessment = assessment)
    val aiGearAdvisorRequest = AiGearAdvisorContract.buildRequest(
        route = route,
        profile = profile,
        assessment = assessment,
        fallbackRecommendations = fallbackGearRecommendations
    )
    val aiGearAdvisorPresentation = AiGearAdvisorContract.resolvePresentation(
        request = aiGearAdvisorRequest,
        response = aiGearAdvisorResponse
    )
    val routeSessionKey = route.sessionKey()
    var selected by rememberSaveable(initiallyShowRouteCockpit) {
        mutableStateOf(
            if (initiallyShowRouteCockpit) {
                RouteDetailTab.Route
            } else {
                RouteDetailTab.Assessment
            }
        )
    }
    var offlineRoutePackReady by rememberSaveable(routeSessionKey, initialOfflineRoutePackReady) {
        mutableStateOf(initialOfflineRoutePackReady)
    }
    var offlineBaseMapTileProofs by remember(routeSessionKey) {
        mutableStateOf(initialOfflineBaseMapTileProofs)
    }
    var offlineBaseMapTileProofMessage by remember(routeSessionKey) {
        mutableStateOf<String?>(null)
    }
    var amapBaseMapRenderedInCurrentSession by remember(routeSessionKey) {
        mutableStateOf(false)
    }
    var hikeStatus by remember(routeSessionKey) { mutableStateOf(HikeSessionStatus.READY) }
    var reachedCheckpointIndex by remember(routeSessionKey) { mutableStateOf(0) }
    var gpsEnabled by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var locationTrackingRestartToken by rememberSaveable(routeSessionKey) { mutableStateOf(0) }
    var locationSnapshot by remember(routeSessionKey) { mutableStateOf(initialLocationSnapshot) }
    var locationGuidanceStatus by remember(routeSessionKey) { mutableStateOf(initialLocationGuidanceStatus) }
    var locationGuidanceCaption by remember(routeSessionKey) { mutableStateOf(initialLocationGuidanceCaption) }
    var locationPresentationNowEpochMillis by remember(routeSessionKey) {
        mutableStateOf(System.currentTimeMillis())
    }
    var returnEtaNowEpochMillis by remember(routeSessionKey) {
        mutableStateOf(System.currentTimeMillis())
    }
    var offlineEmergencyInfoNowEpochMillis by remember(routeSessionKey) {
        mutableStateOf(System.currentTimeMillis())
    }
    var latestLocationFix by remember(routeSessionKey) { mutableStateOf(initialLocationFix) }
    var previousRouteDirectionFix by remember(routeSessionKey) { mutableStateOf<HikeLocationFix?>(null) }
    var wasRecentlyOffRoute by remember(routeSessionKey) { mutableStateOf(initialWasRecentlyOffRoute) }
    var routeDeviationAlertState by remember(routeSessionKey) { mutableStateOf(RouteDeviationAlertState()) }
    var latestRouteDeviationAlertDecision by remember(routeSessionKey) {
        mutableStateOf<RouteDeviationAlertDecision?>(null)
    }
    var runtimeNotificationPermissionGranted by remember(routeSessionKey) {
        mutableStateOf(context.hasTrackNotificationPermission())
    }
    val trackNotificationPermissionGranted =
        notificationPermissionGranted ?: runtimeNotificationPermissionGranted
    val currentRouteKey = route.offlineRoutePackKey()
    var trackRecording by remember(routeSessionKey) {
        mutableStateOf(
            if (
                TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
                    trackRecording = initialTrackRecording,
                    routeName = route.routeName,
                    routeKey = currentRouteKey
                )
            ) {
                initialTrackRecording
            } else {
                TrackRecordingState()
            }
        )
    }
    var trackServiceRestoreAttempted by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var pendingTrackActionAfterLocationPermission by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var pendingTrackActionAfterNotificationPermission by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var pendingLocationSettingsReturn by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var pendingLocationAppSettingsReturn by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var locationPermissionRequested by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var pendingOfflineMapManagerReturn by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var pendingNetworkSettingsReturn by rememberSaveable(routeSessionKey) { mutableStateOf(false) }
    var offlineBaseMapStatusRefreshToken by rememberSaveable(routeSessionKey) { mutableStateOf(0) }
    var pmTilesBasemapRefreshToken by rememberSaveable(routeSessionKey) { mutableStateOf(0) }
    var pmTilesImportMessage by remember(routeSessionKey) { mutableStateOf<String?>(null) }
    var offlineBaseMapReturnStatusCheckToken by rememberSaveable(routeSessionKey) {
        mutableStateOf<Int?>(null)
    }
    var networkSettingsRefreshToken by rememberSaveable(routeSessionKey) { mutableStateOf(0) }
    var targetRouteRegion by remember(routeSessionKey) {
        mutableStateOf<AmapTargetRouteRegion?>(null)
    }
    val pmTilesRemoteImportCoordinator = remember(offlineBasemapCatalogApi) {
        TrailMatePmTilesBasemapRemoteImportCoordinator(
            catalogApi = offlineBasemapCatalogApi,
            downloader = TrailMateHttpPmTilesBasemapFileDownloader()
        )
    }
    val pmTilesBasemapStatus = remember(routeSessionKey, targetRouteRegion, pmTilesBasemapRefreshToken) {
        PmTilesOfflineBasemapStatusEngine.resolve(
            manifest = PmTilesOfflineBasemapManifestReader.read(
                directory = context.pmTilesBasemapDirectory(),
                routePackKey = route.offlineRoutePackKey(),
                targetRegionName = targetRouteRegion?.cityName,
                targetBounds = route.pmTilesTargetBounds()
            ),
            targetRegionName = targetRouteRegion?.cityName
        )
    }
    val mapLibrePmTilesStyleAssetManifest = remember(context) {
        MapLibrePmTilesBundledStyleAssetManifestResolver.resolve { assetPath ->
            runCatching {
                context.assets.open(assetPath).use { true }
            }.getOrDefault(false)
        }
    }
    val mapLibrePmTilesStyleAssetReadiness = remember(mapLibrePmTilesStyleAssetManifest) {
        MapLibrePmTilesStyleAssetReadinessEngine.resolve(mapLibrePmTilesStyleAssetManifest)
    }
    val mapReadiness = TrailMapReadinessEngine.resolve(
        hasAmapKey = amapApiKeyConfigured,
        amapSdkAvailable = amapSdkAvailable,
        amapPrivacyConsentAccepted = amapPrivacyConsent.accepted,
        mapLibreRuntimeAvailable = MapLibreSdkAvailability.isLinked,
        pmTilesBasemapPackReady = pmTilesBasemapStatus.ready,
        pmTilesStyleAssetReadiness = mapLibrePmTilesStyleAssetReadiness,
        offlineRoutePackReady = offlineRoutePackReady,
        gpsEnabled = gpsEnabled,
        locationReadyForFieldUse = TrailMateLocationFixReliability.isReliableForFieldUse(
            snapshot = locationSnapshot,
            nowEpochMillis = locationPresentationNowEpochMillis,
            maxAccuracyMeters = FIELD_LOCATION_MAX_ACCURACY_METERS
        ),
        routePointCount = route.routePoints.size
    )
    LaunchedEffect(routeSessionKey, amapSdkAvailable, amapPrivacyConsent.accepted) {
        targetRouteRegion = null
        if (amapSdkAvailable && amapPrivacyConsent.accepted) {
            targetRouteRegion = withContext(Dispatchers.IO) {
                AmapTargetRouteRegionReader.read(
                    context = context,
                    routePoints = route.routePoints
                )
            }
        }
    }
    val offlineBaseMapTargetHint = remember(targetRouteRegion, routeSessionKey) {
        AmapOfflineBaseMapTargetHintEngine.build(
            targetRegion = targetRouteRegion,
            routePoints = route.routePoints
        )
    }
    var offlineBaseMapStatus by remember(routeSessionKey, amapSdkAvailable, amapPrivacyConsent.accepted) {
        mutableStateOf<AmapOfflineBaseMapStatus?>(null)
    }
    var offlineBaseMapStatusBeforeManager by remember(routeSessionKey) {
        mutableStateOf<AmapOfflineBaseMapStatus?>(null)
    }
    var offlineBaseMapManagerReturnMessage by remember(routeSessionKey) {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(
        routeSessionKey,
        amapSdkAvailable,
        amapPrivacyConsent.accepted,
        offlineBaseMapStatusRefreshToken
    ) {
        val refreshedOfflineBaseMapStatus = readOfflineBaseMapStatus(
            context = context,
            amapSdkAvailable = amapSdkAvailable,
            amapPrivacyConsentAccepted = amapPrivacyConsent.accepted
        )
        offlineBaseMapStatus = refreshedOfflineBaseMapStatus
        if (offlineBaseMapReturnStatusCheckToken == offlineBaseMapStatusRefreshToken) {
            val result = AmapOfflineMapReturnStatusPolicy.resolve(
                returnedFromOfflineManager = true,
                beforeStatus = offlineBaseMapStatusBeforeManager,
                afterStatus = refreshedOfflineBaseMapStatus,
                targetRegionLabel = offlineBaseMapTargetHint.regionLabel
            )
            when (result.action) {
                AmapOfflineMapReturnStatusAction.SHOW_NO_DOWNLOAD_DETECTED ->
                    offlineBaseMapManagerReturnMessage = result.message
                AmapOfflineMapReturnStatusAction.CLEAR_RETURN_MESSAGE ->
                    offlineBaseMapManagerReturnMessage = null
                AmapOfflineMapReturnStatusAction.NONE -> Unit
            }
            offlineBaseMapReturnStatusCheckToken = null
        }
    }
    val refreshOfflineBaseMapStatusAfterManagerReturn: () -> Unit = {
        val nextRefreshToken = offlineBaseMapStatusRefreshToken + 1
        offlineBaseMapReturnStatusCheckToken = nextRefreshToken
        offlineBaseMapStatusRefreshToken = nextRefreshToken
    }
    val openOfflineMapManager: () -> Unit = {
        val statusBeforeOpening = offlineBaseMapStatus
        if (AmapOfflineMapLauncher.open(context)) {
            offlineBaseMapStatusBeforeManager = statusBeforeOpening
            offlineBaseMapManagerReturnMessage = null
            pendingOfflineMapManagerReturn = true
        }
    }
    val pmTilesImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        coroutineScope.launch {
            val candidate = withContext(Dispatchers.IO) {
                context.readPmTilesImportCandidate(uri)
            }
            val decision = PmTilesOfflineBasemapImportPolicy.resolve(
                candidate = candidate,
                routePackKey = route.offlineRoutePackKey(),
                targetBounds = route.pmTilesTargetBounds()
            )
            if (!decision.canImport) {
                pmTilesImportMessage = decision.caption
                return@launch
            }
            val imported = withContext(Dispatchers.IO) {
                context.copyPmTilesBasemap(uri = uri, targetFileName = decision.targetFileName)
            }
            pmTilesImportMessage = if (imported) {
                pmTilesBasemapRefreshToken += 1
                "已导入 ${decision.targetFileName}，正在刷新离线底图状态。"
            } else {
                "PMTiles 导入失败，请重新选择文件。"
            }
        }
    }
    val importPmTilesBasemap: () -> Unit = {
        pmTilesImportMessage = "正在查找当前路线离线底图..."
        coroutineScope.launch {
            val remoteImportResult = withContext(Dispatchers.IO) {
                pmTilesRemoteImportCoordinator.importForRoute(
                    routeBounds = route.pmTilesTargetBounds(),
                    routePackKey = route.offlineRoutePackKey(),
                    targetDirectory = context.pmTilesBasemapDirectory()
                )
            }
            pmTilesImportMessage = remoteImportResult.message
            when (remoteImportResult.action) {
                TrailMatePmTilesRemoteImportAction.IMPORTED -> {
                    pmTilesBasemapRefreshToken += 1
                }
                TrailMatePmTilesRemoteImportAction.OPEN_LOCAL_PICKER -> {
                    pmTilesImportLauncher.launch(arrayOf("*/*"))
                }
            }
        }
    }
    val openNetworkSettings: () -> Unit = {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        }.onFailure {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
        pendingNetworkSettingsReturn = true
    }
    val initialOfflineBaseMapStatus = remember(routeSessionKey, amapSdkAvailable, amapPrivacyConsent.accepted) {
        if (amapSdkAvailable && amapPrivacyConsent.accepted) {
            AmapOfflineBaseMapStatusReader.readDownloadedStatus(context)
        } else {
            null
        }
    }
    LaunchedEffect(initialOfflineBaseMapStatus) {
        if (offlineBaseMapStatus == null && initialOfflineBaseMapStatus != null) {
            offlineBaseMapStatus = initialOfflineBaseMapStatus
        }
    }
    val offlineBaseMapCoversTargetRoute = remember(offlineBaseMapStatus, targetRouteRegion) {
        AmapOfflineBaseMapCoverageEngine.coversTargetRoute(
            status = offlineBaseMapStatus,
            targetRegion = targetRouteRegion
        )
    }
    val offlineBaseMapTilesVerifiedWithoutNetwork = remember(
        offlineBaseMapTileProofs,
        routeSessionKey,
        targetRouteRegion
    ) {
        AmapOfflineBaseMapTileProofEngine.hasVerifiedProof(
            proofs = offlineBaseMapTileProofs,
            routeKey = routeSessionKey,
            targetRegion = targetRouteRegion
        )
    }
    val offlineBaseMapDebugBypassEnabled = remember(offlineBaseMapStatusRefreshToken) {
        context.isOfflineBaseMapDebugBypassEnabled()
    }
    val effectiveOfflineBaseMapDepartureState = remember(
        offlineBaseMapStatus,
        offlineBaseMapCoversTargetRoute,
        offlineBaseMapTilesVerifiedWithoutNetwork,
        offlineBaseMapDebugBypassEnabled
    ) {
        OfflineBaseMapDepartureQaOverridePolicy.apply(
            state = OfflineBaseMapDepartureState(
                downloadedRegionCount = offlineBaseMapStatus?.downloadedRegionCount,
                coversTargetRoute = offlineBaseMapCoversTargetRoute,
                tilesVerifiedWithoutNetwork = offlineBaseMapTilesVerifiedWithoutNetwork
            ),
            debugBypassEnabled = offlineBaseMapDebugBypassEnabled
        )
    }
    val effectiveDepartureGearRecommendations = remember(
        aiGearAdvisorPresentation.recommendations,
        catalogItems,
        offlineBaseMapDebugBypassEnabled
    ) {
        val catalogMatchedRecommendations = GearCatalogSelectionEngine.resolveRouteMatchesForDeparture(
            recommendations = aiGearAdvisorPresentation.recommendations,
            catalogItems = catalogItems
        )
        GearDepartureQaOverridePolicy.apply(
            recommendations = catalogMatchedRecommendations,
            debugBypassEnabled = offlineBaseMapDebugBypassEnabled
        )
    }
    val offlineBaseMapTileProofCaptureState = remember(
        targetRouteRegion,
        offlineBaseMapCoversTargetRoute,
        amapBaseMapRenderedInCurrentSession,
        networkSettingsRefreshToken
    ) {
        AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
            targetRegionKnown = targetRouteRegion != null,
            offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
            networkUnavailable = context.isNetworkUnavailable(),
            amapBaseMapRenderedInCurrentSession = amapBaseMapRenderedInCurrentSession
        )
    }
    val recordOfflineBaseMapTileProof: () -> Unit = {
        val region = targetRouteRegion
        val networkUnavailable = context.isNetworkUnavailable()
        val captureState = AmapOfflineBaseMapTileProofCaptureEngine.evaluate(
            targetRegionKnown = region != null,
            offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
            networkUnavailable = networkUnavailable,
            amapBaseMapRenderedInCurrentSession = amapBaseMapRenderedInCurrentSession
        )
        val proof = if (region != null && captureState.canRecordProof) {
            AmapOfflineBaseMapTileProofEngine.recordProofOrNull(
                routeKey = routeSessionKey,
                targetRegion = region,
                nowEpochMillis = System.currentTimeMillis(),
                networkDisabled = networkUnavailable,
                tileVisible = amapBaseMapRenderedInCurrentSession
            )
        } else {
            null
        }

        if (proof == null) {
            offlineBaseMapTileProofMessage = captureState.failureMessage ?: "当前还不能记录断网瓦片证明。"
        } else {
            val updated = offlineBaseMapTileProofs
                .filterNot { existing ->
                    existing.routeKey == proof.routeKey && existing.targetAdcode == proof.targetAdcode
                } + proof
            offlineBaseMapTileProofs = updated
            offlineBaseMapTileProofMessage = "已记录本路线的断网瓦片证明。"
            onOfflineBaseMapTileProofsChanged(updated)
        }
    }
    val offlineDownloadNetworkValidated = remember(networkSettingsRefreshToken) {
        context.isNetworkValidated()
    }
    val downloadNetworkRefreshHandler = remember { Handler(Looper.getMainLooper()) }
    val refreshDownloadNetworkStatus: () -> Unit = {
        networkSettingsRefreshToken += 1
    }
    val currentRefreshDownloadNetworkStatus by rememberUpdatedState(refreshDownloadNetworkStatus)
    val amapLaunchDiagnostics = AmapLaunchDiagnosticsEngine.build(
        packageName = context.packageName,
        packageSha1 = packageSha1,
        hasAmapKey = amapApiKeyConfigured,
        amapSdkAvailable = amapSdkAvailable,
        amapPrivacyConsentAccepted = amapPrivacyConsent.accepted,
        offlineMapActivityRegistered = AmapOfflineMapLauncher.isRegistered(context),
        productionMapReady = mapReadiness.isProductionMapReady,
        routePointCount = route.routePoints.size,
        gpsEnabled = gpsEnabled,
        locationReadyForFieldUse = TrailMateLocationFixReliability.isReliableForFieldUse(
            snapshot = locationSnapshot,
            nowEpochMillis = locationPresentationNowEpochMillis,
            maxAccuracyMeters = FIELD_LOCATION_MAX_ACCURACY_METERS
        ),
        preciseLocationPermissionGranted = locationTracker.hasPrecisePermission(),
        gpsProviderEnabled = locationTracker.hasEnabledProvider(),
        offlineDownloadNetworkValidated = offlineDownloadNetworkValidated,
        offlineBaseMapDownloadedRegionCount = offlineBaseMapStatus?.downloadedRegionCount,
        offlineBaseMapPendingRegionCount = offlineBaseMapStatus?.pendingRegionCount,
        offlineBaseMapPendingRegionLabels = offlineBaseMapStatus?.pendingRegionLabels.orEmpty(),
        offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
        offlineBaseMapTilesVerifiedWithoutNetwork = offlineBaseMapTilesVerifiedWithoutNetwork,
        targetOfflineBaseMapRegionLabel = offlineBaseMapTargetHint.regionLabel,
        targetOfflineBaseMapHint = offlineBaseMapTargetHint.fallbackHint
    )
    val hikeSession = HikeSessionState(
        status = hikeStatus,
        reachedCheckpointIndex = reachedCheckpointIndex.coerceIn(0, plan.checkpoints.lastIndex.coerceAtLeast(0))
    )
    val resetRouteDeviationAlert: () -> Unit = {
        routeDeviationAlertState = RouteDeviationAlertState()
        latestRouteDeviationAlertDecision = null
    }
    val clearProjectedLocationFix: () -> Unit = {
        latestLocationFix = null
        previousRouteDirectionFix = null
    }
    val publishProjectedLocationFix: (HikeLocationFix) -> Unit = { projectedFix ->
        if (latestLocationFix?.timestampEpochMillis != projectedFix.timestampEpochMillis) {
            previousRouteDirectionFix = latestLocationFix
        }
        latestLocationFix = projectedFix
    }
    val updateHikeSession: (HikeSessionState) -> Unit = { session ->
        hikeStatus = session.status
        reachedCheckpointIndex = session.reachedCheckpointIndex
        if (session.status == HikeSessionStatus.READY) {
            resetRouteDeviationAlert()
        }
    }
    val focusPlanCheckpoint: (HikePlanCheckpoint) -> Unit = { checkpoint ->
        val checkpointIndex = plan.checkpoints.indexOf(checkpoint)
        if (checkpointIndex >= 0) {
            updateHikeSession(hikeSession.copy(reachedCheckpointIndex = checkpointIndex))
        }
    }
    val publishTrackRecording: (TrackRecordingState) -> Unit = { updated ->
        trackRecording = updated
        onTrackRecordingChanged(updated)
    }
    val updateOfflineRoutePackReady: (Boolean) -> Unit = { ready ->
        offlineRoutePackReady = ready
        onOfflineRoutePackReadyChanged(ready)
    }
    val toggleOfflineRoutePackReady: () -> Unit = {
        updateOfflineRoutePackReady(!offlineRoutePackReady)
    }
    val prepareLocationForActiveUse: () -> Unit = {
        val decision = TrailMateLocationActiveUsePolicy.prepare(
            currentSnapshot = locationSnapshot,
            nowEpochMillis = System.currentTimeMillis(),
            maxAccuracyMeters = FIELD_LOCATION_MAX_ACCURACY_METERS
        )
        gpsEnabled = true
        locationSnapshot = decision.snapshot
        if (decision.shouldClearProjectedFix) {
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING
            clearProjectedLocationFix()
            wasRecentlyOffRoute = false
        }
        if (decision.shouldClearProjectedFix || decision.shouldRestartTracking) {
            resetRouteDeviationAlert()
        }
        if (decision.shouldRestartTracking) {
            locationTrackingRestartToken += 1
        }
    }
    val applyTrackRecordingAction: () -> Unit = {
        val now = System.currentTimeMillis()
        val decision = TrackRecordingUiActionEngine.resolvePrimaryAction(
            current = trackRecording,
            nowEpochMillis = now
        )
        when (decision.serviceCommand) {
            TrackRecordingServiceCommand.START -> {
                prepareLocationForActiveUse()
                TrackRecordingForegroundService.startRecording(
                    context = context,
                    routeName = route.routeName,
                    routeKey = route.offlineRoutePackKey()
                )
            }
            TrackRecordingServiceCommand.RESUME -> {
                prepareLocationForActiveUse()
                TrackRecordingForegroundService.resumeRecording(context)
            }
            TrackRecordingServiceCommand.PAUSE -> TrackRecordingForegroundService.pauseRecording(context)
            TrackRecordingServiceCommand.FINISH -> TrackRecordingForegroundService.finishRecording(context)
            TrackRecordingServiceCommand.NONE -> Unit
        }
        if (decision.shouldPublishTrackRecording) {
            publishTrackRecording(decision.trackRecording)
        }
    }
    val waitForReliableLocationBeforeTrackAction: () -> Unit = {
        prepareLocationForActiveUse()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        runtimeNotificationPermissionGranted = granted
        val shouldContinueTrackAction = pendingTrackActionAfterNotificationPermission
        pendingTrackActionAfterNotificationPermission = false
        if (granted && shouldContinueTrackAction) {
            when (
                TrackRecordingActionGateEngine.resolve(
                    status = trackRecording.status,
                    hasForegroundLocationPermission = locationTracker.hasPrecisePermission(),
                    notificationPermissionGranted = true,
                    locationSnapshot = locationSnapshot
                )
            ) {
                TrackRecordingActionGateStep.APPLY_TRACK_ACTION -> applyTrackRecordingAction()
                else -> waitForReliableLocationBeforeTrackAction()
            }
        }
    }
    val requestTrackNotificationPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            runtimeNotificationPermissionGranted = true
            val shouldContinueTrackAction = pendingTrackActionAfterNotificationPermission
            pendingTrackActionAfterNotificationPermission = false
            if (shouldContinueTrackAction) {
                when (
                    TrackRecordingActionGateEngine.resolve(
                        status = trackRecording.status,
                        hasForegroundLocationPermission = locationTracker.hasPrecisePermission(),
                        notificationPermissionGranted = true,
                        locationSnapshot = locationSnapshot
                    )
                ) {
                    TrackRecordingActionGateStep.APPLY_TRACK_ACTION -> applyTrackRecordingAction()
                    else -> waitForReliableLocationBeforeTrackAction()
                }
            }
        }
    }
    val showLocationPermissionRequired: () -> Unit = {
        pendingTrackActionAfterLocationPermission = false
        gpsEnabled = false
        locationSnapshot = TrailMateLocationSnapshot.permissionRequired()
        locationGuidanceStatus = LocationBackedHikeStatus.WAITING
        clearProjectedLocationFix()
        wasRecentlyOffRoute = false
        resetRouteDeviationAlert()
    }
    val showLocationSettingsUnavailable: () -> Unit = {
        gpsEnabled = false
        locationSnapshot = TrailMateLocationSnapshot.unavailable()
        locationGuidanceStatus = LocationBackedHikeStatus.WAITING
        clearProjectedLocationFix()
        wasRecentlyOffRoute = false
        resetRouteDeviationAlert()
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        locationPermissionRequested = true
        if (TrailMateLocationPermissionResult.isPreciseLocationGranted(grants)) {
            gpsEnabled = true
            locationSnapshot = TrailMateLocationSnapshot.searching()
            locationGuidanceStatus = LocationBackedHikeStatus.WAITING
            clearProjectedLocationFix()
            wasRecentlyOffRoute = false
            resetRouteDeviationAlert()
            locationTrackingRestartToken += 1
            val shouldContinueTrackAction = pendingTrackActionAfterLocationPermission
            pendingTrackActionAfterLocationPermission = false
            if (shouldContinueTrackAction) {
                if (trackNotificationPermissionGranted) {
                    waitForReliableLocationBeforeTrackAction()
                } else {
                    pendingTrackActionAfterNotificationPermission = true
                    requestTrackNotificationPermission()
                }
            }
        } else {
            showLocationPermissionRequired()
        }
    }
    val openAppLocationSettings: () -> Unit = {
        locationPermissionRequested = true
        val appSettingsOpened = runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", context.packageName, null))
            )
            true
        }.getOrDefault(false)
        val generalSettingsOpened = if (appSettingsOpened) {
            false
        } else {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                true
            }.getOrDefault(false)
        }
        if (appSettingsOpened || generalSettingsOpened) {
            pendingLocationAppSettingsReturn = true
            showLocationPermissionRequired()
        } else {
            showLocationSettingsUnavailable()
        }
    }
    val requestForegroundLocationPermission: () -> Unit = {
        when (
            TrailMateLocationActivationEngine.resolveRequestAction(
                hasForegroundPermission = locationTracker.hasForegroundPermission(),
                hasPreciseLocationPermission = locationTracker.hasPrecisePermission(),
                hasEnabledProvider = locationTracker.hasEnabledProvider(),
                hasRequestedLocationPermissionBefore = locationPermissionRequested,
                shouldShowPreciseLocationRationale = context.findActivity()
                    ?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) == true
            )
        ) {
            TrailMateLocationActivationAction.OPEN_APP_LOCATION_SETTINGS -> openAppLocationSettings()
            else -> {
                locationPermissionRequested = true
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    val startLocationCalibration: () -> Unit = {
        gpsEnabled = true
        locationSnapshot = TrailMateLocationSnapshot.searching()
        locationGuidanceStatus = LocationBackedHikeStatus.WAITING
        clearProjectedLocationFix()
        wasRecentlyOffRoute = false
        resetRouteDeviationAlert()
        locationTrackingRestartToken += 1
    }
    val showProviderDisabled: () -> Unit = {
        gpsEnabled = false
        locationSnapshot = TrailMateLocationSnapshot.providerDisabled()
        locationGuidanceStatus = LocationBackedHikeStatus.WAITING
        clearProjectedLocationFix()
        wasRecentlyOffRoute = false
        resetRouteDeviationAlert()
    }
    val openSystemLocationSettings: () -> Unit = {
        val locationSettingsOpened = runCatching {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            true
        }.getOrDefault(false)
        val generalSettingsOpened = if (locationSettingsOpened) {
            false
        } else {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                true
            }.getOrDefault(false)
        }
        when (
            TrailMateLocationSettingsLaunchPolicy.resolve(
                locationSettingsOpened = locationSettingsOpened,
                generalSettingsOpened = generalSettingsOpened
            )
        ) {
            TrailMateLocationSettingsLaunchOutcome.WAIT_FOR_RETURN -> {
                pendingLocationSettingsReturn = true
                showProviderDisabled()
            }
            TrailMateLocationSettingsLaunchOutcome.SHOW_UNAVAILABLE -> {
                pendingLocationSettingsReturn = false
                showLocationSettingsUnavailable()
            }
        }
    }
    val requestLocation: () -> Unit = {
        when (
            TrailMateLocationActivationEngine.resolveRequestAction(
                hasForegroundPermission = locationTracker.hasForegroundPermission(),
                hasPreciseLocationPermission = locationTracker.hasPrecisePermission(),
                hasEnabledProvider = locationTracker.hasEnabledProvider(),
                hasRequestedLocationPermissionBefore = locationPermissionRequested,
                shouldShowPreciseLocationRationale = context.findActivity()
                    ?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) == true
            )
        ) {
            TrailMateLocationActivationAction.REQUEST_PERMISSION -> requestForegroundLocationPermission()
            TrailMateLocationActivationAction.OPEN_APP_LOCATION_SETTINGS -> openAppLocationSettings()
            TrailMateLocationActivationAction.OPEN_SYSTEM_LOCATION_SETTINGS -> openSystemLocationSettings()
            TrailMateLocationActivationAction.START_TRACKING -> startLocationCalibration()
            TrailMateLocationActivationAction.SHOW_PROVIDER_DISABLED -> showProviderDisabled()
            TrailMateLocationActivationAction.NONE -> Unit
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentStartLocationCalibration by rememberUpdatedState(startLocationCalibration)
    val currentShowProviderDisabled by rememberUpdatedState(showProviderDisabled)
    val currentShowLocationPermissionRequired by rememberUpdatedState(showLocationPermissionRequired)
    val currentRequestForegroundLocationPermission by rememberUpdatedState(requestForegroundLocationPermission)
    val currentRefreshOfflineBaseMapStatusAfterManagerReturn by rememberUpdatedState(
        refreshOfflineBaseMapStatusAfterManagerReturn
    )
    DisposableEffect(context, routeSessionKey) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            onDispose {}
        } else {
            fun refreshFromNetworkCallback() {
                if (
                    AmapDownloadNetworkStatusRefreshPolicy.resolve(routeVisible = true) ==
                    AmapDownloadNetworkStatusRefreshAction.REFRESH_NETWORK_STATUS
                ) {
                    downloadNetworkRefreshHandler.post {
                        currentRefreshDownloadNetworkStatus()
                    }
                }
            }
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    refreshFromNetworkCallback()
                }

                override fun onLost(network: Network) {
                    refreshFromNetworkCallback()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    refreshFromNetworkCallback()
                }
            }
            runCatching {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            }
            onDispose {
                runCatching {
                    connectivityManager.unregisterNetworkCallback(networkCallback)
                }
            }
        }
    }
    DisposableEffect(
        lifecycleOwner,
        routeSessionKey,
        pendingLocationSettingsReturn,
        pendingLocationAppSettingsReturn,
        pendingOfflineMapManagerReturn,
        pendingNetworkSettingsReturn,
        amapSdkAvailable,
        amapPrivacyConsent.accepted
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pendingLocationSettingsReturn) {
                pendingLocationSettingsReturn = false
                when (
                    TrailMateLocationSettingsReturnEffectEngine.resolve(
                        pendingSettingsReturn = true,
                        hasForegroundPermission = locationTracker.hasForegroundPermission(),
                        hasPreciseLocationPermission = locationTracker.hasPrecisePermission(),
                        hasEnabledProvider = locationTracker.hasEnabledProvider()
                    )
                ) {
                    TrailMateLocationSettingsReturnEffect.REQUEST_PERMISSION ->
                        currentRequestForegroundLocationPermission()
                    TrailMateLocationSettingsReturnEffect.START_TRACKING -> currentStartLocationCalibration()
                    TrailMateLocationSettingsReturnEffect.SHOW_PROVIDER_DISABLED -> currentShowProviderDisabled()
                    TrailMateLocationSettingsReturnEffect.NONE -> Unit
                }
            }
            if (event == Lifecycle.Event.ON_RESUME && pendingLocationAppSettingsReturn) {
                pendingLocationAppSettingsReturn = false
                when (
                    TrailMateLocationAppSettingsReturnEffectEngine.resolve(
                        pendingAppSettingsReturn = true,
                        hasForegroundPermission = locationTracker.hasForegroundPermission(),
                        hasPreciseLocationPermission = locationTracker.hasPrecisePermission(),
                        hasEnabledProvider = locationTracker.hasEnabledProvider()
                    )
                ) {
                    TrailMateLocationAppSettingsReturnEffect.START_TRACKING -> currentStartLocationCalibration()
                    TrailMateLocationAppSettingsReturnEffect.SHOW_PROVIDER_DISABLED -> currentShowProviderDisabled()
                    TrailMateLocationAppSettingsReturnEffect.SHOW_PERMISSION_REQUIRED ->
                        currentShowLocationPermissionRequired()
                    TrailMateLocationAppSettingsReturnEffect.NONE -> Unit
                }
            }
            if (event == Lifecycle.Event.ON_RESUME && pendingOfflineMapManagerReturn) {
                val action = AmapOfflineMapReturnRefreshPolicy.resolve(
                    pendingOfflineMapManagerReturn = true,
                    amapSdkAvailable = amapSdkAvailable,
                    amapPrivacyConsentAccepted = amapPrivacyConsent.accepted
                )
                pendingOfflineMapManagerReturn = false
                if (action == AmapOfflineMapReturnAction.REFRESH_DOWNLOADED_STATUS) {
                    currentRefreshOfflineBaseMapStatusAfterManagerReturn()
                }
            }
            if (event == Lifecycle.Event.ON_RESUME && pendingNetworkSettingsReturn) {
                val action = AmapNetworkSettingsReturnRefreshPolicy.resolve(
                    pendingNetworkSettingsReturn = true
                )
                pendingNetworkSettingsReturn = false
                if (action == AmapNetworkSettingsReturnAction.REFRESH_NETWORK_STATUS) {
                    networkSettingsRefreshToken += 1
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val stopLocationUpdates: () -> Unit = {
        gpsEnabled = false
        locationSnapshot = TrailMateLocationSnapshot.disabled()
        locationGuidanceStatus = LocationBackedHikeStatus.WAITING
        clearProjectedLocationFix()
        wasRecentlyOffRoute = false
        resetRouteDeviationAlert()
    }
    val requestTrackActionWithPermissionGate: () -> Unit = {
        when (
            TrackRecordingActionGateEngine.resolve(
                status = trackRecording.status,
                hasForegroundLocationPermission = locationTracker.hasPrecisePermission(),
                notificationPermissionGranted = trackNotificationPermissionGranted,
                locationSnapshot = locationSnapshot
            )
        ) {
            TrackRecordingActionGateStep.REQUEST_FOREGROUND_LOCATION -> {
                pendingTrackActionAfterLocationPermission = true
                requestForegroundLocationPermission()
            }
            TrackRecordingActionGateStep.REQUEST_NOTIFICATION -> {
                pendingTrackActionAfterNotificationPermission = true
                requestTrackNotificationPermission()
            }
            TrackRecordingActionGateStep.WAIT_FOR_RELIABLE_LOCATION -> waitForReliableLocationBeforeTrackAction()
            TrackRecordingActionGateStep.APPLY_TRACK_ACTION -> applyTrackRecordingAction()
        }
    }
    val checkpointDetailFor: (HikePlanCheckpoint) -> HikeCheckpointDetail = { checkpoint ->
        HikeCheckpointDetailAdvisor.build(
            checkpoint = checkpoint,
            trackRecording = trackRecording,
            gearRecommendations = aiGearAdvisorPresentation.recommendations
        )
    }
    val liveCheckpointGuidance = LiveCheckpointGuidanceEngine.build(
        plan = plan,
        session = hikeSession,
        trackRecording = trackRecording,
        gearRecommendations = aiGearAdvisorPresentation.recommendations
    )
    DisposableEffect(context, routeSessionKey) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val updated = TrackRecordingForegroundService.trackRecordingFrom(intent)
                if (
                    updated != null &&
                    TrackRecordingRouteIdentityPolicy.recordingBelongsToRoute(
                        trackRecording = updated,
                        routeName = route.routeName,
                        routeKey = currentRouteKey
                    )
                ) {
                    publishTrackRecording(updated)
                }
            }
        }
        val filter = IntentFilter(TrackRecordingForegroundService.ACTION_RECORDING_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    LaunchedEffect(
        routeSessionKey,
        trackRecording.status,
        trackRecording.routeName,
        trackRecording.routeKey,
        trackServiceRestoreAttempted
    ) {
        if (
            TrackRecordingForegroundRecoveryPolicy.shouldResumeForegroundService(
                current = trackRecording,
                routeName = route.routeName,
                routeKey = currentRouteKey,
                alreadyAttempted = trackServiceRestoreAttempted
            )
        ) {
            trackServiceRestoreAttempted = true
            TrackRecordingForegroundService.resumeRecording(context)
        }
    }
    val handleLocationSnapshot: (TrailMateLocationSnapshot) -> Unit = { snapshot ->
        locationSnapshot = snapshot
        val shouldKeepLocationRequestActive = TrailMateLocationSessionPolicy.shouldKeepLocationRequestActive(snapshot)
        if (!shouldKeepLocationRequestActive) {
            gpsEnabled = false
            resetRouteDeviationAlert()
        }
        val latitude = snapshot.latitude
        val longitude = snapshot.longitude
        val accuracy = snapshot.horizontalAccuracyMeters
        if (shouldKeepLocationRequestActive && latitude != null && longitude != null && accuracy != null) {
            if (hikeSession.status == HikeSessionStatus.ACTIVE && route.routePoints.isNotEmpty()) {
                val projectedFix = RouteGeometryEngine.projectToRoute(
                    route = route,
                    latitude = latitude,
                    longitude = longitude,
                    horizontalAccuracyMeters = accuracy,
                    timestampEpochMillis = snapshot.timestampEpochMillis
                )
                publishProjectedLocationFix(projectedFix)
                val nowEpochMillis = System.currentTimeMillis()
                val update = LocationBackedHikeSessionEngine.applyLocationFix(
                    plan = plan,
                    session = hikeSession,
                    fix = projectedFix,
                    nowEpochMillis = nowEpochMillis
                )
                updateHikeSession(update.session)
                locationGuidanceStatus = update.status
                locationGuidanceCaption = update.caption
                val alertDecision = RouteDeviationAlertPolicy.evaluate(
                    status = update.status,
                    fix = projectedFix,
                    state = routeDeviationAlertState,
                    nowEpochMillis = nowEpochMillis
                )
                if (RouteDeviationAlertDeliveryOwnerPolicy.routeScreenMayDeliver(trackRecording.status)) {
                    RouteDeviationAlertAndroidDelivery.deliver(
                        context = context,
                        decision = alertDecision,
                        notificationPermissionGranted = trackNotificationPermissionGranted
                    )
                }
                routeDeviationAlertState = alertDecision.nextState
                latestRouteDeviationAlertDecision = RouteDeviationAlertPresentationEngine.displayDecision(
                    previous = latestRouteDeviationAlertDecision,
                    next = alertDecision
                )
                when (update.status) {
                    LocationBackedHikeStatus.CHECK_ROUTE -> wasRecentlyOffRoute = true
                    LocationBackedHikeStatus.LOW_ACCURACY,
                    LocationBackedHikeStatus.WAITING,
                    LocationBackedHikeStatus.FINISHED -> wasRecentlyOffRoute = false
                    LocationBackedHikeStatus.ON_ROUTE -> Unit
                }
            }
        } else {
            clearProjectedLocationFix()
        }
    }
    val currentHandleLocationSnapshot by rememberUpdatedState(handleLocationSnapshot)
    LaunchedEffect(gpsEnabled, locationSnapshot.status, routeSessionKey, trackRecording.status) {
        locationPresentationNowEpochMillis = System.currentTimeMillis()
        while (
            RouteLocationPresentationClockPolicy.shouldRefresh(
                gpsEnabled = gpsEnabled,
                locationStatus = locationSnapshot.status,
                trackRecordingStatus = trackRecording.status
            )
        ) {
            delay(5_000L)
            locationPresentationNowEpochMillis = System.currentTimeMillis()
        }
    }
    LaunchedEffect(routeSessionKey, trackRecording.status, trackRecording.startedAtEpochMillis) {
        returnEtaNowEpochMillis = System.currentTimeMillis()
        while (
            trackRecording.status == TrackRecordingStatus.RECORDING ||
            trackRecording.status == TrackRecordingStatus.PAUSED
        ) {
            delay(60_000L)
            returnEtaNowEpochMillis = System.currentTimeMillis()
        }
    }
    LaunchedEffect(routeSessionKey, trackRecording.status, trackRecording.startedAtEpochMillis) {
        if (trackRecording.status != TrackRecordingStatus.RECORDING) {
            previousRouteDirectionFix = null
        }
    }
    LaunchedEffect(routeSessionKey) {
        while (true) {
            offlineEmergencyInfoNowEpochMillis = System.currentTimeMillis()
            delay(60_000L)
        }
    }
    DisposableEffect(gpsEnabled, locationTrackingRestartToken, routeSessionKey) {
        if (gpsEnabled) {
            locationTracker.start { snapshot ->
                currentHandleLocationSnapshot(snapshot)
            }
        } else {
            locationTracker.stop()
        }
        onDispose { locationTracker.stop() }
    }
    val trackActionGateStep = TrackRecordingActionGateEngine.resolve(
        status = trackRecording.status,
        hasForegroundLocationPermission = locationTracker.hasPrecisePermission(),
        notificationPermissionGranted = trackNotificationPermissionGranted,
        locationSnapshot = locationSnapshot
    )
    val trackActionLabel = TrackRecordingActionGateEngine.primaryActionLabel(
        status = trackRecording.status,
        step = trackActionGateStep
    )

    val routeCockpitContent: @Composable () -> Unit = {
        RouteCockpitTabContent(
            route = route,
            assessment = assessment,
            plan = plan,
            hikeSession = hikeSession,
            liveGuidance = liveCheckpointGuidance,
            mapReadiness = mapReadiness,
            pmTilesStyleAssetReadiness = mapLibrePmTilesStyleAssetReadiness,
            offlineRoutePackReady = offlineRoutePackReady,
            offlineBaseMapRegionCount = effectiveOfflineBaseMapDepartureState.downloadedRegionCount,
            offlineBaseMapCoversTargetRoute = effectiveOfflineBaseMapDepartureState.coversTargetRoute,
            offlineBaseMapTilesVerifiedWithoutNetwork = effectiveOfflineBaseMapDepartureState.tilesVerifiedWithoutNetwork,
            gearRecommendations = effectiveDepartureGearRecommendations,
            onSessionChange = updateHikeSession,
            onCheckpointFocused = focusPlanCheckpoint,
            onOfflineRoutePackToggle = toggleOfflineRoutePackReady,
            onShowGearTab = { selected = RouteDetailTab.Gear },
            gpsEnabled = gpsEnabled,
            locationSnapshot = locationSnapshot,
            locationPresentationNowEpochMillis = locationPresentationNowEpochMillis,
            returnEtaNowEpochMillis = returnEtaNowEpochMillis,
            offlineEmergencyInfoNowEpochMillis = offlineEmergencyInfoNowEpochMillis,
            locationGuidanceStatus = locationGuidanceStatus,
            locationGuidanceCaption = locationGuidanceCaption,
            latestLocationFix = latestLocationFix,
            previousRouteDirectionFix = previousRouteDirectionFix,
            latestRouteDeviationAlertDecision = latestRouteDeviationAlertDecision,
            wasRecentlyOffRoute = wasRecentlyOffRoute,
            trackRecording = trackRecording,
            notificationPermissionGranted = trackNotificationPermissionGranted,
            trackActionLabel = trackActionLabel,
            checkpointDetailFor = checkpointDetailFor,
            onRequestLocation = requestLocation,
            onStopLocationUpdates = stopLocationUpdates,
            onShareSafetyText = { text -> context.shareSafetyText(text) },
            onShareTrailMateText = { text, chooserTitle -> context.shareTrailMateText(text, chooserTitle) },
            onTrackAction = requestTrackActionWithPermissionGate,
            onRequestNotificationPermission = requestTrackNotificationPermission,
            onOpenTrackDataRequested = onOpenTrackDataRequested,
            onFinishTrack = {
                val decision = TrackRecordingUiActionEngine.resolveFinishAction(
                    current = trackRecording,
                    nowEpochMillis = System.currentTimeMillis()
                )
                TrackRecordingForegroundService.finishRecording(context)
                if (decision.shouldPublishTrackRecording) {
                    publishTrackRecording(decision.trackRecording)
                }
            },
            onAcknowledgeRouteRejoin = {
                wasRecentlyOffRoute = false
                resetRouteDeviationAlert()
            },
            initiallyExpandDiagnostics = initiallyExpandRouteDiagnostics,
            amapLaunchDiagnostics = amapLaunchDiagnostics,
            onOpenOfflineMap = openOfflineMapManager,
            onImportPmTilesBasemap = importPmTilesBasemap,
            onOpenNetworkSettings = openNetworkSettings,
            onRecordOfflineBaseMapTileProof = recordOfflineBaseMapTileProof,
            offlineBaseMapTileProofCaptureState = offlineBaseMapTileProofCaptureState,
            offlineBaseMapTileProofMessage = offlineBaseMapTileProofMessage,
            offlineBaseMapManagerReturnMessage = offlineBaseMapManagerReturnMessage,
            pmTilesImportMessage = pmTilesImportMessage,
            onAmapBaseMapRenderedChange = { rendered ->
                amapBaseMapRenderedInCurrentSession = rendered
            },
            navigationFullscreen = routeNavigationFullscreen,
            onNavigationFullscreenChange = onRouteNavigationFullscreenChanged
        )
    }

    if (routeNavigationFullscreen) {
        routeCockpitContent()
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            TrailMateLineIcon(
                glyph = TrailMateGlyph.Back,
                contentDescription = "返回路线准备",
                modifier = Modifier
                    .size(32.dp)
                    .let { base ->
                        if (onBackToRouteWorkspace == null) {
                            base
                        } else {
                            base.clickable { onBackToRouteWorkspace.invoke() }
                        }
                    },
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = assessment.routeName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${assessment.distanceKm}km · 累计爬升 ${assessment.ascentMeters}m",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        TrailMateSegmentedControl(
            labels = RouteDetailTab.entries.map { it.label },
            selected = selected.label,
            onSelected = { label ->
                selected = RouteDetailTab.entries.first { it.label == label }
            }
        )
        if (selected == RouteDetailTab.Route || selected == RouteDetailTab.Plan) {
            RouteReadinessStrip(
                plan = plan,
                gearStatusLabel = aiGearAdvisorPresentation.recommendations.routeGearStatusLabel(),
                offlineRoutePackReady = offlineRoutePackReady,
                mapReadiness = mapReadiness,
                onOfflineRoutePackSave = { updateOfflineRoutePackReady(true) },
                onOfflineBaseMapAction = importPmTilesBasemap
            )
        }
        when (selected) {
            RouteDetailTab.Assessment -> RouteAssessmentTab(
                route = route,
                assessment = assessment,
                plan = plan,
                onOpenRoute = { selected = RouteDetailTab.Route },
                onOpenGear = { selected = RouteDetailTab.Gear }
            )
            RouteDetailTab.Route -> routeCockpitContent()
            RouteDetailTab.Plan -> RoutePlanTab(plan = plan)
                RouteDetailTab.Gear -> RouteGearTab(
                    recommendations = aiGearAdvisorPresentation.recommendations,
                    catalogItems = catalogItems,
                    catalogStatusLabel = catalogStatusLabel,
                    aiGearAdvisorPresentation = aiGearAdvisorPresentation,
                    onViewGearMatches = onViewGearMatchesRequested
                )
            }
    }
}

private enum class RouteDetailTab(val label: String) {
    Assessment("评估"),
    Route("路线"),
    Plan("计划"),
    Gear("装备")
}

@Composable
private fun rememberRouteBatteryStatus(context: Context): RouteBatteryStatus {
    var batteryStatus by remember(context) { mutableStateOf(context.readRouteBatteryStatus()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                batteryStatus = intent.toRouteBatteryStatus()
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        batteryStatus = initialIntent.toRouteBatteryStatus()

        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    return batteryStatus
}

private fun Context.readRouteBatteryStatus(): RouteBatteryStatus =
    registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)).toRouteBatteryStatus()

private fun Intent?.toRouteBatteryStatus(): RouteBatteryStatus {
    val level = this?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = this?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val percent = if (level >= 0 && scale > 0) {
        (level * 100) / scale
    } else {
        null
    }
    return RouteBatteryStatus.fromPercent(percent)
}

private fun TrailMapReadiness.offlineBaseMapStep(): TrailMapReadinessStep? =
    setupSteps.firstOrNull { it.label == "离线地图包" }

private fun TrailMapReadiness.isOfflineBaseMapReady(): Boolean =
    provider == TrailMapProvider.MAPLIBRE_PMTILES && isProductionMapReady

@Composable
private fun RouteReadinessStrip(
    plan: HikePlanSummary,
    gearStatusLabel: String,
    offlineRoutePackReady: Boolean,
    mapReadiness: TrailMapReadiness,
    onOfflineRoutePackSave: () -> Unit,
    onOfflineBaseMapAction: () -> Unit
) {
    val offlineBasemapStep = mapReadiness.offlineBaseMapStep()
    val offlineBasemapReady = mapReadiness.isOfflineBaseMapReady()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RouteReadinessTile(
                title = if (offlineRoutePackReady) "离线路线：已保存" else "保存离线路线",
                value = if (offlineRoutePackReady) "GPX 路线包" else "待保存",
                glyph = if (offlineRoutePackReady) TrailMateGlyph.Check else TrailMateGlyph.Map,
                active = offlineRoutePackReady,
                onClick = if (offlineRoutePackReady) null else onOfflineRoutePackSave,
                modifier = Modifier
                    .weight(1f)
                    .testTag("route-readiness-offline-route-pack")
            )
            RouteReadinessTile(
                title = if (offlineBasemapReady) "离线地图包：已导入" else "导入离线地图包",
                value = offlineBasemapStep?.value ?: "待导入",
                glyph = if (offlineBasemapReady) TrailMateGlyph.Check else TrailMateGlyph.Map,
                active = offlineBasemapReady,
                onClick = if (offlineBasemapReady) null else onOfflineBaseMapAction,
                modifier = Modifier
                    .weight(1f)
                    .testTag("route-readiness-offline-basemap")
            )
            RouteReadinessTile(
                title = "装备需求",
                value = gearStatusLabel,
                glyph = TrailMateGlyph.Gear,
                active = gearStatusLabel.startsWith("已"),
                modifier = Modifier.weight(1f)
            )
            RouteReadinessTile(
                title = "计划补给",
                value = "${plan.checkpointCount} 点",
                glyph = TrailMateGlyph.Location,
                active = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RouteReadinessTile(
    title: String,
    value: String,
    glyph: TrailMateGlyph,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val itemModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Surface(
        modifier = itemModifier,
        shape = RoundedCornerShape(14.dp),
        color = if (active) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (active) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = if (active) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun RouteCockpitTabContent(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    liveGuidance: LiveCheckpointGuidance,
    mapReadiness: TrailMapReadiness,
    pmTilesStyleAssetReadiness: MapLibrePmTilesStyleAssetReadiness? = null,
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
    returnEtaNowEpochMillis: Long = System.currentTimeMillis(),
    offlineEmergencyInfoNowEpochMillis: Long = System.currentTimeMillis(),
    locationGuidanceStatus: LocationBackedHikeStatus,
    locationGuidanceCaption: String,
    latestLocationFix: HikeLocationFix?,
    previousRouteDirectionFix: HikeLocationFix? = null,
    latestRouteDeviationAlertDecision: RouteDeviationAlertDecision? = null,
    wasRecentlyOffRoute: Boolean,
    trackRecording: TrackRecordingState,
    notificationPermissionGranted: Boolean,
    trackActionLabel: String,
    checkpointDetailFor: (HikePlanCheckpoint) -> HikeCheckpointDetail,
    onRequestLocation: () -> Unit,
    onStopLocationUpdates: () -> Unit,
    onShareSafetyText: (String) -> Unit,
    onShareTrailMateText: (String, String) -> Unit = { _, _ -> },
    onTrackAction: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenTrackDataRequested: () -> Unit,
    onFinishTrack: () -> Unit,
    onAcknowledgeRouteRejoin: () -> Unit,
    amapLaunchDiagnostics: AmapLaunchDiagnostics? = null,
    onOpenOfflineMap: () -> Unit = {},
    onImportPmTilesBasemap: () -> Unit = {},
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
    pmTilesImportMessage: String? = null,
    onAmapBaseMapRenderedChange: (Boolean) -> Unit = {},
    navigationFullscreen: Boolean = false,
    onNavigationFullscreenChange: (Boolean) -> Unit = {},
    initiallyExpandDiagnostics: Boolean = false
) {
    val context = LocalContext.current
    val routeBatteryStatus = rememberRouteBatteryStatus(context)
    val offlineBaseMapReady = mapReadiness.isOfflineBaseMapReady()
    var diagnosticsExpanded by rememberSaveable(route.offlineRoutePackKey(), initiallyExpandDiagnostics) {
        mutableStateOf(initiallyExpandDiagnostics)
    }
    var diagnosticsReportCopied by rememberSaveable { mutableStateOf(false) }
    val diagnosticsReport = amapLaunchDiagnostics?.let { diagnostics ->
        TrailMateDeviceDiagnosticsReportFormatter.format(
            launchDiagnostics = diagnostics,
            deviceIdentity = TrailMateDeviceIdentity.from(context),
            locationSnapshot = locationSnapshot,
            pmTilesStyleAssetReadiness = pmTilesStyleAssetReadiness
        )
    }
    LaunchedEffect(amapLaunchDiagnostics?.statusLabel, locationSnapshot.status) {
        diagnosticsReportCopied = false
    }
    val diagnosticsReportAction =
        TrailMateDeviceDiagnosticsReportActionPresenter.present(diagnosticsReportCopied)
    val copyDiagnosticsReport: (String) -> Unit = { report ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("TrailMate 真机诊断", report))
        }
        diagnosticsReportCopied = clipboard != null
    }
    val departureReadiness = DepartureReadinessEngine.build(
        mapReadiness = mapReadiness,
        offlineRoutePackReady = offlineRoutePackReady,
        offlineBaseMapRequirement = OfflineBaseMapRequirementPolicy.resolve(assessment),
        offlineBaseMapRegionCount = offlineBaseMapRegionCount,
        offlineBaseMapCoversTargetRoute = offlineBaseMapCoversTargetRoute,
        offlineBaseMapTilesVerifiedWithoutNetwork = offlineBaseMapTilesVerifiedWithoutNetwork,
        targetOfflineBaseMapRegionLabel = amapLaunchDiagnostics?.targetOfflineBaseMapRegionLabel,
        gpsEnabled = gpsEnabled,
        locationSnapshot = locationSnapshot,
        gearRecommendations = gearRecommendations
    )
    val departureReadinessPrimaryAction = DepartureReadinessPrimaryActionEngine.resolve(departureReadiness)
    val mapLayerLegend = TrailMapLayerLegendEngine.build(
        readiness = mapReadiness,
        routePointCount = route.routePoints.size,
        checkpointCount = plan.checkpointCount,
        recordedTrackPointCount = trackRecording.pointCount,
        showUserLocation = gpsEnabled
    )
    val fieldLocationReliability = TrailMateLocationReliabilityEngine.present(
        snapshot = locationSnapshot,
        routePointCount = route.routePoints.size,
        guidanceStatus = locationGuidanceStatus,
        guidanceCaption = locationGuidanceCaption,
        nowEpochMillis = locationPresentationNowEpochMillis
    )
    val fieldStatus = RouteFieldStatusEngine.build(
        mapReadiness = mapReadiness,
        locationReliability = fieldLocationReliability,
        trackRecording = trackRecording,
        notificationPermissionGranted = notificationPermissionGranted,
        batteryStatus = routeBatteryStatus
    )
    val gpsSignalLossWatch = GpsSignalLossWatchEngine.present(
        snapshot = locationSnapshot,
        trackRecording = trackRecording,
        nowEpochMillis = locationPresentationNowEpochMillis
    )
    val lowPowerGuidance = LowPowerGuidanceEngine.present(
        batteryStatus = routeBatteryStatus,
        trackRecording = trackRecording,
        offlineRouteReady = offlineRoutePackReady,
        offlineBaseMapReady = offlineBaseMapReady
    )
    val plannedDurationMinutes = route.durationMinutes ?: plan.estimatedDurationMinutesFromFinish()
    val activeExpectedFinishEpochMillis = trackRecording.expectedFinishEpochMillis(plannedDurationMinutes)
    val progressSafetyWatch = ProgressSafetyWatchEngine.present(
        route = route,
        plan = plan,
        trackRecording = trackRecording,
        fix = latestLocationFix,
        nowEpochMillis = returnEtaNowEpochMillis
    )
    val routeDirectionWatch = RouteDirectionWatchEngine.present(
        previousFix = previousRouteDirectionFix,
        currentFix = latestLocationFix,
        locationStatus = locationGuidanceStatus,
        trackRecording = trackRecording
    )
    val daylightReturnWatch = DaylightReturnWatchEngine.present(
        route = route,
        trackRecording = trackRecording,
        expectedFinishEpochMillis = activeExpectedFinishEpochMillis,
        nowEpochMillis = returnEtaNowEpochMillis,
        zoneId = ZoneId.systemDefault()
    )
    val safetyShareLocation = SafetyShareLocation(
        latitude = locationSnapshot.latitude,
        longitude = locationSnapshot.longitude,
        horizontalAccuracyMeters = locationSnapshot.horizontalAccuracyMeters,
        timestampEpochMillis = locationSnapshot.timestampEpochMillis
    )
    val safetyShareRoutePlan = SafetyShareRoutePlan(
        distanceKm = route.distanceKm,
        ascentMeters = route.ascentMeters,
        estimatedDurationMinutes = plannedDurationMinutes,
        expectedFinishEpochMillis = activeExpectedFinishEpochMillis
    )
    val safetyShare = SafetyShareEngine.present(
        routeName = route.routeName,
        location = safetyShareLocation,
        trackRecording = trackRecording,
        routePlan = safetyShareRoutePlan,
        nowEpochMillis = locationPresentationNowEpochMillis
    )
    val safetyShareShortcut = RouteSafetyShareShortcutPresentationEngine.present(
        presentation = safetyShare
    )
    val cockpitPresentation = RouteCockpitPresentationEngine.build(
        route = route,
        plan = plan,
        session = hikeSession,
        liveGuidance = liveGuidance,
        mapReadiness = mapReadiness,
        departureReadiness = departureReadiness,
        locationSnapshot = locationSnapshot,
        locationGuidanceStatus = locationGuidanceStatus,
        trackRecording = trackRecording,
        wasRecentlyOffRoute = wasRecentlyOffRoute
    )
    val handleOfflineBaseMapAction: () -> Unit = {
        if (
            shouldOpenPmTilesImport(mapReadiness, cockpitPresentation.primaryAction.label) ||
            shouldOpenPmTilesImport(mapReadiness, departureReadiness.primaryActionLabel)
        ) {
            onImportPmTilesBasemap()
        } else {
            onOpenOfflineMap()
        }
    }
    val gatedTrackAction = TrackRecordingDepartureGateEngine.present(
        hikeSessionStatus = hikeSession.status,
        trackRecordingStatus = trackRecording.status,
        currentTrackActionLabel = trackActionLabel,
        departureReadiness = departureReadiness
    )
    val handleGatedTrackAction: () -> Unit = {
        when (gatedTrackAction.kind) {
            TrackRecordingDepartureGateActionKind.APPLY_TRACK_ACTION -> onTrackAction()
            TrackRecordingDepartureGateActionKind.SAVE_OFFLINE_ROUTE_PACK -> onOfflineRoutePackToggle()
            TrackRecordingDepartureGateActionKind.OPEN_OFFLINE_BASE_MAP -> handleOfflineBaseMapAction()
            TrackRecordingDepartureGateActionKind.REQUEST_LOCATION,
            TrackRecordingDepartureGateActionKind.OPEN_LOCATION_SETTINGS -> onRequestLocation()
            TrackRecordingDepartureGateActionKind.SHOW_GEAR -> onShowGearTab()
            TrackRecordingDepartureGateActionKind.BLOCKED -> Unit
        }
    }
    val handlePrimaryAction: () -> Unit = {
        when (cockpitPresentation.primaryAction.kind) {
            RouteCockpitPrimaryActionKind.REQUEST_LOCATION -> onRequestLocation()
            RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS -> onRequestLocation()
            RouteCockpitPrimaryActionKind.START_HIKE -> {
                onSessionChange(HikeSessionEngine.start(hikeSession))
                onTrackAction()
            }
            RouteCockpitPrimaryActionKind.START_RECORDING,
            RouteCockpitPrimaryActionKind.PAUSE_RECORDING,
            RouteCockpitPrimaryActionKind.RESUME_RECORDING -> onTrackAction()
            RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK -> onOfflineRoutePackToggle()
            RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP -> handleOfflineBaseMapAction()
            RouteCockpitPrimaryActionKind.SHOW_GEAR -> onShowGearTab()
            RouteCockpitPrimaryActionKind.VIEW_RECOVERY -> {
                diagnosticsExpanded = true
                if (navigationFullscreen) {
                    onNavigationFullscreenChange(false)
                }
            }
            RouteCockpitPrimaryActionKind.REVIEW_TRACK -> onOpenTrackDataRequested()
            RouteCockpitPrimaryActionKind.RESET_SESSION ->
                onSessionChange(HikeSessionEngine.ready(plan))
            RouteCockpitPrimaryActionKind.BLOCKED -> Unit
        }
    }
    val handleSafetyShare: () -> Unit = {
        SafetyShareActionEngine.resolveShareAction(
            routeName = route.routeName,
            location = safetyShareLocation,
            trackRecording = trackRecording,
            routePlan = safetyShareRoutePlan
        ).shareText?.let(onShareSafetyText) ?: onRequestLocation()
    }
    val handleSafetyShareShortcut: () -> Unit = {
        when (safetyShareShortcut.kind) {
            RouteSafetyShareShortcutActionKind.REQUEST_LOCATION -> onRequestLocation()
            RouteSafetyShareShortcutActionKind.SHARE_LOCATION -> handleSafetyShare()
        }
    }
    val handleFieldSafetyWatchAction: (FieldSafetyWatchPanelActionKind) -> Unit = { kind ->
        when (kind) {
            FieldSafetyWatchPanelActionKind.REQUEST_LOCATION -> onRequestLocation()
            FieldSafetyWatchPanelActionKind.SHARE_LOCATION -> handleSafetyShare()
            FieldSafetyWatchPanelActionKind.NONE -> Unit
        }
    }
    val handleMarkNextCheckpoint: () -> Unit = {
        onSessionChange(HikeSessionEngine.advance(plan, hikeSession))
    }
    if (navigationFullscreen) {
        RouteNavigationFullscreen(
            route = route,
            assessment = assessment,
            plan = plan,
            hikeSession = hikeSession,
            presentation = cockpitPresentation,
            liveGuidance = liveGuidance,
            mapReadiness = mapReadiness,
            fieldStatus = fieldStatus,
            gpsSignalLossWatch = gpsSignalLossWatch,
            directionWatch = routeDirectionWatch,
            safetyShareShortcut = safetyShareShortcut,
            trackRecording = trackRecording,
            showUserLocationOnAmap = gpsEnabled,
            locationSnapshot = locationSnapshot,
            checkpointDetailFor = checkpointDetailFor,
            onLocateRequested = onRequestLocation,
            onCheckpointFocused = onCheckpointFocused,
            onExitFullscreen = { onNavigationFullscreenChange(false) },
            onPrimaryAction = handlePrimaryAction,
            onSafetyShare = handleSafetyShareShortcut,
            onMarkNextCheckpoint = handleMarkNextCheckpoint,
            onFinishTrack = onFinishTrack,
            onAmapBaseMapRenderedChange = onAmapBaseMapRenderedChange
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RouteCockpitSection(
            route = route,
            assessment = assessment,
            plan = plan,
            hikeSession = hikeSession,
            presentation = cockpitPresentation,
            mapReadiness = mapReadiness,
            safetyShareShortcut = safetyShareShortcut,
            trackRecording = trackRecording,
            showUserLocationOnAmap = gpsEnabled,
            locationSnapshot = locationSnapshot,
            checkpointDetailFor = checkpointDetailFor,
            onLocateRequested = onRequestLocation,
            onCheckpointFocused = onCheckpointFocused,
            onPrimaryAction = handlePrimaryAction,
            onSaveOfflineRoutePack = onOfflineRoutePackToggle,
            onOpenOfflineBaseMap = handleOfflineBaseMapAction,
            onSafetyShare = handleSafetyShareShortcut,
            onEnterFullscreen = { onNavigationFullscreenChange(true) },
            onAmapBaseMapRenderedChange = onAmapBaseMapRenderedChange
        )
        GpsSignalLossWatchPanel(
            presentation = gpsSignalLossWatch,
            onPrimaryAction = onRequestLocation
        )
        LowPowerGuidancePanel(
            presentation = lowPowerGuidance,
            onPrimaryAction = onRequestLocation
        )
        ProgressSafetyWatchPanel(
            presentation = progressSafetyWatch,
            safetyShareShortcut = safetyShareShortcut,
            onPrimaryAction = handleFieldSafetyWatchAction
        )
        RouteDirectionWatchPanel(
            presentation = routeDirectionWatch,
            onPrimaryAction = onRequestLocation
        )
        DaylightReturnWatchPanel(
            presentation = daylightReturnWatch,
            safetyShareShortcut = safetyShareShortcut,
            onPrimaryAction = handleFieldSafetyWatchAction
        )
        RouteCockpitDiagnosticsDisclosure(
            plan = plan,
            expanded = diagnosticsExpanded,
            onToggle = { diagnosticsExpanded = !diagnosticsExpanded }
        )
        if (diagnosticsExpanded) {
            RouteCockpitDiagnosticsHeader(title = "现场详情")
            RouteFieldStatusPanel(summary = fieldStatus)
            RouteCockpitDiagnosticsHeader(title = "位置可靠性")
            GpsTrackPanel(
                route = route,
                plan = plan,
                hikeSession = hikeSession,
                locationSnapshot = locationSnapshot,
                locationPresentationNowEpochMillis = locationPresentationNowEpochMillis,
                returnEtaNowEpochMillis = returnEtaNowEpochMillis,
                offlineEmergencyInfoNowEpochMillis = offlineEmergencyInfoNowEpochMillis,
                locationGuidanceStatus = locationGuidanceStatus,
                locationGuidanceCaption = locationGuidanceCaption,
                latestLocationFix = latestLocationFix,
                latestRouteDeviationAlertDecision = latestRouteDeviationAlertDecision,
                wasRecentlyOffRoute = wasRecentlyOffRoute,
                trackRecording = trackRecording,
                notificationPermissionGranted = notificationPermissionGranted,
                trackActionLabel = gatedTrackAction.label,
                trackActionEnabled = gatedTrackAction.enabled,
                onRequestLocation = onRequestLocation,
                onStopLocationUpdates = onStopLocationUpdates,
                onShareSafetyText = onShareSafetyText,
                onShareTrailMateText = onShareTrailMateText,
                onTrackAction = handleGatedTrackAction,
                onRequestNotificationPermission = onRequestNotificationPermission,
                onOpenTrackDataRequested = onOpenTrackDataRequested,
                onFinishTrack = onFinishTrack,
                onAcknowledgeRouteRejoin = onAcknowledgeRouteRejoin
            )
            MapLayerLegendPanel(legend = mapLayerLegend)
            MapSetupHintPanel(
                hint = mapReadiness.setupHint,
                actionLabel = if (mapReadiness.shouldShowPmTilesSetupAction()) "导入离线地图包" else null,
                onAction = onImportPmTilesBasemap
            )
            pmTilesImportMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            amapLaunchDiagnostics?.let { diagnostics ->
                AmapLaunchDiagnosticsPanel(
                    diagnostics = diagnostics,
                    diagnosticsReport = diagnosticsReport,
                    diagnosticsReportAction = diagnosticsReportAction,
                    onCopyDiagnosticsReport = copyDiagnosticsReport,
                    onOpenOfflineMap = onOpenOfflineMap,
                    onOpenNetworkSettings = onOpenNetworkSettings,
                    onRecordOfflineBaseMapTileProof = onRecordOfflineBaseMapTileProof,
                    offlineBaseMapTileProofCaptureState = offlineBaseMapTileProofCaptureState,
                    offlineBaseMapTileProofMessage = offlineBaseMapTileProofMessage,
                    offlineBaseMapManagerReturnMessage = offlineBaseMapManagerReturnMessage
                )
            }
            DepartureReadinessPanel(
                summary = departureReadiness,
                primaryAction = departureReadinessPrimaryAction,
                onPrimaryAction = {
                    when (departureReadinessPrimaryAction.kind) {
                        DepartureReadinessPrimaryActionKind.START_HIKE_AND_RECORD -> {
                            onSessionChange(HikeSessionEngine.start(hikeSession))
                            onTrackAction()
                        }
                        DepartureReadinessPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK -> onOfflineRoutePackToggle()
                        DepartureReadinessPrimaryActionKind.OPEN_OFFLINE_BASE_MAP -> handleOfflineBaseMapAction()
                        DepartureReadinessPrimaryActionKind.REQUEST_LOCATION,
                        DepartureReadinessPrimaryActionKind.OPEN_LOCATION_SETTINGS -> onRequestLocation()
                        DepartureReadinessPrimaryActionKind.SHOW_GEAR -> onShowGearTab()
                        DepartureReadinessPrimaryActionKind.BLOCKED -> Unit
                    }
                }
            )
        }
    }
}

@Composable
private fun RouteNavigationFullscreen(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    presentation: RouteCockpitPresentation,
    liveGuidance: LiveCheckpointGuidance,
    mapReadiness: TrailMapReadiness,
    fieldStatus: RouteFieldStatusSummary,
    gpsSignalLossWatch: GpsSignalLossWatchPresentation,
    directionWatch: RouteDirectionWatchPresentation,
    safetyShareShortcut: RouteSafetyShareShortcutPresentation,
    trackRecording: TrackRecordingState,
    showUserLocationOnAmap: Boolean,
    locationSnapshot: TrailMateLocationSnapshot,
    checkpointDetailFor: (HikePlanCheckpoint) -> HikeCheckpointDetail,
    onLocateRequested: () -> Unit,
    onCheckpointFocused: (HikePlanCheckpoint) -> Unit,
    onExitFullscreen: () -> Unit,
    onPrimaryAction: () -> Unit,
    onSafetyShare: () -> Unit,
    onMarkNextCheckpoint: () -> Unit,
    onFinishTrack: () -> Unit,
    onAmapBaseMapRenderedChange: (Boolean) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("route-navigation-fullscreen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ReferenceRouteSurface(
                route = route,
                assessment = assessment,
                plan = plan,
                hikeSession = hikeSession,
                mapReadiness = mapReadiness,
                trackRecording = trackRecording,
                showUserLocationOnAmap = showUserLocationOnAmap,
                locationSnapshot = locationSnapshot,
                checkpointDetailFor = checkpointDetailFor,
                onLocateRequested = onLocateRequested,
                onCheckpointFocused = onCheckpointFocused,
                mapHeight = 860.dp,
                showMapReadinessFloatingCard = false,
                showAssessmentFloatingCard = false,
                showCurrentCheckpointMiniCard = false,
                showCheckpointLayerCard = false,
                onAmapBaseMapRenderedChange = onAmapBaseMapRenderedChange
            )
            RouteNavigationFullscreenTopBar(
                routeName = route.routeName,
                statusLabel = presentation.routeMatchLabel,
                onExitFullscreen = onExitFullscreen,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 18.dp)
            )
            RouteNavigationFullscreenDock(
                presentation = presentation,
                liveGuidance = liveGuidance,
                fieldStatus = fieldStatus,
                gpsSignalLossWatch = gpsSignalLossWatch,
                directionWatch = directionWatch,
                safetyShareShortcut = safetyShareShortcut,
                trackRecording = trackRecording,
                session = hikeSession,
                onPrimaryAction = onPrimaryAction,
                onGpsSignalLossAction = onLocateRequested,
                onDirectionWatchAction = onLocateRequested,
                onSafetyShare = onSafetyShare,
                onMarkNextCheckpoint = onMarkNextCheckpoint,
                onFinishTrack = onFinishTrack,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun RouteNavigationFullscreenTopBar(
    routeName: String,
    statusLabel: String,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .testTag("route-navigation-fullscreen-exit")
                .semantics { contentDescription = "退出全屏导航" }
                .clickable(onClick = onExitFullscreen),
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Back,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.64f)
            ),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "全屏导航",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = routeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                TrailMateStatusPill(
                    text = statusLabel,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RouteNavigationFullscreenDock(
    presentation: RouteCockpitPresentation,
    liveGuidance: LiveCheckpointGuidance,
    fieldStatus: RouteFieldStatusSummary,
    gpsSignalLossWatch: GpsSignalLossWatchPresentation,
    directionWatch: RouteDirectionWatchPresentation,
    safetyShareShortcut: RouteSafetyShareShortcutPresentation,
    trackRecording: TrackRecordingState,
    session: HikeSessionState,
    onPrimaryAction: () -> Unit,
    onGpsSignalLossAction: () -> Unit,
    onDirectionWatchAction: () -> Unit,
    onSafetyShare: () -> Unit,
    onMarkNextCheckpoint: () -> Unit,
    onFinishTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("route-navigation-fullscreen-dock"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        ),
        shadowElevation = 7.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GpsSignalLossWatchCompactBanner(
                presentation = gpsSignalLossWatch,
                onPrimaryAction = onGpsSignalLossAction
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "当前检查点",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = presentation.currentCheckpointLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = presentation.nextCheckpointLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                TrailMateStatusPill(
                    text = fieldStatus.statusLabel,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { presentation.progressFraction },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = liveGuidance.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presentation.progressLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = trackRecording.summaryLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                fieldStatus.items.take(3).forEach { item ->
                    TrailMateStatusPill(
                        text = "${item.label}：${item.value}",
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            RouteDirectionWatchCompactBanner(
                presentation = directionWatch,
                onPrimaryAction = onDirectionWatchAction
            )
            Button(
                onClick = onPrimaryAction,
                enabled = presentation.primaryAction.enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("route-navigation-fullscreen-primary-action")
            ) {
                TrailMateLineIcon(
                    glyph = presentation.primaryAction.kind.primaryActionGlyph(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = presentation.primaryAction.label,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSafetyShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Text(safetyShareShortcut.label, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onMarkNextCheckpoint,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    enabled = session.status != HikeSessionStatus.READY &&
                        session.status != HikeSessionStatus.COMPLETED
                ) {
                    Text("标记点", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onFinishTrack,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    enabled = trackRecording.status == TrackRecordingStatus.RECORDING ||
                        trackRecording.status == TrackRecordingStatus.PAUSED
                ) {
                    Text("结束记录", maxLines = 1)
                }
            }
            Text(
                text = "仅提供路线辅助，不替代路标与离线地图。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GpsSignalLossWatchCompactBanner(
    presentation: GpsSignalLossWatchPresentation,
    onPrimaryAction: () -> Unit
) {
    if (!presentation.visible) {
        return
    }
    val contentColor = presentation.tone.gpsSignalLossContentColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gps-signal-loss-watch-compact")
            .clip(RoundedCornerShape(14.dp))
            .background(presentation.tone.gpsSignalLossContainerColor())
            .border(1.dp, contentColor.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .clickable(onClick = onPrimaryAction)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrailMateLineIcon(
            glyph = TrailMateGlyph.Warning,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${presentation.statusLabel} · ${presentation.primaryActionLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = presentation.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun RouteDirectionWatchCompactBanner(
    presentation: RouteDirectionWatchPresentation,
    onPrimaryAction: () -> Unit
) {
    if (!presentation.visible) {
        return
    }
    val contentColor = presentation.tone.routeDirectionContentColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(presentation.tone.routeDirectionContainerColor())
            .border(1.dp, contentColor.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .clickable(onClick = onPrimaryAction)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrailMateLineIcon(
            glyph = TrailMateGlyph.Warning,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = contentColor
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${presentation.statusLabel} · ${presentation.primaryActionLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = presentation.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun RouteCockpitStatusSummary(summary: RouteFieldStatusSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "定位与记录",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TrailMateStatusPill(
                text = summary.statusLabel,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RouteCockpitDiagnosticsDisclosure(
    plan: HikePlanSummary,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("route-cockpit-diagnostics-toggle")
            .clickable(onClick = onToggle)
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "检查点与补给",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = plan.routeDetailDisclosureSubtitle(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TrailMateStatusPill(
                text = if (expanded) "收起" else "展开",
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun HikePlanSummary.routeDetailDisclosureSubtitle(): String {
    val fieldLabels = checkpoints
        .mapNotNull { checkpoint -> checkpoint.type.routeDetailLabel() }
        .distinct()

    return if (fieldLabels.isEmpty()) {
        "$checkpointCount 个检查点"
    } else {
        "$checkpointCount 个检查点 · ${fieldLabels.joinToString("/")}"
    }
}

private fun HikePlanCheckpointType.routeDetailLabel(): String? =
    when (this) {
        HikePlanCheckpointType.ENERGY_CHECK -> "补给"
        HikePlanCheckpointType.REST_CHECK -> "休息"
        HikePlanCheckpointType.RISK_CHECK -> "风险"
        HikePlanCheckpointType.START,
        HikePlanCheckpointType.FINISH -> null
    }

@Composable
private fun RouteCockpitDiagnosticsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun RouteCockpitSection(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    presentation: RouteCockpitPresentation,
    mapReadiness: TrailMapReadiness,
    safetyShareShortcut: RouteSafetyShareShortcutPresentation,
    trackRecording: TrackRecordingState,
    showUserLocationOnAmap: Boolean,
    locationSnapshot: TrailMateLocationSnapshot,
    checkpointDetailFor: (HikePlanCheckpoint) -> HikeCheckpointDetail,
    onLocateRequested: () -> Unit,
    onCheckpointFocused: (HikePlanCheckpoint) -> Unit,
    onPrimaryAction: () -> Unit,
    onSaveOfflineRoutePack: () -> Unit,
    onOpenOfflineBaseMap: () -> Unit,
    onSafetyShare: () -> Unit,
    onEnterFullscreen: () -> Unit,
    onAmapBaseMapRenderedChange: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("route-cockpit"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ReferenceRouteSurface(
                route = route,
                assessment = assessment,
                plan = plan,
                hikeSession = hikeSession,
                mapReadiness = mapReadiness,
                trackRecording = trackRecording,
                showUserLocationOnAmap = showUserLocationOnAmap,
                locationSnapshot = locationSnapshot,
                checkpointDetailFor = checkpointDetailFor,
                onLocateRequested = onLocateRequested,
                onCheckpointFocused = onCheckpointFocused,
                mapHeight = 320.dp,
                showMapReadinessFloatingCard = false,
                showAssessmentFloatingCard = false,
                showCurrentCheckpointMiniCard = false,
                showCheckpointLayerCard = false,
                onAmapBaseMapRenderedChange = onAmapBaseMapRenderedChange
            )
        }
        RouteCockpitActionDrawer(
            presentation = presentation,
            onPrimaryAction = onPrimaryAction,
            onSaveOfflineRoutePack = onSaveOfflineRoutePack,
            onOpenOfflineBaseMap = onOpenOfflineBaseMap,
            safetyShareShortcut = safetyShareShortcut,
            onSafetyShare = onSafetyShare,
            onEnterFullscreen = onEnterFullscreen
        )
    }
}

@Composable
private fun RouteMapStatusOverlay(
    mapReadiness: TrailMapReadiness,
    presentation: RouteCockpitPresentation,
    locationSnapshot: TrailMateLocationSnapshot,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.66f)
        ),
        shadowElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "地图准备",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                TrailMateStatusPill(
                    text = presentation.routeMatchLabel,
                    containerColor = presentation.routeMatchContainerColor(),
                    contentColor = presentation.routeMatchContentColor()
                )
            }
            Text(
                text = mapReadiness.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TrailMateStatusPill(
                    text = locationSnapshot.status.displayLabel(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                mapReadiness.layerChips.take(1).forEach { layer ->
                    TrailMateStatusPill(
                        text = layer,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteCockpitActionDrawer(
    presentation: RouteCockpitPresentation,
    onPrimaryAction: () -> Unit,
    onSaveOfflineRoutePack: () -> Unit,
    onOpenOfflineBaseMap: () -> Unit,
    safetyShareShortcut: RouteSafetyShareShortcutPresentation,
    onSafetyShare: () -> Unit,
    onEnterFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryActionOpensFullscreen = presentation.primaryAction.kind.opensFullscreenFromCockpit()
    val showFullscreenShortcut = presentation.primaryAction.kind.showsFullscreenShortcutInActionDrawer()
    val cockpitPrimaryLabel = when {
        primaryActionOpensFullscreen -> "进入导航"
        presentation.primaryAction.kind == RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK -> "保存路线包"
        else -> presentation.primaryAction.label
    }
    val cockpitPrimaryGlyph = if (primaryActionOpensFullscreen) {
        TrailMateGlyph.Compass
    } else {
        presentation.primaryAction.kind.primaryActionGlyph()
    }
    val cockpitPrimaryEnabled = if (primaryActionOpensFullscreen) {
        true
    } else {
        presentation.primaryAction.enabled
    }
    val cockpitPrimaryAction = if (primaryActionOpensFullscreen) {
        onEnterFullscreen
    } else {
        onPrimaryAction
    }
    val offlineRouteItem = presentation.readinessItems.firstOrNull { it.label == "离线路线" }
    val offlineBaseMapItem = presentation.readinessItems.firstOrNull { it.label == "离线地图包" }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("route-cockpit-action-drawer"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        ),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2D75E8)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Location,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "当前检查点",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        TrailMateStatusPill(
                            text = presentation.routeMatchLabel,
                            containerColor = presentation.routeMatchContainerColor(),
                            contentColor = presentation.routeMatchContentColor()
                        )
                    }
                    Text(
                        text = presentation.currentCheckpointLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = presentation.nextCheckpointLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = cockpitPrimaryAction,
                    enabled = cockpitPrimaryEnabled,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp)
                        .testTag("route-cockpit-primary-action"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    TrailMateLineIcon(
                        glyph = cockpitPrimaryGlyph,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = cockpitPrimaryLabel,
                        modifier = Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            LinearProgressIndicator(
                progress = { presentation.progressFraction },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            if (offlineRouteItem != null && offlineBaseMapItem != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RouteCockpitPrepActionButton(
                        label = if (offlineRouteItem.tone == RouteCockpitReadinessTone.READY) {
                            "离线路线已保存"
                        } else {
                            "离线路线"
                        },
                        value = offlineRouteItem.value,
                        glyph = TrailMateGlyph.Folder,
                        enabled = offlineRouteItem.tone != RouteCockpitReadinessTone.READY,
                        onClick = onSaveOfflineRoutePack,
                        modifier = Modifier.weight(1f)
                    )
                    RouteCockpitPrepActionButton(
                        label = if (offlineBaseMapItem.tone == RouteCockpitReadinessTone.READY) {
                            "离线地图已导入"
                        } else if (presentation.primaryAction.kind == RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP) {
                            "离线地图包"
                        } else {
                            "导入离线地图包"
                        },
                        value = offlineBaseMapItem.value,
                        glyph = TrailMateGlyph.Map,
                        enabled = offlineBaseMapItem.tone != RouteCockpitReadinessTone.READY,
                        onClick = onOpenOfflineBaseMap,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = presentation.progressLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "仅提供路线辅助，不替代路标与离线地图",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (showFullscreenShortcut) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEnterFullscreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrailMateLineIcon(
                                glyph = TrailMateGlyph.Compass,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("全屏导航", maxLines = 1)
                        }
                    }
                    OutlinedButton(
                        onClick = onSafetyShare,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TrailMateLineIcon(
                                glyph = TrailMateGlyph.Location,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(safetyShareShortcut.label, maxLines = 1)
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onSafetyShare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrailMateLineIcon(
                            glyph = TrailMateGlyph.Location,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(safetyShareShortcut.label, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteCockpitPrepActionButton(
    label: String,
    value: String,
    glyph: TrailMateGlyph,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
    ) {
        TrailMateLineIcon(
            glyph = glyph,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Column(
            modifier = Modifier.padding(start = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RouteCockpitFieldReadinessStrip(
    items: List<RouteCockpitReadinessItem>,
    onItemAction: (RouteCockpitReadinessItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("route-cockpit-readiness-strip"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { item ->
                    RouteCockpitReadinessTile(
                        item = item,
                        modifier = Modifier.weight(1f),
                        onAction = onItemAction
                    )
                }
                repeat(3 - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RouteCockpitReadinessTile(
    item: RouteCockpitReadinessItem,
    modifier: Modifier = Modifier,
    onAction: (RouteCockpitReadinessItem) -> Unit
) {
    val isActionable = item.actionKind != RouteCockpitReadinessActionKind.NONE
    val tileModifier = modifier
        .testTag("route-cockpit-readiness-${item.label}")
        .semantics { contentDescription = "${item.label}：${item.value}" }
        .let { base ->
            if (isActionable) {
                base.clickable { onAction(item) }
            } else {
                base
            }
        }
    Surface(
        modifier = tileModifier,
        shape = RoundedCornerShape(14.dp),
        color = item.tone.readinessContainerColor(),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            item.tone.readinessContentColor().copy(alpha = 0.24f)
        ),
        shadowElevation = if (isActionable) 1.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(item.tone.readinessContentColor().copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = when (item.tone) {
                        RouteCockpitReadinessTone.READY -> TrailMateGlyph.Check
                        RouteCockpitReadinessTone.ATTENTION -> TrailMateGlyph.More
                        RouteCockpitReadinessTone.BLOCKED -> TrailMateGlyph.Warning
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = item.tone.readinessContentColor()
                )
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RouteCockpitReadinessTone.readinessContainerColor(): Color =
    when (this) {
        RouteCockpitReadinessTone.READY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        RouteCockpitReadinessTone.ATTENTION -> Color(0xFFFFF3D7)
        RouteCockpitReadinessTone.BLOCKED -> Color(0xFFFFE6E2)
    }

@Composable
private fun RouteCockpitReadinessTone.readinessContentColor(): Color =
    when (this) {
        RouteCockpitReadinessTone.READY -> MaterialTheme.colorScheme.primary
        RouteCockpitReadinessTone.ATTENTION -> Color(0xFF8B5A00)
        RouteCockpitReadinessTone.BLOCKED -> Color(0xFFB3261E)
    }

@Composable
private fun RouteCockpitPresentation.routeMatchContainerColor(): Color =
    when {
        routeMatchLabel.contains("核对") -> Color(0xFFFFF3D7)
        routeMatchLabel.contains("完成") || routeMatchLabel.contains("在线路") ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun RouteCockpitPresentation.routeMatchContentColor(): Color =
    when {
        routeMatchLabel.contains("核对") -> Color(0xFF8B5A00)
        routeMatchLabel.contains("完成") || routeMatchLabel.contains("在线路") ->
            MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

private fun RouteCockpitPrimaryActionKind.primaryActionGlyph(): TrailMateGlyph =
    when (this) {
        RouteCockpitPrimaryActionKind.REQUEST_LOCATION,
        RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS -> TrailMateGlyph.Location
        RouteCockpitPrimaryActionKind.START_HIKE,
        RouteCockpitPrimaryActionKind.START_RECORDING,
        RouteCockpitPrimaryActionKind.RESUME_RECORDING -> TrailMateGlyph.Route
        RouteCockpitPrimaryActionKind.PAUSE_RECORDING -> TrailMateGlyph.More
        RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK -> TrailMateGlyph.Folder
        RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP -> TrailMateGlyph.Map
        RouteCockpitPrimaryActionKind.SHOW_GEAR -> TrailMateGlyph.Gear
        RouteCockpitPrimaryActionKind.VIEW_RECOVERY -> TrailMateGlyph.Warning
        RouteCockpitPrimaryActionKind.REVIEW_TRACK -> TrailMateGlyph.Chart
        RouteCockpitPrimaryActionKind.RESET_SESSION -> TrailMateGlyph.Route
        RouteCockpitPrimaryActionKind.BLOCKED -> TrailMateGlyph.Warning
    }

internal fun RouteCockpitPrimaryActionKind.opensFullscreenFromCockpit(): Boolean =
    when (this) {
        RouteCockpitPrimaryActionKind.START_RECORDING,
        RouteCockpitPrimaryActionKind.PAUSE_RECORDING,
        RouteCockpitPrimaryActionKind.RESUME_RECORDING -> true
        RouteCockpitPrimaryActionKind.START_HIKE,
        RouteCockpitPrimaryActionKind.REQUEST_LOCATION,
        RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS,
        RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK,
        RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP,
        RouteCockpitPrimaryActionKind.SHOW_GEAR,
        RouteCockpitPrimaryActionKind.VIEW_RECOVERY,
        RouteCockpitPrimaryActionKind.REVIEW_TRACK,
        RouteCockpitPrimaryActionKind.RESET_SESSION,
        RouteCockpitPrimaryActionKind.BLOCKED -> false
    }

internal fun RouteCockpitPrimaryActionKind.showsFullscreenShortcutInActionDrawer(): Boolean =
    when (this) {
        RouteCockpitPrimaryActionKind.START_RECORDING,
        RouteCockpitPrimaryActionKind.PAUSE_RECORDING,
        RouteCockpitPrimaryActionKind.RESUME_RECORDING -> true
        RouteCockpitPrimaryActionKind.START_HIKE,
        RouteCockpitPrimaryActionKind.REQUEST_LOCATION,
        RouteCockpitPrimaryActionKind.OPEN_LOCATION_SETTINGS,
        RouteCockpitPrimaryActionKind.SAVE_OFFLINE_ROUTE_PACK,
        RouteCockpitPrimaryActionKind.OPEN_OFFLINE_BASE_MAP,
        RouteCockpitPrimaryActionKind.SHOW_GEAR,
        RouteCockpitPrimaryActionKind.VIEW_RECOVERY,
        RouteCockpitPrimaryActionKind.REVIEW_TRACK,
        RouteCockpitPrimaryActionKind.RESET_SESSION,
        RouteCockpitPrimaryActionKind.BLOCKED -> false
    }

@Composable
private fun DepartureReadinessPanel(
    summary: DepartureReadinessSummary,
    primaryAction: DepartureReadinessPrimaryAction,
    onPrimaryAction: () -> Unit
) {
    val buttonPresentation = DepartureReadinessPanelButtonPresentationEngine.present(primaryAction)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "出发检查",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = summary.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = summary.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TrailMateStatusPill(
                    text = summary.statusLabel,
                    containerColor = if (summary.statusLabel == "可以出发") {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        Color(0xFFFFF3D7)
                    },
                    contentColor = if (summary.statusLabel == "可以出发") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color(0xFF8B5A00)
                    }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                summary.steps.chunked(3).forEach { rowSteps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSteps.forEach { step ->
                            DepartureReadinessStepTile(
                                step = step,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowSteps.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth(),
                enabled = buttonPresentation.enabled
            ) {
                Text(buttonPresentation.label)
            }
        }
    }
}

@Composable
private fun DepartureReadinessStepTile(
    step: DepartureReadinessStep,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (step.ready) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (step.ready) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (step.ready) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = if (step.ready) TrailMateGlyph.Check else TrailMateGlyph.More,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = if (step.ready) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Text(
                text = step.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = step.value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MapLayerLegendPanel(legend: TrailMapLayerLegend) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = legend.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TrailMateStatusPill(
                    text = "${legend.items.count { item -> item.status == TrailMapLayerLegendItemStatus.READY }}/${legend.items.size} 可用",
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                legend.items.forEach { item ->
                    MapLayerLegendRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun MapLayerLegendRow(item: TrailMapLayerLegendItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(item.status.layerLegendContainerColor())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        TrailMateStatusPill(
            text = item.value,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
            contentColor = item.status.layerLegendContentColor()
        )
    }
}

@Composable
private fun TrailMapLayerLegendItemStatus.layerLegendContainerColor(): Color =
    when (this) {
        TrailMapLayerLegendItemStatus.READY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        TrailMapLayerLegendItemStatus.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
        TrailMapLayerLegendItemStatus.BLOCKED -> Color(0xFFFFE6E2)
    }

@Composable
private fun TrailMapLayerLegendItemStatus.layerLegendContentColor(): Color =
    when (this) {
        TrailMapLayerLegendItemStatus.READY -> MaterialTheme.colorScheme.primary
        TrailMapLayerLegendItemStatus.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
        TrailMapLayerLegendItemStatus.BLOCKED -> Color(0xFFB3261E)
    }

@Composable
private fun RouteFieldStatusPanel(summary: RouteFieldStatusSummary) {
    val accent = summary.statusAccentColor()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            accent.copy(alpha = 0.24f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = if (summary.statusLabel.contains("待")) {
                            TrailMateGlyph.Compass
                        } else {
                            TrailMateGlyph.Location
                        },
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = accent
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                    text = "定位与记录",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = summary.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = summary.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TrailMateStatusPill(
                    text = summary.statusLabel,
                    containerColor = accent.copy(alpha = 0.12f),
                    contentColor = accent
                )
            }
            RouteFieldStatusItemGrid(items = summary.items)
        }
    }
}

@Composable
private fun RouteFieldStatusItemGrid(items: List<RouteFieldStatusItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    RouteFieldStatusItemChip(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RouteFieldStatusItemChip(
    item: RouteFieldStatusItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
        )
    ) {
        Text(
            text = "${item.label}：${item.value}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun RouteFieldStatusSummary.statusAccentColor(): Color =
    when {
        statusLabel.contains("记录中") || statusLabel == "可开始" || statusLabel == "已保存" ->
            MaterialTheme.colorScheme.primary
        statusLabel.contains("暂停") ->
            Color(0xFF9A6400)
        else ->
            Color(0xFF2D75E8)
    }

@Composable
private fun MapSetupHintPanel(
    hint: TrailMapSetupHint,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Map,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                    text = "地图准备",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = hint.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = hint.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TrailMateStatusPill(
                    text = hint.statusLabel,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
            actionLabel?.let { label ->
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun AmapLaunchDiagnosticsPanel(
    diagnostics: AmapLaunchDiagnostics,
    diagnosticsReport: String?,
    diagnosticsReportAction: TrailMateDeviceDiagnosticsReportAction,
    onCopyDiagnosticsReport: (String) -> Unit,
    onOpenOfflineMap: () -> Unit,
    onOpenNetworkSettings: () -> Unit,
    onRecordOfflineBaseMapTileProof: () -> Unit,
    offlineBaseMapTileProofCaptureState: AmapOfflineBaseMapTileProofCaptureState,
    offlineBaseMapTileProofMessage: String?,
    offlineBaseMapManagerReturnMessage: String?
) {
    val accent = diagnostics.statusAccentColor()
    val tileProofHelperText =
        offlineBaseMapTileProofMessage ?: offlineBaseMapTileProofCaptureState.failureMessage
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            accent.copy(alpha = 0.24f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Map,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = accent
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = diagnostics.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = diagnostics.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "包名 ${diagnostics.packageName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TrailMateStatusPill(
                    text = diagnostics.statusLabel,
                    containerColor = accent.copy(alpha = 0.12f),
                    contentColor = accent
                )
            }
            AmapLaunchDiagnosticsGrid(items = diagnostics.items)
            diagnosticsReport?.let { report ->
                OutlinedButton(
                    onClick = { onCopyDiagnosticsReport(report) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amap-copy-device-diagnostics")
                        .semantics {
                            contentDescription = diagnosticsReportAction.contentDescription
                        }
                ) {
                    Text(diagnosticsReportAction.label)
                }
            }
            diagnostics.networkSettingsActionLabel?.let { actionLabel ->
                OutlinedButton(
                    onClick = onOpenNetworkSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amap-open-network-settings")
                ) {
                    Text(actionLabel)
                }
            }
            diagnostics.offlineMapActionLabel?.let { actionLabel ->
                OutlinedButton(
                    onClick = onOpenOfflineMap,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionLabel)
                }
            }
            offlineBaseMapManagerReturnMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedButton(
                onClick = onRecordOfflineBaseMapTileProof,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amap-record-offline-tile-proof"),
                enabled = offlineBaseMapTileProofCaptureState.canRecordProof
            ) {
                Text(offlineBaseMapTileProofCaptureState.actionLabel)
            }
            tileProofHelperText?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AmapLaunchDiagnosticsGrid(items: List<AmapLaunchDiagnosticItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    AmapLaunchDiagnosticsChip(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AmapLaunchDiagnosticsChip(
    item: AmapLaunchDiagnosticItem,
    modifier: Modifier = Modifier
) {
    val contentColor = item.status.launchDiagnosticContentColor()
    Surface(
        modifier = modifier.testTag("amap-launch-${item.label}"),
        shape = RoundedCornerShape(14.dp),
        color = contentColor.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            contentColor.copy(alpha = 0.22f)
        )
    ) {
        Text(
            text = "${item.label}：${item.value}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun AmapLaunchDiagnostics.statusAccentColor(): Color =
    if (statusLabel == "可真机验证") {
        MaterialTheme.colorScheme.primary
    } else {
        Color(0xFF9A6400)
    }

@Composable
private fun AmapLaunchDiagnosticStatus.launchDiagnosticContentColor(): Color =
    when (this) {
        AmapLaunchDiagnosticStatus.READY -> MaterialTheme.colorScheme.primary
        AmapLaunchDiagnosticStatus.MANUAL_CHECK -> Color(0xFF2D75E8)
        AmapLaunchDiagnosticStatus.NEEDS_ACTION -> Color(0xFF9A6400)
    }

@Composable
private fun TrackNotificationPermissionRow(
    granted: Boolean,
    onRequestPermission: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            TrailMateLineIcon(
                glyph = TrailMateGlyph.Bell,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "轨迹通知",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "锁屏或切后台时可看到记录状态，并可从通知暂停或结束。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (granted) {
            TrailMateStatusPill(
                text = "已开启",
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        } else {
            OutlinedButton(onClick = onRequestPermission) {
                Text("允许通知")
            }
        }
    }
}

@Composable
private fun GpsTrackPanel(
    route: ImportedRoute,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    locationSnapshot: TrailMateLocationSnapshot,
    locationPresentationNowEpochMillis: Long,
    returnEtaNowEpochMillis: Long,
    offlineEmergencyInfoNowEpochMillis: Long,
    locationGuidanceStatus: LocationBackedHikeStatus,
    locationGuidanceCaption: String,
    latestLocationFix: HikeLocationFix?,
    latestRouteDeviationAlertDecision: RouteDeviationAlertDecision?,
    wasRecentlyOffRoute: Boolean,
    trackRecording: TrackRecordingState,
    notificationPermissionGranted: Boolean,
    trackActionLabel: String,
    trackActionEnabled: Boolean,
    onRequestLocation: () -> Unit,
    onStopLocationUpdates: () -> Unit,
    onShareSafetyText: (String) -> Unit,
    onShareTrailMateText: (String, String) -> Unit,
    onTrackAction: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenTrackDataRequested: () -> Unit,
    onFinishTrack: () -> Unit,
    onAcknowledgeRouteRejoin: () -> Unit
) {
    val locationCaption = locationSnapshot.locationCaption(route, locationGuidanceCaption)
    val reliabilityPresentation = TrailMateLocationReliabilityEngine.present(
        snapshot = locationSnapshot,
        routePointCount = route.routePoints.size,
        guidanceStatus = locationGuidanceStatus,
        guidanceCaption = locationGuidanceCaption,
        nowEpochMillis = locationPresentationNowEpochMillis
    )
    val guidancePresentation = LocationGuidancePresentationEngine.present(
        status = locationGuidanceStatus,
        caption = locationCaption
    )
    val safetyShareLocation = SafetyShareLocation(
        latitude = locationSnapshot.latitude,
        longitude = locationSnapshot.longitude,
        horizontalAccuracyMeters = locationSnapshot.horizontalAccuracyMeters,
        timestampEpochMillis = locationSnapshot.timestampEpochMillis
    )
    val plannedDurationMinutes = route.durationMinutes ?: plan.estimatedDurationMinutesFromFinish()
    val activeExpectedFinishEpochMillis = trackRecording.expectedFinishEpochMillis(plannedDurationMinutes)
    val safetyShareRoutePlan = SafetyShareRoutePlan(
        distanceKm = route.distanceKm,
        ascentMeters = route.ascentMeters,
        estimatedDurationMinutes = plannedDurationMinutes,
        expectedFinishEpochMillis = activeExpectedFinishEpochMillis
    )
    val safetyShare = SafetyShareEngine.present(
        routeName = route.routeName,
        location = safetyShareLocation,
        trackRecording = trackRecording,
        routePlan = safetyShareRoutePlan,
        nowEpochMillis = locationPresentationNowEpochMillis
    )
    val safetyShareShortcut = RouteSafetyShareShortcutPresentationEngine.present(
        presentation = safetyShare
    )
    val recoverySafetyShare = SafetyShareEngine.present(
        routeName = route.routeName,
        location = safetyShareLocation,
        trackRecording = trackRecording,
        routePlan = safetyShareRoutePlan.copy(estimatedDurationMinutes = null),
        nowEpochMillis = locationPresentationNowEpochMillis
    )
    val deviationRecovery = RouteDeviationRecoveryEngine.present(
        status = locationGuidanceStatus,
        fix = latestLocationFix,
        safetyShareAvailable = recoverySafetyShare.shareText != null,
        wasRecentlyOffRoute = wasRecentlyOffRoute
    )
    val deviationRecoveryButton = RouteDeviationRecoveryPanelButtonPresentationEngine.present(
        presentation = deviationRecovery,
        safetyShareTextAvailable = recoverySafetyShare.shareText != null
    )
    val exitGuidance = RouteExitGuidanceEngine.present(
        route = route,
        plan = plan,
        locationStatus = locationGuidanceStatus,
        fix = latestLocationFix,
        trackRecording = trackRecording
    )
    val breadcrumbGuidance = BacktrackBreadcrumbGuidanceEngine.present(
        trackRecording = trackRecording,
        nowEpochMillis = locationPresentationNowEpochMillis
    )
    val breadcrumbButton = BacktrackBreadcrumbGuidancePanelButtonPresentationEngine.present(
        presentation = breadcrumbGuidance,
        trackRecordingStatus = trackRecording.status,
        currentTrackActionLabel = trackActionLabel,
        trackActionEnabled = trackActionEnabled
    )
    val handleBacktrackBreadcrumbAction: () -> Unit = {
        when (breadcrumbButton.kind) {
            BacktrackBreadcrumbGuidancePanelActionKind.VIEW_TRACK -> onOpenTrackDataRequested()
            BacktrackBreadcrumbGuidancePanelActionKind.REQUEST_LOCATION -> onRequestLocation()
            BacktrackBreadcrumbGuidancePanelActionKind.CONTINUE_RECORDING -> onTrackAction()
            BacktrackBreadcrumbGuidancePanelActionKind.NONE -> Unit
        }
    }
    val handleReturnEtaWatchAction: (ReturnEtaWatchPanelActionKind) -> Unit = { kind ->
        when (kind) {
            ReturnEtaWatchPanelActionKind.REQUEST_LOCATION -> onRequestLocation()
            ReturnEtaWatchPanelActionKind.SHARE_LOCATION -> {
                SafetyShareActionEngine.resolveShareAction(
                    routeName = route.routeName,
                    location = safetyShareLocation,
                    trackRecording = trackRecording,
                    routePlan = safetyShareRoutePlan
                ).shareText?.let(onShareSafetyText) ?: onRequestLocation()
            }
            ReturnEtaWatchPanelActionKind.NONE -> Unit
        }
    }
    val returnEtaWatch = ReturnEtaWatchEngine.present(
        plan = ReturnEtaPlan(
            estimatedDurationMinutes = plannedDurationMinutes
        ),
        trackRecording = trackRecording,
        nowEpochMillis = returnEtaNowEpochMillis
    )
    val departureBriefPlan = DepartureBriefPlan(
        routeName = route.routeName,
        distanceKm = route.distanceKm,
        ascentMeters = route.ascentMeters,
        estimatedDurationMinutes = plannedDurationMinutes
    )
    val departureBriefShare = DepartureBriefShareEngine.present(
        plan = departureBriefPlan,
        trackRecording = trackRecording,
        routeSessionCompleted = hikeSession.status == HikeSessionStatus.COMPLETED,
        nowEpochMillis = returnEtaNowEpochMillis
    )
    val currentCheckpoint = HikeSessionEngine.currentCheckpoint(plan, hikeSession)
    val nextCheckpoint = HikeSessionEngine.nextCheckpoint(plan, hikeSession)
    val offlineEmergencyRoute = OfflineEmergencyRouteSummary(
        routeName = route.routeName,
        distanceKm = route.distanceKm,
        ascentMeters = route.ascentMeters
    )
    val offlineEmergencyLocation = OfflineEmergencyLocation(
        latitude = locationSnapshot.latitude,
        longitude = locationSnapshot.longitude,
        horizontalAccuracyMeters = locationSnapshot.horizontalAccuracyMeters,
        timestampEpochMillis = locationSnapshot.timestampEpochMillis
    )
    val offlineEmergencyProgress = OfflineEmergencyProgress(
        currentCheckpointLabel = currentCheckpoint?.let { checkpoint -> "当前 ${checkpoint.title}" } ?: "当前检查点待确认",
        nextCheckpointLabel = nextCheckpoint?.let { checkpoint -> "下一站 ${checkpoint.title}" },
        recordedDistanceKm = trackRecording.totalDistanceKm,
        recordingActive = trackRecording.status == TrackRecordingStatus.RECORDING ||
            trackRecording.status == TrackRecordingStatus.PAUSED
    )
    val offlineEmergencyInfo = OfflineEmergencyInfoEngine.present(
        route = offlineEmergencyRoute,
        location = offlineEmergencyLocation,
        progress = offlineEmergencyProgress,
        nowEpochMillis = offlineEmergencyInfoNowEpochMillis
    )
    val routeDeviationAlert = RouteDeviationAlertPresentationEngine.present(latestRouteDeviationAlertDecision)
    val trackReview = TrackRecordingReviewEngine.present(trackRecording)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LocationReliabilityPanel(
                presentation = reliabilityPresentation,
                rawStatusLabel = locationSnapshot.status.displayLabel(),
                actionLabel = reliabilityPresentation.actionLabel,
                onAction = if (reliabilityPresentation.actionLabel == null) null else onRequestLocation
            )
            LocationGuidanceStatusCard(presentation = guidancePresentation)
            if (routeDeviationAlert.visible) {
                RouteDeviationAlertBanner(
                    presentation = routeDeviationAlert,
                    onPrimaryAction = {
                        if (routeDeviationAlert.tone == RouteDeviationAlertTone.REJOINED) {
                            onAcknowledgeRouteRejoin()
                        } else {
                            onRequestLocation()
                        }
                    }
                )
            }
            if (deviationRecovery.visible) {
                RouteDeviationRecoveryPanel(
                    presentation = deviationRecovery,
                    button = deviationRecoveryButton,
                    onPrimaryAction = {
                        when (deviationRecoveryButton.kind) {
                            RouteDeviationRecoveryPanelActionKind.ACKNOWLEDGE_REJOIN -> onAcknowledgeRouteRejoin()
                            RouteDeviationRecoveryPanelActionKind.REQUEST_LOCATION -> onRequestLocation()
                            RouteDeviationRecoveryPanelActionKind.SHARE_LOCATION -> {
                                SafetyShareActionEngine.resolveShareAction(
                                    routeName = route.routeName,
                                    location = safetyShareLocation,
                                    trackRecording = trackRecording,
                                    routePlan = safetyShareRoutePlan.copy(estimatedDurationMinutes = null)
                                ).shareText?.let(onShareSafetyText) ?: onRequestLocation()
                            }
                            RouteDeviationRecoveryPanelActionKind.NONE -> Unit
                        }
                    }
                )
            }
            RouteExitGuidancePanel(
                presentation = exitGuidance,
                onPrimaryAction = onRequestLocation
            )
            BacktrackBreadcrumbGuidancePanel(
                presentation = breadcrumbGuidance,
                button = breadcrumbButton,
                onPrimaryAction = handleBacktrackBreadcrumbAction
            )
            ReturnEtaWatchPanel(
                presentation = returnEtaWatch,
                safetyShareShortcut = safetyShareShortcut,
                onPrimaryAction = handleReturnEtaWatchAction
            )
            TrailMatePanel(
                title = "轨迹记录",
                value = trackRecording.summaryLabel(),
                caption = "前台服务记录真实定位轨迹，锁屏或切后台仍可继续；记录仅保存在本机。",
                tone = TrailMatePanelTone.Neutral
            )
            if (trackReview.visible) {
                TrackRecordingReviewPanel(
                    presentation = trackReview,
                    onPrimaryAction = onOpenTrackDataRequested
                )
            }
            TrackNotificationPermissionRow(
                granted = notificationPermissionGranted,
                onRequestPermission = onRequestNotificationPermission
            )
            DepartureBriefSharePanel(
                presentation = departureBriefShare,
                onPrimaryAction = {
                    val action = DepartureBriefShareActionEngine.resolveShareAction(
                        plan = departureBriefPlan,
                        trackRecording = trackRecording,
                        routeSessionCompleted = hikeSession.status == HikeSessionStatus.COMPLETED
                    )
                    action.shareText?.let { text ->
                        onShareTrailMateText(text, action.chooserTitle ?: "发送出发报备")
                    }
                }
            )
            OfflineEmergencyInfoPanel(
                presentation = offlineEmergencyInfo,
                onPrimaryAction = {
                    val action = OfflineEmergencyInfoActionEngine.resolveShareAction(
                        route = offlineEmergencyRoute,
                        location = offlineEmergencyLocation,
                        progress = offlineEmergencyProgress,
                        nowEpochMillis = System.currentTimeMillis()
                    )
                    onShareTrailMateText(action.shareText, action.chooserTitle)
                }
            )
            SafetySharePanel(
                presentation = safetyShare,
                onPrimaryAction = {
                    SafetyShareActionEngine.resolveShareAction(
                        routeName = route.routeName,
                        location = safetyShareLocation,
                        trackRecording = trackRecording,
                        routePlan = safetyShareRoutePlan
                    ).shareText?.let(onShareSafetyText) ?: onRequestLocation()
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTrackAction,
                    modifier = Modifier.weight(1f),
                    enabled = trackActionEnabled
                ) {
                    Text(trackActionLabel)
                }
                OutlinedButton(
                    onClick = onFinishTrack,
                    modifier = Modifier.weight(1f),
                    enabled = trackRecording.status == TrackRecordingStatus.RECORDING ||
                        trackRecording.status == TrackRecordingStatus.PAUSED
                ) {
                    Text("结束记录")
                }
            }
        }
    }
}

@Composable
private fun TrackRecordingReviewPanel(
    presentation: TrackRecordingReviewPresentation,
    onPrimaryAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("track-recording-review"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Check,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = presentation.routeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = presentation.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrackRecordingReviewMetric(
                    label = "距离",
                    value = presentation.distanceLabel,
                    modifier = Modifier.weight(1f)
                )
                TrackRecordingReviewMetric(
                    label = "轨迹点",
                    value = presentation.pointCountLabel,
                    modifier = Modifier.weight(1f)
                )
                TrackRecordingReviewMetric(
                    label = "用时",
                    value = presentation.durationLabel,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(presentation.primaryActionLabel)
            }
        }
    }
}

@Composable
private fun TrackRecordingReviewMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LocationReliabilityPanel(
    presentation: LocationReliabilityPresentation,
    rawStatusLabel: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(presentation.level.reliabilityContainerColor())
            .border(
                1.dp,
                presentation.level.reliabilityContentColor().copy(alpha = 0.22f),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(presentation.level.reliabilityContentColor().copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            TrailMateLineIcon(
                glyph = TrailMateGlyph.Location,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = presentation.level.reliabilityContentColor()
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前位置",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                TrailMateStatusPill(
                    text = presentation.statusLabel,
                    containerColor = presentation.level.reliabilityContentColor().copy(alpha = 0.13f),
                    contentColor = presentation.level.reliabilityContentColor()
                )
            }
            Text(
                text = presentation.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = presentation.caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LocationReliabilityDetailList(details = presentation.details)
            Text(
                text = rawStatusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun LocationReliabilityDetailList(details: List<LocationReliabilityDetail>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        details.take(3).forEach { detail ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun DepartureBriefSharePanel(
    presentation: DepartureBriefSharePresentation,
    onPrimaryAction: () -> Unit
) {
    val button = DepartureBriefSharePanelButtonPresentationEngine.present(presentation)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.52f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Route,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (presentation.details.isNotEmpty()) {
                    DepartureBriefShareDetailList(details = presentation.details)
                }
            }
            if (button.visible) {
                OutlinedButton(onClick = onPrimaryAction) {
                    Text(button.label)
                }
            }
        }
    }
}

@Composable
private fun DepartureBriefShareDetailList(details: List<DepartureBriefShareDetail>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        details.take(3).forEach { detail ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun OfflineEmergencyInfoPanel(
    presentation: OfflineEmergencyInfoPresentation,
    onPrimaryAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.07f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = presentation.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        TrailMateStatusPill(
                            text = presentation.statusLabel,
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = presentation.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (presentation.details.isNotEmpty()) {
                OfflineEmergencyInfoDetailList(details = presentation.details)
            }
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(presentation.primaryActionLabel)
            }
        }
    }
}

@Composable
private fun OfflineEmergencyInfoDetailList(details: List<OfflineEmergencyInfoDetail>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(3).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SafetySharePanel(
    presentation: SafetySharePresentation,
    onPrimaryAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Location,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "安全分享",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = presentation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (presentation.details.isNotEmpty()) {
                    SafetyShareDetailList(details = presentation.details)
                }
            }
            OutlinedButton(onClick = onPrimaryAction) {
                Text(presentation.primaryActionLabel)
            }
        }
    }
}

@Composable
private fun SafetyShareDetailList(details: List<SafetyShareDetail>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        details.take(3).forEach { detail ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun RouteDeviationAlertBanner(
    presentation: RouteDeviationAlertPresentation,
    onPrimaryAction: () -> Unit
) {
    val contentColor = presentation.tone.alertContentColor()
    val glyph = when (presentation.tone) {
        RouteDeviationAlertTone.URGENT,
        RouteDeviationAlertTone.CAUTION -> TrailMateGlyph.Warning
        RouteDeviationAlertTone.REJOINED -> TrailMateGlyph.Check
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(presentation.tone.alertContainerColor())
            .border(1.dp, contentColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = if (presentation.shouldRequestAttention) 0.18f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TrailMateStatusPill(
                        text = presentation.tone.alertStatusLabel(),
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (presentation.primaryActionLabel.isNotBlank()) {
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(presentation.primaryActionLabel)
            }
        }
    }
}

@Composable
private fun ReturnEtaWatchPanel(
    presentation: ReturnEtaWatchPresentation,
    safetyShareShortcut: RouteSafetyShareShortcutPresentation,
    onPrimaryAction: (ReturnEtaWatchPanelActionKind) -> Unit
) {
    val button = ReturnEtaWatchPanelButtonPresentationEngine.present(
        primaryActionLabel = presentation.primaryActionLabel,
        primaryActionRequiresSafetyShare = presentation.primaryActionRequiresSafetyShare,
        safetyShareTextAvailable = safetyShareShortcut.kind == RouteSafetyShareShortcutActionKind.SHARE_LOCATION,
        safetyShareRepairLabel = safetyShareShortcut.label
    )
    val contentColor = presentation.tone.returnEtaContentColor()
    val containerColor = presentation.tone.returnEtaContainerColor()
    val glyph = when (presentation.tone) {
        ReturnEtaWatchTone.NEUTRAL -> TrailMateGlyph.Compass
        ReturnEtaWatchTone.READY -> TrailMateGlyph.Check
        ReturnEtaWatchTone.CAUTION -> TrailMateGlyph.Warning
        ReturnEtaWatchTone.ALERT -> TrailMateGlyph.Warning
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (presentation.details.isNotEmpty()) {
            ReturnEtaWatchDetailList(details = presentation.details)
        }
        ReturnEtaWatchManualGuidance(
            label = button.manualGuidanceLabel,
            contentColor = contentColor
        )
        if (button.visible) {
            OutlinedButton(
                onClick = { onPrimaryAction(button.kind) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(button.label)
            }
        }
    }
}

@Composable
private fun ReturnEtaWatchManualGuidance(
    label: String,
    contentColor: Color
) {
    if (label.isBlank()) {
        return
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ReturnEtaWatchDetailList(details: List<ReturnEtaWatchDetail>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(3).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DaylightReturnWatchPanel(
    presentation: DaylightReturnWatchPresentation,
    safetyShareShortcut: RouteSafetyShareShortcutPresentation,
    onPrimaryAction: (FieldSafetyWatchPanelActionKind) -> Unit
) {
    if (!presentation.visible) {
        return
    }
    val tone = presentation.tone ?: return
    val contentColor = tone.daylightContentColor()
    val button = FieldSafetyWatchPanelButtonPresentationEngine.present(
        primaryActionLabel = presentation.primaryActionLabel,
        primaryActionRequiresSafetyShare = presentation.primaryActionRequiresSafetyShare,
        safetyShareTextAvailable = safetyShareShortcut.kind == RouteSafetyShareShortcutActionKind.SHARE_LOCATION,
        safetyShareRepairLabel = safetyShareShortcut.label
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tone.daylightContainerColor())
            .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Weather,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DaylightReturnWatchDetailList(details = presentation.details)
        FieldSafetyWatchManualGuidance(
            label = button.manualGuidanceLabel,
            contentColor = contentColor
        )
        if (button.visible) {
            OutlinedButton(
                onClick = { onPrimaryAction(button.kind) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(button.label)
            }
        }
    }
}

@Composable
private fun DaylightReturnWatchDetailList(details: List<DaylightReturnWatchDetail>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(3).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DaylightReturnWatchTone.daylightContainerColor(): Color =
    when (this) {
        DaylightReturnWatchTone.CAUTION -> Color(0xFFFFF4E0)
        DaylightReturnWatchTone.ALERT -> Color(0xFFFFEDE6)
    }

@Composable
private fun DaylightReturnWatchTone.daylightContentColor(): Color =
    when (this) {
        DaylightReturnWatchTone.CAUTION -> Color(0xFF9A5B00)
        DaylightReturnWatchTone.ALERT -> Color(0xFFB3261E)
    }

@Composable
private fun GpsSignalLossWatchPanel(
    presentation: GpsSignalLossWatchPresentation,
    onPrimaryAction: () -> Unit
) {
    if (!presentation.visible) {
        return
    }
    val contentColor = presentation.tone.gpsSignalLossContentColor()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gps-signal-loss-watch")
            .clip(RoundedCornerShape(16.dp))
            .background(presentation.tone.gpsSignalLossContainerColor())
            .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        GpsSignalLossWatchDetailList(details = presentation.details)
        OutlinedButton(
            onClick = onPrimaryAction,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(presentation.primaryActionLabel)
        }
    }
}

@Composable
private fun ProgressSafetyWatchPanel(
    presentation: ProgressSafetyWatchPresentation,
    safetyShareShortcut: RouteSafetyShareShortcutPresentation,
    onPrimaryAction: (FieldSafetyWatchPanelActionKind) -> Unit
) {
    if (!presentation.visible) {
        return
    }
    val tone = presentation.tone ?: return
    val contentColor = tone.progressSafetyContentColor()
    val button = FieldSafetyWatchPanelButtonPresentationEngine.present(
        primaryActionLabel = presentation.primaryActionLabel,
        primaryActionRequiresSafetyShare = presentation.primaryActionRequiresSafetyShare,
        safetyShareTextAvailable = safetyShareShortcut.kind == RouteSafetyShareShortcutActionKind.SHARE_LOCATION,
        safetyShareRepairLabel = safetyShareShortcut.label
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tone.progressSafetyContainerColor())
            .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ProgressSafetyWatchDetailList(details = presentation.details)
        FieldSafetyWatchManualGuidance(
            label = button.manualGuidanceLabel,
            contentColor = contentColor
        )
        if (button.visible) {
            OutlinedButton(
                onClick = { onPrimaryAction(button.kind) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(button.label)
            }
        }
    }
}

@Composable
private fun FieldSafetyWatchManualGuidance(
    label: String,
    contentColor: Color
) {
    if (label.isBlank()) {
        return
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun RouteDirectionWatchPanel(
    presentation: RouteDirectionWatchPresentation,
    onPrimaryAction: () -> Unit
) {
    if (!presentation.visible) {
        return
    }
    val contentColor = presentation.tone.routeDirectionContentColor()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(presentation.tone.routeDirectionContainerColor())
            .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        RouteDirectionWatchDetailList(details = presentation.details)
        OutlinedButton(
            onClick = onPrimaryAction,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(presentation.primaryActionLabel)
        }
    }
}

@Composable
private fun RouteDirectionWatchDetailList(details: List<RouteDirectionWatchDetail>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(2).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GpsSignalLossWatchDetailList(details: List<GpsSignalLossWatchDetail>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(3).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ProgressSafetyWatchDetailList(details: List<ProgressSafetyWatchDetail>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(3).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GpsSignalLossWatchTone.gpsSignalLossContainerColor(): Color =
    when (this) {
        GpsSignalLossWatchTone.CAUTION -> Color(0xFFFFF4E0)
        GpsSignalLossWatchTone.ALERT -> Color(0xFFFFEDE6)
    }

@Composable
private fun GpsSignalLossWatchTone.gpsSignalLossContentColor(): Color =
    when (this) {
        GpsSignalLossWatchTone.CAUTION -> Color(0xFF9A5B00)
        GpsSignalLossWatchTone.ALERT -> Color(0xFFB3261E)
    }

@Composable
private fun ProgressSafetyWatchTone.progressSafetyContainerColor(): Color =
    when (this) {
        ProgressSafetyWatchTone.CAUTION -> Color(0xFFFFF4E0)
        ProgressSafetyWatchTone.ALERT -> Color(0xFFFFEDE6)
    }

@Composable
private fun ProgressSafetyWatchTone.progressSafetyContentColor(): Color =
    when (this) {
        ProgressSafetyWatchTone.CAUTION -> Color(0xFF9A5B00)
        ProgressSafetyWatchTone.ALERT -> Color(0xFFB3261E)
    }

@Composable
private fun RouteDirectionWatchTone.routeDirectionContainerColor(): Color =
    when (this) {
        RouteDirectionWatchTone.NEUTRAL,
        RouteDirectionWatchTone.READY -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f)
        RouteDirectionWatchTone.ALERT -> Color(0xFFFFEDE6)
    }

@Composable
private fun RouteDirectionWatchTone.routeDirectionContentColor(): Color =
    when (this) {
        RouteDirectionWatchTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        RouteDirectionWatchTone.READY -> MaterialTheme.colorScheme.primary
        RouteDirectionWatchTone.ALERT -> Color(0xFFB3261E)
    }

@Composable
private fun LowPowerGuidancePanel(
    presentation: LowPowerGuidancePresentation,
    onPrimaryAction: () -> Unit
) {
    if (!presentation.visible) {
        return
    }
    val tone = presentation.tone ?: return
    val contentColor = tone.lowPowerContentColor()
    val containerColor = tone.lowPowerContainerColor()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LowPowerGuidanceActionList(
            actions = presentation.actions,
            contentColor = contentColor
        )
        if (presentation.primaryActionRequestsFinalFix) {
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(presentation.primaryActionLabel)
            }
        }
    }
}

@Composable
private fun LowPowerGuidanceActionList(
    actions: List<LowPowerGuidanceAction>,
    contentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.take(4).forEach { action ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f))
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Check,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = contentColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = action.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LowPowerGuidanceTone.lowPowerContainerColor(): Color =
    when (this) {
        LowPowerGuidanceTone.CAUTION -> Color(0xFFFFF4E0)
        LowPowerGuidanceTone.ALERT -> Color(0xFFFFEDE6)
    }

@Composable
private fun LowPowerGuidanceTone.lowPowerContentColor(): Color =
    when (this) {
        LowPowerGuidanceTone.CAUTION -> Color(0xFF9A5B00)
        LowPowerGuidanceTone.ALERT -> Color(0xFFB3261E)
    }

@Composable
private fun ReturnEtaWatchTone.returnEtaContainerColor(): Color =
    when (this) {
        ReturnEtaWatchTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
        ReturnEtaWatchTone.READY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ReturnEtaWatchTone.CAUTION -> Color(0xFFFFF4E0)
        ReturnEtaWatchTone.ALERT -> Color(0xFFFFEDE6)
    }

@Composable
private fun ReturnEtaWatchTone.returnEtaContentColor(): Color =
    when (this) {
        ReturnEtaWatchTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        ReturnEtaWatchTone.READY -> MaterialTheme.colorScheme.primary
        ReturnEtaWatchTone.CAUTION -> Color(0xFF9A5B00)
        ReturnEtaWatchTone.ALERT -> Color(0xFFB3261E)
}

private fun TrackRecordingState.expectedFinishEpochMillis(durationMinutes: Int?): Long? {
    val startedAt = startedAtEpochMillis?.takeIf { it > 0L } ?: return null
    val duration = durationMinutes?.takeIf { it > 0 } ?: return null
    return startedAt + duration * 60_000L
}

private fun HikePlanSummary.estimatedDurationMinutesFromFinish(): Int? {
    val finish = checkpoints.lastOrNull { checkpoint -> checkpoint.type == HikePlanCheckpointType.FINISH }
        ?: checkpoints.lastOrNull()
        ?: return null
    val parts = finish.timeFromStart.split(":")
    if (parts.size != 2) {
        return null
    }
    val hours = parts[0].toIntOrNull() ?: return null
    val minutes = parts[1].toIntOrNull() ?: return null
    if (hours < 0 || minutes !in 0..59) {
        return null
    }
    return (hours * 60 + minutes).takeIf { it > 0 }
}

@Composable
private fun BacktrackBreadcrumbGuidancePanel(
    presentation: BacktrackBreadcrumbGuidancePresentation,
    button: BacktrackBreadcrumbGuidancePanelButtonPresentation,
    onPrimaryAction: () -> Unit
) {
    if (!presentation.visible) {
        return
    }

    val contentColor = presentation.tone.backtrackBreadcrumbContentColor()
    val containerColor = presentation.tone.backtrackBreadcrumbContainerColor()
    val glyph = when (presentation.tone) {
        BacktrackBreadcrumbGuidanceTone.READY -> TrailMateGlyph.Route
        BacktrackBreadcrumbGuidanceTone.CAUTION -> TrailMateGlyph.Route
        BacktrackBreadcrumbGuidanceTone.ALERT -> TrailMateGlyph.Warning
        BacktrackBreadcrumbGuidanceTone.UNAVAILABLE -> TrailMateGlyph.Map
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        BacktrackBreadcrumbGuidanceDetailList(
            details = presentation.details,
            contentColor = contentColor
        )
        if (button.visible) {
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(button.label)
            }
        }
    }
}

@Composable
private fun BacktrackBreadcrumbGuidanceDetailList(
    details: List<BacktrackBreadcrumbGuidanceDetail>,
    contentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(3).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BacktrackBreadcrumbGuidanceTone.backtrackBreadcrumbContainerColor(): Color =
    when (this) {
        BacktrackBreadcrumbGuidanceTone.READY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
        BacktrackBreadcrumbGuidanceTone.CAUTION -> Color(0xFFFFF4E0)
        BacktrackBreadcrumbGuidanceTone.ALERT -> Color(0xFFFFEDE6)
        BacktrackBreadcrumbGuidanceTone.UNAVAILABLE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
    }

@Composable
private fun BacktrackBreadcrumbGuidanceTone.backtrackBreadcrumbContentColor(): Color =
    when (this) {
        BacktrackBreadcrumbGuidanceTone.READY -> MaterialTheme.colorScheme.primary
        BacktrackBreadcrumbGuidanceTone.CAUTION -> Color(0xFF9A5B00)
        BacktrackBreadcrumbGuidanceTone.ALERT -> Color(0xFFB3261E)
        BacktrackBreadcrumbGuidanceTone.UNAVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
private fun RouteExitGuidancePanel(
    presentation: RouteExitGuidancePresentation,
    onPrimaryAction: () -> Unit
) {
    val button = RouteExitGuidancePanelButtonPresentationEngine.present(presentation)
    val contentColor = presentation.tone.exitGuidanceContentColor()
    val containerColor = presentation.tone.exitGuidanceContainerColor()
    val glyph = when (presentation.tone) {
        RouteExitGuidanceTone.READY -> TrailMateGlyph.Route
        RouteExitGuidanceTone.CAUTION -> TrailMateGlyph.Warning
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        RouteExitGuidanceOptionList(
            options = presentation.options,
            contentColor = contentColor
        )
        if (button.visible) {
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(button.label)
            }
        }
    }
}

@Composable
private fun RouteExitGuidanceOptionList(
    options: List<RouteExitGuidanceOption>,
    contentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.take(2).forEach { option ->
            val optionColor = if (option.emphasized) contentColor else MaterialTheme.colorScheme.onSurface
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (option.emphasized) {
                            contentColor.copy(alpha = 0.10f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(optionColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = if (option.emphasized) TrailMateGlyph.Check else TrailMateGlyph.Route,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = optionColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelLarge,
                            color = optionColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = option.distanceLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = optionColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = option.caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteExitGuidanceTone.exitGuidanceContainerColor(): Color =
    when (this) {
        RouteExitGuidanceTone.READY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        RouteExitGuidanceTone.CAUTION -> Color(0xFFFFF4E0)
    }

@Composable
private fun RouteExitGuidanceTone.exitGuidanceContentColor(): Color =
    when (this) {
        RouteExitGuidanceTone.READY -> MaterialTheme.colorScheme.primary
        RouteExitGuidanceTone.CAUTION -> Color(0xFF9A5B00)
    }

@Composable
private fun RouteDeviationRecoveryPanel(
    presentation: RouteDeviationRecoveryPresentation,
    button: RouteDeviationRecoveryPanelButtonPresentation,
    onPrimaryAction: () -> Unit
) {
    val contentColor = presentation.tone.recoveryContentColor()
    val containerColor = presentation.tone.recoveryContainerColor()
    val glyph = when (presentation.tone) {
        RouteDeviationRecoveryTone.OFF_ROUTE -> TrailMateGlyph.Warning
        RouteDeviationRecoveryTone.REJOINED -> TrailMateGlyph.Check
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = glyph,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    TrailMateStatusPill(
                        text = presentation.statusLabel,
                        containerColor = contentColor.copy(alpha = 0.12f),
                        contentColor = contentColor
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (presentation.details.isNotEmpty()) {
            RouteDeviationRecoveryDetailList(details = presentation.details)
        }
        if (presentation.actions.isNotEmpty()) {
            RouteDeviationRecoveryActionList(
                actions = presentation.actions,
                contentColor = contentColor
            )
        } else {
            RouteDeviationRecoveryStepList(
                steps = presentation.steps,
                contentColor = contentColor
            )
        }
        if (button.visible) {
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(button.label)
            }
        }
    }
}

@Composable
private fun RouteDeviationRecoveryDetailList(details: List<RouteDeviationRecoveryDetail>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        details.take(3).forEach { detail ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = detail.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.value,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RouteDeviationRecoveryActionList(
    actions: List<RouteDeviationRecoveryAction>,
    contentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.take(3).forEach { action ->
            val actionColor = when {
                !action.enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                action.emphasized -> contentColor
                else -> MaterialTheme.colorScheme.onSurface
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (action.emphasized) {
                            contentColor.copy(alpha = 0.10f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(actionColor.copy(alpha = if (action.enabled) 0.12f else 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    TrailMateLineIcon(
                        glyph = action.glyph(),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = actionColor
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = actionColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = action.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun RouteDeviationRecoveryAction.glyph(): TrailMateGlyph =
    when (kind) {
        RouteDeviationRecoveryActionKind.STOP_AND_CONFIRM -> TrailMateGlyph.Warning
        RouteDeviationRecoveryActionKind.RETURN_TO_ROUTE -> TrailMateGlyph.Route
        RouteDeviationRecoveryActionKind.SHARE_LOCATION -> TrailMateGlyph.Location
        RouteDeviationRecoveryActionKind.WAIT_FOR_GPS -> TrailMateGlyph.Location
        RouteDeviationRecoveryActionKind.CONTINUE_NAVIGATION -> TrailMateGlyph.Compass
        RouteDeviationRecoveryActionKind.CHECK_NEXT_CHECKPOINT -> TrailMateGlyph.Check
    }

@Composable
private fun RouteDeviationRecoveryStepList(
    steps: List<RouteDeviationRecoveryStep>,
    contentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.take(3).forEachIndexed { index, step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = step.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = step.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationGuidanceStatusCard(presentation: LocationGuidancePresentation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = presentation.tone.guidanceContainerColor(),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            presentation.tone.guidanceContentColor().copy(alpha = 0.22f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "路线校验",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = presentation.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TrailMateStatusPill(
                text = presentation.statusLabel,
                containerColor = presentation.tone.guidanceContentColor().copy(alpha = 0.13f),
                contentColor = presentation.tone.guidanceContentColor()
            )
        }
    }
}

@Composable
private fun RouteDeviationAlertTone.alertContainerColor(): Color =
    when (this) {
        RouteDeviationAlertTone.URGENT -> Color(0xFFFFE6E2)
        RouteDeviationAlertTone.CAUTION -> Color(0xFFFFF3D7)
        RouteDeviationAlertTone.REJOINED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }

@Composable
private fun RouteDeviationAlertTone.alertContentColor(): Color =
    when (this) {
        RouteDeviationAlertTone.URGENT -> Color(0xFFB3261E)
        RouteDeviationAlertTone.CAUTION -> Color(0xFF8B5A00)
        RouteDeviationAlertTone.REJOINED -> MaterialTheme.colorScheme.primary
    }

private fun RouteDeviationAlertTone.alertStatusLabel(): String =
    when (this) {
        RouteDeviationAlertTone.URGENT -> "立即核对"
        RouteDeviationAlertTone.CAUTION -> "持续关注"
        RouteDeviationAlertTone.REJOINED -> "回到路线"
    }

@Composable
private fun RouteDeviationRecoveryTone.recoveryContainerColor(): Color =
    when (this) {
        RouteDeviationRecoveryTone.OFF_ROUTE -> Color(0xFFFFEDE6)
        RouteDeviationRecoveryTone.REJOINED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }

@Composable
private fun RouteDeviationRecoveryTone.recoveryContentColor(): Color =
    when (this) {
        RouteDeviationRecoveryTone.OFF_ROUTE -> Color(0xFFB3261E)
        RouteDeviationRecoveryTone.REJOINED -> MaterialTheme.colorScheme.primary
    }

@Composable
private fun LocationGuidanceTone.guidanceContainerColor(): Color =
    when (this) {
        LocationGuidanceTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
        LocationGuidanceTone.GOOD -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        LocationGuidanceTone.WARNING -> Color(0xFFFFF3D7)
        LocationGuidanceTone.DANGER -> Color(0xFFFFE6E2)
    }

@Composable
private fun LocationGuidanceTone.guidanceContentColor(): Color =
    when (this) {
        LocationGuidanceTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        LocationGuidanceTone.GOOD -> MaterialTheme.colorScheme.primary
        LocationGuidanceTone.WARNING -> Color(0xFF8B5A00)
        LocationGuidanceTone.DANGER -> Color(0xFFB3261E)
    }

@Composable
private fun LocationReliabilityLevel.reliabilityContainerColor(): Color =
    when (this) {
        LocationReliabilityLevel.OFF -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
        LocationReliabilityLevel.SEARCHING -> Color(0xFFEFF6FF)
        LocationReliabilityLevel.GOOD -> MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
        LocationReliabilityLevel.CAUTION -> Color(0xFFFFF3D7)
        LocationReliabilityLevel.BLOCKED -> Color(0xFFFFE6E2)
    }

@Composable
private fun LocationReliabilityLevel.reliabilityContentColor(): Color =
    when (this) {
        LocationReliabilityLevel.OFF -> MaterialTheme.colorScheme.onSurfaceVariant
        LocationReliabilityLevel.SEARCHING -> Color(0xFF2563EB)
        LocationReliabilityLevel.GOOD -> MaterialTheme.colorScheme.primary
        LocationReliabilityLevel.CAUTION -> Color(0xFF8B5A00)
        LocationReliabilityLevel.BLOCKED -> Color(0xFFB3261E)
    }

@Composable
internal fun ReferenceRouteSurface(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    mapReadiness: TrailMapReadiness,
    trackRecording: TrackRecordingState,
    showUserLocationOnAmap: Boolean,
    locationSnapshot: TrailMateLocationSnapshot,
    checkpointDetailFor: (HikePlanCheckpoint) -> HikeCheckpointDetail,
    onLocateRequested: () -> Unit,
    onCheckpointFocused: (HikePlanCheckpoint) -> Unit,
    mapHeight: Dp = 620.dp,
    showMapReadinessFloatingCard: Boolean = true,
    showAssessmentFloatingCard: Boolean = true,
    showCurrentCheckpointMiniCard: Boolean = true,
    showCheckpointLayerCard: Boolean = true,
    onAmapBaseMapRenderedChange: (Boolean) -> Unit = {}
) {
    var selectedCheckpointIndex by rememberSaveable(route.sessionKey()) { mutableStateOf<Int?>(null) }
    val selectedCheckpoint = selectedCheckpointIndex?.let { index -> plan.checkpoints.getOrNull(index) }
    val mapSurfaceMode = TrailMapSurfaceSelector.select(mapReadiness)
    val context = LocalContext.current
    var nativeMapLoaded by remember(route.sessionKey(), mapSurfaceMode) {
        mutableStateOf(mapSurfaceMode == TrailMapSurfaceMode.LOCAL_CANVAS)
    }
    var mapLoadElapsedMillis by remember(route.sessionKey(), mapSurfaceMode) { mutableStateOf(0L) }
    val mapLibrePmTilesStyleAssetManifest = remember(context) {
        MapLibrePmTilesBundledStyleAssetManifestResolver.resolve { assetPath ->
            runCatching {
                context.assets.open(assetPath).use { true }
            }.getOrDefault(false)
        }
    }

    LaunchedEffect(route.sessionKey(), mapSurfaceMode, nativeMapLoaded) {
        onAmapBaseMapRenderedChange(false)
        if (mapSurfaceMode != TrailMapSurfaceMode.LOCAL_CANVAS && !nativeMapLoaded) {
            mapLoadElapsedMillis = 0L
            delay(NATIVE_MAP_SLOW_LOAD_HINT_MILLIS)
            mapLoadElapsedMillis = NATIVE_MAP_SLOW_LOAD_HINT_MILLIS
        }
    }
    val mapLoadingPresentation = TrailMapLoadingPresentationEngine.present(
        provider = mapReadiness.provider,
        mapLoaded = nativeMapLoaded,
        elapsedMillis = mapLoadElapsedMillis
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(mapHeight)
            .clip(RoundedCornerShape(0.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when (mapSurfaceMode) {
            TrailMapSurfaceMode.MAPLIBRE_PMTILES -> MapLibrePmTilesRouteMap(
                route = route,
                plan = plan,
                trackRecording = trackRecording,
                pmTilesFile = context.pmTilesBasemapDirectory().resolve("${route.offlineRoutePackKey()}.pmtiles"),
                styleAssetManifest = mapLibrePmTilesStyleAssetManifest,
                onMapLoaded = { nativeMapLoaded = true },
                modifier = Modifier.fillMaxSize()
            )
            TrailMapSurfaceMode.LOCAL_CANVAS -> RouteMapCanvas(
                route = route,
                plan = plan,
                trackRecording = trackRecording,
                modifier = Modifier.fillMaxSize()
            )
        }
        MapToolButton(
            glyph = TrailMateGlyph.Location,
            contentDescription = "定位",
            onClick = onLocateRequested,
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopStart).padding(start = 14.dp, top = 28.dp)
        )
        if (showAssessmentFloatingCard) {
            RouteAssessmentFloatingCard(
                assessment = assessment,
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopEnd)
                    .padding(top = 22.dp, end = 14.dp)
            )
        }
        if (selectedCheckpoint == null) {
            if (showCurrentCheckpointMiniCard) {
                CurrentCheckpointMiniCard(
                    plan = plan,
                    session = hikeSession,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 14.dp, end = 14.dp)
                )
            }
            if (showCheckpointLayerCard) {
                MapCheckpointLayerCard(
                    plan = plan,
                    onCheckpointSelected = { checkpoint ->
                        selectedCheckpointIndex = plan.checkpoints.indexOf(checkpoint).takeIf { it >= 0 }
                    },
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 116.dp)
                )
            }
        } else {
            MapCheckpointDetailSheet(
                checkpoint = selectedCheckpoint,
                detail = checkpointDetailFor(selectedCheckpoint),
                onFocus = {
                    onCheckpointFocused(selectedCheckpoint)
                    selectedCheckpointIndex = null
                },
                onDismiss = { selectedCheckpointIndex = null },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            )
        }
        if (showMapReadinessFloatingCard) {
            MapReadinessFloatingCard(
                readiness = mapReadiness,
                recordedTrackPointCount = trackRecording.pointCount,
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopStart)
                    .padding(start = 76.dp, top = 28.dp, end = 14.dp)
            )
        }
        if (mapLoadingPresentation.showOverlay) {
            MapLoadingOverlay(
                presentation = mapLoadingPresentation,
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.TopCenter)
                    .padding(top = 86.dp, start = 14.dp, end = 14.dp)
            )
        }
    }
}

@Composable
private fun MapToolButton(
    glyph: TrailMateGlyph,
    contentDescription: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val buttonModifier = if (onClick != null) {
        modifier
            .size(50.dp)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick)
    } else {
        modifier.size(50.dp)
    }
    Surface(
        modifier = buttonModifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            TrailMateLineIcon(
                glyph = glyph,
                contentDescription = if (onClick == null) contentDescription else null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MapLoadingOverlay(
    presentation: TrailMapLoadingPresentation,
    modifier: Modifier = Modifier
) {
    val isSlow = presentation.title.contains("较慢")
    val accentColor = if (isSlow) Color(0xFF9A6400) else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .fillMaxWidth(0.74f)
            .testTag("amap-loading-overlay"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.22f)
        ),
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Map,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = accentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = presentation.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun RouteAssessmentFloatingCard(
    assessment: RouteAssessmentSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(0.38f),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "路线评估",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = assessment.matchLevel.displayTitle(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "风险因素",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            assessment.risks.take(1).forEach { risk ->
                TrailMateStatusPill(
                    text = risk.routeRiskChipLabel(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.54f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private const val NATIVE_MAP_SLOW_LOAD_HINT_MILLIS = 3_100L
private const val FIELD_LOCATION_MAX_ACCURACY_METERS = 50.0
private const val OFFLINE_BASE_MAP_DEBUG_BYPASS_SETTING =
    "trailmate_debug_allow_offline_basemap_bypass"

@Composable
private fun MapReadinessFloatingCard(
    readiness: TrailMapReadiness,
    recordedTrackPointCount: Int,
    modifier: Modifier = Modifier
) {
    val nextStep = readiness.setupSteps.firstOrNull { step ->
        step.status != TrailMapReadinessStepStatus.READY
    }
    Surface(
        modifier = modifier.fillMaxWidth(0.36f),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = readiness.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = readiness.caption.substringBefore("，"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            nextStep?.let { step ->
                TrailMateStatusPill(
                    text = step.mapCheckLabel(),
                    containerColor = step.status.mapCheckContainerColor(),
                    contentColor = step.status.mapCheckContentColor()
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if (recordedTrackPointCount > 0) {
                    TrailMateStatusPill(
                        text = "实走轨迹",
                        containerColor = Color(0xFFFFE8D6),
                        contentColor = Color(0xFF9A4F00)
                    )
                }
                readiness.layerChips.take(2).forEach { layer ->
                    TrailMateStatusPill(
                        text = layer,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun TrailMapReadinessStep.mapCheckLabel(): String =
    "$label：$value"

internal fun TrailMapReadiness.shouldImportPmTilesBasemap(): Boolean =
    provider == com.trailmate.app.core.map.TrailMapProvider.LOCAL_GPX_PREVIEW &&
        actionLabel.isPmTilesBasemapImportAction() &&
        setupSteps.any { step ->
            (step.label == "离线地图包" || step.label == "底图") &&
                (step.value == "待导入" || step.value == "待下载")
        }

internal fun shouldOpenPmTilesImport(
    readiness: TrailMapReadiness,
    visibleActionLabel: String
): Boolean =
    readiness.shouldImportPmTilesBasemap() ||
        visibleActionLabel.isPmTilesBasemapImportAction()

private fun TrailMapReadiness.shouldShowPmTilesSetupAction(): Boolean =
    !isProductionMapReady &&
        (setupHint.title.contains("PMTiles") || setupHint.caption.contains("PMTiles"))

private fun String.isPmTilesBasemapImportAction(): Boolean =
    this == "导入离线地图包" ||
        this == "导入底图" ||
        this == "下载底图" ||
        this == "下载离线底图" ||
        this == "导入离线底图"

@Composable
private fun TrailMapReadinessStepStatus.mapCheckContainerColor(): Color =
    when (this) {
        TrailMapReadinessStepStatus.READY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        TrailMapReadinessStepStatus.NEEDS_ACTION -> Color(0xFFFFF3D7)
        TrailMapReadinessStepStatus.BLOCKED -> Color(0xFFFFE6E2)
    }

@Composable
private fun TrailMapReadinessStepStatus.mapCheckContentColor(): Color =
    when (this) {
        TrailMapReadinessStepStatus.READY -> MaterialTheme.colorScheme.primary
        TrailMapReadinessStepStatus.NEEDS_ACTION -> Color(0xFF9A6400)
        TrailMapReadinessStepStatus.BLOCKED -> Color(0xFFB3261E)
    }

@Composable
private fun MapCheckpointLayerCard(
    plan: HikePlanSummary,
    onCheckpointSelected: (HikePlanCheckpoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val routeHints = plan.checkpoints
        .filterNot { checkpoint ->
            checkpoint.type == HikePlanCheckpointType.START || checkpoint.type == HikePlanCheckpointType.FINISH
        }
        .take(3)
    if (routeHints.isEmpty()) {
        return
    }

    Surface(
        modifier = modifier.fillMaxWidth(0.46f),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "路线提示点",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            routeHints.forEach { checkpoint ->
                TrailMateStatusPill(
                    text = checkpoint.mapLayerLabel(),
                    modifier = Modifier.clickable { onCheckpointSelected(checkpoint) },
                    containerColor = checkpoint.mapLayerColor().copy(alpha = 0.16f),
                    contentColor = checkpoint.mapLayerColor()
                )
            }
        }
    }
}

@Composable
private fun MapCheckpointDetailSheet(
    checkpoint: HikePlanCheckpoint,
    detail: HikeCheckpointDetail,
    onFocus: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)
        ),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "提示点详情",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = checkpoint.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                TrailMateStatusPill(
                    text = checkpoint.mapLayerLabel(),
                    containerColor = checkpoint.mapLayerColor().copy(alpha = 0.16f),
                    contentColor = checkpoint.mapLayerColor()
                )
            }
            TrailMateMetricRow(
                items = listOf(
                    "距离" to detail.distanceLabel,
                    "预计到达" to detail.etaLabel,
                    "状态" to detail.readinessLabel
                )
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "建议动作",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detail.actionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = checkpoint.mapLayerColor(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detail.readinessCaption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = checkpoint.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onFocus,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("设为当前关注")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun RouteProgressFloatingCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(0.34f),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "路线进度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(9.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF0C5D3F),
                        startAngle = -90f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "33%",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "已完成 5.1 / 15.2 km\n累计爬升 420 / 860 m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CurrentCheckpointMiniCard(
    plan: HikePlanSummary,
    session: HikeSessionState,
    modifier: Modifier = Modifier
) {
    val current = HikeSessionEngine.currentCheckpoint(plan, session) ?: plan.checkpoints.firstOrNull()
    val next = HikeSessionEngine.nextCheckpoint(plan, session)

    Surface(
        modifier = modifier.fillMaxWidth(0.62f),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2D75E8)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Location,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "地图位置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = current?.let { "${it.title} · ${it.distanceKm}km" } ?: "起点 · 0km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = next?.let { "下一个：${it.title}" } ?: "已到达终点",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CurrentCheckpointPanel(plan: HikePlanSummary, session: HikeSessionState) {
    val current = HikeSessionEngine.currentCheckpoint(plan, session) ?: plan.checkpoints.firstOrNull()
    val next = HikeSessionEngine.nextCheckpoint(plan, session)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF2D75E8)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            TrailMateLineIcon(
                glyph = TrailMateGlyph.Location,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "当前检查点",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = current?.let { "${it.title} · ${it.distanceKm}km" } ?: "CP2 · 5.1km",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = next?.let { "距离下一个 ${it.distanceKm}km · ${it.note}" } ?: "距离下一个 3.1km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF0F6FF),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB8D2FF))
        ) {
            Text(
                text = "详情",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                color = Color(0xFF2D75E8),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RouteMapCanvas(
    route: ImportedRoute,
    plan: HikePlanSummary,
    trackRecording: TrackRecordingState,
    modifier: Modifier = Modifier
) {
    val routeBlue = Color(0xFF2D75E8)
    val trackOrange = Color(0xFFE07A1F)
    val green = MaterialTheme.colorScheme.primary
    val checkpointMarkers = TrailMapCheckpointProjector.project(route = route, plan = plan)
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFFE8F0E6), Color(0xFFF8F8F0), Color(0xFFE5EFE3))
            )
        )
        repeat(12) { index ->
            val y = size.height * (index + 1) / 14f
            drawLine(
                color = Color(0xFFBFD9C5).copy(alpha = 0.42f),
                start = Offset(0f, y),
                end = Offset(size.width, y + ((index % 3) - 1) * 28f),
                strokeWidth = 1.2.dp.toPx()
            )
        }
        val projectedLayers = TrailMapProjection.projectLayers(
            routePoints = route.routePoints,
            recordedTrackPoints = trackRecording.points,
            width = size.width,
            height = size.height
        )
        val projectedRoute = projectedLayers.route
        val projectedTrack = projectedLayers.recordedTrack
        val routePath = if (projectedRoute.size >= 2) {
            Path().apply {
                projectedRoute.forEachIndexed { index, point ->
                    if (index == 0) {
                        moveTo(point.x, point.y)
                    } else {
                        lineTo(point.x, point.y)
                    }
                }
            }
        } else {
            Path().apply {
                moveTo(size.width * 0.23f, size.height * 0.83f)
                cubicTo(size.width * 0.38f, size.height * 0.75f, size.width * 0.25f, size.height * 0.64f, size.width * 0.38f, size.height * 0.56f)
                cubicTo(size.width * 0.48f, size.height * 0.48f, size.width * 0.30f, size.height * 0.39f, size.width * 0.43f, size.height * 0.31f)
                cubicTo(size.width * 0.56f, size.height * 0.23f, size.width * 0.45f, size.height * 0.17f, size.width * 0.58f, size.height * 0.10f)
            }
        }
        drawPath(routePath, color = Color.White, style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round))
        drawPath(routePath, color = routeBlue, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
        if (projectedTrack.size >= 2) {
            val trackPath = Path().apply {
                projectedTrack.forEachIndexed { index, point ->
                    if (index == 0) {
                        moveTo(point.x, point.y)
                    } else {
                        lineTo(point.x, point.y)
                    }
                }
            }
            drawPath(trackPath, color = Color.White, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
            drawPath(trackPath, color = trackOrange, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(Color.White, radius = 10.dp.toPx(), center = Offset(projectedTrack.last().x, projectedTrack.last().y))
            drawCircle(trackOrange, radius = 7.dp.toPx(), center = Offset(projectedTrack.last().x, projectedTrack.last().y))
        } else if (projectedTrack.size == 1) {
            drawCircle(Color.White, radius = 10.dp.toPx(), center = Offset(projectedTrack.first().x, projectedTrack.first().y))
            drawCircle(trackOrange, radius = 7.dp.toPx(), center = Offset(projectedTrack.first().x, projectedTrack.first().y))
        }
        val visiblePoints = if (projectedRoute.size >= 2) {
            listOf(
                projectedRoute.first(),
                projectedRoute[projectedRoute.size / 4],
                projectedRoute[projectedRoute.size / 2],
                projectedRoute[(projectedRoute.size * 3 / 4).coerceAtMost(projectedRoute.lastIndex)],
                projectedRoute.last()
            ).map { Offset(it.x, it.y) }.distinct()
        } else {
            listOf(
                Offset(size.width * 0.23f, size.height * 0.83f),
                Offset(size.width * 0.36f, size.height * 0.65f),
                Offset(size.width * 0.37f, size.height * 0.52f),
                Offset(size.width * 0.42f, size.height * 0.36f),
                Offset(size.width * 0.58f, size.height * 0.10f)
            )
        }
        visiblePoints.forEachIndexed { index, point ->
            val isCurrent = index == 2
            if (isCurrent) {
                drawCircle(routeBlue.copy(alpha = 0.20f), radius = 32.dp.toPx(), center = point)
                drawCircle(Color.White, radius = 19.dp.toPx(), center = point)
                drawCircle(routeBlue, radius = 14.dp.toPx(), center = point)
            } else {
                drawCircle(Color.White, radius = 12.dp.toPx(), center = point)
                drawCircle(if (index == 0) Color(0xFF4E8B2C) else green, radius = 8.dp.toPx(), center = point)
            }
        }
        if (projectedRoute.size >= 2) {
            checkpointMarkers.forEach { marker ->
                val point = projectedRoute.screenPointAtDistance(marker.distanceKm) ?: return@forEach
                val markerColor = marker.mapLayerColor()
                drawCircle(markerColor.copy(alpha = 0.18f), radius = 22.dp.toPx(), center = point)
                drawCircle(Color.White, radius = 13.dp.toPx(), center = point)
                drawCircle(markerColor, radius = 8.dp.toPx(), center = point)
            }
        }
    }
}

private fun MatchLevel.displayTitle(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "推荐尝试"
        MatchLevel.CAUTION -> "谨慎尝试"
        MatchLevel.NOT_RECOMMENDED -> "不建议尝试"
    }

private fun String.routeRiskChipLabel(): String =
    when {
        contains("距离") -> "长距离"
        contains("爬升") -> "爬升较高"
        contains("天气") -> "天气多变"
        contains("补给") -> "补给间隔"
        contains("信号") -> "信号较弱"
        contains("历史") -> "历史样本少"
        contains("问卷") -> "资料待补全"
        else -> take(6)
    }

private fun HikePlanCheckpoint.mapLayerLabel(): String =
    when (type) {
        HikePlanCheckpointType.START -> "起点"
        HikePlanCheckpointType.ENERGY_CHECK -> "补给 · ${distanceKm}km"
        HikePlanCheckpointType.REST_CHECK -> "休息 · ${distanceKm}km"
        HikePlanCheckpointType.RISK_CHECK -> "风险 · ${distanceKm}km"
        HikePlanCheckpointType.FINISH -> "终点"
    }

private fun HikePlanCheckpoint.mapLayerColor(): Color =
    type.mapLayerColor()

private fun TrailMapCheckpointMarker.mapLayerColor(): Color =
    type.mapLayerColor()

private fun HikePlanCheckpointType.mapLayerColor(): Color =
    when (this) {
        HikePlanCheckpointType.START -> Color(0xFF2E7D32)
        HikePlanCheckpointType.ENERGY_CHECK -> Color(0xFFE07A1F)
        HikePlanCheckpointType.REST_CHECK -> Color(0xFF2D75E8)
        HikePlanCheckpointType.RISK_CHECK -> Color(0xFFD64945)
        HikePlanCheckpointType.FINISH -> Color(0xFF1D5B42)
    }

private fun HikePlanCheckpointType.mapDetailLabel(): String =
    when (this) {
        HikePlanCheckpointType.START -> "起点"
        HikePlanCheckpointType.ENERGY_CHECK -> "补给"
        HikePlanCheckpointType.REST_CHECK -> "休息"
        HikePlanCheckpointType.RISK_CHECK -> "风险"
        HikePlanCheckpointType.FINISH -> "终点"
    }

private fun HikePlanCheckpoint.actionTitle(): String =
    when (type) {
        HikePlanCheckpointType.START -> "确认离线路线与装备状态"
        HikePlanCheckpointType.ENERGY_CHECK -> "补水、补能量，复核配速"
        HikePlanCheckpointType.REST_CHECK -> "根据体感安排短休"
        HikePlanCheckpointType.RISK_CHECK -> "停下复核风险和天气"
        HikePlanCheckpointType.FINISH -> "结束记录并保存复盘"
    }

private fun List<MapScreenPoint>.screenPointAtDistance(distanceKm: Double): Offset? {
    if (isEmpty()) {
        return null
    }
    val clampedDistance = distanceKm.coerceIn(first().distanceAlongRouteKm, last().distanceAlongRouteKm)
    val nextIndex = indexOfFirst { point -> point.distanceAlongRouteKm >= clampedDistance }
    if (nextIndex <= 0) {
        return Offset(first().x, first().y)
    }
    val from = this[nextIndex - 1]
    val to = this[nextIndex]
    val segmentDistance = to.distanceAlongRouteKm - from.distanceAlongRouteKm
    if (segmentDistance <= 0.0) {
        return Offset(to.x, to.y)
    }

    val progress = ((clampedDistance - from.distanceAlongRouteKm) / segmentDistance).toFloat()
    return Offset(
        x = from.x + (to.x - from.x) * progress,
        y = from.y + (to.y - from.y) * progress
    )
}

private fun List<GearRecommendation>.routeGearStatusLabel(): String =
    if (isEmpty()) {
        "待生成"
    } else {
        "${count { it.status != GearStatus.MISSING }}/${size.coerceAtLeast(12)}"
    }

private fun HikeSessionStatus.displayLabel(): String =
    when (this) {
        HikeSessionStatus.READY -> "准备中"
        HikeSessionStatus.ACTIVE -> "进行中"
        HikeSessionStatus.PAUSED -> "已暂停"
        HikeSessionStatus.COMPLETED -> "已完成"
    }

private fun TrailMateLocationStatus.displayLabel(): String =
    when (this) {
        TrailMateLocationStatus.DISABLED -> "未启用"
        TrailMateLocationStatus.PERMISSION_REQUIRED -> "需要定位权限"
        TrailMateLocationStatus.SEARCHING -> "定位中"
        TrailMateLocationStatus.LOCATED -> "已定位"
        TrailMateLocationStatus.LOW_ACCURACY -> "精度较低"
        TrailMateLocationStatus.PROVIDER_DISABLED -> "系统定位未开启"
        TrailMateLocationStatus.UNAVAILABLE -> "定位不可用"
    }

private fun TrailMateLocationSnapshot.locationCaption(
    route: ImportedRoute,
    guidanceCaption: String
): String {
    val accuracy = horizontalAccuracyMeters?.let { "精度约 ${it.toInt()} m" } ?: "等待定位精度"
    val geometry = if (route.routePoints.isEmpty()) {
        "当前路线缺少完整几何，仅记录轨迹。"
    } else {
        guidanceCaption
    }

    return "$accuracy · $geometry"
}

private fun TrackRecordingState.summaryLabel(): String =
    "已记录 ${String.format(Locale.US, "%.1f", totalDistanceKm)} km / $pointCount 个点"

private fun Context.hasTrackNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun Context.isNetworkUnavailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return true
    val activeNetwork = connectivityManager.activeNetwork ?: return true
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return true

    return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun Context.isNetworkValidated(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun Context.isOfflineBaseMapDebugBypassEnabled(): Boolean =
    BuildConfig.DEBUG && runCatching {
        Settings.Global.getInt(contentResolver, OFFLINE_BASE_MAP_DEBUG_BYPASS_SETTING, 0) == 1
    }.getOrDefault(false)

private fun Context.pmTilesBasemapDirectory() =
    filesDir.resolve("pmtiles-basemaps").apply { mkdirs() }

private fun Context.readPmTilesImportCandidate(uri: Uri): PmTilesOfflineBasemapImportCandidate {
    var displayName: String? = null
    var sizeBytes: Long? = null
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex >= 0 && !cursor.isNull(displayNameIndex)) {
                displayName = cursor.getString(displayNameIndex)
            }
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                sizeBytes = cursor.getLong(sizeIndex)
            }
        }
    }
    val headerBytes = contentResolver.openInputStream(uri)?.use { input ->
        val bytes = ByteArray(127)
        val count = input.read(bytes)
        if (count <= 0) ByteArray(0) else bytes.copyOf(count)
    } ?: ByteArray(0)
    return PmTilesOfflineBasemapImportCandidate(
        displayName = displayName ?: uri.lastPathSegment,
        sizeBytes = sizeBytes,
        archiveInspection = PmTilesArchiveHeaderParser.inspect(
            bytes = headerBytes,
            fileSizeBytes = sizeBytes
        )
    )
}

private fun Context.copyPmTilesBasemap(uri: Uri, targetFileName: String): Boolean =
    runCatching {
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val targetFile = pmTilesBasemapDirectory().resolve(targetFileName)
        contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return@runCatching false
        targetFile.isFile && targetFile.length() > 0L
    }.getOrDefault(false)

private suspend fun readOfflineBaseMapStatus(
    context: Context,
    amapSdkAvailable: Boolean,
    amapPrivacyConsentAccepted: Boolean
): AmapOfflineBaseMapStatus? =
    if (amapSdkAvailable && amapPrivacyConsentAccepted) {
        withContext(Dispatchers.IO) {
            AmapOfflineBaseMapStatusReader.readDownloadedStatus(context)
        }
    } else {
        null
    }

private fun Context.shareSafetyText(text: String) {
    shareTrailMateText(text = text, chooserTitle = "分享安全位置", preferWechat = true)
}

private fun Context.shareTrailMateText(
    text: String,
    chooserTitle: String,
    preferWechat: Boolean = true
) {
    if (preferWechat) {
        val wechatStatus = TrailMateWechatTextShareLauncher(
            context = this,
            appId = BuildConfig.TRAILMATE_WECHAT_APP_ID
        ).shareText(text)
        if (wechatStatus == TrailMateWechatTextShareSendStatus.REQUEST_ACCEPTED) {
            return
        }
    }

    val shareIntent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(shareIntent, chooserTitle))
}

private fun ImportedRoute.sessionKey(): String =
    offlineRoutePackKey()

private fun ImportedRoute.pmTilesTargetBounds(): PmTilesLatLngBounds? {
    if (routePoints.isEmpty()) return null
    val latitudes = routePoints.map { it.latitude }
    val longitudes = routePoints.map { it.longitude }
    return PmTilesLatLngBounds(
        minLongitude = longitudes.minOrNull() ?: return null,
        minLatitude = latitudes.minOrNull() ?: return null,
        maxLongitude = longitudes.maxOrNull() ?: return null,
        maxLatitude = latitudes.maxOrNull() ?: return null
    )
}

@Composable
private fun RouteSketch() {
    val routeColor = MaterialTheme.colorScheme.primary
    val checkpointColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val points = listOf(
            Offset(size.width * 0.08f, size.height * 0.72f),
            Offset(size.width * 0.28f, size.height * 0.54f),
            Offset(size.width * 0.44f, size.height * 0.64f),
            Offset(size.width * 0.63f, size.height * 0.32f),
            Offset(size.width * 0.84f, size.height * 0.22f)
        )
        for (index in 0 until points.lastIndex) {
            drawLine(
                color = routeColor,
                start = points[index],
                end = points[index + 1],
                strokeWidth = 10.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        points.forEachIndexed { index, point ->
            drawCircle(
                color = if (index == 0 || index == points.lastIndex) checkpointColor else routeColor,
                radius = 8.dp.toPx(),
                center = point
            )
            drawCircle(
                color = checkpointColor.copy(alpha = 0.25f),
                radius = 18.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
