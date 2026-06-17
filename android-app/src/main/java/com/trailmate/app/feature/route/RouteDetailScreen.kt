package com.trailmate.app.feature.route

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.TrailMateSampleData

@Composable
fun RouteDetailScreen() {
    var selected by rememberSaveable { mutableStateOf("Assessment") }
    val assessment = TrailMateSampleData.routeAssessment

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = assessment.routeName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        TrailMateSegmentedControl(
            labels = listOf("Assessment", "Route", "Plan", "Gear"),
            selected = selected,
            onSelected = { selected = it }
        )
        when (selected) {
            "Assessment" -> AssessmentTab()
            "Route" -> RouteTab()
            "Plan" -> PlanTab()
            "Gear" -> GearTab()
        }
    }
}

@Composable
private fun AssessmentTab() {
    val assessment = TrailMateSampleData.routeAssessment

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TrailMatePanel(
            title = "Cautious attempt",
            value = assessment.estimatedDurationRange,
            caption = "${assessment.distanceKm} km / +${assessment.ascentMeters} m / confidence ${assessment.confidenceLevel}"
        )
        TrailMateMetricRow(
            items = listOf(
                "Match" to assessment.matchLevel.name,
                "Risk flags" to assessment.risks.size.toString(),
                "Checkpoints" to "4"
            )
        )
        assessment.risks.forEach { risk ->
            TrailMatePanel(
                title = "Risk",
                value = risk,
                caption = "Keep route plan deterministic; AI gear suggestions do not change this assessment.",
                tone = TrailMatePanelTone.Neutral
            )
        }
    }
}

@Composable
private fun RouteTab() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .padding(20.dp)
        ) {
            RouteSketch()
        }
        TrailMatePanel(
            title = "Next checkpoint",
            value = "1.8 km",
            caption = "Expected 38-46 min. Fuel check before the long climb.",
            tone = TrailMatePanelTone.Primary
        )
    }
}

@Composable
private fun PlanTab() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TrailMatePanel(
            title = "Start window",
            value = "08:45",
            caption = "Offline route saved, weather check needed before departure."
        )
        TrailMatePanel(
            title = "Plan checkpoints",
            value = "4 stops",
            caption = "Fuel check, risk start, rest check, descend before dusk.",
            tone = TrailMatePanelTone.Neutral
        )
    }
}

@Composable
private fun GearTab() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TrailMateSampleData.gearRecommendations.forEach { item ->
            TrailMatePanel(
                title = item.status.name.lowercase().replaceFirstChar { it.titlecase() },
                value = item.category,
                caption = item.rationale,
                tone = when (item.status) {
                    GearStatus.MISSING -> TrailMatePanelTone.Secondary
                    GearStatus.CHECK -> TrailMatePanelTone.Neutral
                    else -> TrailMatePanelTone.Primary
                }
            )
        }
    }
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
