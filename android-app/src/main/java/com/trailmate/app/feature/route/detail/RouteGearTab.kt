package com.trailmate.app.feature.route.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.trailmate.app.core.design.TrailMateStatusPill
import com.trailmate.app.core.model.AiGearAdvisorPresentation
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus

@Composable
internal fun RouteGearTab(
    recommendations: List<GearRecommendation>,
    inventory: GearInventory,
    aiGearAdvisorPresentation: AiGearAdvisorPresentation,
    onAddGearRequested: (String) -> Unit
) {
    val routeChecklist = recommendations.sortedWith(
        compareBy<GearRecommendation> { it.status.routeGearSortOrder() }
            .thenBy { it.category }
    )
    val requiredItems = routeChecklist.filterNot { it.status == GearStatus.OPTIONAL }
    val readyCount = requiredItems.count { it.status == GearStatus.COVERED }
    val missingCount = routeChecklist.count { it.status == GearStatus.MISSING }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GearAdvisorCard(
            presentation = aiGearAdvisorPresentation,
            inventoryCount = inventory.items.size
        )
        GearChecklistHeader(
            readyCount = readyCount,
            requiredCount = requiredItems.size,
            missingCount = missingCount
        )
        routeChecklist.forEach { item ->
            val matchedItem = item.matchedGearItemId?.let { matchedId ->
                inventory.items.firstOrNull { it.id == matchedId }
            }
            GearChecklistRow(
                recommendation = item,
                matchedItem = matchedItem,
                onAddGearRequested = onAddGearRequested
            )
        }
    }
}

@Composable
private fun GearAdvisorCard(
    presentation: AiGearAdvisorPresentation,
    inventoryCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.82f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = TrailMateGlyph.Spark,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI 装备建议",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    TrailMateStatusPill(
                        text = "Beta",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = presentation.caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TrailMateStatusPill(
                text = presentation.statusLabel,
                containerColor = presentation.statusContainerColor(),
                contentColor = presentation.statusContentColor()
            )
        }
    }
    Text(
        text = "我的装备库：$inventoryCount 件，可用于本路线匹配。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun GearChecklistHeader(
    readyCount: Int,
    requiredCount: Int,
    missingCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "路线清单",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "必备装备 $readyCount/$requiredCount · 缺失 $missingCount 项",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TrailMateStatusPill(
            text = if (missingCount == 0) "可出发" else "待补齐",
            containerColor = if (missingCount == 0) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                Color(0xFFFFE6E2)
            },
            contentColor = if (missingCount == 0) {
                MaterialTheme.colorScheme.primary
            } else {
                Color(0xFFB3261E)
            }
        )
    }
}

@Composable
private fun GearChecklistRow(
    recommendation: GearRecommendation,
    matchedItem: GearItem?,
    onAddGearRequested: (String) -> Unit
) {
    val statusColor = recommendation.status.statusColor()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                TrailMateLineIcon(
                    glyph = recommendation.status.statusGlyph(),
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = statusColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recommendation.category,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    TrailMateStatusPill(
                        text = recommendation.status.routeGearDisplayLabel(),
                        containerColor = statusColor.copy(alpha = 0.12f),
                        contentColor = statusColor
                    )
                }
                Text(
                    text = gearRecommendationCaption(recommendation, matchedItem),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (recommendation.status == GearStatus.MISSING) {
                OutlinedButton(onClick = { onAddGearRequested(recommendation.category) }) {
                    Text("添加已有装备")
                }
            } else {
                TrailMateStatusPill(
                    text = matchedItem?.let { "已匹配" } ?: "查看匹配",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        " 已匹配 ${name.ifBlank { item.category }}。"
    }.orEmpty()

    return recommendation.rationale + matchedText
}

private fun GearStatus.routeGearDisplayLabel(): String =
    when (this) {
        GearStatus.COVERED -> "已匹配"
        GearStatus.CHECK -> "需检查"
        GearStatus.MISSING -> "未添加"
        GearStatus.OPTIONAL -> "可选"
    }

private fun GearStatus.statusGlyph(): TrailMateGlyph =
    when (this) {
        GearStatus.COVERED -> TrailMateGlyph.Check
        GearStatus.CHECK -> TrailMateGlyph.Warning
        GearStatus.MISSING -> TrailMateGlyph.Minus
        GearStatus.OPTIONAL -> TrailMateGlyph.Gear
    }

private fun GearStatus.statusColor(): Color =
    when (this) {
        GearStatus.COVERED -> Color(0xFF0C5D3F)
        GearStatus.CHECK -> Color(0xFFD18400)
        GearStatus.MISSING -> Color(0xFFD64945)
        GearStatus.OPTIONAL -> Color(0xFF5F6863)
    }

private fun GearStatus.routeGearSortOrder(): Int =
    when (this) {
        GearStatus.MISSING -> 0
        GearStatus.CHECK -> 1
        GearStatus.COVERED -> 2
        GearStatus.OPTIONAL -> 3
    }

@Composable
private fun AiGearAdvisorPresentation.statusContainerColor(): Color =
    when {
        isStaleResponse -> Color(0xFFFFE6E2)
        isFallbackActive -> Color(0xFFFFF3D7)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }

@Composable
private fun AiGearAdvisorPresentation.statusContentColor(): Color =
    when {
        isStaleResponse -> Color(0xFFB3261E)
        isFallbackActive -> Color(0xFF8B5A00)
        else -> MaterialTheme.colorScheme.primary
    }
