package com.trailmate.app.feature.routes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
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
private val WarningOrange = Color(0xFFD97706)
private val SuccessGreen = Color(0xFF247A4D)
private val InfoBlue = Color(0xFF2563EB)

@Composable
fun RoutesScreen(
    modifier: Modifier = Modifier,
    state: RoutesTabState = RoutesTabSampleState.build(),
    onImportClick: () -> Unit = {},
    onSaveImportClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RoutesHeader(state)
        ImportActionButton(state.importActionLabel, onImportClick)
        SearchField(state.searchPlaceholder)
        RouteFilterRow(state.filters, state.selectedFilter)
        ImportStateContent(state, onImportClick, onSaveImportClick)
        SectionLabel("路线资产")
        state.assets.forEach { asset ->
            RouteAssetCard(asset)
        }
    }
}

@Composable
private fun RoutesHeader(state: RoutesTabState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = state.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText,
        )
    }
}

@Composable
private fun ImportActionButton(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.FileUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ImportStateContent(
    state: RoutesTabState,
    onImportClick: () -> Unit,
    onSaveImportClick: () -> Unit,
) {
    when (state.importFlowStatus) {
        RouteImportFlowStatus.Idle -> ImportEmptyCard(
            title = state.importEmptyState.title,
            body = state.importEmptyState.body,
            tone = MaterialTheme.colorScheme.primary,
            icon = Icons.Outlined.FileUpload,
        )
        RouteImportFlowStatus.Importing -> ImportLoadingCard()
        RouteImportFlowStatus.Cancelled -> ImportEmptyCard(
            title = state.importEmptyState.title,
            body = "已取消导入",
            tone = MutedText,
            icon = Icons.Outlined.Info,
        )
        RouteImportFlowStatus.PreviewReady,
        RouteImportFlowStatus.Failed -> {
            state.importPreview?.let { preview ->
                ImportPreviewCard(preview, onImportClick, onSaveImportClick)
            }
        }
    }
}

@Composable
private fun ImportEmptyCard(
    title: String,
    body: String,
    tone: Color,
    icon: ImageVector,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusIcon(icon, tone)
            Spacer(modifier = Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                )
            }
        }
    }
}

@Composable
private fun ImportLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(34.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "正在解析路线文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "解析完成后会显示距离、爬升、航点和轨迹点数量。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                )
            }
        }
    }
}

@Composable
private fun SearchField(placeholder: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
            )
        }
    }
}

@Composable
private fun RouteFilterRow(
    filters: List<RouteFilterState>,
    selectedFilter: RouteFilterKey,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        filters.forEach { filter ->
            val selected = filter.key == selectedFilter
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Hairline),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    text = filter.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MutedText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ImportPreviewCard(
    preview: RouteImportPreviewState,
    onRetryImport: () -> Unit,
    onSaveImportClick: () -> Unit,
) {
    val statusTone = if (preview.canUseRouteActions) SuccessGreen else WarningOrange
    val statusIcon = if (preview.canUseRouteActions) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusIcon(statusIcon, statusTone)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preview.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${preview.statusLabel} · ${preview.routeName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
            }
            ImportMetricGrid(preview)
            QualityNotes(preview.qualityNotes)
            RouteOnlyNotice(preview.routeOnlyCopy)
            if (preview.canUseRouteActions) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StaticSecondaryAction(
                        label = preview.saveActionLabel,
                        modifier = Modifier.weight(1f),
                        onClick = onSaveImportClick,
                    )
                    StaticSecondaryAction(
                        label = preview.detailActionLabel,
                        modifier = Modifier.weight(1f),
                    )
                }
                StaticPrimaryAction(
                    label = preview.startActionLabel,
                    icon = Icons.Outlined.PlayArrow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )
            } else {
                StaticSecondaryAction(
                    label = "重新选择文件",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetryImport,
                )
            }
        }
    }
}

@Composable
private fun ImportMetricGrid(preview: RouteImportPreviewState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile("距离", preview.distanceLabel, Modifier.weight(1f))
            MetricTile("爬升", preview.elevationGainLabel, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile("航点", preview.waypointCountLabel, Modifier.weight(1f))
            MetricTile("轨迹点", preview.trackPointCountLabel, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedText,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QualityNotes(notes: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        notes.forEach { note ->
            Pill(
                label = note,
                tone = if (note.contains("包含")) SuccessGreen else WarningOrange,
            )
        }
    }
}

@Composable
private fun RouteOnlyNotice(copy: String) {
    Surface(
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
                text = copy,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RouteAssetCard(asset: RouteAssetCardState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                StatusIcon(
                    icon = if (asset.offlineStatusLabel == "可离线导航") Icons.Outlined.CloudDone else Icons.Outlined.Map,
                    tone = if (asset.offlineStatusLabel == "可离线导航") SuccessGreen else MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${asset.sourceLabel} · ${asset.region}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CompactMetric(Icons.Outlined.Map, asset.distanceLabel)
                        CompactMetric(Icons.Outlined.Terrain, asset.elevationGainLabel)
                        CompactMetric(Icons.Outlined.Schedule, asset.estimatedDurationLabel)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Pill(asset.offlineStatusLabel, asset.offlineStatusTone())
                        Pill(asset.difficultyLabel, MaterialTheme.colorScheme.primary)
                        Pill(asset.confidenceLabel, MaterialTheme.colorScheme.primary)
                    }
                    asset.lastUsedLabel?.let { lastUsed ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lastUsed,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText,
                        )
                    }
                    if (asset.riskTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            asset.riskTags.forEach { tag ->
                                Pill(tag, WarningOrange)
                            }
                        }
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(22.dp),
                )
            }
            if (asset.startActionLabel != null || asset.detailActionLabel != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Hairline),
                )
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    asset.startActionLabel?.let { label ->
                        StaticPrimaryAction(
                            label = label,
                            icon = Icons.Outlined.PlayArrow,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    asset.detailActionLabel?.let { label ->
                        StaticSecondaryAction(
                            label = label,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactMetric(
    icon: ImageVector,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StatusIcon(
    icon: ImageVector,
    tone: Color,
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = tone.copy(alpha = 0.12f),
    ) {
        Icon(
            modifier = Modifier.padding(9.dp),
            imageVector = icon,
            contentDescription = null,
            tint = tone,
        )
    }
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
private fun StaticPrimaryAction(
    label: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StaticSecondaryAction(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val actionModifier = if (onClick == null) {
        modifier.height(42.dp)
    } else {
        modifier
            .height(42.dp)
            .clickable(onClick = onClick)
    }

    Surface(
        modifier = actionModifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Hairline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun RouteAssetCardState.offlineStatusTone(): Color =
    if (offlineStatusLabel == "可离线导航") SuccessGreen else InfoBlue

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
    )
}
