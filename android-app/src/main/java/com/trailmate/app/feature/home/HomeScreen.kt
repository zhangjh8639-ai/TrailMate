package com.trailmate.app.feature.home

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.gpx.TargetRouteImportState
import com.trailmate.app.core.gpx.TargetRouteImportQueueState
import com.trailmate.app.core.gpx.TargetRouteImportQueueSummary
import com.trailmate.app.core.gpx.TargetRouteImporter
import com.trailmate.app.core.persistence.TrailMateDataControlEngine
import com.trailmate.app.core.persistence.TrailMateDataControlSummary
import com.trailmate.app.core.persistence.TrailMateSnapshot
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RouteAssessmentEngine
import com.trailmate.app.core.model.RouteGearAdvisorEngine
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.feature.gear.MyGearScreen
import com.trailmate.app.feature.route.RouteDetailScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    profile: BaselineProfile = TrailMateSampleData.baselineProfile,
    initialInventory: GearInventory = GearInventory(TrailMateSampleData.gearItems),
    initialImportedRoute: ImportedRoute? = null,
    onInventoryChanged: (GearInventory) -> Unit = {},
    onRouteImported: (ImportedRoute) -> Unit = {},
    onClearLocalData: () -> Unit = {}
) {
    val context = LocalContext.current
    val importScope = rememberCoroutineScope()
    var selectedSection by rememberSaveable { mutableStateOf(HomeSection.Route) }
    var requestedGearCategory by rememberSaveable { mutableStateOf("Trekking poles") }
    var routeImportQueue by rememberSaveable(stateSaver = TargetRouteImportQueueStateSaver) {
        mutableStateOf(TargetRouteImportQueueState.fromRoute(initialImportedRoute))
    }
    var inventory by rememberSaveable(stateSaver = GearInventoryStateSaver) {
        mutableStateOf(initialInventory)
    }
    val importedRoute = routeImportQueue.lastImportedRoute
    val routeAssessment = importedRoute?.takeIf { it.readyForAssessment() }?.let { route ->
        RouteAssessmentEngine.assess(profile = profile, route = route)
    }
    val routeGearRecommendations = if (importedRoute?.readyForAssessment() == true && routeAssessment != null) {
        inventory.applyTo(
            RouteGearAdvisorEngine.recommend(
                route = importedRoute,
                assessment = routeAssessment
            )
        )
    } else {
        emptyList()
    }
    val dataControlSummary = TrailMateDataControlEngine.summarize(
        TrailMateSnapshot(
            profile = profile,
            inventory = inventory,
            importedRoute = importedRoute
        )
    )
    val applyImportState: (TargetRouteImportState) -> Unit = { state ->
        routeImportQueue = routeImportQueue.complete(state)
        if (state is TargetRouteImportState.Imported) {
            onRouteImported(state.route)
        }
    }
    val routePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            routeImportQueue = routeImportQueue.start("selected-route.gpx")
            importScope.launch {
                val state = withContext(Dispatchers.IO) {
                    context.importRouteFromUri(uri)
                }
                applyImportState(state)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Trail coach",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        TrailMatePanel(
            title = "Temporary profile",
            value = profile.initialConfidence().name,
            caption = profile.explanation(),
            tone = TrailMatePanelTone.Secondary
        )
        TrailMateMetricRow(
            items = listOf(
                "Exercise" to profile.exerciseFrequency.homeLabel(),
                "Outdoors" to profile.experienceLevel.homeLabel(),
                "Body" to profile.bodyMetricsLabel()
            )
        )
        TrailMateMetricRow(
            items = listOf(
                "Ascent" to profile.ascentExperience.homeLabel(),
                "Pack" to profile.packWeightLabel(),
                "Evidence" to "0/3 GPX"
            )
        )
        TrailMateMetricRow(
            items = listOf(
                "Distance" to (importedRoute?.let { String.format(java.util.Locale.US, "%.1f km", it.distanceKm) } ?: "--"),
                "Ascent" to (importedRoute?.let { "+${it.ascentMeters} m" } ?: "--"),
                "ETA" to (routeAssessment?.estimatedDurationRange?.substringBefore("-") ?: "--")
            )
        )
        TrailMateSegmentedControl(
            labels = HomeSection.entries.map { it.label },
            selected = selectedSection.label,
            onSelected = { label ->
                selectedSection = HomeSection.entries.first { it.label == label }
            }
        )
        when (selectedSection) {
            HomeSection.Route -> {
                RouteImportPanel(
                    importedRoute = importedRoute,
                    importSummary = routeImportQueue.summary(),
                    isImporting = routeImportQueue.isImporting,
                    canRetry = routeImportQueue.canRetry,
                    onPickRouteFile = {
                        routePicker.launch(GPX_MIME_TYPES)
                    },
                    onImportSampleRoute = {
                        routeImportQueue = routeImportQueue.start("longjing-ridge-target.gpx")
                        applyImportState(TargetRouteImporter.importText(
                            fileName = "longjing-ridge-target.gpx",
                            content = TrailMateSampleData.sampleTargetGpx
                        ))
                    }
                )
                if (importedRoute?.readyForAssessment() == true && routeAssessment != null) {
                    RouteDetailScreen(
                        route = importedRoute,
                        profile = profile,
                        inventory = inventory,
                        gearRecommendations = routeGearRecommendations,
                        onAddGearRequested = { category ->
                            requestedGearCategory = category
                            selectedSection = HomeSection.MyGear
                        }
                    )
                }
            }

            HomeSection.MyGear -> MyGearScreen(
                inventory = inventory,
                routeGearRecommendations = routeGearRecommendations,
                requestedCategory = requestedGearCategory,
                onAddBrandGear = { category, brand, model, weightGrams ->
                    val updatedInventory = inventory.addBrandGear(
                        category = category,
                        brand = brand,
                        model = model,
                        weightGrams = weightGrams
                    )
                    inventory = updatedInventory
                    onInventoryChanged(updatedInventory)
                },
                onSetAvailability = { itemId, available ->
                    val updatedInventory = inventory.setAvailability(itemId = itemId, available = available)
                    inventory = updatedInventory
                    onInventoryChanged(updatedInventory)
                },
                onDeleteGear = { itemId ->
                    val updatedInventory = inventory.remove(itemId)
                    inventory = updatedInventory
                    onInventoryChanged(updatedInventory)
                }
            )

            HomeSection.Data -> DataControlPanel(
                summary = dataControlSummary,
                onClearLocalData = onClearLocalData
            )
        }
    }
}

