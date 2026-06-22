package com.trailmate.app.feature.route.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateClay
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateIconBadge
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMateMoss
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.ConfidenceLevel
import com.trailmate.app.core.model.HikePlanSummary
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.MatchLevel
import com.trailmate.app.core.model.RouteAssessmentSummary
import java.util.Locale

@Composable
internal fun RouteAssessmentTab(
    route: ImportedRoute,
    assessment: RouteAssessmentSummary,
    plan: HikePlanSummary,
    onOpenRoute: () -> Unit,
    onOpenGear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("route-assessment-decision"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AssessmentDecisionCard(
            assessment = assessment,
            onOpenGear = onOpenGear,
            onOpenRoute = onOpenRoute
        )
        TrailMateMetricRow(
            items = listOf(
                "距离" to String.format(Locale.US, "%.1f km", route.distanceKm),
                "累计爬升" to "+${route.ascentMeters} m",
                "预计用时" to assessment.estimatedDurationRange
            )
        )
        RiskSummaryCard(assessment = assessment)
        NextPlanCard(plan = plan, onOpenRoute = onOpenRoute)
    }
}

@Composable
private fun AssessmentDecisionCard(
    assessment: RouteAssessmentSummary,
    onOpenGear: () -> Unit,
    onOpenRoute: () -> Unit
) {
    val colors = assessment.matchLevel.assessmentColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.container,
        border = BorderStroke(1.dp, colors.content.copy(alpha = 0.24f)),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrailMateIconBadge(
                    glyph = assessment.matchLevel.assessmentGlyph(),
                    containerColor = colors.content,
                    modifier = Modifier.size(58.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "路线评估",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.content,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = assessment.matchLevel.assessmentTitle(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = assessment.matchLevel.assessmentCaption(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TrailMateStatusPill(
                    text = assessment.confidenceLevel.confidenceLabel(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    contentColor = colors.content
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenGear,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TrailMateMoss)
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Gear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("检查装备")
                }
                OutlinedButton(
                    onClick = onOpenRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("进入路线")
                }
            }
        }
    }
}

@Composable
private fun RiskSummaryCard(assessment: RouteAssessmentSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)),
        shadowElevation = 1.dp
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
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "关键风险",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "仅展示最影响决策的因素。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TrailMateStatusPill(
                    text = "${assessment.risks.take(3).size} 项",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                assessment.risks.take(3).ifEmpty { listOf("暂无明显风险，仍需结合天气和现场路况判断。") }
                    .forEach { risk ->
                        RiskRow(text = risk)
                    }
            }
        }
    }
}

@Composable
private fun RiskRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3D7)),
            contentAlignment = Alignment.Center
        ) {
            TrailMateLineIcon(
                glyph = TrailMateGlyph.Warning,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = Color(0xFF8B5A00)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NextPlanCard(
    plan: HikePlanSummary,
    onOpenRoute: () -> Unit
) {
    val next = plan.nextMovingCheckpoint()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD8E2FF)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Location,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF2D75E8)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "下一步建议",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = next?.let { "${it.title} · ${it.distanceKm} km" } ?: "进入路线查看检查点",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                        text = next?.note ?: "先确认定位、离线和装备状态，再开始轻导航。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onOpenRoute) {
                Text("看路线")
            }
        }
    }
}

private data class AssessmentColors(
    val container: Color,
    val content: Color
)

private fun MatchLevel.assessmentTitle(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "推荐尝试"
        MatchLevel.CAUTION -> "谨慎尝试"
        MatchLevel.NOT_RECOMMENDED -> "不建议尝试"
    }

private fun MatchLevel.assessmentCaption(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "路线强度与当前能力较匹配，出发前仍需确认天气和装备。"
        MatchLevel.CAUTION -> "这条路线有明显压力点，建议先补齐装备和离线准备。"
        MatchLevel.NOT_RECOMMENDED -> "当前证据显示风险偏高，建议换线或降低路线强度。"
    }

private fun MatchLevel.assessmentGlyph(): TrailMateGlyph =
    when (this) {
        MatchLevel.RECOMMENDED -> TrailMateGlyph.Check
        MatchLevel.CAUTION -> TrailMateGlyph.Warning
        MatchLevel.NOT_RECOMMENDED -> TrailMateGlyph.Minus
    }

private fun MatchLevel.assessmentColors(): AssessmentColors =
    when (this) {
        MatchLevel.RECOMMENDED -> AssessmentColors(container = Color(0xFFEAF6EE), content = TrailMateMoss)
        MatchLevel.CAUTION -> AssessmentColors(container = Color(0xFFFFF7E5), content = Color(0xFFD18400))
        MatchLevel.NOT_RECOMMENDED -> AssessmentColors(container = Color(0xFFFFEEE9), content = TrailMateClay)
    }

private fun ConfidenceLevel.confidenceLabel(): String =
    when (this) {
        ConfidenceLevel.LOW -> "置信度低"
        ConfidenceLevel.MEDIUM -> "置信度中"
        ConfidenceLevel.HIGH -> "置信度高"
    }
