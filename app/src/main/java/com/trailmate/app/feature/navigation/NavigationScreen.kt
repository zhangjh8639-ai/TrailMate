package com.trailmate.app.feature.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Hairline = Color(0xFFE3E7E2)
private val MutedText = Color(0xFF687076)
private val SuccessGreen = Color(0xFF247A4D)
private val WarningOrange = Color(0xFFD97706)
private val SoftGreen = Color(0xFFE7F3EC)

@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    state: NavigationTabState = NavigationTabSampleState.build(),
    onSelectRouteClick: () -> Unit = {},
    onStartTrackingClick: () -> Unit = {},
    onStopTrackingClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        NavigationHeader(
            title = state.title,
            hasSelectedRoute = state.selectedRoute != null,
            trackingStartState = state.trackingStartState,
        )
        val selectedRoute = state.selectedRoute
        if (selectedRoute == null) {
            NavigationIdleCard(
                state = state.idleState,
                onSelectRouteClick = onSelectRouteClick,
            )
        } else {
            NavigationRouteReadyContent(
                route = selectedRoute,
                trackingStartState = state.trackingStartState,
                onChangeRouteClick = onSelectRouteClick,
                onStartTrackingClick = onStartTrackingClick,
                onStopTrackingClick = onStopTrackingClick,
            )
        }
    }
}

@Composable
private fun NavigationHeader(
    title: String,
    hasSelectedRoute: Boolean,
    trackingStartState: TrackingStartUiState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = when {
                !hasSelectedRoute -> "选择路线后，这里会进入轨迹导航准备状态。"
                trackingStartState.mode == TrackingStartMode.Active -> "前台导航服务已启动，路线仍以计划轨迹为准。"
                else -> "已选择计划轨迹，开始时会请求定位权限。"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText,
        )
    }
}

@Composable
private fun NavigationIdleCard(
    state: NavigationIdleState,
    onSelectRouteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(Icons.Outlined.Map, MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = state.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
            }
            PrimaryNavigationAction(
                label = state.routeActionLabel,
                icon = Icons.Outlined.Map,
                onClick = onSelectRouteClick,
            )
        }
    }
}

@Composable
private fun NavigationRouteReadyContent(
    route: NavigationRouteReadyState,
    trackingStartState: TrackingStartUiState,
    onChangeRouteClick: () -> Unit,
    onStartTrackingClick: () -> Unit,
    onStopTrackingClick: () -> Unit,
) {
    RouteReadyHero(route, trackingStartState)
    TrackingStartSection(
        state = trackingStartState,
        onStartClick = onStartTrackingClick,
        onStopClick = onStopTrackingClick,
    )
    SecondaryNavigationAction(
        label = route.changeRouteActionLabel,
        icon = Icons.Outlined.Map,
        onClick = onChangeRouteClick,
    )
    MetricGrid(route.metrics)
    StatusSection(route)
    BoundarySection(route.boundaryNotes)
}

@Composable
private fun RouteReadyHero(
    route: NavigationRouteReadyState,
    trackingStartState: TrackingStartUiState,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = SoftGreen,
        border = BorderStroke(1.dp, Color(0xFFD4E3DA)),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(Icons.Outlined.Navigation, MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.statusLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = route.routeName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = route.region,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedText,
                    )
                }
            }
            Text(
                text = if (trackingStartState.mode == TrackingStartMode.Active) {
                    "已启动前台导航服务。真实位置、偏航判断和轨迹记录会在后续版本显示。"
                } else {
                    "尚未启动定位或记录。此路线已进入导航页，后续流程将以它作为计划轨迹。"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }
    }
}

@Composable
private fun TrackingStartSection(
    state: TrackingStartUiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(
                    icon = if (state.mode == TrackingStartMode.Active) Icons.Outlined.Navigation else Icons.Outlined.PlayArrow,
                    tone = if (state.mode == TrackingStartMode.PermissionDenied || state.mode == TrackingStartMode.NotificationDenied) {
                        WarningOrange
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = state.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
            }
            state.primaryActionLabel?.let { label ->
                PrimaryNavigationAction(
                    label = label,
                    icon = Icons.Outlined.PlayArrow,
                    onClick = onStartClick,
                )
            }
            state.secondaryActionLabel?.let { label ->
                SecondaryNavigationAction(
                    label = label,
                    icon = Icons.Outlined.Stop,
                    onClick = onStopClick,
                )
            }
        }
    }
}

@Composable
private fun MetricGrid(metrics: List<NavigationMetricState>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowMetrics.forEach { metric ->
                    MetricTile(metric, Modifier.weight(1f))
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    metric: NavigationMetricState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedText,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = metric.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusSection(route: NavigationRouteReadyState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("路线状态")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Pill(route.sourceLabel, MaterialTheme.colorScheme.primary)
                Pill(route.offlineStatusLabel, if (route.offlineStatusLabel == "可离线导航") SuccessGreen else WarningOrange)
                Pill(route.confidenceLabel, MaterialTheme.colorScheme.primary)
                route.riskTags.forEach { risk ->
                    Pill(risk, WarningOrange)
                }
            }
        }
    }
}

@Composable
private fun BoundarySection(notes: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("边界说明")
        notes.forEach { note ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8FAF7),
                border = BorderStroke(1.dp, Hairline),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryNavigationAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SecondaryNavigationAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun Pill(
    label: String,
    tone: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tone.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, tone.copy(alpha = 0.24f)),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tone,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RoundIcon(
    icon: ImageVector,
    tone: Color,
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = tone.copy(alpha = 0.12f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tone,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

private fun NavigationMetricState.icon(): ImageVector =
    when (label) {
        "距离" -> Icons.Outlined.Map
        "累计爬升" -> Icons.Outlined.Terrain
        "预计用时" -> Icons.Outlined.Schedule
        else -> Icons.Outlined.CheckCircle
    }