private enum class HomeSection(val label: String) {
    Route("Route"),
    MyGear("My Gear"),
    Data("Data")
}

@Composable
private fun DataControlPanel(
    summary: TrailMateDataControlSummary,
    onClearLocalData: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TrailMatePanel(
            title = "Local data",
            value = summary.profileLine,
            caption = "Stored on this device for the prototype.",
            tone = TrailMatePanelTone.Neutral
        )
        TrailMatePanel(
            title = "Route data",
            value = summary.routeLine,
            caption = "Last imported target route.",
            tone = TrailMatePanelTone.Neutral
        )
        TrailMatePanel(
            title = "Gear data",
            value = summary.inventoryLine,
            caption = "Current personal gear inventory.",
            tone = TrailMatePanelTone.Neutral
        )
        TrailMatePanel(
            title = "Export preview",
            value = "Ready",
            caption = summary.exportPreview,
            tone = TrailMatePanelTone.Primary
        )
        OutlinedButton(
            onClick = onClearLocalData,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear local data")
        }
    }
}

@Composable
private fun RouteImportPanel(
    importedRoute: ImportedRoute?,
    importSummary: TargetRouteImportQueueSummary,
    isImporting: Boolean,
    canRetry: Boolean,
    onPickRouteFile: () -> Unit,
    onImportSampleRoute: () -> Unit
) {
    if (importedRoute == null) {
        TrailMatePanel(
            title = "Target route",
            value = "Import GPX",
            caption = "Pick a target route before viewing assessment, light navigation, plan, and gear checks.",
            tone = TrailMatePanelTone.Primary
        )
        ImportQueuePanel(importSummary = importSummary, canRetry = canRetry)
        Button(
            onClick = onPickRouteFile,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImporting
        ) {
            Text(if (isImporting) "Importing GPX" else "Choose GPX file")
        }
        OutlinedButton(
            onClick = onImportSampleRoute,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImporting
        ) {
            Text("Use sample GPX")
        }
        if (canRetry) {
            OutlinedButton(
                onClick = onPickRouteFile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting
            ) {
                Text("Retry GPX import")
            }
        }
    } else {
        TrailMatePanel(
            title = "Imported GPX",
            value = importedRoute.routeName,
            caption = "${importedRoute.fileName} / ${importedRoute.summaryLabel()} / ${importedRoute.pointCount} points",
            tone = TrailMatePanelTone.Primary
        )
        ImportQueuePanel(importSummary = importSummary, canRetry = canRetry)
        Button(
            onClick = onPickRouteFile,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImporting
        ) {
            Text(if (isImporting) "Importing GPX" else "Replace GPX file")
        }
        if (canRetry) {
            OutlinedButton(
                onClick = onPickRouteFile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting
            ) {
                Text("Retry GPX import")
            }
        }
    }
}

