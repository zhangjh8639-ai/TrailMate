package com.trailmate.app.feature.data

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMatePageScaffold
import com.trailmate.app.core.design.TrailMateSectionHeader
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.gpx.HistoricalActivityImportUiState
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TrackRecordingStatus
import java.util.Locale

@Composable
fun DataScreen(
    latestTrackRecording: TrackRecordingState,
    historicalActivities: List<HistoricalActivity>,
    historyImportUiState: HistoricalActivityImportUiState,
    onPickHistoryGpx: () -> Unit,
    onOpenRouteTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrailMatePageScaffold(
        title = "数据",
        caption = "复盘实际徒步表现，沉淀为下一次路线评估的背景。",
        modifier = modifier
    ) {
        if (latestTrackRecording.pointCount == 0) {
            TrackReviewPromptCard(
                onAction = onOpenRouteTab
            )
        } else {
            LatestTrackReviewCard(latestTrackRecording = latestTrackRecording)
        }

        TrailMateSectionHeader(
            title = "历史活动",
            action = if (historicalActivities.isEmpty()) null else "${historicalActivities.size} 条"
        )
        if (historicalActivities.isEmpty()) {
            EmptyActivityTrendCard()
        } else {
            ActivityTrendSummary(historicalActivities = historicalActivities)
        }

        TrailMateSectionHeader(
            title = "历史资料",
            action = if (historyImportUiState.isImporting) historyImportUiState.value else "已导入 ${historicalActivities.size} 条"
        )
        HistoricalEvidenceCard(
            historyImportUiState = historyImportUiState,
            onPickHistoryGpx = onPickHistoryGpx
        )
    }
}

@Composable
private fun TrackReviewPromptCard(onAction: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.11f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Route,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "完成一次记录后会出现复盘",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "记录结束后查看距离与轨迹表现。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                modifier = Modifier.heightIn(min = 46.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
                onClick = onAction
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "去路线页",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LatestTrackReviewCard(latestTrackRecording: TrackRecordingState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                TrackReviewCanvas()
                TrailMateStatusPill(
                    text = "本次活动复盘",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = latestTrackRecording.displayName(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = latestTrackRecording.activitySummaryLine(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Chart,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                TrackMetricStrip(latestTrackRecording = latestTrackRecording)
            }
        }
    }
}

@Composable
private fun TrackMetricStrip(latestTrackRecording: TrackRecordingState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrackMetricTile(
            label = "距离",
            value = String.format(Locale.US, "%.1fkm", latestTrackRecording.totalDistanceKm),
            modifier = Modifier.weight(1f)
        )
        TrackMetricTile(
            label = "轨迹点",
            value = "${latestTrackRecording.pointCount}",
            modifier = Modifier.weight(1f)
        )
        TrackMetricTile(
            label = "状态",
            value = latestTrackRecording.statusLabel(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TrackMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyActivityTrendCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrailMateLineIcon(
                glyph = TrailMateGlyph.Chart,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "活动趋势",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "暂无记录",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "完成活动后显示距离、爬升和时长变化。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ActivityTrendSummary(historicalActivities: List<HistoricalActivity>) {
    val totalDistance = historicalActivities.sumOf { it.distanceKm }
    val totalAscent = historicalActivities.sumOf { it.ascentMeters }
    val totalMinutes = historicalActivities.sumOf { it.durationMinutes }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "活动趋势",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrackMetricTile(
                    label = "累计距离",
                    value = String.format(Locale.US, "%.1fkm", totalDistance),
                    modifier = Modifier.weight(1f)
                )
                TrackMetricTile(
                    label = "累计爬升",
                    value = "+${totalAscent}m",
                    modifier = Modifier.weight(1f)
                )
                TrackMetricTile(
                    label = "户外时长",
                    value = totalMinutes.durationLabel(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HistoricalEvidenceCard(
    historyImportUiState: HistoricalActivityImportUiState,
    onPickHistoryGpx: () -> Unit
) {
    val isImporting = historyImportUiState.isImporting

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "历史 GPX",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isImporting) historyImportUiState.value else "可导入",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "用于下次路线评估的能力背景。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                modifier = Modifier.heightIn(min = 46.dp),
                shape = RoundedCornerShape(999.dp),
                color = if (isImporting) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
                onClick = onPickHistoryGpx,
                enabled = !isImporting
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isImporting) "正在导入历史 GPX" else "导入历史 GPX",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isImporting) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackReviewCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
    ) {
        drawRect(
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFFE9F1E5),
                    Color(0xFFF7F6EF),
                    Color(0xFFDCEAF4)
                )
            )
        )
        repeat(7) { index ->
            val y = size.height * (0.16f + index * 0.11f)
            drawLine(
                color = Color(0xFFBBD5C3).copy(alpha = 0.38f),
                start = Offset(size.width * -0.08f, y),
                end = Offset(size.width * 1.08f, y + if (index % 2 == 0) 20f else -16f),
                strokeWidth = 1.1.dp.toPx()
            )
        }
        val planned = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.74f)
            cubicTo(size.width * 0.30f, size.height * 0.55f, size.width * 0.42f, size.height * 0.72f, size.width * 0.52f, size.height * 0.45f)
            cubicTo(size.width * 0.62f, size.height * 0.22f, size.width * 0.74f, size.height * 0.42f, size.width * 0.86f, size.height * 0.24f)
        }
        val recorded = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.74f)
            cubicTo(size.width * 0.28f, size.height * 0.60f, size.width * 0.39f, size.height * 0.66f, size.width * 0.50f, size.height * 0.50f)
            cubicTo(size.width * 0.62f, size.height * 0.32f, size.width * 0.70f, size.height * 0.44f, size.width * 0.82f, size.height * 0.30f)
        }
        drawPath(planned, color = Color.White, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
        drawPath(planned, color = Color(0xFF2D75E8), style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round))
        drawPath(recorded, color = Color.White, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
        drawPath(recorded, color = Color(0xFFE36F43), style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
        listOf(
            Offset(size.width * 0.14f, size.height * 0.74f),
            Offset(size.width * 0.50f, size.height * 0.50f),
            Offset(size.width * 0.82f, size.height * 0.30f)
        ).forEachIndexed { index, point ->
            drawCircle(Color.White, radius = 10.dp.toPx(), center = point)
            drawCircle(
                color = if (index == 2) Color(0xFFE36F43) else Color(0xFF0C5D3F),
                radius = 6.dp.toPx(),
                center = point
            )
        }
    }
}

private fun TrackRecordingState.displayName(): String =
    routeName?.takeIf { it.isNotBlank() } ?: "未命名轨迹"

private fun TrackRecordingState.activitySummaryLine(): String =
    if (totalDistanceKm > 0.0) {
        String.format(Locale.US, "已记录 %.1f km，可回顾本次路线表现。", totalDistanceKm)
    } else {
        "已完成记录，可回顾本次路线表现。"
    }

private fun TrackRecordingState.statusLabel(): String =
    when (status) {
        TrackRecordingStatus.RECORDING -> "记录中"
        TrackRecordingStatus.PAUSED -> "已暂停"
        TrackRecordingStatus.FINISHED -> "已完成"
        TrackRecordingStatus.IDLE -> "待开始"
    }

private fun Int.durationLabel(): String {
    val hours = this / 60
    val minutes = this % 60

    return if (hours > 0) {
        "${hours}h${minutes}m"
    } else {
        "${minutes}m"
    }
}
