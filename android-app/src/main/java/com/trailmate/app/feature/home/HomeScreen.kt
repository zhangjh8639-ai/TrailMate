package com.trailmate.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.feature.gear.MyGearScreen
import com.trailmate.app.feature.route.RouteDetailScreen

@Composable
fun HomeScreen() {
    var selectedSection by rememberSaveable { mutableStateOf(HomeSection.Route) }
    var requestedGearCategory by rememberSaveable { mutableStateOf("Trekking poles") }
    var inventory by remember { mutableStateOf(GearInventory(TrailMateSampleData.gearItems)) }
    val routeGearRecommendations = inventory.applyTo(TrailMateSampleData.gearRecommendations)

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
            value = "LOW",
            caption = "Import 3 GPX activities to calibrate with real history.",
            tone = TrailMatePanelTone.Secondary
        )
        TrailMateMetricRow(
            items = listOf(
                "Distance" to "15.2 km",
                "Ascent" to "+860 m",
                "ETA" to "6:40"
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
            HomeSection.Route -> RouteDetailScreen(
                inventory = inventory,
                gearRecommendations = routeGearRecommendations,
                onAddGearRequested = { category ->
                    requestedGearCategory = category
                    selectedSection = HomeSection.MyGear
                }
            )

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
