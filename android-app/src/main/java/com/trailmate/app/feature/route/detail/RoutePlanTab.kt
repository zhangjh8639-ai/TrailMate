package com.trailmate.app.feature.route.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.HikePlanCheckpoint
import com.trailmate.app.core.model.HikePlanCheckpointType
import com.trailmate.app.core.model.HikePlanSummary
import java.util.Locale

@Composable
internal fun RoutePlanTab(plan: HikePlanSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PlanSummaryCard(plan = plan)
        WeatherReviewCard(plan = plan)
        TimelineSectionHeader(plan = plan)
        plan.checkpoints.forEachIndexed { index, checkpoint ->
            PlanTimelineRow(
                checkpoint = checkpoint,
                index = index,
                isLast = checkpoint == plan.checkpoints.lastOrNull()
            )
        }
    }
}

@Composable
private fun PlanSummaryCard(plan: HikePlanSummary) {
    val movingCheckpoints = plan.checkpoints.filterNot { checkpoint ->
        checkpoint.type == HikePlanCheckpointType.START || checkpoint.type == HikePlanCheckpointType.FINISH
    }
    val finishTime = plan.checkpoints.lastOrNull()?.timeFromStart ?: "待估算"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)),
        shadowElevation = 2.dp
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
                        text = "行程节奏",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "补给与休息计划 · 离线也可查看",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TrailMateStatusPill(
                    text = "天气待复核",
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
            TrailMateMetricRow(
                items = listOf(
                    "预计完成" to finishTime,
                    "检查点" to "${plan.checkpointCount}",
                    "补给点" to "${movingCheckpoints.count { it.type == HikePlanCheckpointType.ENERGY_CHECK }}",
                    "风险核对" to "${movingCheckpoints.count { it.type == HikePlanCheckpointType.RISK_CHECK }}"
                )
            )
        }
    }
}

@Composable
private fun WeatherReviewCard(plan: HikePlanSummary) {
    val riskCheckpoint = plan.checkpoints.firstOrNull { checkpoint ->
        checkpoint.type == HikePlanCheckpointType.RISK_CHECK
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF8EA),
        border = BorderStroke(1.dp, Color(0xFFE8C16F).copy(alpha = 0.56f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE7AD)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Weather,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color(0xFF9A6400)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "天气与返程判断",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = riskCheckpoint?.let { checkpoint ->
                        "TrailMate 暂未接入真实天气源；到 ${checkpoint.title} 前复核天气、体感和剩余电量。"
                    } ?: "TrailMate 暂未接入真实天气源；出发当天仍需复核天气、日照和现场路况。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TrailMateStatusPill(
                text = "需复核",
                containerColor = Color(0xFFFFE7AD),
                contentColor = Color(0xFF8B5A00)
            )
        }
    }
}

@Composable
private fun TimelineSectionHeader(plan: HikePlanSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "检查点时间线",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "按距离推进，每一站只保留当天要做的动作。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TrailMateStatusPill(
            text = "${plan.checkpointCount} 点",
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanTimelineRow(
    checkpoint: HikePlanCheckpoint,
    index: Int,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TimelineMarker(
            checkpoint = checkpoint,
            isLast = isLast
        )
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${checkpoint.markerLabel(index)} · ${checkpoint.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = checkpoint.suggestionTitle(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = checkpoint.typeColor(),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TrailMateStatusPill(
                        text = checkpoint.typeLabel(),
                        containerColor = checkpoint.typeColor().copy(alpha = 0.13f),
                        contentColor = checkpoint.typeColor()
                    )
                }
                Text(
                    text = checkpoint.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrailMateStatusPill(
                        text = String.format(Locale.US, "%.1f km", checkpoint.distanceKm),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TrailMateStatusPill(
                        text = checkpoint.timeFromStart,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun HikePlanCheckpoint.markerLabel(index: Int): String =
    when (type) {
        HikePlanCheckpointType.START -> "起点"
        HikePlanCheckpointType.FINISH -> "终点"
        else -> "CP$index"
    }

@Composable
private fun TimelineMarker(
    checkpoint: HikePlanCheckpoint,
    isLast: Boolean
) {
    Column(
        modifier = Modifier.size(width = 34.dp, height = 112.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(checkpoint.typeColor()),
            contentAlignment = Alignment.Center
        ) {
            TrailMateLineIcon(
                glyph = checkpoint.typeGlyph(),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = Color.White
            )
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .size(width = 2.dp, height = 1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

private fun HikePlanCheckpoint.typeLabel(): String =
    when (type) {
        HikePlanCheckpointType.START -> "起点"
        HikePlanCheckpointType.ENERGY_CHECK -> "补给"
        HikePlanCheckpointType.REST_CHECK -> "休息"
        HikePlanCheckpointType.RISK_CHECK -> "风险"
        HikePlanCheckpointType.FINISH -> "终点"
    }

private fun HikePlanCheckpoint.suggestionTitle(): String =
    when (type) {
        HikePlanCheckpointType.START -> "建议出发前确认定位、装备和离线路线。"
        HikePlanCheckpointType.ENERGY_CHECK -> "建议补水和补能量。"
        HikePlanCheckpointType.REST_CHECK -> "建议短休并检查体感。"
        HikePlanCheckpointType.RISK_CHECK -> "建议停下核对天气、路况和方向。"
        HikePlanCheckpointType.FINISH -> "建议结束记录并保存复盘。"
    }

private fun HikePlanCheckpoint.typeGlyph(): TrailMateGlyph =
    when (type) {
        HikePlanCheckpointType.START -> TrailMateGlyph.Route
        HikePlanCheckpointType.ENERGY_CHECK -> TrailMateGlyph.Spark
        HikePlanCheckpointType.REST_CHECK -> TrailMateGlyph.Location
        HikePlanCheckpointType.RISK_CHECK -> TrailMateGlyph.Warning
        HikePlanCheckpointType.FINISH -> TrailMateGlyph.Check
    }

private fun HikePlanCheckpoint.typeColor(): Color =
    when (type) {
        HikePlanCheckpointType.START -> Color(0xFF2E7D32)
        HikePlanCheckpointType.ENERGY_CHECK -> Color(0xFFE07A1F)
        HikePlanCheckpointType.REST_CHECK -> Color(0xFF2D75E8)
        HikePlanCheckpointType.RISK_CHECK -> Color(0xFFD64945)
        HikePlanCheckpointType.FINISH -> Color(0xFF0C5D3F)
    }
