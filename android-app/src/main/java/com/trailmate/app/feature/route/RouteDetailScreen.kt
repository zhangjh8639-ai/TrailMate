package com.trailmate.app.feature.route

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import com.trailmate.app.core.model.AiGearAdvisorContract
import com.trailmate.app.core.model.AiGearAdvisorPresentation
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanCheckpointType
import com.trailmate.app.core.model.HikePlanEngine
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.HikeSessionEngine
import com.trailmate.app.core.model.HikeSessionState
import com.trailmate.app.core.model.HikeSessionStatus
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
    routeAssessment: RouteAssessmentSummary? = null,
    gearRecommendations: List<GearRecommendation>? = null,
    aiGearAdvisorResponse: AiGearAdvisorResponse? = null,
    onAddGearRequested: (String) -> Unit = {}
) {
    val assessment = routeAssessment ?: RouteAssessmentEngine.assess(profile = profile, route = route)
    val plan = HikePlanEngine.build(route = route, assessment = assessment)
    val fallbackGearRecommendations = gearRecommendations ?: inventory.applyTo(
        RouteGearAdvisorEngine.recommend(route = route, assessment = assessment)
    )
    val aiGearAdvisorRequest = AiGearAdvisorContract.buildRequest(
        route = route,
        profile = profile,
        assessment = assessment,
        inventory = inventory,
        fallbackRecommendations = fallbackGearRecommendations
    )
    val aiGearAdvisorPresentation = AiGearAdvisorContract.resolvePresentation(
        request = aiGearAdvisorRequest,
        response = aiGearAdvisorResponse
    )
    val routeSessionKey = route.sessionKey()
    var selected by rememberSaveable { mutableStateOf(RouteDetailTab.Assessment) }
    var hikeStatus by rememberSaveable(routeSessionKey) { mutableStateOf(HikeSessionStatus.READY) }
    var reachedCheckpointIndex by rememberSaveable(routeSessionKey) { mutableStateOf(0) }
    val hikeSession = HikeSessionState(
        status = hikeStatus,
        reachedCheckpointIndex = reachedCheckpointIndex.coerceIn(0, plan.checkpoints.lastIndex.coerceAtLeast(0))
    )
    val updateHikeSession: (HikeSessionState) -> Unit = { session ->
        hikeStatus = session.status
        reachedCheckpointIndex = session.reachedCheckpointIndex
    }

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
            RouteDetailTab.Route -> RouteTab(
                assessment = assessment,
                plan = plan,
                hikeSession = hikeSession,
                onHikeSessionChange = updateHikeSession
            )
            RouteDetailTab.Plan -> PlanTab(plan = plan)
            RouteDetailTab.Gear -> GearTab(
                recommendations = aiGearAdvisorPresentation.recommendations,
                inventory = inventory,
                aiGearAdvisorPresentation = aiGearAdvisorPresentation,
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
private fun RouteTab(
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    hikeSession: HikeSessionState,
    onHikeSessionChange: (HikeSessionState) -> Unit
) {
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
        ActiveHikePanel(
            plan = plan,
            session = hikeSession,
            onSessionChange = onHikeSessionChange
        )
    }
}

@Composable
private fun ActiveHikePanel(
    plan: HikePlanSummary,
    session: HikeSessionState,
    onSessionChange: (HikeSessionState) -> Unit
) {
    val current = HikeSessionEngine.currentCheckpoint(plan, session)
    val next = HikeSessionEngine.nextCheckpoint(plan, session)
    val progress = HikeSessionEngine.progressFraction(plan, session).toFloat()
    val nextLabel = next?.title ?: "Finish reached"

    TrailMatePanel(
        title = "Active hike",
        value = nextLabel,
        caption = "Current ${current?.title.orEmpty().ifBlank { "Route" }} / ${session.status.displayLabel()}",
        tone = TrailMatePanelTone.Neutral
    )
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth()
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (session.status) {
            HikeSessionStatus.READY -> Button(
                onClick = { onSessionChange(HikeSessionEngine.start(session)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Start hike")
            }

            HikeSessionStatus.ACTIVE -> OutlinedButton(
                onClick = { onSessionChange(HikeSessionEngine.pause(session)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Pause")
            }

            HikeSessionStatus.PAUSED -> Button(
                onClick = { onSessionChange(HikeSessionEngine.resume(session)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Resume")
            }

            HikeSessionStatus.COMPLETED -> OutlinedButton(
                onClick = { onSessionChange(HikeSessionEngine.ready(plan)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
        }
        OutlinedButton(
            onClick = { onSessionChange(HikeSessionEngine.advance(plan, session)) },
            modifier = Modifier.weight(1f),
            enabled = session.status != HikeSessionStatus.READY && session.status != HikeSessionStatus.COMPLETED
        ) {
            Text("Mark next checkpoint")
        }
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
    aiGearAdvisorPresentation: AiGearAdvisorPresentation,
    onAddGearRequested: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TrailMatePanel(
            title = "AI advisor",
            value = aiGearAdvisorPresentation.statusLabel,
            caption = aiGearAdvisorPresentation.caption,
            tone = when {
                aiGearAdvisorPresentation.isStaleResponse -> TrailMatePanelTone.Secondary
                aiGearAdvisorPresentation.isFallbackActive -> TrailMatePanelTone.Neutral
                else -> TrailMatePanelTone.Primary
            }
        )
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

private fun HikeSessionStatus.displayLabel(): String =
    when (this) {
        HikeSessionStatus.READY -> "ready"
        HikeSessionStatus.ACTIVE -> "active"
        HikeSessionStatus.PAUSED -> "paused"
        HikeSessionStatus.COMPLETED -> "completed"
    }

private fun ImportedRoute.sessionKey(): String =
    "$fileName|$routeName|$distanceKm|$ascentMeters|$pointCount"

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
