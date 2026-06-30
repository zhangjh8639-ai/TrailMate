package com.trailmate.app.feature.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trailmate.app.R
import com.trailmate.app.core.design.TrailMateAmber
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateIconBadge
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMateSectionHeader
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.gpx.HistoricalActivityImportBatch
import com.trailmate.app.core.gpx.HistoricalActivityImportFailure
import com.trailmate.app.core.gpx.HistoricalActivityImportUiReducer
import com.trailmate.app.core.gpx.HistoricalActivityImportUiState
import com.trailmate.app.core.gpx.HistoricalActivityImporter
import com.trailmate.app.core.gpx.HistoricalActivityImportState
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobIds
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.gpx.GpxImportQueuePolicy
import com.trailmate.app.core.gpx.TargetRouteImportState
import com.trailmate.app.core.gpx.TargetRouteImportQueueState
import com.trailmate.app.core.gpx.TargetRouteImporter
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.CapabilityProfileSummary
import com.trailmate.app.core.model.GearCatalogItem
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.MatchLevel
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RouteAssessmentEngine
import com.trailmate.app.core.model.RouteAssessmentSummary
import com.trailmate.app.core.model.RouteGearCatalogReadinessEngine
import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.TrailMateGearCatalogPreviewData
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.offlineRoutePackKey
import com.trailmate.app.core.model.summaryLabel
import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateGearCatalogApi
import com.trailmate.app.core.network.TrailMateGearCatalogItemDto
import com.trailmate.app.core.network.TrailMateOfflineBasemapCatalogApi
import com.trailmate.app.feature.data.DataScreen
import com.trailmate.app.feature.gear.GearCatalogSourceUiState
import com.trailmate.app.feature.gear.GearMatchScreen
import com.trailmate.app.feature.profile.ProfileSettingsScreen
import com.trailmate.app.feature.route.RouteDetailScreen
import com.trailmate.app.feature.route.RouteWorkspaceScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    profile: BaselineProfile = TrailMateSampleData.baselineProfile,
    initialImportedRoute: ImportedRoute? = null,
    initialHistoricalActivities: List<HistoricalActivity> = emptyList(),
    initialGpxImportQueue: GpxImportQueue = GpxImportQueue(),
    initialTrackRecording: TrackRecordingState = TrackRecordingState(),
    initialAmapPrivacyConsent: AmapPrivacyConsent = AmapPrivacyConsent(),
    initialOfflineRoutePackKeys: Set<String> = emptySet(),
    initialOfflineBaseMapTileProofs: List<AmapOfflineBaseMapTileProof> = emptyList(),
    gearCatalogApi: TrailMateGearCatalogApi? = null,
    offlineBasemapCatalogApi: TrailMateOfflineBasemapCatalogApi? = null,
    showSampleRouteAction: Boolean = false,
    onRouteImported: (ImportedRoute) -> Unit = {},
    onHistoricalActivitiesChanged: (List<HistoricalActivity>) -> Unit = {},
    onGpxImportQueueChanged: (GpxImportQueue) -> Unit = {},
    onTrackRecordingChanged: (TrackRecordingState) -> Unit = {},
    onOfflineRoutePackKeysChanged: (Set<String>) -> Unit = {},
    onOfflineBaseMapTileProofsChanged: (List<AmapOfflineBaseMapTileProof>) -> Unit = {},
    onLogout: () -> Unit = {},
    onClearLocalData: () -> Unit = {}
) {
    val context = LocalContext.current
    val importScope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Home) }
    var isRouteDetailOpen by rememberSaveable { mutableStateOf(false) }
    var routeDetailStartsInCockpit by rememberSaveable { mutableStateOf(false) }
    var routeDetailInitialDiagnosticsExpanded by rememberSaveable { mutableStateOf(false) }
    var routeNavigationFullscreen by rememberSaveable { mutableStateOf(false) }
    var requestedGearCategory by rememberSaveable { mutableStateOf("") }
    var gearCatalogItems by remember { mutableStateOf(TrailMateGearCatalogPreviewData.items) }
    var gearCatalogSourceState by remember { mutableStateOf(GearCatalogSourceUiState.localPreview()) }
    var gearCatalogReloadToken by rememberSaveable { mutableStateOf(0) }
    var historyImportUiState by remember { mutableStateOf(HistoricalActivityImportUiState()) }
    var gpxImportQueue by remember { mutableStateOf(initialGpxImportQueue) }
    var routeImportQueue by rememberSaveable(stateSaver = TargetRouteImportQueueStateSaver) {
        mutableStateOf(TargetRouteImportQueueState.fromRoute(initialImportedRoute))
    }
    var historicalActivities by rememberSaveable(stateSaver = HistoricalActivitiesStateSaver) {
        mutableStateOf(initialHistoricalActivities)
    }
    var amapPrivacyConsent by rememberSaveable(stateSaver = AmapPrivacyConsentStateSaver) {
        mutableStateOf(initialAmapPrivacyConsent)
    }
    var offlineRoutePackKeys by rememberSaveable {
        mutableStateOf(initialOfflineRoutePackKeys.toList())
    }
    var offlineBaseMapTileProofs by remember {
        mutableStateOf(initialOfflineBaseMapTileProofs)
    }
    val savedOfflineRoutePackKeys = offlineRoutePackKeys.toSet()
    val updateOfflineRoutePackKeys: (Set<String>) -> Unit = { keys ->
        offlineRoutePackKeys = keys.toList()
        onOfflineRoutePackKeysChanged(keys)
    }
    val updateOfflineBaseMapTileProofs: (List<AmapOfflineBaseMapTileProof>) -> Unit = { proofs ->
        offlineBaseMapTileProofs = proofs
        onOfflineBaseMapTileProofsChanged(proofs)
    }
    val importedRoute = routeImportQueue.lastImportedRoute
    LaunchedEffect(gearCatalogApi, gearCatalogReloadToken) {
        val api = gearCatalogApi
        if (api == null) {
            gearCatalogItems = TrailMateGearCatalogPreviewData.items
            gearCatalogSourceState = GearCatalogSourceUiState.localPreview()
            return@LaunchedEffect
        }

        gearCatalogSourceState = GearCatalogSourceUiState.loading()
        when (val result = withContext(Dispatchers.IO) { api.searchGearCatalog(category = "", query = "") }) {
            is TrailMateApiResult.Success -> {
                gearCatalogItems = result.value.map { it.toGearCatalogItem() }
                gearCatalogSourceState = GearCatalogSourceUiState.serverSynced(result.value.size)
                Log.i("TrailMateGear", "Loaded ${result.value.size} catalog items from server.")
            }
            is TrailMateApiResult.Failure -> {
                gearCatalogItems = TrailMateGearCatalogPreviewData.items
                gearCatalogSourceState = GearCatalogSourceUiState.fallbackCache()
                Log.w(
                    "TrailMateGear",
                    "Fell back to local gear catalog: ${result.error.code} ${result.error.message}"
                )
            }
        }
    }
    val routeAssessment = importedRoute?.takeIf { it.readyForAssessment() }?.let { route ->
        RouteAssessmentEngine.assess(
            profile = profile,
            route = route,
            historicalActivities = historicalActivities
        )
    }
    val routeGearRecommendations = if (importedRoute?.readyForAssessment() == true && routeAssessment != null) {
        RouteGearCatalogReadinessEngine.resolve(
            route = importedRoute,
            assessment = routeAssessment,
            catalogItems = gearCatalogItems
        )
    } else {
        emptyList()
    }
    val mainScrollState = rememberScrollState()
    LaunchedEffect(selectedTab, isRouteDetailOpen) {
        mainScrollState.scrollTo(0)
        if (selectedTab != HomeTab.Route || !isRouteDetailOpen) {
            routeNavigationFullscreen = false
        }
    }
    val applyImportState: (TargetRouteImportState) -> Unit = { state ->
        routeImportQueue = routeImportQueue.complete(state)
        if (state is TargetRouteImportState.Imported) {
            onRouteImported(state.route)
        }
    }
    val publishGpxImportQueue: (GpxImportQueue) -> Unit = { queue ->
        gpxImportQueue = queue
        onGpxImportQueueChanged(queue)
    }
    val enqueueGpxImportJob: (String, GpxImportJobKind, String, String) -> GpxImportQueue = { id, kind, sourceUri, fileName ->
        val now = System.currentTimeMillis()
        val queuedQueue = gpxImportQueue
            .enqueue(
                id = id,
                kind = kind,
                sourceUri = sourceUri,
                fileName = fileName,
                nowEpochMillis = now
            )
        publishGpxImportQueue(queuedQueue)
        queuedQueue
    }
    val startQueuedGpxImportJob: (GpxImportQueue, String) -> GpxImportQueue = { queue, id ->
        val runningQueue = queue.startJob(
            id = id,
            nowEpochMillis = System.currentTimeMillis()
        )
        publishGpxImportQueue(runningQueue)
        runningQueue
    }
    val finishTargetRouteImportJob: (String, TargetRouteImportState) -> Unit = { jobId, state ->
        val now = System.currentTimeMillis()
        val completedQueue = when (state) {
            TargetRouteImportState.Empty -> gpxImportQueue
            is TargetRouteImportState.Imported -> gpxImportQueue.markSucceeded(id = jobId, nowEpochMillis = now)
            is TargetRouteImportState.Failed -> gpxImportQueue.markFailed(
                id = jobId,
                message = state.message,
                nowEpochMillis = now,
                retryDelayMillis = GpxImportQueuePolicy.RETRY_DELAY_MILLIS
            )
        }
        publishGpxImportQueue(completedQueue)
    }
    val routePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
                isRouteDetailOpen = false
                routeDetailStartsInCockpit = false
                val fileName = context.displayNameForOrDefault(uri = uri, defaultName = "selected-route.gpx")
            if (gpxImportQueue.hasRunningJob()) {
                routeImportQueue = routeImportQueue.complete(
                    TargetRouteImportState.Failed(
                        fileName = fileName,
                        message = GPX_IMPORT_BUSY_MESSAGE
                    )
                )
            } else {
                context.takePersistableReadPermission(uri)
                val jobId = GpxImportJobIds.create(kind = GpxImportJobKind.TARGET_ROUTE, nonce = System.nanoTime())
                val queuedQueue = enqueueGpxImportJob(jobId, GpxImportJobKind.TARGET_ROUTE, uri.toString(), fileName)
                val runningQueue = startQueuedGpxImportJob(queuedQueue, jobId)
                if (runningQueue.isRunningJob(jobId)) {
                    routeImportQueue = routeImportQueue.start(fileName)
                    importScope.launch {
                        val state = withContext(Dispatchers.IO) {
                            context.importRouteFromUri(uri = uri, fileName = fileName)
                        }
                        applyImportState(state)
                        finishTargetRouteImportJob(jobId, state)
                    }
                }
            }
        }
    }
    val historyPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            if (gpxImportQueue.hasRunningJob()) {
                historyImportUiState = HistoricalActivityImportUiState().failed(GPX_IMPORT_BUSY_MESSAGE)
                return@rememberLauncherForActivityResult
            }
            uris.forEach { uri -> context.takePersistableReadPermission(uri) }
            val historyImportNonce = System.nanoTime()
            val jobs = uris.mapIndexed { index, uri ->
                PendingGpxImportJob(
                    id = GpxImportJobIds.create(
                        kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
                        nonce = historyImportNonce + index
                    ),
                    sourceUri = uri.toString(),
                    fileName = context.displayNameForOrDefault(uri = uri, defaultName = "history.gpx"),
                    uri = uri
                )
            }
            var queuedJobs = gpxImportQueue
            jobs.forEach { job ->
                queuedJobs = queuedJobs
                    .enqueue(
                        id = job.id,
                        kind = GpxImportJobKind.HISTORICAL_ACTIVITY,
                        sourceUri = job.sourceUri,
                        fileName = job.fileName,
                        nowEpochMillis = System.currentTimeMillis()
                    )
            }
            publishGpxImportQueue(queuedJobs)
            historyImportUiState = historyImportUiState.importing(uris.size)
            importScope.launch {
                try {
                    val startingActivities = historicalActivities
                    var workingActivities = startingActivities
                    var workingQueue = queuedJobs
                    val importedActivities = mutableListOf<HistoricalActivity>()
                    val failures = mutableListOf<HistoricalActivityImportFailure>()
                    jobs.forEach { job ->
                        workingQueue = workingQueue.startJob(
                            id = job.id,
                            nowEpochMillis = System.currentTimeMillis()
                        )
                        publishGpxImportQueue(workingQueue)
                        if (!workingQueue.isRunningJob(job.id)) {
                            return@forEach
                        }
                        when (val state = withContext(Dispatchers.IO) {
                            context.importHistoricalActivityFromUri(uri = job.uri, fileName = job.fileName)
                        }) {
                            is HistoricalActivityImportState.Imported -> {
                                importedActivities += state.activity
                                val singleResult = HistoricalActivityImportUiReducer.applyBatch(
                                    currentActivities = workingActivities,
                                    batch = HistoricalActivityImportBatch(
                                        activities = listOf(state.activity),
                                        failures = emptyList()
                                    )
                                )
                                if (singleResult.activities != workingActivities) {
                                    workingActivities = singleResult.activities
                                    historicalActivities = workingActivities
                                    onHistoricalActivitiesChanged(workingActivities)
                                }
                                workingQueue = workingQueue.markSucceeded(
                                    id = job.id,
                                    nowEpochMillis = System.currentTimeMillis()
                                )
                                publishGpxImportQueue(workingQueue)
                            }
                            is HistoricalActivityImportState.Failed -> {
                                failures += HistoricalActivityImportFailure(
                                    fileName = state.fileName,
                                    message = state.message
                                )
                                workingQueue = workingQueue.markFailed(
                                    id = job.id,
                                    message = state.message,
                                    nowEpochMillis = System.currentTimeMillis(),
                                    retryDelayMillis = GpxImportQueuePolicy.RETRY_DELAY_MILLIS
                                )
                                publishGpxImportQueue(workingQueue)
                            }
                        }
                    }
                    val batch = HistoricalActivityImportBatch(
                        activities = importedActivities,
                        failures = failures
                    )
                    val result = HistoricalActivityImportUiReducer.applyBatch(
                        currentActivities = startingActivities,
                        batch = batch
                    )
                    if (result.activities != historicalActivities) {
                        historicalActivities = result.activities
                    }
                    historyImportUiState = result.uiState
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    historyImportUiState = HistoricalActivityImportUiReducer.applyFailure(
                        currentActivities = historicalActivities,
                        message = error.message ?: "无法导入所选 GPX 文件。"
                    ).uiState
                } finally {
                    if (historyImportUiState.isImporting) {
                        historyImportUiState = historyImportUiState.copy(isImporting = false)
                    }
                }
            }
        }
    }

    val importSampleRoute: () -> Unit = {
        isRouteDetailOpen = false
        routeDetailStartsInCockpit = false
        val fileName = "longjing-ridge-target.gpx"
        if (gpxImportQueue.hasRunningJob()) {
            routeImportQueue = routeImportQueue.complete(
                TargetRouteImportState.Failed(
                    fileName = fileName,
                    message = GPX_IMPORT_BUSY_MESSAGE
                )
            )
        } else {
            val jobId = GpxImportJobIds.create(
                kind = GpxImportJobKind.TARGET_ROUTE,
                nonce = System.nanoTime()
            )
            val queuedQueue = enqueueGpxImportJob(
                jobId,
                GpxImportJobKind.TARGET_ROUTE,
                "sample://$fileName",
                fileName
            )
            val runningQueue = startQueuedGpxImportJob(queuedQueue, jobId)
            if (runningQueue.isRunningJob(jobId)) {
                routeImportQueue = routeImportQueue.start(fileName)
                val state = TargetRouteImporter.importText(
                    fileName = fileName,
                    content = TrailMateSampleData.sampleTargetGpx
                )
                applyImportState(state)
                finishTargetRouteImportJob(jobId, state)
            }
        }
    }
    val pickRouteFile: () -> Unit = {
        isRouteDetailOpen = false
        routeDetailStartsInCockpit = false
        routeDetailInitialDiagnosticsExpanded = false
        routePicker.launch(GPX_MIME_TYPES)
    }
    Scaffold(
        bottomBar = {
            if (!routeNavigationFullscreen) {
                TrailMateBottomNavigation(
                    selectedTab = selectedTab,
                    onSelected = { selectedTab = it }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clipToBounds()
                .then(
                    if (routeNavigationFullscreen) {
                        Modifier
                    } else {
                        Modifier
                            .verticalScroll(mainScrollState)
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    }
                ),
            verticalArrangement = if (routeNavigationFullscreen) {
                Arrangement.Top
            } else {
                Arrangement.spacedBy(16.dp)
            }
        ) {
            when (selectedTab) {
                HomeTab.Home -> HomeDashboardScreen(
                    importedRoute = importedRoute,
                    routeAssessment = routeAssessment,
                    onImportRoute = pickRouteFile,
                    onOpenRouteAssessment = {
                        routeDetailStartsInCockpit = false
                        routeDetailInitialDiagnosticsExpanded = false
                        isRouteDetailOpen = importedRoute?.readyForAssessment() == true && routeAssessment != null
                        selectedTab = HomeTab.Route
                    },
                    onStartLightNavigation = {
                        routeDetailStartsInCockpit = true
                        routeDetailInitialDiagnosticsExpanded = false
                        isRouteDetailOpen = importedRoute?.readyForAssessment() == true && routeAssessment != null
                        selectedTab = HomeTab.Route
                    },
                    onOpenGearChecklist = { selectedTab = HomeTab.Gear }
                )

                HomeTab.Route -> {
                    val route = importedRoute
                    val assessment = routeAssessment
                    if (isRouteDetailOpen && route?.readyForAssessment() == true && assessment != null) {
                        val offlineRoutePackKey = route.offlineRoutePackKey()
                        RouteDetailScreen(
                            route = route,
                            profile = profile,
                            catalogItems = gearCatalogItems,
                            catalogStatusLabel = gearCatalogSourceState.label,
                            offlineBasemapCatalogApi = offlineBasemapCatalogApi,
                            routeAssessment = assessment,
                            gearRecommendations = routeGearRecommendations,
                            initialTrackRecording = initialTrackRecording,
                            initialOfflineRoutePackReady = offlineRoutePackKey in savedOfflineRoutePackKeys,
                            initialOfflineBaseMapTileProofs = offlineBaseMapTileProofs,
                            initiallyShowRouteCockpit = routeDetailStartsInCockpit,
                            initiallyExpandRouteDiagnostics = routeDetailInitialDiagnosticsExpanded,
                            amapPrivacyConsent = amapPrivacyConsent,
                            routeNavigationFullscreen = routeNavigationFullscreen,
                            onTrackRecordingChanged = onTrackRecordingChanged,
                            onOfflineRoutePackReadyChanged = { ready ->
                                val updated = if (ready) {
                                    savedOfflineRoutePackKeys + offlineRoutePackKey
                                } else {
                                    savedOfflineRoutePackKeys - offlineRoutePackKey
                                }
                                updateOfflineRoutePackKeys(updated)
                            },
                            onOfflineBaseMapTileProofsChanged = updateOfflineBaseMapTileProofs,
                            onRouteNavigationFullscreenChanged = { fullscreen ->
                                routeNavigationFullscreen = fullscreen
                            },
                            onOpenTrackDataRequested = { selectedTab = HomeTab.Data },
                    onViewGearMatchesRequested = { category ->
                                requestedGearCategory = category
                                selectedTab = HomeTab.Gear
                            },
                            onBackToRouteWorkspace = {
                                routeNavigationFullscreen = false
                                isRouteDetailOpen = false
                                routeDetailStartsInCockpit = false
                                routeDetailInitialDiagnosticsExpanded = false
                            }
                        )
                    } else {
                        RouteWorkspaceScreen(
                            importedRoute = importedRoute,
                            importSummary = routeImportQueue.summary(),
                            isImporting = routeImportQueue.isImporting,
                            canRetry = routeImportQueue.canRetry,
                            offlineRoutePackReady = importedRoute
                                ?.offlineRoutePackKey()
                                ?.let { key -> key in savedOfflineRoutePackKeys } == true,
                            offlineBaseMapReady = importedRoute?.let { route ->
                                offlineBaseMapTileProofs.any { proof ->
                                    proof.routeKey == route.offlineRoutePackKey() && proof.tileVisible
                                }
                            } == true,
                            showSampleRouteAction = showSampleRouteAction,
                            onPickRouteFile = pickRouteFile,
                            onImportSampleRoute = importSampleRoute,
                            onSaveOfflineRoute = {
                                importedRoute?.let { routeToSave ->
                                    updateOfflineRoutePackKeys(savedOfflineRoutePackKeys + routeToSave.offlineRoutePackKey())
                                }
                            },
                            onOpenRouteDetail = {
                                routeDetailStartsInCockpit = false
                                routeDetailInitialDiagnosticsExpanded = false
                                isRouteDetailOpen = true
                            },
                            onOpenBasemapPreparation = {
                                routeDetailStartsInCockpit = true
                                routeDetailInitialDiagnosticsExpanded = true
                                isRouteDetailOpen = importedRoute?.readyForAssessment() == true && routeAssessment != null
                            }
                        )
                    }
                }

                HomeTab.Gear -> GearMatchScreen(
                    routeGearRecommendations = routeGearRecommendations,
                    requestedCategory = requestedGearCategory,
                    catalogItems = gearCatalogItems,
                    catalogStatusLabel = gearCatalogSourceState.label,
                    catalogStatusCaption = gearCatalogSourceState.caption,
                    catalogIsLoading = gearCatalogSourceState.isLoading,
                    onRetryCatalogLoad = if (gearCatalogSourceState.canRetry && gearCatalogApi != null) {
                        { gearCatalogReloadToken += 1 }
                    } else {
                        null
                    }
                )

                HomeTab.Data -> DataScreen(
                    latestTrackRecording = initialTrackRecording,
                    historicalActivities = historicalActivities,
                    historyImportUiState = historyImportUiState,
                    onPickHistoryGpx = { historyPicker.launch(GPX_MIME_TYPES) },
                    onOpenRouteTab = { selectedTab = HomeTab.Route }
                )

                HomeTab.Profile -> ProfileSettingsScreen(
                    profile = profile,
                    onLogout = onLogout,
                    onClearLocalData = onClearLocalData
                )
            }
            if (!routeNavigationFullscreen) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private enum class HomeTab(val label: String) {
    Home("首页"),
    Route("路线"),
    Gear("装备"),
    Data("数据"),
    Profile("我的")
}

private fun HomeTab.glyph(): TrailMateGlyph =
    when (this) {
        HomeTab.Home -> TrailMateGlyph.Home
        HomeTab.Route -> TrailMateGlyph.Map
        HomeTab.Gear -> TrailMateGlyph.Gear
        HomeTab.Data -> TrailMateGlyph.Chart
        HomeTab.Profile -> TrailMateGlyph.Profile
    }

@Composable
private fun TrailMateBottomNavigation(
    selectedTab: HomeTab,
    onSelected: (HomeTab) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        HomeTab.entries.forEach { tab ->
            NavigationBarItem(
                modifier = Modifier.testTag("home-tab-${tab.label}"),
                selected = tab == selectedTab,
                onClick = { onSelected(tab) },
                icon = {
                    TrailMateLineIcon(
                        glyph = tab.glyph(),
                        contentDescription = tab.label,
                        modifier = Modifier.size(26.dp),
                        tint = if (tab == selectedTab) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                label = { Text(tab.label, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun HomeDashboard(
    profile: BaselineProfile,
    capabilityProfile: CapabilityProfileSummary,
    importedRoute: ImportedRoute?,
    routeAssessment: RouteAssessmentSummary?,
    historicalActivities: List<HistoricalActivity>,
    historyImportUiState: HistoricalActivityImportUiState,
    onPickHistoryGpx: () -> Unit,
    onUseSampleHistory: () -> Unit,
    onOpenRoute: () -> Unit,
    onOpenGear: () -> Unit,
    canUseSampleHistory: Boolean
) {
    HomeTopBar()
    HomeGreeting()
    RouteImportRow(onClick = onOpenRoute)
    TrailMateSectionHeader(
        title = "当前路线评估",
        action = "更新"
    )
    CurrentRouteAssessmentCard(
        importedRoute = importedRoute,
        routeAssessment = routeAssessment,
        onOpenRoute = onOpenRoute
    )
    TrailMateSectionHeader(title = "快速开始")
    QuickStartGrid(
        onRoute = onOpenRoute,
        onNavigation = onOpenRoute,
        onGear = onOpenGear
    )
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MountainLogo(modifier = Modifier.size(34.dp))
            Text(
                text = "TrailMate",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        TrailMateLineIcon(
            glyph = TrailMateGlyph.Bell,
            contentDescription = "通知",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun HomeGreeting() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "下午好，",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "准备走哪条线？",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Weather,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = TrailMateAmber
                )
                Text(
                    text = "26°C 多云",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "杭州 · 西湖区",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RouteImportRow(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "导入 GPX 文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TrailMateLineIcon(
                glyph = TrailMateGlyph.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CurrentRouteAssessmentCard(
    importedRoute: ImportedRoute?,
    routeAssessment: RouteAssessmentSummary?,
    onOpenRoute: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clipToBounds()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.trailmate_route_hero),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.10f),
                                    Color.Black.copy(alpha = 0.54f)
                                )
                            )
                        )
                )
                TrailMateStatusPill(
                    text = "推荐路线",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(18.dp)
                )
                Column(
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = importedRoute?.routeName ?: "尚未导入路线",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = importedRoute?.summaryLabel() ?: "先导入目标 GPX",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (routeAssessment != null) {
                    Surface(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomEnd)
                            .padding(18.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "!",
                                color = TrailMateAmber,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = routeAssessment.matchLevel.displayTitle(),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "路线难度",
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            TrailMateMetricRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                items = listOf(
                    "预计用时" to (routeAssessment?.estimatedDurationRange ?: "--"),
                    "最高海拔" to "968m",
                    "累计爬升" to (importedRoute?.let { "+${it.ascentMeters}m" } ?: "--"),
                    "体能消耗" to (routeAssessment?.matchLevel?.effortLabel() ?: "--")
                )
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable(onClick = onOpenRoute),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = if (importedRoute == null) {
                            "下一步：导入 GPX 或选择历史路线"
                        } else {
                            "下一步：查看休息补给与装备"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStartGrid(
    onRoute: () -> Unit,
    onNavigation: () -> Unit,
    onGear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStartCard(TrailMateGlyph.Mountain, "路线评估", "评估风险与难度", onRoute, Modifier.weight(1f))
        QuickStartCard(TrailMateGlyph.Compass, "路线辅助", "定位与记录", onNavigation, Modifier.weight(1f))
        QuickStartCard(TrailMateGlyph.Gear, "装备匹配", "品牌装备候选", onGear, Modifier.weight(1f))
    }
}

@Composable
private fun QuickStartCard(
    glyph: TrailMateGlyph,
    title: String,
    caption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(118.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TrailMateIconBadge(glyph = glyph, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MountainLogo(modifier: Modifier = Modifier) {
    val logoColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width * 0.28f, size.height * 0.22f)
            lineTo(size.width * 0.44f, size.height * 0.67f)
            lineTo(size.width * 0.62f, size.height * 0.04f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path, color = logoColor)
    }
}

@Composable
private fun MountainHeroCanvas() {
    val primary = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF81B7E2), Color(0xFF5E8D68), Color(0xFF28452F))
            )
        )
        val far = Path().apply {
            moveTo(0f, size.height * 0.7f)
            lineTo(size.width * 0.18f, size.height * 0.5f)
            lineTo(size.width * 0.34f, size.height * 0.64f)
            lineTo(size.width * 0.52f, size.height * 0.42f)
            lineTo(size.width * 0.72f, size.height * 0.62f)
            lineTo(size.width, size.height * 0.38f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(far, color = Color(0xFF1E5D3C).copy(alpha = 0.82f))
        val ridge = Path().apply {
            moveTo(size.width * 0.06f, size.height)
            lineTo(size.width * 0.32f, size.height * 0.76f)
            lineTo(size.width * 0.52f, size.height * 0.48f)
            lineTo(size.width * 0.68f, size.height * 0.62f)
            lineTo(size.width, size.height * 0.86f)
        }
        drawPath(
            ridge,
            color = Color(0xFFE9D29A),
            style = Stroke(width = 5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f))
            )
        )
        drawCircle(primary.copy(alpha = 0.14f), radius = size.width * 0.42f, center = Offset(size.width * 0.82f, size.height * 0.18f))
    }
}

private fun com.trailmate.app.core.model.ConfidenceLevel.homeLabel(): String =
    when (this) {
        com.trailmate.app.core.model.ConfidenceLevel.LOW -> "低"
        com.trailmate.app.core.model.ConfidenceLevel.MEDIUM -> "中"
        com.trailmate.app.core.model.ConfidenceLevel.HIGH -> "高"
    }

private fun MatchLevel.displayTitle(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "推荐尝试"
        MatchLevel.CAUTION -> "谨慎尝试"
        MatchLevel.NOT_RECOMMENDED -> "不建议尝试"
    }

private fun MatchLevel.effortLabel(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "适中"
        MatchLevel.CAUTION -> "较高"
        MatchLevel.NOT_RECOMMENDED -> "很高"
    }

private fun TrailMateGearCatalogItemDto.toGearCatalogItem(): GearCatalogItem =
    GearCatalogItem(
        catalogItemId = catalogItemId,
        category = category,
        brand = brand,
        model = model,
        displayName = displayName,
        weightGrams = weightGrams,
        tags = tags,
        imageUrl = imageUrl,
        imageAttribution = imageAttribution,
        source = source
    )

@Suppress("UNCHECKED_CAST")
private val AmapPrivacyConsentStateSaver = mapSaver(
    save = { consent ->
        mapOf(
            "accepted" to consent.accepted,
            "acceptedAtEpochMillis" to (consent.acceptedAtEpochMillis ?: -1L),
            "policyVersion" to consent.policyVersion
        )
    },
    restore = { saved ->
        AmapPrivacyConsent(
            accepted = saved["accepted"] as Boolean,
            acceptedAtEpochMillis = (saved["acceptedAtEpochMillis"] as Long).takeIf { it >= 0L },
            policyVersion = saved["policyVersion"] as String
        )
    }
)

@Suppress("UNCHECKED_CAST")
internal val TargetRouteImportQueueStateSaver = mapSaver(
    save = { queue ->
        val route = queue.lastImportedRoute
        mapOf(
            "routeName" to route?.routeName.orEmpty(),
            "fileName" to route?.fileName.orEmpty(),
            "distanceKm" to (route?.distanceKm ?: -1.0),
            "ascentMeters" to (route?.ascentMeters ?: -1),
            "pointCount" to (route?.pointCount ?: -1),
            "durationMinutes" to (route?.durationMinutes ?: -1),
            "routePointLatitudes" to route.orEmptyRoutePoints().map { it.latitude },
            "routePointLongitudes" to route.orEmptyRoutePoints().map { it.longitude },
            "routePointElevations" to route.orEmptyRoutePoints().map { it.elevationMeters ?: Double.NaN },
            "routePointDistances" to route.orEmptyRoutePoints().map { it.distanceAlongRouteKm },
            "failedFileName" to queue.failedFileName.orEmpty(),
            "failureMessage" to queue.failureMessage.orEmpty()
        )
    },
    restore = { saved ->
        val routeName = saved["routeName"] as String
        val route = if (routeName.isBlank()) {
            null
        } else {
            val latitudes = saved["routePointLatitudes"] as? List<Double> ?: emptyList()
            val longitudes = saved["routePointLongitudes"] as? List<Double> ?: emptyList()
            val elevations = saved["routePointElevations"] as? List<Double> ?: emptyList()
            val distances = saved["routePointDistances"] as? List<Double> ?: emptyList()
            ImportedRoute(
                routeName = routeName,
                fileName = saved["fileName"] as String,
                distanceKm = saved["distanceKm"] as Double,
                ascentMeters = saved["ascentMeters"] as Int,
                status = RouteImportStatus.PARSED,
                pointCount = saved["pointCount"] as Int,
                durationMinutes = (saved["durationMinutes"] as? Int)?.takeIf { it >= 0 },
                routePoints = latitudes.indices.mapNotNull { index ->
                    val longitude = longitudes.getOrNull(index) ?: return@mapNotNull null
                    val distance = distances.getOrNull(index) ?: return@mapNotNull null
                    RoutePoint(
                        latitude = latitudes[index],
                        longitude = longitude,
                        elevationMeters = elevations.getOrNull(index)?.takeIf { !it.isNaN() },
                        distanceAlongRouteKm = distance
                    )
                }
            )
        }

        TargetRouteImportQueueState(
            lastImportedRoute = route,
            failedFileName = (saved["failedFileName"] as String).ifBlank { null },
            failureMessage = (saved["failureMessage"] as String).ifBlank { null }
        )
    }
)

private fun ImportedRoute?.orEmptyRoutePoints(): List<RoutePoint> =
    this?.routePoints ?: emptyList()

@Suppress("UNCHECKED_CAST")
private val HistoricalActivitiesStateSaver = mapSaver(
    save = { activities ->
        mapOf(
            "routeNames" to activities.map { it.routeName },
            "distances" to activities.map { it.distanceKm },
            "ascents" to activities.map { it.ascentMeters },
            "durations" to activities.map { it.durationMinutes }
        )
    },
    restore = { saved ->
        val routeNames = saved["routeNames"] as List<String>
        val distances = saved["distances"] as List<Double>
        val ascents = saved["ascents"] as List<Int>
        val durations = saved["durations"] as List<Int>

        routeNames.indices.map { index ->
            HistoricalActivity(
                routeName = routeNames[index],
                distanceKm = distances[index],
                ascentMeters = ascents[index],
                durationMinutes = durations[index]
            )
        }
    }
)

private data class PendingGpxImportJob(
    val id: String,
    val sourceUri: String,
    val fileName: String,
    val uri: Uri
)

private fun GpxImportQueue.isRunningJob(id: String): Boolean =
    jobs.any { job -> job.id == id && job.status == GpxImportJobStatus.RUNNING }

private fun Context.importRouteFromUri(uri: Uri, fileName: String): TargetRouteImportState {
    val content = runCatching {
        contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
    }.getOrElse { error ->
        return TargetRouteImportState.Failed(
            fileName = fileName,
            message = error.message ?: "无法打开所选 GPX 文件。"
        )
    } ?: return TargetRouteImportState.Failed(
        fileName = fileName,
        message = "无法打开所选 GPX 文件。"
    )

    return TargetRouteImporter.importText(fileName = fileName, content = content)
}

private fun Context.importHistoricalActivityFromUri(uri: Uri, fileName: String): HistoricalActivityImportState {
    val content = runCatching {
        contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
    }.getOrElse { error ->
        return HistoricalActivityImportState.Failed(
            fileName = fileName,
            message = error.message ?: "无法打开所选 GPX 文件。"
        )
    } ?: return HistoricalActivityImportState.Failed(
        fileName = fileName,
        message = "无法打开所选 GPX 文件。"
    )

    return HistoricalActivityImporter.importText(fileName = fileName, content = content)
}

private fun Context.displayNameForOrDefault(uri: Uri, defaultName: String): String =
    runCatching { displayNameFor(uri) }.getOrDefault(defaultName)

private fun Context.takePersistableReadPermission(uri: Uri) {
    runCatching {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

private fun Context.displayNameFor(uri: Uri): String {
    val displayName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
        } else {
            null
        }
    }

    return displayName ?: uri.lastPathSegment?.substringAfterLast('/') ?: "selected-route.gpx"
}

private val GPX_MIME_TYPES = arrayOf(
    "application/gpx+xml",
    "application/xml",
    "text/xml",
    "*/*"
)

private const val GPX_IMPORT_BUSY_MESSAGE = "另一个 GPX 导入任务正在运行。"
