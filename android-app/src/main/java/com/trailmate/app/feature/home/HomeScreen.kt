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
import com.trailmate.app.core.gpx.TargetRouteImporter
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.RouteImportStatus
import com.trailmate.app.core.model.RouteAssessmentEngine
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
    onRouteImported: (ImportedRoute) -> Unit = {}
) {
    val context = LocalContext.current
    val importScope = rememberCoroutineScope()
    var selectedSection by rememberSaveable { mutableStateOf(HomeSection.Route) }
    var requestedGearCategory by rememberSaveable { mutableStateOf("Trekking poles") }
    var routeImported by rememberSaveable { mutableStateOf(initialImportedRoute != null) }
    var routeImporting by remember { mutableStateOf(false) }
    var routeImportError by rememberSaveable { mutableStateOf<String?>(null) }
    var routeName by rememberSaveable { mutableStateOf(initialImportedRoute?.routeName.orEmpty()) }
    var routeFileName by rememberSaveable { mutableStateOf(initialImportedRoute?.fileName.orEmpty()) }
    var routeDistanceKm by rememberSaveable { mutableStateOf(initialImportedRoute?.distanceKm ?: 0.0) }
    var routeAscentMeters by rememberSaveable { mutableStateOf(initialImportedRoute?.ascentMeters ?: 0) }
    var routePointCount by rememberSaveable { mutableStateOf(initialImportedRoute?.pointCount ?: 0) }
    var inventory by rememberSaveable(stateSaver = GearInventoryStateSaver) {
        mutableStateOf(initialInventory)
    }
    val importedRoute = if (routeImported) {
        ImportedRoute(
            routeName = routeName,
            fileName = routeFileName,
            distanceKm = routeDistanceKm,
            ascentMeters = routeAscentMeters,
            status = RouteImportStatus.PARSED,
            pointCount = routePointCount
        )
    } else {
        null
    }
    val routeAssessment = importedRoute?.takeIf { it.readyForAssessment() }?.let { route ->
        RouteAssessmentEngine.assess(profile = profile, route = route)
    }
    val routeGearRecommendations = if (importedRoute?.readyForAssessment() == true) {
        inventory.applyTo(TrailMateSampleData.gearRecommendations)
    } else {
        emptyList()
    }
    val applyImportState: (TargetRouteImportState) -> Unit = { state ->
        when (state) {
            TargetRouteImportState.Empty -> Unit
            is TargetRouteImportState.Imported -> {
                routeName = state.route.routeName
                routeFileName = state.route.fileName
                routeDistanceKm = state.route.distanceKm
                routeAscentMeters = state.route.ascentMeters
                routePointCount = state.route.pointCount
                routeImported = true
                routeImportError = null
                onRouteImported(state.route)
            }
            is TargetRouteImportState.Failed -> {
                routeImportError = "${state.fileName}: ${state.message}"
            }
        }
    }
    val routePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            routeImporting = true
            routeImportError = null
            importScope.launch {
                val state = withContext(Dispatchers.IO) {
                    context.importRouteFromUri(uri)
                }
                applyImportState(state)
                routeImporting = false
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
                    isImporting = routeImporting,
                    importError = routeImportError,
                    onPickRouteFile = {
                        routePicker.launch(GPX_MIME_TYPES)
                    },
                    onImportSampleRoute = {
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
        }
    }
}

private enum class HomeSection(val label: String) {
    Route("Route"),
    MyGear("My Gear")
}

@Composable
private fun RouteImportPanel(
    importedRoute: ImportedRoute?,
    isImporting: Boolean,
    importError: String?,
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
        if (importError != null) {
            TrailMatePanel(
                title = "Import issue",
                value = "Check GPX",
                caption = importError,
                tone = TrailMatePanelTone.Secondary
            )
        }
    } else {
        TrailMatePanel(
            title = "Imported GPX",
            value = importedRoute.routeName,
            caption = "${importedRoute.fileName} / ${importedRoute.summaryLabel()} / ${importedRoute.pointCount} points",
            tone = TrailMatePanelTone.Primary
        )
        Button(
            onClick = onPickRouteFile,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isImporting
        ) {
            Text(if (isImporting) "Importing GPX" else "Replace GPX file")
        }
        if (importError != null) {
            TrailMatePanel(
                title = "Import issue",
                value = "Keeping current route",
                caption = importError,
                tone = TrailMatePanelTone.Secondary
            )
        }
    }
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