@Composable
private fun ImportQueuePanel(
    importSummary: TargetRouteImportQueueSummary,
    canRetry: Boolean
) {
    TrailMatePanel(
        title = "Import queue",
        value = importSummary.value,
        caption = importSummary.caption,
        tone = if (canRetry) TrailMatePanelTone.Secondary else TrailMatePanelTone.Neutral
    )
}

private fun ExerciseFrequency.homeLabel(): String =
    when (this) {
        ExerciseFrequency.RARELY -> "Rarely"
        ExerciseFrequency.ONE_TO_TWO_PER_WEEK -> "1-2/wk"
        ExerciseFrequency.THREE_PLUS_PER_WEEK -> "3+/wk"
    }

private fun ExperienceLevel.homeLabel(): String =
    when (this) {
        ExperienceLevel.BEGINNER -> "Beginner"
        ExperienceLevel.REGULAR -> "Regular"
        ExperienceLevel.EXPERIENCED -> "Experienced"
    }

private fun AscentExperience.homeLabel(): String =
    when (this) {
        AscentExperience.UNDER_300 -> "<300m"
        AscentExperience.M300_TO_800 -> "300-800m"
        AscentExperience.OVER_800 -> "800m+"
    }

@Suppress("UNCHECKED_CAST")
private val GearInventoryStateSaver = mapSaver(
    save = { inventory ->
        mapOf(
            "ids" to inventory.items.map { it.id },
            "categories" to inventory.items.map { it.category },
            "brands" to inventory.items.map { it.brand.orEmpty() },
            "models" to inventory.items.map { it.model.orEmpty() },
            "weights" to inventory.items.map { it.weightGrams ?: -1 },
            "availability" to inventory.items.map { it.available }
        )
    },
    restore = { saved ->
        val ids = saved["ids"] as List<String>
        val categories = saved["categories"] as List<String>
        val brands = saved["brands"] as List<String>
        val models = saved["models"] as List<String>
        val weights = saved["weights"] as List<Int>
        val availability = saved["availability"] as List<Boolean>

        GearInventory(
            items = ids.indices.map { index ->
                GearItem(
                    id = ids[index],
                    category = categories[index],
                    brand = brands[index].ifBlank { null },
                    model = models[index].ifBlank { null },
                    weightGrams = weights[index].takeIf { it >= 0 },
                    available = availability[index]
                )
            }
        )
    }
)

@Suppress("UNCHECKED_CAST")
private val TargetRouteImportQueueStateSaver = mapSaver(
    save = { queue ->
        val route = queue.lastImportedRoute
        mapOf(
            "routeName" to route?.routeName.orEmpty(),
            "fileName" to route?.fileName.orEmpty(),
            "distanceKm" to (route?.distanceKm ?: -1.0),
            "ascentMeters" to (route?.ascentMeters ?: -1),
            "pointCount" to (route?.pointCount ?: -1),
            "failedFileName" to queue.failedFileName.orEmpty(),
            "failureMessage" to queue.failureMessage.orEmpty()
        )
    },
    restore = { saved ->
        val routeName = saved["routeName"] as String
        val route = if (routeName.isBlank()) {
            null
        } else {
            ImportedRoute(
                routeName = routeName,
                fileName = saved["fileName"] as String,
                distanceKm = saved["distanceKm"] as Double,
                ascentMeters = saved["ascentMeters"] as Int,
                status = RouteImportStatus.PARSED,
                pointCount = saved["pointCount"] as Int
            )
        }

        TargetRouteImportQueueState(
            lastImportedRoute = route,
            failedFileName = (saved["failedFileName"] as String).ifBlank { null },
            failureMessage = (saved["failureMessage"] as String).ifBlank { null }
        )
    }
)

private fun Context.importRouteFromUri(uri: Uri): TargetRouteImportState {
    val fileName = runCatching { displayNameFor(uri) }.getOrDefault("selected-route.gpx")
    val content = runCatching {
        contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
    }.getOrElse { error ->
        return TargetRouteImportState.Failed(
            fileName = fileName,
            message = error.message ?: "Unable to open selected GPX file."
        )
    } ?: return TargetRouteImportState.Failed(
        fileName = fileName,
        message = "Unable to open selected GPX file."
    )

    return TargetRouteImporter.importText(fileName = fileName, content = content)
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
