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
import androidx.compose.material3.OutlinedButton
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
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanCheckpointType
import com.trailmate.app.core.model.HikePlanEngine
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.MatchLevel
import com.trailmate.app.core.model.RouteAssessmentEngine
import com.trailmate.app.core.model.RouteAssessmentSummary
import com.trailmate.app.core.model.RouteGearAdvisorEngine
import com.trailmate.app.core.model.TrailMateSampleData

@Composable
fun RouteDetailScreen(
    route: ImportedRoute = TrailMateSampleData.importedTargetRoute,
    profile: BaselineProfile = TrailMateSampleData.baselineProfile,
    inventory: GearInventory = GearInventory(TrailMateSampleData.gearItems),
    gearRecommendations: List<GearRecommendation>? = null,
    onAddGearRequested: (String) -> Unit = {}
) {
    val assessment = RouteAssessmentEngine.assess(profile = profile, route = route)
    val plan = HikePlanEngine.build(route = route, assessment = assessment)
    val resolvedGearRecommendations = gearRecommendations ?: inventory.applyTo(
        RouteGearAdvisorEngine.recommend(route = route, assessment = assessment)
    )
    var selected by rememberSaveable { mutableStateOf(RouteDetailTab.Assessment) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = assessment.routeName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        TrailMateSegmentedControl(
            labels = RouteDetailTab.entries.map { it.label },
            selected = selected.label,
            onSelected = { label ->
                selected = RouteDetailTab.entries.first { it.label == label }
            }
        )
        when (selected) {
            RouteDetailTab.Assessment -> AssessmentTab(assessment = assessment, plan = plan)
            RouteDetailTab.Route -> RouteTab(assessment = assessment, plan = plan)
            RouteDetailTab.Plan -> PlanTab(plan = plan)
            RouteDetailTab.Gear -> GearTab(
                recommendations = resolvedGearRecommendations,
                inventory = inventory,
                onAddGearRequested = onAddGearRequested
            )
        }
    }
}

private enum class RouteDetailTab(val label: String) {
    Assessment("Assessment"),
    Route("Route"),
    Plan("Plan"),
    Gear("Gear")
}

@Composable
private fun AssessmentTab(assessment: RouteAssessmentSummary, plan: HikePlanSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TrailMatePanel(
            title = assessment.matchLevel.displayTitle(),
            value = assessment.estimatedDurationRange,
            caption = "${assessment.distanceKm} km / +${assessment.ascentMeters} m / confidence ${assessment.confidenceLevel}"
        )
        TrailMateMetricRow(
            items = listOf(
                "Match" to assessment.matchLevel.name,
                "Risk flags" to assessment.risks.size.toString(),
                "Checkpoints" to plan.checkpointCount.toString()
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
private fun RouteTab(assessment: RouteAssessmentSummary, plan: HikePlanSummary) {
    val nextCheckpoint = plan.nextMovingCheckpoint()

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
            value = nextCheckpoint?.routeValue().orEmpty().ifBlank { assessment.estimatedDurationRange },
            caption = nextCheckpoint?.let { checkpoint ->
                "${checkpoint.title}: ${checkpoint.note}"
            } ?: "Assessment window ${assessment.estimatedDurationRange}.",
            tone = TrailMatePanelTone.Primary
        )
    }
}

@Composable
private fun PlanTab(plan: HikePlanSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        plan.checkpoints.forEach { checkpoint ->
            TrailMatePanel(
                title = checkpoint.title,
                value = checkpoint.routeValue(),
                caption = checkpoint.note,
                tone = checkpoint.panelTone()
            )
        }
    }
}

@Composable
private fun GearTab(
    recommendations: List<GearRecommendation>,
    inventory: GearInventory,
    onAddGearRequested: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        recommendations.forEach { item ->
            val matchedItem = item.matchedGearItemId?.let { matchedId ->
                inventory.items.firstOrNull { it.id == matchedId }
            }
            TrailMatePanel(
                title = item.status.name.lowercase().replaceFirstChar { it.titlecase() },
                value = item.category,
                caption = gearRecommendationCaption(item, matchedItem),
                tone = when (item.status) {
                    GearStatus.MISSING -> TrailMatePanelTone.Secondary
                    GearStatus.CHECK -> TrailMatePanelTone.Neutral
                    else -> TrailMatePanelTone.Primary
                }
            )
            if (item.status == GearStatus.MISSING) {
                OutlinedButton(
                    onClick = { onAddGearRequested(item.category) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add ${item.category} to My Gear")
                }
            }
        }
    }
}

private fun gearRecommendationCaption(
    recommendation: GearRecommendation,
    matchedItem: GearItem?
): String {
    val matchedText = matchedItem?.let { item ->
        val name = listOfNotNull(item.brand, item.model).joinToString(" ")
        " Matched with ${name.ifBlank { item.category }}."
    }.orEmpty()

    return recommendation.rationale + matchedText
}

private fun MatchLevel.displayTitle(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "Recommended"
        MatchLevel.CAUTION -> "Cautious attempt"
        MatchLevel.NOT_RECOMMENDED -> "Not recommended"
    }

private fun HikePlanCheckpoint.routeValue(): String =
    "${distanceKm} km / $timeFromStart"

private fun HikePlanCheckpoint.panelTone(): TrailMatePanelTone =
    when (type) {
        HikePlanCheckpointType.RISK_CHECK -> TrailMatePanelTone.Secondary
        HikePlanCheckpointType.FINISH -> TrailMatePanelTone.Primary
        else -> TrailMatePanelTone.Neutral
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
