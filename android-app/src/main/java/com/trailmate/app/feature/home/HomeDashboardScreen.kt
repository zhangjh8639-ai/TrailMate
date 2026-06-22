package com.trailmate.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.trailmate.app.R
import com.trailmate.app.core.design.TrailMateActionRow
import com.trailmate.app.core.design.TrailMateAmber
import com.trailmate.app.core.design.TrailMateEmptyState
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateSectionHeader
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.MatchLevel
import com.trailmate.app.core.model.RouteAssessmentSummary
import java.util.Locale

@Composable
fun HomeDashboardScreen(
    importedRoute: ImportedRoute?,
    routeAssessment: RouteAssessmentSummary?,
    onImportRoute: () -> Unit,
    onOpenRouteAssessment: () -> Unit,
    onStartLightNavigation: () -> Unit,
    onOpenGearChecklist: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeIdentityWeatherHeader()
        HomeDashboardGreeting()
        TrailMateSectionHeader(title = "当前路线评估")
        if (importedRoute == null) {
            TrailMateEmptyState(
                title = "还没有目标路线",
                caption = "先导入今天要走的 GPX 文件，再查看距离、爬升和准备建议。",
                actionLabel = "导入 GPX 文件",
                onAction = onImportRoute
            )
        } else {
            TrailMateActionRow(
                label = "导入 GPX 文件",
                caption = "替换今天的目标路线",
                onClick = onImportRoute
            )
            CurrentRoutePreparationCard(
                importedRoute = importedRoute,
                routeAssessment = routeAssessment,
                onOpenRouteAssessment = onOpenRouteAssessment
            )
        }
        TrailMateSectionHeader(title = "快速开始")
        QuickStartActions(
            onRouteAssessment = onOpenRouteAssessment,
            onLightNavigation = onStartLightNavigation,
            onGearChecklist = onOpenGearChecklist
        )
    }
}

@Composable
private fun HomeIdentityWeatherHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.Mountain,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Text(
                text = "TrailMate",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Weather,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TrailMateAmber
                )
                Text(
                    text = "26°C 多云",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Location,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "杭州 · 西湖区",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HomeDashboardGreeting() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "下午好，",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "准备走哪条线？",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CurrentRoutePreparationCard(
    importedRoute: ImportedRoute,
    routeAssessment: RouteAssessmentSummary?,
    onOpenRouteAssessment: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onOpenRouteAssessment
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
            ) {
                RouteHeroSurface()
                TrailMateStatusPill(
                    text = routeAssessment?.matchLevel?.dashboardTitle() ?: "待评估",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.78f)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = importedRoute.routeName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = importedRoute.summaryLabel(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.92f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RoutePreparationStats(
                    importedRoute = importedRoute,
                    routeAssessment = routeAssessment
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "查看路线评估",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = routeAssessment?.estimatedDurationRange ?: "补全评估后生成今日准备建议",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TrailMateLineIcon(
                        glyph = TrailMateGlyph.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteHeroSurface() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.trailmate_route_hero),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.58f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun RoutePreparationStats(
    importedRoute: ImportedRoute,
    routeAssessment: RouteAssessmentSummary?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RoutePreparationStat(
            label = "距离",
            value = String.format(Locale.US, "%.1f km", importedRoute.distanceKm),
            modifier = Modifier.weight(1f)
        )
        RoutePreparationStat(
            label = "累计爬升",
            value = "+${importedRoute.ascentMeters} m",
            modifier = Modifier.weight(1f)
        )
        RoutePreparationStat(
            label = "预计用时",
            value = routeAssessment?.estimatedDurationRange ?: "--",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoutePreparationStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 2
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickStartActions(
    onRouteAssessment: () -> Unit,
    onLightNavigation: () -> Unit,
    onGearChecklist: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickStartAction(
            glyph = TrailMateGlyph.Route,
            title = "路线评估",
            caption = "风险与难度",
            onClick = onRouteAssessment,
            modifier = Modifier.weight(1f)
        )
        QuickStartAction(
            glyph = TrailMateGlyph.Compass,
            title = "轻导航",
            caption = "沿线提醒",
            onClick = onLightNavigation,
            modifier = Modifier.weight(1f)
        )
        QuickStartAction(
            glyph = TrailMateGlyph.Gear,
            title = "装备清单",
            caption = "出发检查",
            onClick = onGearChecklist,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStartAction(
    glyph: TrailMateGlyph,
    title: String,
    caption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 106.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TrailMateLineIcon(
                glyph = glyph,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private fun MatchLevel.dashboardTitle(): String =
    when (this) {
        MatchLevel.RECOMMENDED -> "推荐尝试"
        MatchLevel.CAUTION -> "谨慎尝试"
        MatchLevel.NOT_RECOMMENDED -> "不建议尝试"
    }
