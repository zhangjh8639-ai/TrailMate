package com.trailmate.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.gpx.TargetRouteGpxParser
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

@Composable
fun HomeScreen(profile: BaselineProfile = TrailMateSampleData.baselineProfile) {
    var selectedSection by rememberSaveable { mutableStateOf(HomeSection.Route) }
    var requestedGearCategory by rememberSaveable { mutableStateOf("Trekking poles") }
    var routeImported by rememberSaveable { mutableStateOf(false) }
    var routeName by rememberSaveable { mutableStateOf("") }
    var routeFileName by rememberSaveable { mutableStateOf("") }
    var routeDistanceKm by rememberSaveable { mutableStateOf(0.0) }
    var routeAscentMeters by rememberSaveable { mutableStateOf(0) }
    var routePointCount by rememberSaveable { mutableStateOf(0) }
    var inventory by rememberSaveable(stateSaver = GearInventoryStateSaver) {
        mutableStateOf(GearInventory(TrailMateSampleData.gearItems))
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
                    onImportSampleRoute = {
                        val parsedRoute = TargetRouteGpxParser.parse(
                            fileName = "longjing-ridge-target.gpx",
                            content = TrailMateSampleData.sampleTargetGpx
                        )
                        routeName = parsedRoute.routeName
                        routeFileName = parsedRoute.fileName
                        routeDistanceKm = parsedRoute.distanceKm
                        routeAscentMeters = parsedRoute.ascentMeters
                        routePointCount = parsedRoute.pointCount
                        routeImported = true
                    }
                )
                if (importedRoute?.readyForAssessment() == true && routeAssessment != null) {
                    RouteDetailScreen(
                        assessment = routeAssessment,
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
                    inventory = inventory.addBrandGear(
                        category = category,
                        brand = brand,
                        model = model,
                        weightGrams = weightGrams
                    )
                },
                onSetAvailability = { itemId, available ->
                    inventory = inventory.setAvailability(itemId = itemId, available = available)
                },
                onDeleteGear = { itemId ->
                    inventory = inventory.remove(itemId)
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
            onClick = onImportSampleRoute,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import sample GPX")
        }
    } else {
        TrailMatePanel(
            title = "Imported GPX",
            value = importedRoute.routeName,
            caption = "${importedRoute.fileName} / ${importedRoute.summaryLabel()} / ${importedRoute.pointCount} points",
            tone = TrailMatePanelTone.Primary
        )
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
