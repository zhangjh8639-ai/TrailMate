package com.trailmate.app.feature.route

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateAmber
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateSectionHeader
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.gpx.TargetRouteImportQueueSummary
import com.trailmate.app.core.model.ImportedRoute
import java.util.Locale

@Composable
fun RouteWorkspaceScreen(
    importedRoute: ImportedRoute?,
    importSummary: TargetRouteImportQueueSummary,
    isImporting: Boolean,
    canRetry: Boolean,
    showSampleRouteAction: Boolean = false,
    onPickRouteFile: () -> Unit,
    onImportSampleRoute: () -> Unit,
    onOpenRouteDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasRoute = importedRoute != null

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RouteWorkspaceHeader()
        CurrentRouteWorkspaceCard(
            importedRoute = importedRoute,
            isImporting = isImporting,
            onPrimaryAction = if (hasRoute) onOpenRouteDetail else onPickRouteFile
        )

        if (isImporting || canRetry) {
            ImportStatusPanel(importSummary = importSummary, canRetry = canRetry)
        }

        RouteWorkspaceActions(
            hasRoute = hasRoute,
            isImporting = isImporting,
            canRetry = canRetry,
            showSampleRouteAction = showSampleRouteAction,
            onPickRouteFile = onPickRouteFile,
            onImportSampleRoute = onImportSampleRoute
        )
    }
}

@Composable
private fun RouteWorkspaceHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "路线准备",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "导入目标 GPX，先确认路线，再进入评估、导航和装备准备。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CurrentRouteWorkspaceCard(
    importedRoute: ImportedRoute?,
    isImporting: Boolean,
    onPrimaryAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 4.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                RouteMapPreview(hasRoute = importedRoute != null)
                TrailMateStatusPill(
                    text = if (importedRoute == null) "等待 GPX" else "GPX 就绪",
                    containerColor = if (importedRoute == null) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (importedRoute == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
                RouteFloatingStatus(
                    importedRoute = importedRoute,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CurrentRouteSummary(importedRoute = importedRoute)
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isImporting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    TrailMateLineIcon(
                        glyph = if (importedRoute == null) TrailMateGlyph.Folder else TrailMateGlyph.Compass,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (importedRoute == null) "导入 GPX 文件" else "继续准备")
                }
            }
        }
    }
}

@Composable
private fun CurrentRouteSummary(importedRoute: ImportedRoute?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    text = "当前路线",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = importedRoute?.routeName ?: "尚未导入路线",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = importedRoute?.let { "来自 ${it.fileName}" } ?: "导入后会生成评估、计划、装备和轻导航入口。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TrailMateLineIcon(
                glyph = TrailMateGlyph.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        RouteMetricStrip(importedRoute = importedRoute)
    }
}

@Composable
private fun RouteMetricStrip(importedRoute: ImportedRoute?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RouteMetricTile(
            label = "距离",
            value = importedRoute?.let { String.format(Locale.US, "%.1fkm", it.distanceKm) } ?: "--",
            modifier = Modifier.weight(1f)
        )
        RouteMetricTile(
            label = "累计爬升",
            value = importedRoute?.let { "+${it.ascentMeters}m" } ?: "--",
            modifier = Modifier.weight(1f)
        )
        RouteMetricTile(
            label = "轨迹点",
            value = importedRoute?.let { "${it.pointCount}" } ?: "--",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RouteMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 70.dp),
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
private fun RouteFloatingStatus(
    importedRoute: ImportedRoute?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(0.72f),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (importedRoute == null) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Route,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (importedRoute == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (importedRoute == null) "导入目标路线" else "准备进入路线详情",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = if (importedRoute == null) "支持 GPX 文件" else "评估 · 计划 · 装备 · 轻导航",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RouteWorkspaceActions(
    hasRoute: Boolean,
    isImporting: Boolean,
    canRetry: Boolean,
    showSampleRouteAction: Boolean,
    onPickRouteFile: () -> Unit,
    onImportSampleRoute: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TrailMateSectionHeader(title = if (hasRoute) "路线管理" else "快速开始")
        if (hasRoute) {
            OutlinedButton(
                onClick = onPickRouteFile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("更换 GPX")
            }
        } else if (showSampleRouteAction) {
            TextButton(
                onClick = onImportSampleRoute,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("sample-route-button"),
                enabled = !isImporting
            ) {
                Text("使用示例 GPX")
            }
        }

        if (canRetry) {
            OutlinedButton(
                onClick = onPickRouteFile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isImporting
            ) {
                Text("重试导入")
            }
        }
    }
}

@Composable
private fun RouteMapPreview(hasRoute: Boolean) {
    val routeColor = if (hasRoute) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.46f)
    val checkpointColor = TrailMateAmber
    val startColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(216.dp)
    ) {
        drawRect(
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFFEAF2E4),
                    Color(0xFFFBFAF0),
                    Color(0xFFDDECF5)
                )
            )
        )
        repeat(10) { index ->
            val y = size.height * (0.10f + index * 0.09f)
            drawLine(
                color = Color(0xFFB8D4C0).copy(alpha = 0.42f),
                start = Offset(size.width * -0.08f, y),
                end = Offset(size.width * 1.08f, y + if (index % 2 == 0) 24f else -18f),
                strokeWidth = 1.2.dp.toPx()
            )
        }
        val routePath = Path().apply {
            moveTo(size.width * 0.20f, size.height * 0.82f)
            cubicTo(
                size.width * 0.30f,
                size.height * 0.64f,
                size.width * 0.22f,
                size.height * 0.50f,
                size.width * 0.38f,
                size.height * 0.42f
            )
            cubicTo(
                size.width * 0.55f,
                size.height * 0.33f,
                size.width * 0.44f,
                size.height * 0.20f,
                size.width * 0.62f,
                size.height * 0.16f
            )
            cubicTo(
                size.width * 0.74f,
                size.height * 0.12f,
                size.width * 0.78f,
                size.height * 0.24f,
                size.width * 0.86f,
                size.height * 0.10f
            )
        }
        drawPath(
            path = routePath,
            color = Color.White.copy(alpha = 0.96f),
            style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = routePath,
            color = routeColor,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
        listOf(
            Offset(size.width * 0.20f, size.height * 0.82f),
            Offset(size.width * 0.38f, size.height * 0.42f),
            Offset(size.width * 0.62f, size.height * 0.16f),
            Offset(size.width * 0.86f, size.height * 0.10f)
        ).forEachIndexed { index, point ->
            val isFocus = hasRoute && index == 1
            if (isFocus) {
                drawCircle(Color(0xFF2D75E8).copy(alpha = 0.18f), radius = 30.dp.toPx(), center = point)
            }
            drawCircle(Color.White, radius = 12.dp.toPx(), center = point)
            drawCircle(
                color = when (index) {
                    0 -> startColor
                    3 -> Color(0xFF16211D)
                    else -> checkpointColor
                },
                radius = 7.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun ImportStatusPanel(
    importSummary: TargetRouteImportQueueSummary,
    canRetry: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (canRetry) {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrailMateLineIcon(
                glyph = if (canRetry) TrailMateGlyph.Warning else TrailMateGlyph.Folder,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (canRetry) TrailMateAmber else MaterialTheme.colorScheme.primary
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = importSummary.value,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = importSummary.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
